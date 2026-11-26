package com.akhnaton.foodvisits.ui.home.bundlevisits

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.data.model.bundleVisits.BundleRejectReq
import com.akhnaton.foodvisits.data.model.bundleVisits.BundleVisitsRes
import com.akhnaton.foodvisits.data.model.bundleVisits.ReasonsData
import com.akhnaton.foodvisits.data.model.bundleVisits.RejectReason
import com.akhnaton.foodvisits.data.model.bundleVisits.SearchCustomerData
import com.akhnaton.foodvisits.data.statusValue.bundlevisits.BundleVisitsIntent
import com.akhnaton.foodvisits.data.statusValue.bundlevisits.BundleVisitsStatus
import com.akhnaton.foodvisits.databinding.FragmentBundleVisitDetailsBinding
import com.akhnaton.foodvisits.shared.DialogUtils
import com.akhnaton.foodvisits.shared.ProgressDialogHelper
import com.akhnaton.foodvisits.shared.SharedPreferencesHelper
import com.akhnaton.foodvisits.shared.location.RequestPermission
import com.akhnaton.foodvisits.ui.auth.LoginActivity2
import com.akhnaton.foodvisits.ui.home.promoter.SelectedImagesAdapter
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.log

class BundleVisitDetailsFragment : Fragment() {

    companion object {
        private const val TAG = "BundleVisitDetails"
    }

    private lateinit var binding: FragmentBundleVisitDetailsBinding
    private lateinit var dialog: AlertDialog
    private val viewModel: BundleVisitsViewModel by viewModels()

    private var reasons = listOf<RejectReason>()
    private lateinit var imagesAdapter: SelectedImagesAdapter

    private var received: Boolean? = null
    private var isCoded: Boolean? = null
    private var customerFound = false
    private var selectedReasonId: String? = null
    private val pendingRetries = mutableListOf<() -> Unit>()
    private var lastSaveRetry: (() -> Unit)? = null
    private var isRefreshing = false
    private var handledSearch: Any? = null
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            imagesAdapter.setImages(listOf(uri))
            updateImagesVisibility()
        }
    }
    private var cameraImageUri: Uri? = null

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            cameraImageUri?.let { uri ->
                imagesAdapter.setImages(listOf(uri))
                updateImagesVisibility()
            }
        }
    }

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) openCamera()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            layoutInflater, R.layout.fragment_bundle_visit_details, container, false
        )
        return binding.root
    }

    @Suppress("DEPRECATION", "UNCHECKED_CAST")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        handleTopBottomKeyboard()

        dialog = ProgressDialogHelper().showAlertProgress(requireContext(), "Loading..")
        dialog.hide()

        setupImages()
        binding.progressLoading.visibility = View.VISIBLE
        viewLifecycleOwner.lifecycleScope.launch {
            delay(1000)
            withTimeoutOrNull(5000) {
                viewModel.locationState.first { it != null }
            }
            binding.progressLoading.visibility = View.GONE
        }
        binding.btnBack.setOnClickListener { findNavController().popBackStack() }

        binding.cardYes.setOnClickListener { selectAnswer(true) }
        binding.cardNo.setOnClickListener { selectAnswer(false) }

        binding.cardCoded.setOnClickListener { selectCoded(true) }
        binding.cardNotCoded.setOnClickListener { selectCoded(false) }
        binding.etCode.addTextChangedListener { onCodeTyping() }
        binding.etCode.afterTextChangedDelayed { onCodeChanged(it.trim()) }
        binding.etCustomerName.addTextChangedListener {
            Log.d("RESETX", "name changed: ${it}")
            resetReceiveSection()
            updateReceiveVisibility()
        }
        binding.etCustomerAddress.addTextChangedListener {
            resetReceiveSection()
            updateReceiveVisibility()
        }

        binding.btnAddImages.setOnClickListener {
            showImageSourceDialog()
        }

        binding.btnSave.setOnClickListener { onSaveClicked() }

        fetchData()
    }
    private fun onCodeTyping() {
        Log.d("RESETX", "onCodeTyping: ${binding.etCode.text}")
        customerFound = false
        binding.tvCodeError.visibility = View.GONE
        binding.llNameAddress.visibility = View.GONE
        resetReceiveSection()
        updateReceiveVisibility()
    }
    private fun selectCoded(coded: Boolean) {
        isCoded = coded
        customerFound = false

        binding.etCode.text?.clear()
        binding.etCustomerName.text?.clear()
        binding.etCustomerAddress.text?.clear()

        val selected = ContextCompat.getColor(requireContext(), R.color.colorPrimary)
        val normal = ContextCompat.getColor(requireContext(), R.color.box_unselected)

        binding.cardCoded.setCardBackgroundColor(if (coded) selected else normal)
        binding.cardNotCoded.setCardBackgroundColor(if (!coded) selected else normal)
        binding.tvCoded.setTextColor(if (coded) Color.WHITE else Color.BLACK)
        binding.tvNotCoded.setTextColor(if (!coded) Color.WHITE else Color.BLACK)

        binding.cardCustomerInput.visibility = View.VISIBLE
        updateReceiveVisibility()
        binding.layoutCode.visibility = if (coded) View.VISIBLE else View.GONE
        binding.tvCodeError.visibility = View.GONE
        binding.llNameAddress.visibility = if (coded) View.GONE else View.VISIBLE
        binding.etCustomerName.isEnabled = !coded
        binding.etCustomerAddress.isEnabled = !coded
    }
    private fun onCodeChanged(code: String) {
        if (code.isNotBlank()) searchCustomer(code)
    }

    private fun searchCustomer(code: String) {
    viewModel.intent.trySend(BundleVisitsIntent.SearchCustomer(code))
    }
    private fun TextInputEditText.afterTextChangedDelayed(afterTextChanged: (String) -> Unit) {
        this.addTextChangedListener(object : TextWatcher {
            var timer: CountDownTimer? = null

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(editable: Editable?) {
                timer?.cancel()
                timer = object : CountDownTimer(1000, 1500) {
                    override fun onTick(millisUntilFinished: Long) {}
                    override fun onFinish() {
                        afterTextChanged.invoke(editable.toString())
                    }
                }.start()
            }
        })
    }
    private fun loadReasons() {
        lifecycleScope.launch {
            viewModel.intent.send(BundleVisitsIntent.GetRejectReasons)
        }
    }

    private fun refreshThenRetry(retry: () -> Unit) {
        pendingRetries.add(retry)
        refreshToken()
    }

    private fun refreshToken() {
        if (isRefreshing) return
        isRefreshing = true
        lifecycleScope.launch {
            viewModel.intent.send(
                BundleVisitsIntent.RefreshToken(
                    SharedPreferencesHelper.getInstance().getEmployeeId(),
                    SharedPreferencesHelper.getInstance().getUserToken()
                )
            )
        }
    }
    private fun handleSearchResponse(res: BundleVisitsRes) {
        if (isCoded != true) return
        when (res.status) {
            200 -> {
                val data = Gson().fromJson(res.data, SearchCustomerData::class.java)
                val name = data.customerName.orEmpty().trim()
                val address = data.customerAddress.orEmpty().trim()
                if (binding.etCustomerName.text.toString() != name) binding.etCustomerName.setText(name)
                if (binding.etCustomerAddress.text.toString() != address) binding.etCustomerAddress.setText(address)
                binding.tvCodeError.visibility = View.GONE
                binding.llNameAddress.visibility = View.VISIBLE
                customerFound = true
                updateReceiveVisibility()
            }
            401 -> {
                refreshThenRetry { searchCustomer(binding.etCode.text.toString().trim()) }
            }

            else -> {
                customerFound = false
                binding.llNameAddress.visibility = View.GONE
                updateReceiveVisibility()
                showError(res.message)
            }
        }
    }

    private fun setupReasons() {
        binding.rgReasons.removeAllViews()
        reasons.forEach { reason ->
            val rb = layoutInflater.inflate(
                R.layout.item_reason_radio, binding.rgReasons, false
            ) as RadioButton
            rb.id = View.generateViewId()
            rb.text = reason.name
            rb.tag = reason.id
            binding.rgReasons.addView(rb)
        }
        binding.rgReasons.setOnCheckedChangeListener { group, checkedId ->
            selectedReasonId = group.findViewById<RadioButton>(checkedId)?.tag as? String
        }
    }

    private fun setupImages() {
        imagesAdapter = SelectedImagesAdapter(
            onRemoveClick = {
                imagesAdapter.setImages(emptyList())
                updateImagesVisibility()
            }
        )
        binding.rvImages.layoutManager = LinearLayoutManager(
            requireContext(), LinearLayoutManager.HORIZONTAL, false
        )
        binding.rvImages.adapter = imagesAdapter

        binding.rvImages.addOnItemTouchListener(
            object : RecyclerView.SimpleOnItemTouchListener() {
                private val detector = android.view.GestureDetector(
                    requireContext(),
                    object : android.view.GestureDetector.SimpleOnGestureListener() {
                        override fun onSingleTapUp(e: android.view.MotionEvent) = true
                    }
                )

                override fun onInterceptTouchEvent(
                    rv: RecyclerView,
                    e: android.view.MotionEvent
                ): Boolean {
                    if (detector.onTouchEvent(e)) {
                        val child = rv.findChildViewUnder(e.x, e.y)
                        val close = child?.findViewById<View>(R.id.ivClose)
                        val onClose = close != null && isTouchInside(close, e)
                        if (child != null && !onClose) {
                            imagesAdapter.getImages().firstOrNull()?.let { showImagePreview(it) }
                        }
                    }
                    return false
                }
            }
        )
    }

    private fun isTouchInside(view: View, e: android.view.MotionEvent): Boolean {
        val loc = IntArray(2)
        view.getLocationOnScreen(loc)
        return e.rawX >= loc[0] && e.rawX <= loc[0] + view.width &&
                e.rawY >= loc[1] && e.rawY <= loc[1] + view.height
    }

    private fun showImagePreview(uri: Uri) {
        val previewDialog = android.app.Dialog(
            requireContext(),
            android.R.style.Theme_Black_NoTitleBar_Fullscreen
        )
        val imageView = android.widget.ImageView(requireContext()).apply {
            scaleType = android.widget.ImageView.ScaleType.FIT_CENTER
            setOnClickListener { previewDialog.dismiss() }
        }
        Glide.with(requireContext()).load(uri).into(imageView)
        previewDialog.setContentView(imageView)
        previewDialog.show()
    }

    private fun showImageSourceDialog() {
        val options = arrayOf("المعرض", "الكاميرا")
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("اختيار الصورة")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> pickImageLauncher.launch("image/*")
                    1 -> checkCameraPermission()
                }
            }
            .show()
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            openCamera()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun openCamera() {
        cameraImageUri = createImageUri()
        cameraLauncher.launch(cameraImageUri)
    }

    private fun createImageUri(): Uri {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFile = File.createTempFile("IMG_${timeStamp}_", ".jpg", requireContext().cacheDir)
        return FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.provider",
            imageFile
        )
    }

    private fun updateImagesVisibility() {
        val hasImages = imagesAdapter.getImages().isNotEmpty()
        binding.btnAddImages.visibility = if (hasImages) View.GONE else View.VISIBLE
        binding.rvImages.visibility = if (hasImages) View.VISIBLE else View.GONE
    }

    private fun selectAnswer(yes: Boolean) {
        received = yes
        val green = ContextCompat.getColor(requireContext(), R.color.green)
        val red = ContextCompat.getColor(requireContext(), R.color.red)
        val grey = ContextCompat.getColor(requireContext(), R.color.box_unselected)

        binding.cardYes.setCardBackgroundColor(if (yes) green else grey)
        binding.cardNo.setCardBackgroundColor(if (!yes) red else grey)

        binding.tvYes.setTextColor(if (yes) Color.WHITE else Color.BLACK)
        binding.tvNo.setTextColor(if (!yes) Color.WHITE else Color.BLACK)

        binding.cardPhoto.visibility = if (yes) View.VISIBLE else View.GONE
        binding.cardReasons.visibility = if (yes) View.GONE else View.VISIBLE
        if (!yes) {
            imagesAdapter.setImages(emptyList())
            updateImagesVisibility()
        }
        if (yes) {
            selectedReasonId = null
            binding.rgReasons.clearCheck()
        }
        if (!yes && reasons.isEmpty()) loadReasons()
    }

    private fun resetReceiveSection() {
        Log.d("RESETX", "resetReceiveSection", Throwable())
        received = null
        selectedReasonId = null
        binding.rgReasons.clearCheck()
        imagesAdapter.setImages(emptyList())
        updateImagesVisibility()
        binding.cardPhoto.visibility = View.GONE
        binding.cardReasons.visibility = View.GONE
        binding.cardYes.setCardBackgroundColor(
            ContextCompat.getColor(requireContext(), R.color.box_unselected)
        )
        binding.cardNo.setCardBackgroundColor(
            ContextCompat.getColor(requireContext(), R.color.box_unselected)
        )
        binding.tvYes.setTextColor(Color.BLACK)
        binding.tvNo.setTextColor(Color.BLACK)
    }
    private fun showError(message: String) {
        DialogUtils.showResultDialog(
            context = requireContext(),
            message = message,
            isSuccess = false,
            showOkButton = true
        )
    }

    private fun onSaveClicked() {

        val coded = isCoded

        if (coded == null) {
            showError(getString(R.string.error_choose_coded))
            return
        }
        if(coded && (binding.etCode.text.isNullOrBlank())){
            showError(getString(R.string.error_enter_code))
            return
        }
        if (coded && !customerFound) {
            showError(getString(R.string.error_enter_valid_code))
            return
        }
        if (!coded && (binding.etCustomerName.text.isNullOrBlank() &&
                    binding.etCustomerAddress.text.isNullOrBlank())
        ) {
            showError(getString(R.string.error_enter_name_address))
            return
        }
        if(!coded && (binding.etCustomerName.text.isNullOrBlank())){
            showError(getString(R.string.error_enter_name))
            return
        }
        if(!coded && (binding.etCustomerAddress.text.isNullOrBlank())){
            showError(getString(R.string.error_enter_address))
            return
        }
        val answer = received
        if (answer == null) {
            showError("برجاء اختيار نعم أو لا")
            return
        }

        if (answer && imagesAdapter.getImages().isEmpty()) {
            showError("برجاء رفع صورة إثبات الاستلام")
            return
        }

        val location = viewModel.locationState.value
        if (location == null) {
            showError("الموقع الحالي غير متاح، برجاء الانتظار حتى يتم تحديده")
            return
        }
        if (received == false) saveReject(location.latitude, location.longitude)
        else saveDone(location.latitude, location.longitude)
    }
//30.04823591980596, 31.245965592700067
    private fun saveDone(lat: Double, lng: Double) {
        val appContext = requireContext().applicationContext
        val uri = imagesAdapter.getImages().firstOrNull()

        fun String.toBody(): RequestBody = this.toRequestBody("text/plain".toMediaTypeOrNull())

        val codedValue = if (isCoded == true) "1" else "0"
        val codeBody = if (isCoded == true) {
            binding.etCode.text.toString().trim().toBody()
        } else null
        val nameBody = if (isCoded == true) null else
            binding.etCustomerName.text.toString().trim().toBody()
        val addressBody = if (isCoded == true) null else
            binding.etCustomerAddress.text.toString().trim().toBody()
        val takesBundle = (if (received == true) "1" else "0").toBody()
        val latBody = lat.toString().toBody()
        val lngBody = lng.toString().toBody()

        val send = {
            lifecycleScope.launch {
                val photoPart = uri?.let { uriToMultipart(appContext, it) }
                viewModel.intent.send(
                    BundleVisitsIntent.SaveDone(
                        photo = photoPart,
                        isCoded = codedValue.toBody(),
                        custCode = codeBody,
                        customerName = nameBody,
                        customerAddress = addressBody,
                        takesBundle = takesBundle,
                        latitude = latBody,
                        longitude = lngBody,
                    )
                )
            }
            Unit
        }
        lastSaveRetry = send
        send()
    }

    private fun saveReject(lat: Double, lng: Double) {

        val reasonId = selectedReasonId?.toIntOrNull()
        if (reasonId == null) {
            showError("برجاء اختيار سبب عدم الاستلام")
            return
        }
        val send = {
            lifecycleScope.launch {
                viewModel.intent.send(
                    BundleVisitsIntent.SaveReject(
                        BundleRejectReq(
                            is_coded = if (isCoded == true) 1 else 0,
                            cust_code = if (isCoded == true) binding.etCode.text.toString().trim() else null,
                            customer_name = if (isCoded == true) null else binding.etCustomerName.text.toString().trim(),
                            customer_address = if (isCoded == true) null else binding.etCustomerAddress.text.toString().trim(),
                            reject_reason_id = reasonId,
                            latitude = lat,
                            longitude = lng,
                        )
                    )
                )
            }
            Unit
        }
        lastSaveRetry = send
        send()
    }

    private suspend fun uriToMultipart(context: Context, uri: Uri): MultipartBody.Part? {
        return withContext(Dispatchers.IO) {
            val file = compressImage(context, uri) ?: return@withContext null
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("bundle_image", file.name, requestFile)
        }
    }

    private fun handleSaveResponse(status: Int, message: String) {
        when (status) {
            200 -> {
                lastSaveRetry = null
                DialogUtils.showResultDialog(
                    context = requireContext(),
                    message = message,
                    isSuccess = true,
                    showOkButton = true,
                    onOk = { findNavController().popBackStack() }
                )
            }

            401 -> {
                lastSaveRetry?.let { refreshThenRetry(it) }
            }

            else -> {
                lastSaveRetry = null
                showError(message)
            }
        }
    }

    private fun fetchData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.status.collect {
                    when (it) {
                        is BundleVisitsStatus.Idle -> {}

                        is BundleVisitsStatus.Loading -> binding.progressLoading.visibility = View.VISIBLE

                        is BundleVisitsStatus.SearchCustomer -> {
                            Log.d("CODExx",  "message sent")
                            binding.progressLoading.visibility = View.GONE

                            if (handledSearch !== it) {
                                handledSearch = it
                                handleSearchResponse(it.data)
                            }
                        }

                        is BundleVisitsStatus.GetRejectReasons -> {
                            binding.progressLoading.visibility = View.GONE
                            if (it.data.status == 200) {
                                val data = Gson().fromJson(it.data.data, ReasonsData::class.java)
                                reasons = data.reasons
                                setupReasons()
                            } else if (it.data.status == 401) {
                                refreshThenRetry { loadReasons() }
                            } else {
                                showError(it.data.message)
                            }
                        }

                        is BundleVisitsStatus.SaveDone -> {
                            binding.progressLoading.visibility = View.GONE
                            handleSaveResponse(it.data.status, it.data.message)
                        }

                        is BundleVisitsStatus.SaveReject -> {
                            binding.progressLoading.visibility = View.GONE
                            handleSaveResponse(it.data.status, it.data.message)
                        }

                        is BundleVisitsStatus.RefreshToken -> {
                            dialog.dismiss()
                            isRefreshing = false
                            if (it.data.status == 200) {
                                val data = Gson().fromJson(
                                    it.data.data,
                                    com.akhnaton.foodvisits.data.model.refreshToken.Data::class.java
                                )
                                SharedPreferencesHelper.getInstance().saveUserToken(data.TOKEN)
                                val retries = pendingRetries.toList()
                                pendingRetries.clear()
                                retries.forEach { retry -> retry() }
                            } else {
                                pendingRetries.clear()
                                DialogUtils.showResultDialog(
                                    context = requireContext(),
                                    message = it.data.message,
                                    isSuccess = false,
                                    showOkButton = true,
                                    onOk = {
                                        SharedPreferencesHelper.getInstance().logOut()
                                        startActivity(
                                            Intent(requireContext(), LoginActivity2::class.java)
                                        )
                                        requireActivity().finishAffinity()
                                    }
                                )
                            }
                        }

                        is BundleVisitsStatus.Error -> {
                            Log.d(TAG, "fetchData: ${it.error}")
                            binding.progressLoading.visibility = View.GONE
                            isRefreshing = false
                            pendingRetries.clear()
                            showError(it.error.toString())
                        }

                        else -> {}
                    }
                }
            }
        }
    }

    private fun compressImage(context: Context, uri: Uri): File? {
        val resolver = context.contentResolver

        val options = BitmapFactory.Options().apply { inSampleSize = 2 }
        var bitmap = resolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, options)
        } ?: return null

        val orientation = resolver.openInputStream(uri)?.use {
            ExifInterface(it).getAttributeInt(
                ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL
            )
        }

        val degrees = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }
        if (degrees != 0f) {
            val matrix = Matrix().apply { postRotate(degrees) }
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        }

        val file = File.createTempFile("bundle_img_", ".jpg", context.cacheDir)
        FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.JPEG, 70, it) }
        return file
    }

    private fun updateReceiveVisibility() {
        Log.d("RESETX", "updateReceiveVisibility: isCoded=$isCoded found=$customerFound")
        val ready = when (isCoded) {
            true -> customerFound
            false -> binding.etCustomerName.text.toString().isNotBlank() &&
                    binding.etCustomerAddress.text.toString().isNotBlank()
            null -> false
        }

        binding.cardReceive.visibility = if (ready) View.VISIBLE else View.GONE

        if (!ready) resetReceiveSection()
    }

    private fun handleTopBottomKeyboard() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())

            view.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                maxOf(imeInsets.bottom, systemBars.bottom)
            )
            insets
        }
        ViewCompat.requestApplyInsets(binding.root)
    }

    @RequiresPermission(
        allOf = [
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ]
    )
    override fun onResume() {
        Log.d("RESETX", "onResume")
        super.onResume()
        RequestPermission().enableLocation(requireActivity())
        RequestPermission().permissionCheck(requireActivity())
        viewModel.startLocationUpdates()
    }

    override fun onPause() {
        super.onPause()
        viewModel.stopLocationUpdates()
    }
}
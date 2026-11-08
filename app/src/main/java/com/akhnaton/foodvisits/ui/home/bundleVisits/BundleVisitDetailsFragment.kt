package com.akhnaton.foodvisits.ui.home.bundlevisits

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import android.widget.RadioGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.data.model.bundleVisits.BundleRejectReq
import com.akhnaton.foodvisits.data.model.bundleVisits.BundleVisit
import com.akhnaton.foodvisits.data.model.bundleVisits.RejectReason
import com.akhnaton.foodvisits.data.statusValue.bundlevisits.BundleVisitsIntent
import com.akhnaton.foodvisits.data.statusValue.bundlevisits.BundleVisitsStatus
import com.akhnaton.foodvisits.databinding.FragmentBundleVisitDetailsBinding
import com.akhnaton.foodvisits.shared.DialogUtils
import com.akhnaton.foodvisits.shared.ProgressDialogHelper
import com.akhnaton.foodvisits.shared.SharedPreferencesHelper
import com.akhnaton.foodvisits.shared.location.RequestPermission
import com.akhnaton.foodvisits.shared.openLocationInMap
import com.akhnaton.foodvisits.ui.auth.LoginActivity2
import com.akhnaton.foodvisits.ui.home.promoter.SelectedImagesAdapter
import com.google.gson.Gson
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

class BundleVisitDetailsFragment : Fragment() {

    companion object {
        private const val TAG = "BundleVisitDetails"
    }

    private lateinit var binding: FragmentBundleVisitDetailsBinding
    private lateinit var dialog: AlertDialog
    private val viewModel: BundleVisitsViewModel by viewModels()

    private var visitId: String = ""
    private var visit: BundleVisit? = null
    private var reasons = listOf<RejectReason>()
    private lateinit var imagesAdapter: SelectedImagesAdapter

    private var received: Boolean? = null
    private var selectedReasonId: String? = null
    private var pendingRetry: (() -> Unit)? = null

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

        setupKeyboardInsets()

        visitId = arguments?.getString("visitId").orEmpty()

        dialog = ProgressDialogHelper().showAlertProgress(requireContext(), "Loading..")
        dialog.hide()

//        fillData()
//        setupReasons()
        setupImages()

        binding.btnBack.setOnClickListener { findNavController().popBackStack() }

        binding.btnOpenMap.setOnClickListener {
            val lat = visit?.latitude?.toDoubleOrNull()
            val lng = visit?.longitude?.toDoubleOrNull()
            if (lat != null && lng != null) openLocationInMap(requireContext(), lat, lng)
        }

        binding.cardYes.setOnClickListener { selectAnswer(true) }
        binding.cardNo.setOnClickListener { selectAnswer(false) }

        binding.btnAddImages.setOnClickListener {
            showImageSourceDialog()
        }

        binding.btnSave.setOnClickListener { onSaveClicked() }

        fetchData()
        loadVisit()
    }

    private fun fillData(visit: BundleVisit) {
        binding.tvCustomerName.text = visit.custName
        binding.tvCustomerCode.text = visit.custId
        binding.tvSiteAddress.text = visit.custAddress.trim()
//        binding.tvOrderType.text = visit.customerOrderType
        binding.tvVisitDate.text = visit.visitDate
//        binding.tvPrevCount.text = visit.countOfVisits
        binding.tvReceiveQuestion.text = "هل استلم ${visit.custName} البندل؟"

        val hasLocation = visit.latitude?.toDoubleOrNull() != null &&
                visit.longitude?.toDoubleOrNull() != null
        binding.btnOpenMap.visibility = if (hasLocation) View.VISIBLE else View.GONE
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

//    private fun setupImages() {
//        imagesAdapter = SelectedImagesAdapter(
//            onRemoveClick = {
//                imagesAdapter.setImages(emptyList())
//                updateImagesVisibility()
//            }
//        )
//        binding.rvImages.layoutManager = LinearLayoutManager(
//            requireContext(), LinearLayoutManager.HORIZONTAL, false
//        )
//        binding.rvImages.adapter = imagesAdapter
//    }
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

            override fun onInterceptTouchEvent(rv: RecyclerView, e: android.view.MotionEvent): Boolean {
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
        val green = Color.parseColor("#34C759")
        val red = Color.parseColor("#FF3B30")
        val grey = Color.parseColor("#F1F1F4")

        binding.cardYes.setCardBackgroundColor(if (yes) green else grey)
        binding.cardNo.setCardBackgroundColor(if (!yes) red else grey)

        binding.tvYes.setTextColor(if (yes) Color.WHITE else Color.BLACK)
        binding.tvNo.setTextColor(if (!yes) Color.WHITE else Color.BLACK)
//
//        setIcon(binding.ivYes, yes, R.drawable.ic_check_circle2).
//        setIcon(binding.ivNo, !yes, R.drawable.ic_close)

        binding.cardPhoto.visibility = if (yes) View.VISIBLE else View.GONE
        binding.cardReasons.visibility = if (yes) View.GONE else View.VISIBLE
    }

    private fun setIcon(iv: android.widget.ImageView, selected: Boolean, selectedRes: Int) {
        if (selected) {
            iv.setImageResource(selectedRes)
            iv.imageTintList = ColorStateList.valueOf(Color.WHITE)
        } else {
            iv.setImageResource(android.R.drawable.radiobutton_off_background)
            iv.imageTintList = null
        }
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
        val answer = received
        if (answer == null) {
            showError("برجاء اختيار نعم أو لا")
            return
        }

        val location = viewModel.locationState.value
        if (location == null) {
            showError("الموقع الحالي غير متاح، برجاء الانتظار حتى يتم تحديده")
            return
        }

        if (answer) saveDone(location.latitude, location.longitude)
        else saveReject(location.latitude, location.longitude)
    }

    private fun saveDone(lat: Double, lng: Double) {
        val uri = imagesAdapter.getImages().firstOrNull()
        val photoPart = uri?.let { uriToMultipart(it) }
        if (photoPart == null) {
            showError("برجاء رفع صورة إثبات الاستلام")
            return
        }

        fun String.toBody(): RequestBody = this.toRequestBody("text/plain".toMediaTypeOrNull())

        val send = {
            lifecycleScope.launch {
                viewModel.intent.send(
                    BundleVisitsIntent.SaveDone(
                        photo = photoPart,
                        visitId = this@BundleVisitDetailsFragment.visitId.toBody(),
                        status = "1".toBody(),
                        latitude = lat.toString().toBody(),
                        longitude = lng.toString().toBody(),
                    )
                )
            }
            Unit
        }
        pendingRetry = send
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
                            visit_id = visitId,
                            reject_reason_id = reasonId,
                            latitude = lat,
                            longitude = lng,
                        )
                    )
                )
            }
            Unit
        }
        pendingRetry = send
        send()
    }

    private fun uriToMultipart(uri: Uri): MultipartBody.Part? {
        val inputStream = requireContext().contentResolver.openInputStream(uri) ?: return null
        val tempFile = File.createTempFile("bundle_img_", ".jpg", requireContext().cacheDir)
        FileOutputStream(tempFile).use { output -> inputStream.copyTo(output) }
        val requestFile = tempFile.asRequestBody("image/*".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData("photo", tempFile.name, requestFile)
    }

    private fun handleSaveResponse(status: Int, message: String) {
        when (status) {
            200 -> {
                pendingRetry = null
                DialogUtils.showResultDialog(
                    context = requireContext(),
                    message = message,
                    isSuccess = true,
                    showOkButton = true,
                    onOk = { findNavController().popBackStack() }
                )
            }

            401 -> {
                lifecycleScope.launch {
                    viewModel.intent.send(
                        BundleVisitsIntent.RefreshToken(
                            SharedPreferencesHelper.getInstance().getEmployeeId(),
                            SharedPreferencesHelper.getInstance().getUserToken()
                        )
                    )
                }
            }

            else -> {
                pendingRetry = null
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

                        is BundleVisitsStatus.Loading -> dialog.show()
                        is BundleVisitsStatus.GetTodayVisits -> {
                            dialog.dismiss()
                            if (it.data.status == 200) {
                                val data = Gson().fromJson(
                                    it.data.data,
                                    com.akhnaton.foodvisits.data.model.bundleVisits.Data::class.java
                                )
                                reasons = data.rejectReasons
                                val found = data.visits.firstOrNull { v -> v.visitId == visitId }
                                if (found != null) {
                                    visit = found
                                    fillData(found)
                                    setupReasons()
                                } else {
                                    showError("الزيارة غير موجودة")
                                }
                            } else if (it.data.status == 401) {
                                pendingRetry = { loadVisit() }
                                lifecycleScope.launch {
                                    viewModel.intent.send(
                                        BundleVisitsIntent.RefreshToken(
                                            SharedPreferencesHelper.getInstance().getEmployeeId(),
                                            SharedPreferencesHelper.getInstance().getUserToken()
                                        )
                                    )
                                }
                            } else {
                                showError(it.data.message)
                            }
                        }
                        is BundleVisitsStatus.SaveDone -> {
                            dialog.dismiss()
                            handleSaveResponse(it.data.status, it.data.message)
                        }

                        is BundleVisitsStatus.SaveReject -> {
                            dialog.dismiss()
                            handleSaveResponse(it.data.status, it.data.message)
                        }

                        is BundleVisitsStatus.RefreshToken -> {
                            dialog.dismiss()
                            if (it.data.status == 200) {
                                val data = Gson().fromJson(
                                    it.data.data,
                                    com.akhnaton.foodvisits.data.model.refreshToken.Data::class.java
                                )
                                SharedPreferencesHelper.getInstance().saveUserToken(data.TOKEN)
                                val retry = pendingRetry
                                pendingRetry = null
                                retry?.invoke()
                            } else {
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
                            dialog.dismiss()
                            showError(it.error.toString())
                        }

                        else -> {}
                    }
                }
            }
        }
    }

    private fun setupKeyboardInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                view.paddingLeft,
                systemBars.top,
                view.paddingRight,
                maxOf(imeInsets.bottom, systemBars.bottom)
            )
            insets
        }
    }
    private fun loadVisit() {
        lifecycleScope.launch {
            viewModel.intent.send(BundleVisitsIntent.GetTodayVisits)
        }
    }
    @RequiresPermission(
        allOf = [
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ]
    )
    override fun onResume() {
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
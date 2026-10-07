package com.akhnaton.foodvisits.ui.home.personCoding

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.data.model.personCoding.Area
import com.akhnaton.foodvisits.data.model.personCoding.AreasData
import com.akhnaton.foodvisits.data.model.personCoding.CustomerType
import com.akhnaton.foodvisits.data.model.personCoding.Governorate
import com.akhnaton.foodvisits.data.model.personCoding.GovernoratesData
import com.akhnaton.foodvisits.data.model.personCoding.LineItem
import com.akhnaton.foodvisits.data.model.personCoding.LinesModel
import com.akhnaton.foodvisits.data.model.personCoding.MainCustomer
import com.akhnaton.foodvisits.data.model.personCoding.MainCustomersLineData
import com.akhnaton.foodvisits.data.model.personCoding.SalesAndCustomerTypesData
import com.akhnaton.foodvisits.data.statusValue.personCoding.PersonIntent
import com.akhnaton.foodvisits.data.statusValue.personCoding.PersonStatus
import com.akhnaton.foodvisits.databinding.FragmentPersonCodingBinding
import com.akhnaton.foodvisits.shared.DialogUtils
import com.akhnaton.foodvisits.shared.NationalIdImageProcessor
import com.akhnaton.foodvisits.shared.SharedPreferencesHelper
import com.akhnaton.foodvisits.ui.auth.LoginActivity2
import com.akhnaton.foodvisits.ui.home.MainActivity
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.gson.Gson
import com.google.mlkit.vision.documentscanner.GmsDocumentScanner
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import java.util.Locale
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

class PersonCodingFragment : Fragment() {

    companion object {
        private const val TAG = "PersonCodingFragment"
    }

    private var _binding: FragmentPersonCodingBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PersonCodingViewModel by viewModels()

    private var selectedCustomerType: CustomerType? = null
    private var selectedOrderType: String? = null
    private var selectedLine: LineItem? = null
    private var selectedMainCustomer: MainCustomer? = null
    private var selectedGovernorate: Governorate? = null
    private var selectedArea: Area? = null

    private var frontIdImageUri: Uri? = null
    private var backIdImageUri: Uri? = null
    private var pickingFrontImage = false

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var pendingRetry: (() -> Unit)? = null
    private var hasRetriedAfterRefresh = false

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            if (pickingFrontImage) {
                frontIdImageUri = uri
                val frontPart = frontIdImageUri?.let { uriToMultipartPart(it, "front_image") }
                callNationalIdScanAPI(frontPart)
            } else {
                backIdImageUri = uri
                binding.imBackIdImage.setImageURI(uri)
                binding.imBackIdImage.visibility = View.VISIBLE
                binding.layoutBackPlaceholder.visibility = View.GONE
            }
        }
    }

    private var cameraProvider: ProcessCameraProvider? = null
    private var imageCapture: ImageCapture? = null
    private val CAMERA_PERMISSION_CODE = 100
    private val NATIONAL_ID_ASPECT_RATIO = 1.586f
    private var capturedFile: File? = null

    private lateinit var documentScanner: GmsDocumentScanner

    private val documentScannerLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { result ->

            if (result.resultCode != android.app.Activity.RESULT_OK) {
                Log.d(TAG, "Document scanner cancelled")
                return@registerForActivityResult
            }

            val scannerResult =
                GmsDocumentScanningResult.fromActivityResultIntent(result.data)

            val page = scannerResult
                ?.getPages()
                ?.firstOrNull()

            val imageUri = page?.getImageUri()

            if (imageUri == null) {
                showError("تعذر الحصول على صورة البطاقة")
                return@registerForActivityResult
            }

            handleScannedFrontImage(imageUri)
        }

    var name: String = "front_image"

    private fun createDocumentScanner() {

        val options =
            GmsDocumentScannerOptions.Builder()
                .setGalleryImportAllowed(true)
                .setPageLimit(1)
                .setResultFormats(
                    GmsDocumentScannerOptions.RESULT_FORMAT_JPEG
                )
                .setScannerMode(
                    GmsDocumentScannerOptions.SCANNER_MODE_FULL
                )
                .build()

        documentScanner =
            GmsDocumentScanning.getClient(options)
    }

    private fun startNationalIdScanner() {
        documentScanner
            .getStartScanIntent(requireActivity())
            .addOnSuccessListener { intentSender ->

                documentScannerLauncher.launch(
                    IntentSenderRequest.Builder(intentSender).build()
                )

            }
            .addOnFailureListener { exception ->

                Log.e(
                    TAG,
                    "Unable to start document scanner",
                    exception
                )

                showError(
                    "تعذر تشغيل ماسح البطاقة"
                )
            }
    }

    fun callNationalIdScanAPI(frontPart: okhttp3.MultipartBody.Part?) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.customerIntent.send(
                PersonIntent.NationalIdScan(frontPart)
            )
        }
    }

    private fun handleScannedFrontImage(
        uri: Uri
    ) {

        frontIdImageUri = uri

        binding.imFrontIdImage.setImageURI(uri)

        binding.imFrontIdImage.visibility =
            View.VISIBLE

        binding.layoutFrontPlaceholder.visibility =
            View.GONE

        hideCamera()

        binding.progressLoading.visibility =
            View.VISIBLE

        val frontPart =
            uriToMultipartPart(
                uri = uri,
                partName = "front_image"
            )

        if (frontPart == null) {

            binding.progressLoading.visibility =
                View.GONE

            showError(
                "تعذر تجهيز صورة البطاقة"
            )

            return
        }

        callNationalIdScanAPI(
            frontPart
        )
    }

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            fetchCurrentLocation()
        } else {
            DialogUtils.showResultDialog(
                context = requireContext(),
                message = "لازم تسمح بالوصول للموقع عشان نحدد العنوان المقترح",
                isSuccess = false,
                showOkButton = true,
                onOk = {})
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPersonCodingBinding.inflate(inflater, container, false)
        //MainActivity.binding.navView2.visibility = View.GONE
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

//        createDocumentScanner()

//        if (ContextCompat.checkSelfPermission(
//                requireContext(),
//                android.Manifest.permission.CAMERA
//            ) == PackageManager.PERMISSION_GRANTED
//        ) {
//            startCamera()
//        } else {
//            requestPermissions(arrayOf(android.Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
//        }

        setupListeners()
        observeStatus()
        checkLocationPermissionAndFetch()

        getSalesAndCustomerTypes()
        getUserAreas()
    }

    private fun openCamera() {

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_CODE
            )
            return
        }

        binding.cameraCard.visibility = View.VISIBLE
        binding.ivCaptureButton.visibility = View.VISIBLE

        // Important:
        // Wait until PreviewView/CardView is actually visible and laid out.
        binding.previewView.post {

            if (!isAdded || _binding == null) {
                return@post
            }

            startCamera()
        }
    }

    private fun setupListeners() {
        binding.btnBackContainer.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

//        binding.layoutFrontPlaceholder.setOnClickListener {
//            binding.cameraCard.visibility = View.VISIBLE
//            binding.ivCaptureButton.visibility = View.VISIBLE
////            startNationalIdScanner()
//            pickingFrontImage = true
////            pickImageLauncher.launch("image/*")
//        }

        binding.layoutFrontPlaceholder.setOnClickListener {
            binding.tvCameraTitle.text = "الصورة الأمامية"
            pickingFrontImage = true
            openCamera()
        }

        binding.imFrontIdImage.setOnClickListener {
            frontIdImageUri?.let { showFullScreenImage(it) }
        }

        binding.layoutBackPlaceholder.setOnClickListener {
            binding.tvCameraTitle.text = "الصورة الخلفية"
//            binding.cameraCard.visibility = View.VISIBLE
//            binding.ivCaptureButton.visibility = View.VISIBLE
            pickingFrontImage = false
            openCamera()
//            pickImageLauncher.launch("image/*")
        }

        binding.imBackIdImage.setOnClickListener {
            backIdImageUri?.let { showFullScreenImage(it) }
        }

        binding.ivCaptureButton.setOnClickListener {
            if (pickingFrontImage) takePhoto("front_image")
            else takePhoto("back_image")
        }

        binding.addCustomerBtn.setOnClickListener {
            submitAddCustomer()
        }
    }

    private fun showFullScreenImage(uri: Uri) {
        val dialog =
            android.app.Dialog(requireContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        val view = layoutInflater.inflate(R.layout.dialog_image_preview, null)
        dialog.setContentView(view)

        val photoView = view.findViewById<com.github.chrisbanes.photoview.PhotoView>(R.id.photoView)
        val btnClose = view.findViewById<View>(R.id.btnCloseDialog)

        photoView.setImageURI(uri)

        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun checkLocationPermissionAndFetch() {
        val fineLocationGranted = ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (fineLocationGranted) {
            fetchCurrentLocation()
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    @Suppress("MissingPermission")
    private fun fetchCurrentLocation() {
        binding.progressLoading.visibility = View.VISIBLE

        val currentLocationRequest =
            CurrentLocationRequest.Builder().setPriority(Priority.PRIORITY_HIGH_ACCURACY).build()

        fusedLocationClient.getCurrentLocation(currentLocationRequest, null)
            .addOnSuccessListener { location ->
                if (location != null) {
                    binding.fieldLongitude.text = location.longitude.toString()
                    binding.fieldLatitude.text = location.latitude.toString()
                    resolveAddressFromLocation(location.latitude, location.longitude)
                } else {
                    binding.progressLoading.visibility = View.GONE
                    showError("تعذر تحديد الموقع الحالي")
                }
            }.addOnFailureListener {
                binding.progressLoading.visibility = View.GONE
                showError("حدث خطأ أثناء تحديد الموقع")
            }
    }

    private fun resolveAddressFromLocation(lat: Double, lng: Double) {
        viewLifecycleOwner.lifecycleScope.launch {
            val addressText = withContext(Dispatchers.IO) {
                try {
                    val geocoder = Geocoder(requireContext(), Locale("ar"))

                    @Suppress("DEPRECATION") val addresses = geocoder.getFromLocation(lat, lng, 1)
                    addresses?.firstOrNull()?.getAddressLine(0)
                } catch (e: Exception) {
                    null
                }
            }

            binding.progressLoading.visibility = View.GONE
            binding.suggestedAddress.setText(
                addressText ?: getString(R.string.hint_no_suggested_address)
            )
        }
    }

    private fun getSalesAndCustomerTypes() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.customerIntent.send(PersonIntent.GetSalesAndCustomerTypes)
        }
    }

    private fun getUserAreas() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.customerIntent.send(PersonIntent.GetUserAreas)
        }
    }

    private fun sendRefreshToken() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.customerIntent.send(
                PersonIntent.RefreshToken(
                    SharedPreferencesHelper.getInstance().getEmployeeId(),
                    SharedPreferencesHelper.getInstance().getUserToken()
                )
            )
        }
    }

    private fun handleResponse(
        code: Int, message: String, retry: () -> Unit, onSuccess: () -> Unit
    ) {
        Log.d(TAG, "response code=$code message=$message retried=$hasRetriedAfterRefresh")
        when (code) {
            200 -> {
                hasRetriedAfterRefresh = false
                onSuccess()
            }

            401 -> {
                if (hasRetriedAfterRefresh) {
                    hasRetriedAfterRefresh = false
                    pendingRetry = null
                    showSessionExpired(message)
                } else {
                    pendingRetry = retry
                    sendRefreshToken()
                }
            }

            else -> {
                hasRetriedAfterRefresh = false
                showError(message)
            }
        }
    }

    private fun showSessionExpired(message: String) {
        DialogUtils.showResultDialog(
            context = requireContext(),
            message = message,
            isSuccess = false,
            showOkButton = true,
            onOk = {
                SharedPreferencesHelper.getInstance().logOut()
                startActivity(Intent(requireContext(), LoginActivity2::class.java))
                requireActivity().finishAffinity()
            })
    }

    private fun observeStatus() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.status.collect { status ->
                    when (status) {
                        is PersonStatus.Loading -> {
                            binding.progressLoading.visibility = View.VISIBLE
                        }

                        is PersonStatus.GetSalesAndCustomerTypes -> {
                            binding.progressLoading.visibility = View.GONE
                            handleResponse(
                                code = status.response.status,
                                message = status.response.message,
                                retry = { getSalesAndCustomerTypes() }) {
                                val data = Gson().fromJson(
                                    status.response.data, SalesAndCustomerTypesData::class.java
                                )
                                bindCustomerAndOrderTypes(data)
                            }
                        }

                        is PersonStatus.GetLines -> {
                            binding.progressLoading.visibility = View.GONE
                            handleResponse(
                                code = status.response.status,
                                message = status.response.message,
                                retry = { tryLoadLines() }) {
                                bindLines(status.response)
                            }
                        }

                        is PersonStatus.GetMainCustomersLine -> {
                            binding.progressLoading.visibility = View.GONE
                            handleResponse(
                                code = status.response.status,
                                message = status.response.message,
                                retry = { tryLoadMainCustomers() }) {
                                val data = Gson().fromJson(
                                    status.response.data, MainCustomersLineData::class.java
                                )
                                bindMainCustomers(data)
                            }
                        }

                        is PersonStatus.RefreshToken -> {
                            binding.progressLoading.visibility = View.GONE
                            Log.d(
                                TAG,
                                "refreshToken status=${status.data.status} message=${status.data.message}"
                            )
                            if (status.data.status == 200) {
                                val tokenData = Gson().fromJson(
                                    status.data.data,
                                    com.akhnaton.foodvisits.data.model.refreshToken.Data::class.java
                                )
                                SharedPreferencesHelper.getInstance().saveUserToken(tokenData.TOKEN)
                                hasRetriedAfterRefresh = true
                                val retry = pendingRetry
                                pendingRetry = null
                                retry?.invoke()
                            } else {
                                pendingRetry = null
                                hasRetriedAfterRefresh = false
                                showSessionExpired(status.data.message)
                            }
                        }

                        is PersonStatus.GetUserAreas -> {
                            binding.progressLoading.visibility = View.GONE
                            handleResponse(
                                code = status.response.status,
                                message = status.response.message,
                                retry = { getUserAreas() }) {
                                val data = Gson().fromJson(
                                    status.response.data, GovernoratesData::class.java
                                )
                                bindGovernorates(data)
                            }
                        }

                        is PersonStatus.GetAreasByGovernorate -> {
                            binding.progressLoading.visibility = View.GONE
                            handleResponse(
                                code = status.response.status,
                                message = status.response.message,
                                retry = {
                                    selectedGovernorate?.let {
                                        loadAreasByGovernorate(it.id)
                                    }
                                }) {
                                val data = Gson().fromJson(
                                    status.response.data, AreasData::class.java
                                )
                                bindAreas(data)
                            }
                        }

                        is PersonStatus.AddCustomer -> {
                            binding.progressLoading.visibility = View.GONE
                            handleResponse(
                                code = status.response.status,
                                message = status.response.message.firstOrNull().orEmpty(),
                                retry = { submitAddCustomer() }) {
                                DialogUtils.showResultDialog(
                                    context = requireContext(),
                                    message = status.response.message.firstOrNull().orEmpty(),
                                    isSuccess = true,
                                    showOkButton = true,
                                    onOk = {
                                        requireActivity().onBackPressedDispatcher.onBackPressed()
                                    })
                            }
                        }

                        is PersonStatus.NationalIdScan -> {
                            binding.progressLoading.visibility = View.GONE

                            handleResponse(
                                code = status.response.status,
                                message = status.response.message,
                                retry = {
                                    val frontPart = frontIdImageUri?.let {
                                        uriToMultipartPart(
                                            it, name
                                        )
                                    }
                                    callNationalIdScanAPI(frontPart)
                                }) {
                                if (status.response.status == 200) {
                                    DialogUtils.showResultDialog(
                                        context = requireContext(),
                                        message = status.response.message,
                                        isSuccess = true,
                                        showOkButton = true,
                                        onOk = {
                                            binding.imFrontIdImage.setImageURI(frontIdImageUri)
                                            binding.imFrontIdImage.visibility = View.VISIBLE
                                            binding.layoutFrontPlaceholder.visibility = View.GONE
                                            binding.layoutNationalID.visibility = View.VISIBLE
                                            binding.customerNational.setText(status.response.data.national_id)
                                            binding.customerCardName.setText(status.response.data.user_name)
                                            binding.customerCardNumber.setText(status.response.data.address)
//                                                requireActivity().onBackPressedDispatcher.onBackPressed()
                                        })
                                } else if (status.response.status == 400) {
                                    DialogUtils.showResultDialog(
                                        context = requireContext(),
                                        message = status.response.message,
                                        isSuccess = false,
                                        showOkButton = true,
                                        onOk = {
//                                                requireActivity().onBackPressedDispatcher.onBackPressed()
                                        })
                                }

                            }
                        }

                        is PersonStatus.Error -> {
                            Log.d(TAG, "observeStatus: ${status.message}")
                            binding.progressLoading.visibility = View.GONE
                        }

                        else -> {}
                    }
                }
            }
        }
    }

    private fun showError(message: String?) {
        DialogUtils.showResultDialog(
            context = requireContext(),
            message = message.orEmpty(),
            isSuccess = false,
            showOkButton = true,
            onOk = {})
    }

    private fun resetLine() {
        selectedLine = null
        binding.lines.setText("", false)
        binding.lines.setAdapter(null)
    }

    private fun resetMainCustomer() {
        selectedMainCustomer = null
        binding.personType.setText("", false)
        binding.personType.setAdapter(null)
    }

    private fun resetArea() {
        selectedArea = null
        binding.spSelectArea.setText("", false)
        binding.spSelectArea.setAdapter(null)
    }

    private fun resetLineAndBelow() {
        resetLine()
        resetMainCustomer()
    }

    private fun bindCustomerAndOrderTypes(data: SalesAndCustomerTypesData?) {
        val customerTypes = data?.customer_types ?: emptyList()
        binding.customerType.setAdapter(
            ArrayAdapter(
                requireContext(), android.R.layout.simple_dropdown_item_1line, customerTypes
            )
        )
        binding.customerType.threshold = 0
        binding.customerType.setOnItemClickListener { _, _, position, _ ->
            selectedCustomerType = customerTypes[position]
            resetLineAndBelow()
            tryLoadLines()
        }

        val orderTypes = data?.sales_types ?: emptyList()
        binding.orderType.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, orderTypes)
        )
        binding.orderType.threshold = 0
        binding.orderType.setOnItemClickListener { _, _, position, _ ->
            selectedOrderType = orderTypes[position]
            resetLineAndBelow()
            tryLoadLines()
        }
    }

    private fun tryLoadLines() {
        val customerType = selectedCustomerType?.TYPE_CHILD_CODE
        val orderType = selectedOrderType
        if (customerType != null && orderType != null) {
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.customerIntent.send(PersonIntent.GetLines(orderType, customerType))
            }
        }
    }

    private fun bindLines(response: LinesModel) {
        val lines = response.data ?: emptyList()
        binding.lines.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, lines)
        )
        binding.lines.threshold = 0
        binding.lines.setOnItemClickListener { _, _, position, _ ->
            selectedLine = lines[position]
            resetMainCustomer()
            tryLoadMainCustomers()
        }
    }

    private fun tryLoadMainCustomers() {
        val lineId = selectedLine?.LINE_CODE
        val orderType = selectedOrderType
        val customerType = selectedCustomerType?.TYPE_CHILD_CODE
        if (lineId != null && orderType != null && customerType != null) {
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.customerIntent.send(
                    PersonIntent.GetMainCustomersLine(lineId, orderType, customerType)
                )
            }
        }
    }

    private fun bindMainCustomers(data: MainCustomersLineData?) {
        val customers = data?.main_customer_line ?: emptyList()
        binding.personType.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, customers)
        )
        binding.personType.threshold = 0
        binding.personType.setOnItemClickListener { _, _, position, _ ->
            selectedMainCustomer = customers[position]
        }
    }

    private fun bindGovernorates(data: GovernoratesData?) {
        val governorates = data?.governorateS ?: emptyList()
        binding.spSelectGovernorate.setAdapter(
            ArrayAdapter(
                requireContext(), android.R.layout.simple_dropdown_item_1line, governorates
            )
        )
        binding.spSelectGovernorate.threshold = 0
        binding.spSelectGovernorate.setOnItemClickListener { _, _, position, _ ->
            selectedGovernorate = governorates[position]
            resetArea()
            loadAreasByGovernorate(selectedGovernorate!!.id)
        }
    }

    private fun loadAreasByGovernorate(governorateId: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.customerIntent.send(PersonIntent.GetAreasByGovernorate(governorateId))
        }
    }

    private fun bindAreas(data: AreasData?) {
        val areas = data?.areas ?: emptyList()
        binding.spSelectArea.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, areas)
        )
        binding.spSelectArea.threshold = 0
        binding.spSelectArea.setOnItemClickListener { _, _, position, _ ->
            selectedArea = areas[position]
        }
    }

    private fun uriToMultipartPart(uri: Uri, partName: String): MultipartBody.Part? {
        return try {
            val inputStream = requireContext().contentResolver.openInputStream(uri) ?: return null
            val file = File(requireContext().cacheDir, "$partName.jpg")
            FileOutputStream(file).use { output -> inputStream.copyTo(output) }
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData(partName, file.name, requestFile)
        } catch (e: Exception) {
            null
        }
    }

    private fun submitAddCustomer() {
        val lineId = selectedLine?.LINE_CODE
        val customerCode = selectedMainCustomer?.customer_code

        val fields = mutableMapOf<String, RequestBody>()

        fun put(key: String, value: String?) {
            if (!value.isNullOrBlank()) {
                fields[key] = value.toRequestBody("text/plain".toMediaTypeOrNull())
            }
        }

        put("line_id", lineId)
        put("customer_code", customerCode)
        put("customer_type", selectedCustomerType?.TYPE_CHILD_CODE)
        put("order_type", selectedOrderType)
        put("governorate_id", selectedGovernorate?.id)
        put("city_id", selectedArea?.id)
        put("customer_name", binding.customerName.text?.toString())
        put("customer_address", binding.customerAddress.text?.toString())
        put("phone", binding.spWriteCustomerPhone.text?.toString())
        put("phone_2", binding.spWriteCustomerMobile.text?.toString())
        put("customer_national_id", binding.customerNational.text?.toString())
        put("customer_latitude", binding.fieldLatitude.text?.toString())
        put("customer_longitude", binding.fieldLongitude.text?.toString())

        put("name_in_national_id", binding.customerCardName.text?.toString())
        put("address_in_national_id", binding.customerCardNumber.text?.toString())

        val frontPart = frontIdImageUri?.let { uriToMultipartPart(it, "id_1") }
        val backPart = backIdImageUri?.let { uriToMultipartPart(it, "id_2") }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.customerIntent.send(
                PersonIntent.AddCustomer(fields, frontPart, backPart)
            )
        }
    }

    private fun startCamera() {

        val context = requireContext()

        val cameraProviderFuture =
            ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({

            try {

                val provider =
                    cameraProviderFuture.get()

                cameraProvider = provider

                // Always clear previous camera use cases
                provider.unbindAll()

                val preview =
                    Preview.Builder()
                        .setTargetAspectRatio(
                            AspectRatio.RATIO_4_3
                        )
                        .build()

                val capture =
                    ImageCapture.Builder()
                        .setCaptureMode(
                            ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY
                        )
                        .setTargetAspectRatio(
                            AspectRatio.RATIO_4_3
                        )
                        .setJpegQuality(95)
                        .build()

                imageCapture = capture

                val cameraSelector =
                    CameraSelector.DEFAULT_BACK_CAMERA

                // Connect Preview to PreviewView
                preview.setSurfaceProvider(
                    binding.previewView.surfaceProvider
                )

                // Bind everything to Fragment lifecycle
                provider.bindToLifecycle(
                    viewLifecycleOwner,
                    cameraSelector,
                    preview,
                    capture
                )

                Log.d(
                    TAG,
                    "Camera started successfully"
                )

            } catch (e: Exception) {

                Log.e(
                    TAG,
                    "Camera binding failed",
                    e
                )

                imageCapture = null

                Toast.makeText(
                    context,
                    "تعذر تشغيل الكاميرا",
                    Toast.LENGTH_SHORT
                ).show()
            }

        }, ContextCompat.getMainExecutor(context))
    }

    private fun takePhoto(name: String) {

        val imageCapture = imageCapture ?: return

        val outputDir =
            requireContext().externalCacheDir ?: return

        val originalFile = File(
            outputDir,
            "national_id_original_${System.currentTimeMillis()}.jpg"
        )

        val outputOptions =
            ImageCapture.OutputFileOptions.Builder(originalFile)
                .build()

        imageCapture.targetRotation =
            binding.previewView.display.rotation

        binding.ivCaptureButton.isEnabled = false

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {

                override fun onImageSaved(
                    output: ImageCapture.OutputFileResults
                ) {

                    try {

                        val croppedFile =
                            NationalIdImageProcessor.process(
                                context = requireContext(),
                                inputFile = originalFile
                            )

                        // Save the final image URI
                        if (name == "front_image") {
                            frontIdImageUri = Uri.fromFile(croppedFile)

                            // Display the exact image sent to API
                            binding.imFrontIdImage.setImageURI(
                                frontIdImageUri
                            )

                            binding.imFrontIdImage.visibility =
                                View.VISIBLE

                            binding.layoutFrontPlaceholder.visibility =
                                View.GONE

                            val part =
                                fileToMultipart(
                                    croppedFile,
                                    name
                                )

                            if (!validateNationalIdImage(croppedFile)) {

                                Toast.makeText(
                                    requireContext(),
                                    "يرجى إعادة تصوير البطاقة بشكل واضح",
                                    Toast.LENGTH_SHORT
                                ).show()

                                return
                            }

                            callNationalIdScanAPI(part)
                        } else {
                            backIdImageUri = Uri.fromFile(croppedFile)

                            // Display the exact image sent to API
                            binding.imBackIdImage.setImageURI(
                                backIdImageUri
                            )

                            binding.imBackIdImage.visibility =
                                View.VISIBLE

                            binding.layoutBackPlaceholder.visibility =
                                View.GONE
                        }

                    } catch (e: Exception) {

                        Log.e(
                            "NationalIDCamera",
                            "Image processing failed",
                            e
                        )

                        Toast.makeText(
                            requireContext(),
                            "تعذر تجهيز صورة البطاقة",
                            Toast.LENGTH_SHORT
                        ).show()

                    } finally {

                        binding.ivCaptureButton.isEnabled = true

                        // Original is no longer needed
                        originalFile.delete()
                    }
                }

                override fun onError(
                    exception: ImageCaptureException
                ) {

                    binding.ivCaptureButton.isEnabled = true

                    Toast.makeText(
                        requireContext(),
                        "خطأ في التصوير",
                        Toast.LENGTH_SHORT
                    ).show()

                    Log.e(
                        "NationalIDCamera",
                        "Capture failed",
                        exception
                    )
                }
            }
        )
    }

    private fun validateNationalIdImage(
        file: File
    ): Boolean {

        val options =
            BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }

        BitmapFactory.decodeFile(
            file.absolutePath,
            options
        )

        val width = options.outWidth
        val height = options.outHeight

        if (width <= 0 || height <= 0) {
            return false
        }

        val ratio =
            maxOf(width, height).toFloat() /
                    minOf(width, height).toFloat()

        return ratio > 1.45f && ratio < 1.75f
    }

    private fun fileToMultipart(
        file: File,
        name: String
    ): MultipartBody.Part {

        val requestFile =
            file.asRequestBody(
                "image/jpeg".toMediaType()
            )

        return MultipartBody.Part.createFormData(
            name = name,
            filename = file.name,
            body = requestFile
        )
    }

    private fun closeCamera() {
        try {
            cameraProvider?.unbindAll()
        } catch (e: Exception) {
            Log.e(
                TAG,
                "Failed to close camera",
                e
            )
        }
        imageCapture = null
    }

    private fun hideCamera() {
        closeCamera()
        binding.cameraCard.visibility = View.GONE
        binding.ivCaptureButton.visibility = View.GONE
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults
        )

        if (requestCode == CAMERA_PERMISSION_CODE) {

            if (
                grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                openCamera()
            } else {
                Toast.makeText(
                    requireContext(),
                    "يجب السماح باستخدام الكاميرا",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        pendingRetry = null
        _binding = null
    }
}
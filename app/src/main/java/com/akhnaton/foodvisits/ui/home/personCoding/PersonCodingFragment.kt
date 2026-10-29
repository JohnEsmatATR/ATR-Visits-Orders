package com.akhnaton.foodvisits.ui.home.personCoding

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Geocoder
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.location.Location
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
import androidx.navigation.fragment.findNavController
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
import com.google.android.gms.tasks.CancellationTokenSource
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
import java.io.IOException

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
    private var isRefreshTokenCalled = false

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
                binding.tvBackRetry.visibility = View.VISIBLE
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

    private val documentScannerLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->

        if (result.resultCode != android.app.Activity.RESULT_OK) {
            Log.d(TAG, "Document scanner cancelled")
            return@registerForActivityResult
        }

        val scannerResult = GmsDocumentScanningResult.fromActivityResultIntent(result.data)

        val page = scannerResult?.getPages()?.firstOrNull()

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
            GmsDocumentScannerOptions.Builder().setGalleryImportAllowed(true).setPageLimit(1)
                .setResultFormats(
                    GmsDocumentScannerOptions.RESULT_FORMAT_JPEG
                ).setScannerMode(
                    GmsDocumentScannerOptions.SCANNER_MODE_FULL
                ).build()

        documentScanner = GmsDocumentScanning.getClient(options)
    }

    private fun startNationalIdScanner() {
        documentScanner.getStartScanIntent(requireActivity()).addOnSuccessListener { intentSender ->

            documentScannerLauncher.launch(
                IntentSenderRequest.Builder(intentSender).build()
            )

        }.addOnFailureListener { exception ->

            Log.e(
                TAG, "Unable to start document scanner", exception
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

        binding.imFrontIdImage.visibility = View.VISIBLE
        binding.tvFrontRetry.visibility = View.VISIBLE

        binding.layoutFrontPlaceholder.visibility = View.GONE

        hideCamera()

        binding.progressLoading.visibility = View.VISIBLE

        val frontPart = uriToMultipartPart(
            uri = uri, partName = "front_image"
        )

        if (frontPart == null) {

            binding.progressLoading.visibility = View.GONE

            showError(
                "تعذر تجهيز صورة البطاقة"
            )

            return
        }

        callNationalIdScanAPI(
            frontPart
        )
    }

    private val locationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->

            Log.d(TAG, "Permission callback: $permissions")

            val fineGranted =
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true

            val coarseGranted =
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

            Log.d(TAG, "Fine granted: $fineGranted")
            Log.d(TAG, "Coarse granted: $coarseGranted")

            if (fineGranted || coarseGranted) {
                Log.d(TAG, "Permission accepted; fetching location")
                fetchCurrentLocation()
            } else {
                Log.e(TAG, "Location permissions denied")

                if (_binding != null) {
                    binding.progressLoading.visibility = View.GONE

                    DialogUtils.showResultDialog(
                        context = requireContext(),
                        message = "لازم تسمح بالوصول للموقع عشان نحدد العنوان المقترح",
                        isSuccess = false,
                        showOkButton = true,
                        onOk = {}
                    )
                }
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
                requireContext(), Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE
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

        binding.tvRetry.setOnClickListener {
            binding.layoutNationalID.visibility = View.GONE
            binding.tvCameraTitle.text = "الصورة الأمامية"
            pickingFrontImage = true
            openCamera()
        }

        binding.imFrontIdImage.setOnClickListener {
            frontIdImageUri?.let { showFullScreenImage(it) }
        }

        binding.tvFrontRetry.setOnClickListener {
            binding.layoutNationalID.visibility = View.GONE
            binding.tvCameraTitle.text = "الصورة الأمامية"
            pickingFrontImage = true
            openCamera()
        }

        binding.tvBackRetry.setOnClickListener {
//            binding.layoutNationalID.visibility = View.GONE
            binding.tvCameraTitle.text = "الصورة الخلفية"
            pickingFrontImage = false
            openCamera()
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
        Log.d(TAG, "========== CHECK LOCATION PERMISSION ==========")
        Log.d(TAG, "Android SDK: ${android.os.Build.VERSION.SDK_INT}")

        val fineGranted = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        Log.d(TAG, "Fine location granted: $fineGranted")
        Log.d(TAG, "Coarse location granted: $coarseGranted")

        if (fineGranted || coarseGranted) {
            Log.d(TAG, "At least one location permission is granted.")
            fetchCurrentLocation()
        } else {
            Log.d(TAG, "No location permission. Launching permission request.")

            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    @Suppress("MissingPermission")
    private fun fetchCurrentLocation() {
        Log.d(TAG, "========== FETCH LOCATION START ==========")

        val ctx = context ?: run {
            Log.e(TAG, "STOP: Fragment context is null")
            return
        }

        Log.d(TAG, "Android version: ${android.os.Build.VERSION.RELEASE}")
        Log.d(TAG, "Android SDK: ${android.os.Build.VERSION.SDK_INT}")

        val fineGranted = ContextCompat.checkSelfPermission(
            ctx,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted = ContextCompat.checkSelfPermission(
            ctx,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        Log.d(TAG, "Fine permission: $fineGranted")
        Log.d(TAG, "Coarse permission: $coarseGranted")

        if (!fineGranted && !coarseGranted) {
            Log.e(TAG, "STOP: No location permission")

            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
            return
        }

        Log.d(TAG, "Location permission check passed")
        Log.d(TAG, "Requesting high-accuracy current location")

        binding.progressLoading.visibility = View.VISIBLE

        val tokenSource = CancellationTokenSource()

        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            tokenSource.token
        )
            .addOnSuccessListener { location ->

                Log.d(TAG, "Current-location task succeeded")
                Log.d(TAG, "Current location is null: ${location == null}")

                if (location != null) {
                    Log.d(TAG, "Current location received")
                    onLocationReceived(location)
                } else {
                    Log.w(TAG, "Current location is null; trying lastLocation")

                    fusedLocationClient.lastLocation
                        .addOnSuccessListener { lastLocation ->

                            Log.d(
                                TAG,
                                "Last-location task succeeded; " +
                                        "location is null: ${lastLocation == null}"
                            )

                            if (lastLocation != null) {
                                Log.d(TAG, "Using last known location")
                                onLocationReceived(lastLocation)
                            } else {
                                Log.e(
                                    TAG,
                                    "Both current location and lastLocation are null"
                                )
                                finishLocationWithError(
                                    "تعذر تحديد الموقع الحالي. تأكد من تشغيل خدمة الموقع وحاول مرة أخرى."
                                )
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.e(
                                TAG,
                                "lastLocation failed",
                                exception
                            )

                            finishLocationWithError(
                                "حدث خطأ أثناء تحديد الموقع"
                            )
                        }
                }
            }
            .addOnFailureListener { exception ->
                Log.e(
                    TAG,
                    "getCurrentLocation failed",
                    exception
                )

                Log.d(TAG, "Trying lastLocation after request failure")

                fusedLocationClient.lastLocation
                    .addOnSuccessListener { lastLocation ->
                        Log.d(
                            TAG,
                            "Fallback completed; location is null: " +
                                    "${lastLocation == null}"
                        )

                        if (lastLocation != null) {
                            onLocationReceived(lastLocation)
                        } else {
                            finishLocationWithError(
                                "تعذر تحديد الموقع الحالي"
                            )
                        }
                    }
                    .addOnFailureListener { fallbackException ->
                        Log.e(
                            TAG,
                            "Fallback lastLocation failed",
                            fallbackException
                        )

                        finishLocationWithError(
                            "حدث خطأ أثناء تحديد الموقع"
                        )
                    }
            }
    }

    private fun onLocationReceived(location: Location) {
        Log.d(TAG, "========== LOCATION RECEIVED ==========")

        Log.d(TAG, "Latitude: ${location.latitude}")
        Log.d(TAG, "Longitude: ${location.longitude}")
        Log.d(TAG, "Provider: ${location.provider}")
        Log.d(TAG, "Accuracy: ${location.accuracy}")
        Log.d(TAG, "Has accuracy: ${location.hasAccuracy()}")
        Log.d(TAG, "Timestamp: ${location.time}")
        Log.d(
            TAG,
            "Age seconds: ${(System.currentTimeMillis() - location.time) / 1000}"
        )

        if (location.latitude !in -90.0..90.0 ||
            location.longitude !in -180.0..180.0
        ) {
            Log.e(TAG, "Invalid coordinates received")
            finishLocationWithError("الإحداثيات المستلمة غير صحيحة")
            return
        }

        binding.fieldLongitude.setText(location.longitude.toString())
        binding.fieldLatitude.setText(location.latitude.toString())

        Log.d(
            TAG,
            "Longitude field after update: ${binding.fieldLongitude.text}"
        )
        Log.d(
            TAG,
            "Latitude field after update: ${binding.fieldLatitude.text}"
        )

        Log.d(TAG, "Starting reverse geocoding")

        resolveAddressFromLocation(
            location.latitude,
            location.longitude
        )
    }

    private fun finishLocationWithError(message: String) {
        Log.e(TAG, "LOCATION FAILED: $message")

        if (_binding == null) return

        binding.progressLoading.visibility = View.GONE
        showError(message)
    }

//    private fun onLocationReceived(location: Location) {
//        binding.fieldLongitude.setText(location.longitude.toString())
//        binding.fieldLatitude.setText(location.latitude.toString())
//
//        resolveAddressFromLocation(
//            location.latitude,
//            location.longitude
//        )
//    }

    private fun resolveAddressFromLocation(
        lat: Double,
        lng: Double
    ) {
        Log.d(TAG, "========== GEOCODING START ==========")
        Log.d(TAG, "Input latitude: $lat")
        Log.d(TAG, "Input longitude: $lng")

        val appContext = context?.applicationContext ?: run {
            Log.e(TAG, "STOP: Application context is null")
            finishLocationWithError("تعذر الوصول إلى بيانات الموقع")
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            val addressText = withContext(Dispatchers.IO) {
                try {
                    Log.d(TAG, "Checking Geocoder availability")

                    if (!Geocoder.isPresent()) {
                        Log.e(TAG, "Geocoder.isPresent() returned false")
                        return@withContext null
                    }

                    Log.d(TAG, "Geocoder is present")

                    val geocoder = Geocoder(
                        appContext,
                        Locale("ar", "EG")
                    )

                    val startTime = System.currentTimeMillis()

                    Log.d(TAG, "Calling getFromLocation()")

                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(
                        lat,
                        lng,
                        1
                    )

                    val elapsed =
                        System.currentTimeMillis() - startTime

                    Log.d(TAG, "Geocoder elapsed time: ${elapsed}ms")
                    Log.d(
                        TAG,
                        "Address count: ${addresses?.size ?: 0}"
                    )

                    val address = addresses?.firstOrNull()

                    if (address == null) {
                        Log.w(TAG, "Geocoder returned no address")
                        null
                    } else {
                        Log.d(TAG, "Country: ${address.countryName}")
                        Log.d(TAG, "Locality: ${address.locality}")
                        Log.d(TAG, "Admin area: ${address.adminArea}")
                        Log.d(TAG, "Feature name: ${address.featureName}")

                        val line = address.getAddressLine(0)

                        Log.d(TAG, "Address line 0: $line")

                        line
                    }

                } catch (e: IOException) {
                    Log.e(TAG, "Geocoder IOException", e)
                    null
                } catch (e: IllegalArgumentException) {
                    Log.e(TAG, "Invalid coordinates passed to Geocoder", e)
                    null
                } catch (e: Exception) {
                    Log.e(TAG, "Unexpected geocoder error", e)
                    null
                }
            }

            if (_binding == null) {
                Log.w(TAG, "Fragment view destroyed before UI update")
                return@launch
            }

            val finalAddress = addressText
                ?.takeIf { it.isNotBlank() }
                ?: getString(R.string.hint_no_suggested_address)

            Log.d(TAG, "Final suggested address: $finalAddress")

            binding.suggestedAddress.setText(finalAddress)
            binding.progressLoading.visibility = View.GONE

            Log.d(
                TAG,
                "Suggested address field after update: " +
                        "${binding.suggestedAddress.text}"
            )
            Log.d(TAG, "========== GEOCODING FINISHED ==========")
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
        if (!isRefreshTokenCalled) {
            isRefreshTokenCalled = true
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.customerIntent.send(
                    PersonIntent.RefreshToken(
                        SharedPreferencesHelper.getInstance().getEmployeeId(),
                        SharedPreferencesHelper.getInstance().getUserToken()
                    )
                )
            }
        }
    }

    private fun handleResponse(
        isRefreshToken: Boolean,
        code: Int,
        message: String,
        retry: () -> Unit,
        onSuccess: () -> Unit
    ) {
        Log.d(TAG, "response code=$code message=$message retried=$hasRetriedAfterRefresh")
        when (code) {
            200 -> {
                hasRetriedAfterRefresh = false
                onSuccess()
            }

            401 -> {
                if (!isRefreshToken) {
                    Log.d("WHATretry", hasRetriedAfterRefresh.toString())
                    sendRefreshToken()
//                    if (hasRetriedAfterRefresh) {
//                        hasRetriedAfterRefresh = false
//                        pendingRetry = null
//                        showSessionExpired(message)
//                    } else {
//                        pendingRetry = retry
//                        sendRefreshToken()
//                    }
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
                                isRefreshToken = false,
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
                                isRefreshToken = false,
                                code = status.response.status,
                                message = status.response.message,
                                retry = { tryLoadLines() }) {
                                bindLines(status.response)
                            }
                        }

                        is PersonStatus.GetMainCustomersLine -> {
                            binding.progressLoading.visibility = View.GONE
                            handleResponse(
                                isRefreshToken = false,
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
                            if (status.data.status == 200) {
                                Log.d("WHATRefreshToken", "${status.data.message}")
                                val data = Gson().fromJson(
                                    status.data.data,
                                    com.akhnaton.foodvisits.data.model.refreshToken.Data::class.java
                                )
                                SharedPreferencesHelper.getInstance().saveUserToken(data.TOKEN)
                                getSalesAndCustomerTypes()
                                getUserAreas()
                            } else {
                                DialogUtils.showResultDialog(
                                    context = requireContext(),
                                    message = status.data.message,
                                    isSuccess = false,
                                    showOkButton = true,
                                    onOk = {
                                        SharedPreferencesHelper.getInstance().logOut()
                                        startActivity(
                                            Intent(
                                                requireContext(), LoginActivity2::class.java
                                            )
                                        )
                                        requireActivity().finishAffinity()
                                    })
                            }
                        }

                        is PersonStatus.GetUserAreas -> {
                            binding.progressLoading.visibility = View.GONE
                            handleResponse(
                                isRefreshToken = false,
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
                                isRefreshToken = false,
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
                                isRefreshToken = false,
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
                                isRefreshToken = false,
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
                                            binding.tvFrontRetry.visibility = View.VISIBLE
                                            binding.layoutFrontPlaceholder.visibility = View.GONE
                                            binding.layoutNationalID.visibility = View.VISIBLE
                                            binding.customerNational.setText(status.response.data.national_id)
                                            binding.customerCardName.setText(status.response.data.user_name)
                                            binding.customerCardNumber.setText(status.response.data.address)
                                            binding.cameraCard.visibility = View.GONE
                                            binding.ivCaptureButton.visibility = View.GONE
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
                            if (!isOnline()) {
                                DialogUtils.showResultDialog(
                                    context = requireContext(),
                                    message = "لا يوجد اتصال بالإنترنت",
                                    description = "يجب الاتصال بالإنترنت",
                                    isSuccess = false,
                                    showOkButton = true,
                                    onOk = {
                                        findNavController().navigate(R.id.toHome)
                                    }
                                )
                            }
                        }

                        else -> {}
                    }
                }
            }
        }
    }
    private fun isOnline(): Boolean {
        val manager =
            requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities = manager.getNetworkCapabilities(manager.activeNetwork)
        return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
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

        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({

            try {

                val provider = cameraProviderFuture.get()

                cameraProvider = provider

                // Always clear previous camera use cases
                provider.unbindAll()

                val preview = Preview.Builder().setTargetAspectRatio(
                    AspectRatio.RATIO_4_3
                ).build()

                val capture = ImageCapture.Builder().setCaptureMode(
                    ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY
                ).setTargetAspectRatio(
                    AspectRatio.RATIO_4_3
                ).setJpegQuality(95).build()

                imageCapture = capture

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                // Connect Preview to PreviewView
                preview.setSurfaceProvider(
                    binding.previewView.surfaceProvider
                )

                // Bind everything to Fragment lifecycle
                provider.bindToLifecycle(
                    viewLifecycleOwner, cameraSelector, preview, capture
                )

                Log.d(
                    TAG, "Camera started successfully"
                )

            } catch (e: Exception) {

                Log.e(
                    TAG, "Camera binding failed", e
                )

                imageCapture = null

                Toast.makeText(
                    context, "تعذر تشغيل الكاميرا", Toast.LENGTH_SHORT
                ).show()
            }

        }, ContextCompat.getMainExecutor(context))
    }

    private fun takePhoto(name: String) {

        val imageCapture = imageCapture ?: return

        val outputDir = requireContext().externalCacheDir ?: return

        val originalFile = File(
            outputDir, "national_id_original_${System.currentTimeMillis()}.jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(originalFile).build()

        imageCapture.targetRotation = binding.previewView.display.rotation

        binding.ivCaptureButton.isEnabled = false
        binding.cameraCard.visibility = View.GONE
        binding.ivCaptureButton.visibility = View.GONE

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {

                override fun onImageSaved(
                    output: ImageCapture.OutputFileResults
                ) {

                    try {

                        val croppedFile = NationalIdImageProcessor.process(
                            context = requireContext(), inputFile = originalFile
                        )

                        // Save the final image URI
                        if (name == "front_image") {
                            frontIdImageUri = Uri.fromFile(croppedFile)

                            // Display the exact image sent to API
                            binding.imFrontIdImage.setImageURI(
                                frontIdImageUri
                            )

                            binding.imFrontIdImage.visibility = View.VISIBLE
                            binding.tvFrontRetry.visibility = View.VISIBLE

                            binding.layoutFrontPlaceholder.visibility = View.GONE

                            val part = fileToMultipart(
                                croppedFile, name
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

                            binding.imBackIdImage.visibility = View.VISIBLE
                            binding.tvBackRetry.visibility = View.VISIBLE

                            binding.layoutBackPlaceholder.visibility = View.GONE
                        }

                    } catch (e: Exception) {

                        Log.e(
                            "NationalIDCamera", "Image processing failed", e
                        )

                        Toast.makeText(
                            requireContext(), "تعذر تجهيز صورة البطاقة", Toast.LENGTH_SHORT
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
                        requireContext(), "خطأ في التصوير", Toast.LENGTH_SHORT
                    ).show()

                    Log.e(
                        "NationalIDCamera", "Capture failed", exception
                    )
                }
            })
    }

    private fun validateNationalIdImage(
        file: File
    ): Boolean {

        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }

        BitmapFactory.decodeFile(
            file.absolutePath, options
        )

        val width = options.outWidth
        val height = options.outHeight

        if (width <= 0 || height <= 0) {
            return false
        }

        val ratio = maxOf(width, height).toFloat() / minOf(width, height).toFloat()

        return ratio > 1.45f && ratio < 1.75f
    }

    private fun fileToMultipart(
        file: File, name: String
    ): MultipartBody.Part {

        val requestFile = file.asRequestBody(
            "image/jpeg".toMediaType()
        )

        return MultipartBody.Part.createFormData(
            name = name, filename = file.name, body = requestFile
        )
    }

    private fun closeCamera() {
        try {
            cameraProvider?.unbindAll()
        } catch (e: Exception) {
            Log.e(
                TAG, "Failed to close camera", e
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
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(
            requestCode, permissions, grantResults
        )

        if (requestCode == CAMERA_PERMISSION_CODE) {

            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                Toast.makeText(
                    requireContext(), "يجب السماح باستخدام الكاميرا", Toast.LENGTH_SHORT
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
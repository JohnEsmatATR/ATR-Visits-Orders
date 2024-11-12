package com.akhnaton.foodvisits.ui.home.customerCoding

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.akhnaton.foodvisits.BuildConfig
import com.akhnaton.foodvisits.data.interfaces.location.ILocationClient
import com.akhnaton.foodvisits.data.model.LocationData
import com.akhnaton.foodvisits.data.model.coding.CodingAreaModel
import com.akhnaton.foodvisits.data.model.coding.CodingCategoryModel
import com.akhnaton.foodvisits.data.model.coding.CodingLineModel
import com.akhnaton.foodvisits.data.model.coding.CodingTypeModel
import com.akhnaton.foodvisits.data.statusValue.customerCoding.CustomerCodingIntent
import com.akhnaton.foodvisits.data.statusValue.customerCoding.CustomerCodingState
import com.akhnaton.foodvisits.data.statusValue.visit.VisitsIntent
import com.akhnaton.foodvisits.databinding.ActivityCustomerCodingBinding
import com.akhnaton.foodvisits.shared.BaseActivity
import com.akhnaton.foodvisits.shared.Common
import com.akhnaton.foodvisits.shared.ProgressDialogHelper
import com.akhnaton.foodvisits.shared.SharedPreferencesHelper
import com.akhnaton.foodvisits.shared.location.DefaultLocationClient
import com.akhnaton.foodvisits.shared.location.GetLocationService
import com.akhnaton.foodvisits.shared.location.RequestPermission
import com.github.dhaval2404.imagepicker.ImagePicker.Companion.REQUEST_CODE
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.github.dhaval2404.imagepicker.ImagePicker.Companion.with
import com.google.android.gms.location.LocationServices
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class CustomerCodingActivity : BaseActivity() {
    private lateinit var binding: ActivityCustomerCodingBinding
    private val viewModel: CustomerCodingViewModel by viewModels()
    private val versionName = BuildConfig.VERSION_NAME
    private val locationPermissionCode = 199
    private var requestPermission = RequestPermission()
    private val typesList: ArrayList<CodingTypeModel> = ArrayList()
    private val linesList: ArrayList<CodingLineModel> = ArrayList()
    private val categoriesList: ArrayList<CodingCategoryModel> = ArrayList()
    private val areasList: ArrayList<CodingAreaModel> = ArrayList()
    private var typePosition: String = ""
    private var linePosition: String = ""
    private var categoryPosition: String = ""
    private var areaPosition: String = ""
    private var imageFrontId: String = "IMG_20241107_104659726.jpg"
    private var imageBackId: String = "IMG_20241107_104659726.jpg"
    private var requestCode = 0
    private var mCurrentLocation: Location? = null
    private lateinit var locationClient: ILocationClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomerCodingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
        onClick()
    }

    override fun onResume() {
        super.onResume()
        RequestPermission().enableLocation(this)
        requestPermission.permissionCheck(this)

        Intent(this, GetLocationService::class.java).apply {
            action = GetLocationService.ACTION_START
            startService(this)
        }

        DefaultLocationClient(
            this@CustomerCodingActivity,
            null
        ).checkGpsOpened(this@CustomerCodingActivity)
    }

    override fun onPause() {
        super.onPause()
        Intent(this, GetLocationService::class.java).apply {
            action = GetLocationService.ACTION_STOP
            startService(this)
        }
    }

    private fun init() {
        askPermission()
        observeCustomerCoding()
        getCustomerType()
        getAreas()
        spTypesSelected()
        spLinesSelected()
        spCategoriesSelected()
        spAreasSelected()

        locationClient = DefaultLocationClient(
            this,
            LocationServices.getFusedLocationProviderClient(this)
        )

        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                when (result.resultCode) {
                    locationPermissionCode -> when (result.resultCode) {
                        Activity.RESULT_OK -> Log.d("abc", "OK")
                        Activity.RESULT_CANCELED -> RequestPermission().enableLocation(this)
                    }
                }
            }
        }
    }

    private fun onClick() {
        binding.imFrontIdImage.setOnClickListener {
            chooseIdPhoto(1)
        }
        binding.imBackIdImage.setOnClickListener {
            chooseIdPhoto(2)
        }
        binding.backBtn.setOnClickListener {
            finish()
        }

        binding.btnSubmit.setOnClickListener {
            postRegisterAsMember()
        }
    }

    private fun spTypesSelected() {
        binding.spSelectCustomerType.setOnItemClickListener { parent, _, position, _ ->
            typePosition = typesList[position].TYPE_CHILD_CODE
            linesList.clear()
            categoriesList.clear()
            linePosition = ""
            categoryPosition = ""
            binding.spSelectCustomerLine.setText("")
            binding.spSelectCustomerCategory.setText("")

            val itemsLines = ArrayList<String>()
            for (i in categoriesList) {
                itemsLines.add(i.CUSTOMER_NAME)
            }
            val adapterCities: ArrayAdapter<String> =
                ArrayAdapter<String>(
                    this@CustomerCodingActivity,
                    android.R.layout.simple_spinner_dropdown_item,
                    itemsLines
                )
            binding.spSelectCustomerLine.setAdapter(adapterCities)


            val itemsArea = ArrayList<String>()
            for (i in categoriesList) {
                itemsArea.add(i.CUSTOMER_NAME)
            }
            val adapterAreas: ArrayAdapter<String> =
                ArrayAdapter<String>(
                    this@CustomerCodingActivity,
                    android.R.layout.simple_spinner_dropdown_item,
                    itemsArea
                )
            binding.spSelectCustomerCategory.setAdapter(adapterAreas)

            getLines(typePosition)
        }
    }


    private fun spLinesSelected() {
        binding.spSelectCustomerLine.setOnItemClickListener { parent, _, position, _ ->
            linePosition = linesList[position].LINE_CODE
            categoriesList.clear()
            categoryPosition = ""
            binding.spSelectCustomerCategory.setText("")

            val itemsCategories = ArrayList<String>()
            for (i in categoriesList) {
                itemsCategories.add(i.CUSTOMER_NAME)
            }
            val adapterAreas: ArrayAdapter<String> =
                ArrayAdapter<String>(
                    this@CustomerCodingActivity,
                    android.R.layout.simple_spinner_dropdown_item,
                    itemsCategories
                )
            binding.spSelectCustomerCategory.setAdapter(adapterAreas)


            getCategories(typePosition, linePosition)
        }
    }


    private fun spCategoriesSelected() {
        binding.spSelectCustomerCategory.setOnItemClickListener { parent, _, position, _ ->
            categoryPosition = categoriesList[position].CUSTOMER_CODE
        }
    }

    private fun spAreasSelected() {
        binding.spSelectArea.setOnItemClickListener { parent, _, position, _ ->
            areaPosition = areasList[position].ATR_ID
        }
    }


    private fun chooseIdPhoto(code: Int) {
        requestCode = code
        with(this)
            .crop()
            .compress(300)
            .maxResultSize(
                1080,
                1080
            )
            .start()
    }

    private fun uploadImagesId(img: String, name: String): MultipartBody.Part {
        val mSaveBit = File(img)

        val requestBody = mSaveBit.asRequestBody("image/*".toMediaTypeOrNull())

        return MultipartBody.Part.createFormData(name, mSaveBit.name, requestBody)
    }


    private fun observeCustomerCoding() {
        lifecycleScope.launch {
            viewModel.state.collect { it ->
                when (it) {
                    is CustomerCodingState.Idle -> Log.d(
                        Common.KeroDebug,
                        "observeCustomerCoding: Idle"
                    )

                    is CustomerCodingState.Loading -> {
                        Log.d(Common.KeroDebug, "observeCustomerCoding: Loading")
                        showProgressDialog(binding.progressLoading)
                    }

                    is CustomerCodingState.GetTypes -> {
                        if (it.data.status == 200) {
                            hideProgressDialog(binding.progressLoading)
                            Log.d(
                                Common.KeroDebug,
                                "observeRCustomerCoding GetCustomerType" + it.data.message
                            )

                            typesList.addAll(it.data.data!!)

                            val items = ArrayList<String>()
                            for (i in it.data.data!!) {
                                items.add(i.TYPE_CHILD_NAME)
                            }
                            val adapter: ArrayAdapter<String> =
                                ArrayAdapter<String>(
                                    this@CustomerCodingActivity,
                                    android.R.layout.simple_spinner_dropdown_item,
                                    items
                                )

                            binding.spSelectCustomerType.setAdapter(adapter)

                        } else {
                            hideProgressDialog(binding.progressLoading)
                            showToastSnack(it.data.message, true)
                        }
                    }

                    is CustomerCodingState.GetLines -> {
                        if (it.data.status == 200) {
                            hideProgressDialog(binding.progressLoading)
                            Log.d(
                                Common.KeroDebug,
                                "observeCustomerCoding GetCustomerLine" + it.data.message
                            )

                            linesList.addAll(it.data.data!!)

                            val items = ArrayList<String>()
                            for (i in it.data.data!!) {
                                items.add(i.LINE_NAME)
                            }
                            val adapter: ArrayAdapter<String> =
                                ArrayAdapter<String>(
                                    this@CustomerCodingActivity,
                                    android.R.layout.simple_spinner_dropdown_item,
                                    items
                                )

                            binding.spSelectCustomerLine.setAdapter(adapter)

                        } else {
                            hideProgressDialog(binding.progressLoading)
                            showToastSnack(it.data.message, true)
                        }
                    }

                    is CustomerCodingState.GetCategories -> {
                        if (it.data.status == 200) {
                            hideProgressDialog(binding.progressLoading)
                            Log.d(
                                Common.KeroDebug,
                                "observeCustomerCoding GetCategories" + it.data.message
                            )

                            categoriesList.addAll(it.data.data!!)

                            val items = ArrayList<String>()
                            for (i in it.data.data!!) {
                                items.add(i.CUSTOMER_NAME)
                            }
                            val adapter: ArrayAdapter<String> =
                                ArrayAdapter<String>(
                                    this@CustomerCodingActivity,
                                    android.R.layout.simple_spinner_dropdown_item,
                                    items
                                )

                            binding.spSelectCustomerCategory.setAdapter(adapter)
                        } else {
                            hideProgressDialog(binding.progressLoading)
                            showToastSnack(it.data.message, false)
                        }
                    }

                    is CustomerCodingState.GetAreas -> {
                        if (it.data.status == 200) {
                            hideProgressDialog(binding.progressLoading)
                            Log.d(
                                Common.KeroDebug,
                                "observeCustomerCoding GetAreas" + it.data.message
                            )

                            areasList.addAll(it.data.data!!)

                            val items = ArrayList<String>()
                            for (i in it.data.data!!) {
                                items.add(i.DEFINTION)
                            }
                            val adapter: ArrayAdapter<String> =
                                ArrayAdapter<String>(
                                    this@CustomerCodingActivity,
                                    android.R.layout.simple_spinner_dropdown_item,
                                    items
                                )

                            binding.spSelectArea.setAdapter(adapter)
                        } else {
                            hideProgressDialog(binding.progressLoading)
                            showToastSnack(it.data.message, false)
                        }
                    }

                    is CustomerCodingState.SendCustomer -> {
                        if (it.data.status == 200) {
                            Log.d(
                                Common.KeroDebug,
                                "observeCustomerCoding status: ${it.data!!.message}"
                            )
                            hideProgressDialog(binding.progressLoading)
                            showToastSnack(it.data!!.message, false)

                            finish()
                        } else {
                            Log.d(
                                Common.KeroDebug,
                                "observeCustomerCoding Send data: ${it.data.message}"
                            )
                            hideProgressDialog(binding.progressLoading)
                        }
                    }

                    is CustomerCodingState.Error -> {
                        Log.d(
                            Common.KeroDebug,
                            "observeCustomerCoding Error: ${it.error.toString()}"
                        )

                        hideProgressDialog(binding.progressLoading)
                        showToastSnack(it.error.toString(), true)
                    }

                }
            }
        }
    }


    private fun postRegisterAsMember() {
        var isError = true

        val apiToken = SharedPreferencesHelper.getInstance().getUserToken()
        val userId = SharedPreferencesHelper.getInstance().getEmployeeId()

        val type_id = typePosition
        val line_id = linePosition
        val category_id = categoryPosition
        val area_id = areaPosition
        val name = binding.layoutWriteCustomerName.editText!!.text.toString().convertArabicToEnglishNumbers()
        val address = binding.layoutWriteCustomerAddress.editText!!.text.toString().convertArabicToEnglishNumbers()
        val phoneNumber = binding.layoutWriteCustomerPhone.editText!!.text.toString().convertArabicToEnglishNumbers()
        val mobileNumber = binding.layoutWriteCustomerMobile.editText!!.text.toString().convertArabicToEnglishNumbers()
        val nationalityId = binding.layoutWriteNationalityId.editText!!.text.toString().convertArabicToEnglishNumbers()
        val nationalityName = binding.layoutWriteNationalityName.editText!!.text.toString().convertArabicToEnglishNumbers()
        val nationalityAddress = binding.layoutWriteNationalityAddress.editText!!.text.toString().convertArabicToEnglishNumbers()
        val front_id_image = uploadImagesId(imageFrontId, "id_1")
        val back_id_image = uploadImagesId(imageBackId, "id_2")
        val latitude = mCurrentLocation?.latitude!!.toString()
        val longitude = mCurrentLocation?.longitude!!.toString()

        Log.d(Common.KeroDebug, "postRegisterAsMember: ${imageFrontId} || ${imageBackId}")

        val app_version = versionName.toRequestBody("text/plain".toMediaTypeOrNull())
        val api_token = apiToken.toRequestBody("text/plain".toMediaTypeOrNull())
        val user_id = userId.toRequestBody("text/plain".toMediaTypeOrNull())
        val _type_id = type_id.toRequestBody("text/plain".toMediaTypeOrNull())
        val _line_id = line_id.toRequestBody("text/plain".toMediaTypeOrNull())
        val _category_id = category_id.toRequestBody("text/plain".toMediaTypeOrNull())
        val _area_id = area_id.toRequestBody("text/plain".toMediaTypeOrNull())
        val _name = name.toRequestBody("text/plain".toMediaTypeOrNull())
        val _address = address.toRequestBody("text/plain".toMediaTypeOrNull())
        val _phoneNumber = phoneNumber.toRequestBody("text/plain".toMediaTypeOrNull())
        val _mobileNumber = mobileNumber.toRequestBody("text/plain".toMediaTypeOrNull())
        val _nationalityId = nationalityId.toRequestBody("text/plain".toMediaTypeOrNull())
        val _nationalityName = nationalityName.toRequestBody("text/plain".toMediaTypeOrNull())
        val _nationalityAddress = nationalityAddress.toRequestBody("text/plain".toMediaTypeOrNull())
        val _latitude = latitude.toRequestBody("text/plain".toMediaTypeOrNull())
        val _longitude = longitude.toRequestBody("text/plain".toMediaTypeOrNull())

        if (type_id == "") {
            showToastSnack("برجاء اختيار نوع عميل", true)
            isError = false
        }
        if (line_id == "") {
            showToastSnack("برجاء اختيار خط العميل", true)
            isError = false
        }
        if (category_id == "") {
            showToastSnack("برجاء اختيار فئة العميل", true)
            isError = false
        }
        if (area_id == "") {
            showToastSnack("برجاء ادخال المنطقة", true)
            isError = false
        }
        if (name.isEmpty()) {
            showToastSnack("برجاء ادخال اسم العميل", true)
            isError = false
        }
        if (address.isEmpty()) {
            showToastSnack("برجاء ادخال عنوان العميل", true)
            isError = false
        }
        if (phoneNumber.isEmpty()) {
            showToastSnack("برجاء ادخال رقم التليفون المحمول", true)
            isError = false
        }
        if (nationalityId.isEmpty() && category_id != "477362") {
            showToastSnack("برجاء ادخال رقم البطاقة", true)
            isError = false
        }
        if (nationalityName.isEmpty() && category_id != "477362") {
            showToastSnack("برجاء ادخال الاسم فى البطاقة", true)
            isError = false
        }
        if (nationalityAddress.isEmpty() && category_id != "477362") {
            showToastSnack("برجاء ادخال العنوان فى البطاقة", true)
            isError = false
        }
        if ((imageFrontId.isEmpty() || imageFrontId == "") && category_id != "477362") {
            showToastSnack("برجاء ادخال وش صورة البطاقة", true)
            isError = false
        }
        if ((imageBackId.isEmpty() || imageBackId == "") && category_id != "477362") {
            showToastSnack("برجاء ادخال ظهر صورة البطاقة", true)
            isError = false
        }
        if (isLocationNotMissing()) {
            showToastSnack("هناك خطأ فى الموقع", true)
            isError = false
        }
        if (isError) {
            lifecycleScope.launch {
                viewModel.customerCodingIntent.send(
                    CustomerCodingIntent.SendCustomer(
                        app_version = app_version,
                        api_token = api_token,
                        user_id = user_id,
                        cust_type = _type_id,
                        line_id = _line_id,
                        cust_code_id = _category_id,
                        area = _area_id,
                        customer_name = _name,
                        customer_address = _address,
                        phoneNumber = _phoneNumber,
                        mobileNumber = _mobileNumber,
                        customer_national_id = _nationalityId,
                        name_in_national_id = _nationalityName,
                        address_in_national_id = _nationalityAddress,
                        id_1 = front_id_image,
                        id_2 = back_id_image,
                        long = _longitude,
                        lat = _latitude,
                    )
                )
            }
        }
    }

    private fun getCustomerType() {
        lifecycleScope.launch {
            viewModel.customerCodingIntent.send(
                CustomerCodingIntent.GetTypes(
                    versionName,
                    SharedPreferencesHelper.getInstance().getUserToken(),
                    SharedPreferencesHelper.getInstance().getEmployeeId(),
                )
            )
        }
    }

    private fun getLines(typeId: String) {
        lifecycleScope.launch {
            viewModel.customerCodingIntent.send(
                CustomerCodingIntent.GetLines(
                    versionName,
                    SharedPreferencesHelper.getInstance().getUserToken(),
                    SharedPreferencesHelper.getInstance().getEmployeeId(),
                    typeId,
                )
            )
        }
    }

    private fun getCategories(typeId: String, lineId: String) {
        lifecycleScope.launch {
            viewModel.customerCodingIntent.send(
                CustomerCodingIntent.GetCategories(
                    versionName,
                    SharedPreferencesHelper.getInstance().getUserToken(),
                    SharedPreferencesHelper.getInstance().getEmployeeId(),
                    typeId,
                    lineId
                )
            )
        }
    }

    private fun getAreas() {
        lifecycleScope.launch {
            viewModel.customerCodingIntent.send(
                CustomerCodingIntent.GetAreas(
                    versionName,
                    SharedPreferencesHelper.getInstance().getUserToken(),
                    SharedPreferencesHelper.getInstance().getEmployeeId(),
                )
            )
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_CANCELED) {
            return
        }

        val contentURI = data?.let { it.data }


        if (this.requestCode == 1 && resultCode == RESULT_OK) {
            if (data != null && contentURI != null) {
                imageFrontId = contentURI.path!!
                binding.imFrontIdImage.setImageURI(contentURI)
            }
        } else if (this.requestCode == 2 && resultCode == RESULT_OK) {
            if (data != null && contentURI != null) {
                imageBackId = contentURI.path!!
                binding.imBackIdImage.setImageURI(contentURI)

            }
        } else {
            showToastSnack("something went wrong, Add Image Again", true)
        }
    }

    private fun validateEgyptianNationalID(nationalID: String): Boolean {
        if (nationalID.length != 14 || !nationalID.all { it.isDigit() }) {
            return false
        }

        val century = when (nationalID[0]) {
            '2' -> "19" // 1900-1999
            '3' -> "20" // 2000-2099
            else -> return false // Invalid century digit
        }

        val birthdateStr = century + nationalID.substring(1, 7)

        val sdf = SimpleDateFormat("yyyyMMdd", Locale.ENGLISH)
        sdf.isLenient = false
        try {
            val birthdate = sdf.parse(birthdateStr)
            // Additional check: ensure birthdate is not in the future
            if (birthdate.after(Date())) return false
        } catch (e: Exception) {
            return false
        }

        val governorateCode = nationalID.substring(7, 9).toInt()
        if (governorateCode !in 1..29 && governorateCode != 88) {
            return false
        }

        val genderDigit = nationalID[12]
        return genderDigit.isDigit()
    }

    private fun isLocationNotMissing(): Boolean {
        return mCurrentLocation?.longitude.toString() == "" || mCurrentLocation?.latitude.toString() == ""
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun updateLocation(locationData: LocationData) {
        Log.i(Common.KeroDebug, "onUpdateLocationMain: " + locationData.latitude + " " + locationData.longitude)
        val location = Location(LocationManager.GPS_PROVIDER)
        location.latitude = locationData.latitude
        location.longitude = locationData.longitude

        binding.fieldLongitude.text = location.latitude.toString()
        binding.fieldLatitude.text = location.longitude.toString()
        mCurrentLocation = location
    }

    private fun askPermission() {

        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (!isGranted) {
                // PERMISSION NOT GRANTED
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                    ),
                    REQUEST_CODE
                )
                ProgressDialogHelper().errorMessage(
                    this,
                    "This app needs you to allow Location permission To Use Attendance " +
                            "you Should allow it"
                )
            } else {
                try {
                    EventBus.getDefault().register(this)
                } catch (e: Exception) {
                    Log.d(Common.KeroDebug, "askPermissionError ${e.message.toString()}")
                }
            }

        }
        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)

        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
            ),
            REQUEST_CODE
        )
    }

    fun String.convertArabicToEnglishNumbers(): String {
        val mapping = mapOf(
            '٠' to '0',
            '١' to '1',
            '٢' to '2',
            '٣' to '3',
            '٤' to '4',
            '٥' to '5',
            '٦' to '6',
            '٧' to '7',
            '٨' to '8',
            '٩' to '9'
        )

        return this.replace(Regex("[٠-٩]")) { result ->
            (mapping[result.value[0]] ?: result.value[0]).toString()
        }
    }
}
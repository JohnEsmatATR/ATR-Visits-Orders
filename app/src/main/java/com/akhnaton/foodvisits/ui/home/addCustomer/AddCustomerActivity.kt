package com.akhnaton.foodvisits.ui.home.addCustomer

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.akhnaton.foodvisits.BuildConfig
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.data.model.CustomerType
import com.akhnaton.foodvisits.data.model.visits.LinesUsers
import com.akhnaton.foodvisits.data.statusValue.addCustomer.AddCustomerIntent
import com.akhnaton.foodvisits.data.statusValue.customerCoding.GetGovernoratesIntent
import com.akhnaton.foodvisits.data.statusValue.customerCoding.GetGovernoratesState
import com.akhnaton.foodvisits.data.statusValue.phoneVisits.PhoneVisitsIntent
import com.akhnaton.foodvisits.data.statusValue.phoneVisits.PhoneVisitsStatus
import com.akhnaton.foodvisits.databinding.ActivityAddEmployeeBinding
import com.akhnaton.foodvisits.shared.BaseActivity
import com.akhnaton.foodvisits.shared.SharedPreferencesHelper
import com.akhnaton.foodvisits.shared.SpinnerHelper
import com.akhnaton.foodvisits.ui.home.MainActivity
import com.akhnaton.foodvisits.ui.home.phoneVisit.PhoneVisitsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.util.Locale

class AddCustomerActivity : BaseActivity(), LocationListener, View.OnClickListener {

    companion object {
        private const val TAG = "AddCustomerActivity"
    }


    private lateinit var binding: ActivityAddEmployeeBinding
    private val viewModel: AddCustomerViewModel by viewModels()
    private val getGovernoratesViewModel: GetGovernoratesViewModel by viewModels()
    private val phoneVisitsViewModel: PhoneVisitsViewModel by viewModels()

    private val versionName = BuildConfig.VERSION_NAME
    private lateinit var locationManager: LocationManager
    private val locationPermissionCode = 2
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    private val customerType: MutableList<String> = ArrayList()
    private val lineType: MutableList<String> = ArrayList()
    private var linesList: List<LinesUsers> = ArrayList()

    private var customerTypeList: List<CustomerType> = ArrayList()
    private var customerTypePosition: String = ""
    private var governorate: String = ""
    private var city: String = ""
    private var lineIdPosition: String = ""
    private var mainCustomerLinePosition: String = ""
    private var orderType: String = ""

    private lateinit var frontImageFile: File
    private lateinit var backImageFile: File
    private val REQUEST_FRONT_IMAGE = 1001
    private val REQUEST_BACK_IMAGE = 1002


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_employee)


        binding.imFrontIdImage.setOnClickListener {
            openCamera(REQUEST_FRONT_IMAGE)
        }

        binding.imBackIdImage.setOnClickListener {
            openCamera(REQUEST_BACK_IMAGE)
        }





        binding.customerType.setOnItemClickListener { adapter: AdapterView<*>?, view: View?, position: Int, p3: Long ->
            binding.linesLayout.visibility = View.VISIBLE
            customerTypePosition = customerTypeList[position].customer_type_id

        }
        observeGovernoratesState()
        observeOrderType()
        lifecycleScope.launch {
            phoneVisitsViewModel.phoneVisitsIntent.send(
                PhoneVisitsIntent.GetPlan(
                    versionName,
                    SharedPreferencesHelper.getInstance().getUserToken()
                )
            )
        }

        lifecycleScope.launch {
            getGovernoratesViewModel.governoratesIntent.send(
                GetGovernoratesIntent.GetGovernorate(
                    version = versionName,
                    token = SharedPreferencesHelper.getInstance().getUserToken(),

                    )
            )
        }
        binding.lines.setOnItemClickListener { adapter: AdapterView<*>?, view: View?, position: Int, p3: Long ->
            binding.linesLayout.visibility = View.VISIBLE
            lineIdPosition = linesList[position].line_id


            lifecycleScope.launch {
                viewModel.customerIntent.send(
                    AddCustomerIntent.GetMainLines(
                        versionName,
                        SharedPreferencesHelper.getInstance().getUserToken(),
                        customerTypePosition,
                        orderType,
                        lineIdPosition
                    )
                )
            }
        }

//        binding.mainLine.setOnItemClickListener { adapter: AdapterView<*>?, view: View?, position: Int, p3: Long ->
//            binding.linesLayout.visibility = View.VISIBLE
//            mainCustomerLinePosition = mainList[position].customer_code
//        }

        binding.addCustomerBtn.setOnClickListener(this)
        binding.backBtn.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        getLocation()
        getDataFromViewModel()
        fetchData()
    }

    private fun observeOrderType() {
        lifecycleScope.launch {
            phoneVisitsViewModel.status.collect {
                when (it) {
                    is PhoneVisitsStatus.Error -> {

                    }

                    is PhoneVisitsStatus.GetAppSetting -> {

                    }

                    is PhoneVisitsStatus.GetCustomerLines -> {

                    }

                    is PhoneVisitsStatus.GetCustomerType -> {

                    }

                    is PhoneVisitsStatus.GetCustomersSite -> {

                    }

                    is PhoneVisitsStatus.GetLines -> {

                    }

                    PhoneVisitsStatus.Idle -> {

                    }

                    PhoneVisitsStatus.Loading -> {
                        showProgressDialog(binding.progressLoading)
                    }

                    is PhoneVisitsStatus.Plan -> {
                        hideProgressDialog(binding.progressLoading)
                        val orderTypes = it.data.data.user_order_type

                        val adapter = ArrayAdapter(
                            this@AddCustomerActivity,
                            android.R.layout.simple_dropdown_item_1line,
                            orderTypes
                        )

                        binding.orderType.setAdapter(adapter)

                        binding.orderType.setOnItemClickListener { parent, view, position, id ->
                            val selectedType = orderTypes[position]
                            binding.orderType.setText(selectedType, false)
                            orderType = selectedType

                            lifecycleScope.launch {
                                viewModel.customerIntent.send(
                                    AddCustomerIntent.GetLines(
                                        versionName,
                                        SharedPreferencesHelper.getInstance().getUserToken(),
                                        customerTypePosition,
                                        orderType
                                    )
                                )
                            }
                        }


                    }

                    is PhoneVisitsStatus.SavePhoneVisits -> TODO()
                }
            }
        }

    }

    private fun validateInputs(): Boolean {
        val name = binding.customerName.text.toString().trim()
        val address = binding.customerAddress.text.toString().trim()
        val nationalId = binding.customerNational.text.toString().trim()
        val phone = binding.spWriteCustomerPhone.text.toString().trim()
        val secondPhone = binding.spWriteCustomerMobile.text.toString().trim()

        return when {
            customerTypePosition.isEmpty() -> {
                Toast.makeText(this, "اختر نوع العميل", Toast.LENGTH_SHORT).show()
                false
            }

            orderType.isEmpty() -> {
                Toast.makeText(this, "اختر نوع الطلب", Toast.LENGTH_SHORT).show()
                false
            }

            lineIdPosition.isEmpty() -> {
                Toast.makeText(this, "اختر خط العميل", Toast.LENGTH_SHORT).show()
                false
            }

            governorate.isEmpty() -> {
                Toast.makeText(this, "اختر المحافظة", Toast.LENGTH_SHORT).show()
                false
            }

            city.isEmpty() -> {
                Toast.makeText(this, "اختر المدينة", Toast.LENGTH_SHORT).show()
                false
            }

            name.isEmpty() -> {
                Toast.makeText(this, "ادخل اسم العميل", Toast.LENGTH_SHORT).show()
                false
            }

            address.isEmpty() -> {
                Toast.makeText(this, "ادخل عنوان العميل", Toast.LENGTH_SHORT).show()
                false
            }

            phone.isEmpty() -> {
                Toast.makeText(this, "ادخل رقم تليفون العميل", Toast.LENGTH_SHORT).show()
                false
            }

            phone.length != 11 -> {
                Toast.makeText(this, "رقم التليفون يجب أن يكون 11 رقم", Toast.LENGTH_SHORT).show()
                false
            }



            nationalId.isEmpty() -> {
                Toast.makeText(this, "ادخل الرقم القومي", Toast.LENGTH_SHORT).show()
                false
            }

            nationalId.length != 14 -> {
                Toast.makeText(this, "الرقم القومي يجب أن يكون 14 رقم", Toast.LENGTH_SHORT).show()
                false
            }

            latitude == null || longitude == null -> {
                Toast.makeText(this, "لم يتم تحديد الموقع", Toast.LENGTH_SHORT).show()
                false
            }

            !::frontImageFile.isInitialized -> {
                Toast.makeText(this, "قم بتصوير البطاقة الأمامية", Toast.LENGTH_SHORT).show()
                false
            }

            !::backImageFile.isInitialized -> {
                Toast.makeText(this, "قم بتصوير البطاقة الخلفية", Toast.LENGTH_SHORT).show()
                false
            }

            else -> true
        }

    }

    private fun openCamera(requestCode: Int) {
        try {

            val imageFile = File.createTempFile("photo_${System.currentTimeMillis()}", ".jpg", cacheDir)

            when (requestCode) {
                REQUEST_FRONT_IMAGE -> frontImageFile = imageFile
                REQUEST_BACK_IMAGE -> backImageFile = imageFile
            }

            val uri = FileProvider.getUriForFile(
                this,
                "${packageName}.provider", // لازم يطابق الـ authority في manifest
                imageFile
            )

            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                putExtra(MediaStore.EXTRA_OUTPUT, uri)
                addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            startActivityForResult(intent, requestCode)

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Camera error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_FRONT_IMAGE -> {
                    val bitmap = BitmapFactory.decodeFile(frontImageFile.absolutePath)
                    binding.imFrontIdImage.setImageBitmap(bitmap)
                }

                REQUEST_BACK_IMAGE -> {
                    val bitmap = BitmapFactory.decodeFile(backImageFile.absolutePath)
                    binding.imBackIdImage.setImageBitmap(bitmap)
                }
            }
        }
    }



    private fun getDataFromViewModel() {
        lifecycleScope.launch {
            viewModel.customerIntent.send(
                AddCustomerIntent.GetCustomerType(
                    versionName,
                    SharedPreferencesHelper.getInstance().getUserToken()
                )
            )
        }
    }

    private fun observeGovernoratesState() {
        lifecycleScope.launch {
            getGovernoratesViewModel.state.collect { state ->
                when (state) {
                    is GetGovernoratesState.Idle -> {

                    }

                    is GetGovernoratesState.Loading -> {
                        showProgressDialog(binding.progressLoading)
                    }

                    is GetGovernoratesState.GovernoratesSuccess -> {
                        hideProgressDialog(binding.progressLoading)
                        val governorates = state.data.data.governorateS
                        val adapter = GovernorateAdapter(this@AddCustomerActivity, governorates)
                        binding.spSelectGovernorate.setAdapter(adapter)

                        binding.spSelectGovernorate.setOnItemClickListener { _, _, position, _ ->
                            val selectedGovernorate = adapter.getItem(position)
                            governorate = selectedGovernorate?.id.toString()
                            binding.spSelectGovernorate.setText(selectedGovernorate?.name_ar ?: "")

                            Log.d("Governorate", "Selected: ${selectedGovernorate?.name_ar}")

                            selectedGovernorate?.let {
                                lifecycleScope.launch {
                                    getGovernoratesViewModel.governoratesIntent.send(
                                        GetGovernoratesIntent.GetCity(
                                            version = versionName,
                                            token = SharedPreferencesHelper.getInstance()
                                                .getUserToken(),
                                            governorateId = it.id
                                        )
                                    )
                                }
                            }
                        }
                    }


                    is GetGovernoratesState.AreasSuccess -> {
                        hideProgressDialog(binding.progressLoading)
                        val areas = state.data.data.areas
                        val adapter = AreaAdapter(this@AddCustomerActivity, areas)
                        binding.spSelectArea.setAdapter(adapter)

                        binding.spSelectArea.setOnItemClickListener { _, _, position, _ ->
                            val selectedArea = adapter.getItem(position)
                            city = selectedArea?.id.toString()
                            binding.spSelectArea.setText(selectedArea?.name_ar ?: "")
                            Log.d("Area", "Selected: ${selectedArea?.name_ar}")
                        }
                    }

                    is GetGovernoratesState.Error -> {
                        showToastSnack(state.message.toString(), true)
                        hideProgressDialog(binding.progressLoading)
                    }
                }
            }
        }

    }


    private fun fetchData() {
        lifecycleScope.launch {
            viewModel.state.collect {
                when (it) {
                    AddCustomerStatus.Idle -> Log.d(TAG, "fetchData: Idle ")
                    AddCustomerStatus.Loading -> {
                        Log.d(TAG, "fetchData: Loading")
                        showProgressDialog(binding.progressLoading)
                    }

                    is AddCustomerStatus.GetCustomerType -> {
                        hideProgressDialog(binding.progressLoading)
                        customerType.clear()
                        binding.customerType.text.clear()
                        it.data.data.user_customer_type.forEach { type -> customerType.add(type.customer_name) }
                        SpinnerHelper().setAutoCompleteSpinnerAdapter(
                            binding.customerType,
                            customerType, this@AddCustomerActivity
                        )
                        customerTypeList = it.data.data.user_customer_type
                    }

                    is AddCustomerStatus.GetLines -> {
                        hideProgressDialog(binding.progressLoading)
                        lineType.clear()
                        binding.lines.text.clear()
                        it.data.data.user_lines.forEach { line -> lineType.add(line.line_name) }
                        linesList = it.data.data.user_lines
                        SpinnerHelper().setAutoCompleteSpinnerAdapter(
                            binding.lines,
                            lineType, this@AddCustomerActivity
                        )

                    }

                    //Get Main Customer Lines
                    is AddCustomerStatus.GetMainLine -> {
                        hideProgressDialog(binding.progressLoading)
//                        mainLine.clear()
//                        binding.mainLine.text.clear()
//                        mainList = it.data.data.main_customer_line
//                        it.data.data.main_customer_line.forEach { line -> mainLine.add(line.customer_name) }
//                        SpinnerHelper().setAutoCompleteSpinnerAdapter(
//                            binding.mainLine,
//                            mainLine,
//                            this@AddCustomerActivity
//                        )
                    }

                    is AddCustomerStatus.CreateCustomer -> {
                        hideProgressDialog(binding.progressLoading)
                        if (it.data.status == 200) {
                            showToastSnack("تم تكويد العميل بنجاح", false)
                            delay(500)
                            startActivity(
                                Intent(
                                    this@AddCustomerActivity,
                                    MainActivity::class.java
                                )
                            )
                            Log.d(
                                TAG,
                                "fetchData: ${it.data.status}"
                            )
                            finishAffinity()
                        } else {
                            Log.d(
                                TAG,
                                "fetchData status: ${it.data.status} , massage ${it.data.message}"
                            )
                        }

                    }

                    is AddCustomerStatus.Error -> {
                        hideProgressDialog(binding.progressLoading)
                        Log.d(TAG, "fetchDataError:${it.error.toString()} ")
                        showToastSnack(it.error.toString(), true)
                    }
                }
            }
        }
    }

    fun File.toMultipartBodyPart(paramName: String): MultipartBody.Part {
        val requestFile = this.asRequestBody("image/*".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(paramName, this.name, requestFile)
    }

    fun String.toPlainRequestBody(): RequestBody {
        return this.toRequestBody("text/plain".toMediaTypeOrNull())
    }

    override fun onClick(v: View) {
        if (!validateInputs()) return

        val name = binding.customerName.text.toString().trim()
        val address = binding.customerAddress.text.toString().trim()
        val nationalId = binding.customerNational.text.toString().trim()
        val phone = binding.spWriteCustomerPhone.text.toString().trim()
        val secondPhone = binding.spWriteCustomerMobile.text.toString().trim()
        val suggestAddress = binding.suggestedAddress.text.toString().trim()

        lifecycleScope.launch {
            viewModel.customerIntent.send(
                AddCustomerIntent.CreateCustomer(
                    versionName.toRequestBody("text/plain".toMediaTypeOrNull()),
                    SharedPreferencesHelper.getInstance().getUserToken().toPlainRequestBody(),
                    customerTypePosition.toPlainRequestBody(),
                    orderType.toPlainRequestBody(),
                    lineIdPosition.toPlainRequestBody(),
                    governorate.toPlainRequestBody(),
                    city.toPlainRequestBody(),
                    name.toPlainRequestBody(),
                    phone.toPlainRequestBody(),
                    secondPhone.toPlainRequestBody(),
                    address.toPlainRequestBody(),
                    nationalId.toPlainRequestBody(),
                    latitude.toString().toPlainRequestBody(),
                    longitude.toString().toPlainRequestBody(),
                    suggestAddress.toPlainRequestBody(),
                    frontImageFile.toMultipartBodyPart("id_1"),
                    backImageFile.toMultipartBodyPart("id_2")
                )
            )
        }
    }



    override fun onLocationChanged(location: Location) {
        if (!location.latitude.equals("")) {
            longitude = location.longitude
            latitude = location.latitude
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun getLocation() {

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if ((ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED)
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                locationPermissionCode
            )
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 1f, this)
        val location =
            locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

        if (location != null) {
            latitude = location.latitude
            longitude = location.longitude
            binding.fieldLongitude.text = longitude.toString()
            binding.fieldLatitude.text = latitude.toString()
            Log.d(TAG, "longitude: ${location.longitude} + latitude: ${location.latitude}")
            getAddressFromLatLng(
                this@AddCustomerActivity,
                latitude,
                longitude
            ) { address ->
                Log.d(TAG, "getLocation: ${address}")
                binding.suggestedAddress.setText(address)
            }
        }

    }

    fun getAddressFromLatLng(
        context: Context,
        latitude: Double,
        longitude: Double,
        onResult: (String?) -> Unit
    ) {
        val geocoder = Geocoder(context, Locale("ar"))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            geocoder.getFromLocation(
                latitude,
                longitude,
                1,
                object : Geocoder.GeocodeListener {
                    override fun onGeocode(addresses: MutableList<Address>) {
                        if (addresses.isNotEmpty()) {
                            val address = addresses[0]
                            val fullAddress =
                                (0..address.maxAddressLineIndex).joinToString(", ") { index ->
                                    address.getAddressLine(index)
                                }
                            onResult(fullAddress)
                        } else {
                            onResult(null)
                        }
                    }

                    override fun onError(errorMessage: String?) {
                        onResult(null)
                    }
                }
            )
        } else {
            // للأجهزة الأقل من API 33، لازم تستخدم النسخة القديمة (synchronous)
            try {
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    val fullAddress =
                        (0..address.maxAddressLineIndex).joinToString(", ") { index ->
                            address.getAddressLine(index)
                        }
                    onResult(fullAddress)
                } else {
                    onResult(null)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onResult(null)
            }
        }
    }


}
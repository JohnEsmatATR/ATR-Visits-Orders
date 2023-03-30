package com.akhnaton.foodvisits.ui.home.visits.promoters.promoterCompetitorsActivity

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.CheckBox
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import cn.pedant.SweetAlert.SweetAlertDialog
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.data.statusValue.promoter.PromoterIntent
import com.akhnaton.foodvisits.data.statusValue.promoter.PromoterStatus
import com.akhnaton.foodvisits.databinding.ActivityPromoterCompetitorsBinding
import com.github.dhaval2404.imagepicker.ImagePicker.Companion.with
import com.google.gson.Gson
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.util.*

class PromoterCompetitorsActivity : AppCompatActivity() {

    private val TAG = "PromoterCompetitors"
    private val returnValue = ArrayList<String>()
    lateinit var binding: ActivityPromoterCompetitorsBinding
    private var code: String? = null
    private var party_site:String? = null
    private var token:String? = null
    private var employee_id:String? = null
    private var customer_code: String? = null
    private var pDialog: SweetAlertDialog? = null
    private var userType = "prom"

    var chk1:CheckBox? = null
    var chk2:CheckBox? = null
    var chk3:CheckBox? = null
    var chk4:CheckBox? = null
    var chk5:CheckBox? = null
    var chk6:CheckBox? = null

    var mCheckList: ArrayList<Int>? = null
    var mListCheckeBox: ArrayList<CheckBoxId>? = null
    var viewModel = PromoterCompetitorsViewModel()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_promoter_competitors)

        mCheckList = ArrayList()
        mListCheckeBox = ArrayList()

        chk1 = findViewById<View>(R.id.chk1) as CheckBox
        chk2 = findViewById<View>(R.id.chk2) as CheckBox
        chk3 = findViewById<View>(R.id.chk3) as CheckBox
        chk4 = findViewById<View>(R.id.chk4) as CheckBox
        chk5 = findViewById<View>(R.id.chk5) as CheckBox
        chk6 = findViewById<View>(R.id.chk6) as CheckBox


        getRequiredData()
        sendCompetitorsData()
        openCalendar()
        getCompetitorImage()
    }


    private fun getRequiredData() {
        code = intent.getStringExtra("cust_code")
        party_site = intent.getStringExtra("party_site")
        token = intent.getStringExtra("token")
        employee_id = intent.getStringExtra("employee_id")
        customer_code = intent.getStringExtra("customer_code")
    }

    private fun getCompetitorImage() {
        binding.ivCompetitorImg.setOnClickListener {
            selectPromotersImages()
        }
    }

    private fun sendCompetitorsData() {
        binding.btnSendImages.setOnClickListener { v ->
            checkboxStatus()
            if (binding.etProductName.text.toString().trim().isEmpty() ||
                binding.etProductDiscountRate.text.toString().trim().isEmpty() ||
                binding.etProductPrice.text.toString().trim().isEmpty() ||
                binding.etProductWeight.text.toString().trim().isEmpty() ||
                binding.etPromotionDate.text.toString().trim().isEmpty()
            ) {
                Toast.makeText(
                    this@PromoterCompetitorsActivity,
                    "Please add all inputs!",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                if (returnValue.size == 0) {
                    Toast.makeText(
                        this@PromoterCompetitorsActivity,
                        "Please add image!",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    if (mCheckList!!.size == 0) {
                        Toast.makeText(
                            this@PromoterCompetitorsActivity,
                            "Please Add Type!",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        //                        uploadImages(returnValue);
                        imageUpload(returnValue)
                    }
                }
            }
        }
    }


    private fun openCalendar() {
        binding.etPromotionDate.setOnClickListener {
            val c = Calendar.getInstance()
            val mYear = c[Calendar.YEAR]
            val mMonth = c[Calendar.MONTH]
            val mDay = c[Calendar.DAY_OF_MONTH]
            val datePickerDialog = DatePickerDialog(
                this@PromoterCompetitorsActivity,
                { view, year, monthOfYear, dayOfMonth -> binding.etPromotionDate.setText(dayOfMonth.toString() + "-" + (monthOfYear + 1) + "-" + year) },
                mYear,
                mMonth,
                mDay
            )
            datePickerDialog.show()
        }
    }

    private fun imageUpload(imagePathList: List<String>) {

        val json: String?
        val datajson = Gson()
        val mcartList: MutableList<String?> = ArrayList()
        json = datajson.toJson(mListCheckeBox)
        mcartList.add(json)


        val apiVersion = 1.0.toString().toRequestBody("multipart/form-data".toMediaTypeOrNull())
        val token = token!!.toRequestBody("multipart/form-data".toMediaTypeOrNull())
        val image: Array<MultipartBody.Part?> = arrayOfNulls<MultipartBody.Part>(imagePathList.size)
        val employee = employee_id!!.toRequestBody("multipart/form-data".toMediaTypeOrNull())
        val date = binding.etPromotionDate.text.toString().trim().toRequestBody("multipart/form-data".toMediaTypeOrNull())
        val partySite = party_site!!.toRequestBody("multipart/form-data".toMediaTypeOrNull())
        val customerCode = customer_code!!.toRequestBody("multipart/form-data".toMediaTypeOrNull())
        val productId = binding.etProductId.text.toString().trim().toRequestBody("multipart/form-data".toMediaTypeOrNull())
        val sPrice = binding.etProductPrice.text.toString().trim().toRequestBody("multipart/form-data".toMediaTypeOrNull())
        val priceAfterDisc = binding.etProductPriceAfterDisc.text.toString().trim().trim().toRequestBody("multipart/form-data".toMediaTypeOrNull())
        val productName = binding.etProductName.text.toString().trim().toRequestBody("multipart/form-data".toMediaTypeOrNull())
        val sWeight = binding.etProductWeight.text.toString().trim().toRequestBody("multipart/form-data".toMediaTypeOrNull())
        val discountRate = binding.etProductDiscountRate.text.toString().trim().trim().toRequestBody("multipart/form-data".toMediaTypeOrNull())
        val promType = json.toRequestBody("multipart/form-data".toMediaTypeOrNull())
        val promDate = binding.etPromotionDate.getText().toString().trim().toRequestBody("multipart/form-data".toMediaTypeOrNull())
        val user_Type = userType.toRequestBody("multipart/form-data".toMediaTypeOrNull())
        val function = "1".toRequestBody("multipart/form-data".toMediaTypeOrNull())


        for (i in imagePathList.indices) {
            val mSaveBit = File(imagePathList[i])
            val requestBody = mSaveBit.asRequestBody("image/jpeg".toMediaTypeOrNull())
            image[i] = MultipartBody.Part.createFormData("image[$i]", mSaveBit.name, requestBody)

        }

        lifecycleScope.launch {
            viewModel.promoterIntent.send(
                PromoterIntent.SendCompetitors(
                    apiVersion,
                    token,
                    image,
                    employee,
                    date,
                    partySite,
                    customerCode,
                    productId,
                    sPrice,
                    priceAfterDisc,
                    productName,
                    sWeight,
                    discountRate,
                    promType,
                    promDate,
                    user_Type,
                    function,
                )
            )
        }

        lifecycleScope.launch {
            viewModel!!.status.collect {
                when (it) {
                    is PromoterStatus.Idle -> Log.d(TAG, "fetchData: Idle")
                    is PromoterStatus.Loading -> {
                        showDialog()
                        Log.d(TAG, "fetchData: Loading")
                    }
                    is PromoterStatus.SendCompetitors -> {
//                        Log.d(TAG, "onResponse (Success): " + it.response.data!![0].message.toString())
                        pDialog!!.dismiss()
                        Toast.makeText(
                            this@PromoterCompetitorsActivity,
                            "Successfully uploaded images",
                            Toast.LENGTH_LONG
                        )
                            .show()
                        finish()
                    }
                    is PromoterStatus.Error -> {
                        Log.d(TAG, "fetchData: ${it.error}")
                        Toast.makeText(
                            this@PromoterCompetitorsActivity,
                            "Error: ${it.error}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }


    private fun selectPromotersImages() {
        with(this)
            .crop()
            .compress(300)
            .maxResultSize(
                1080,
                1080
            ) //Final image resolution will be less than 1080 x 1080(Optional)
            .start()
    }


    private fun showDialog() {
        pDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
        pDialog!!.progressHelper.barColor = Color.parseColor("#A5DC86");
        pDialog!!.titleText = "Loading";
        pDialog!!.setCancelable(false);
        pDialog!!.show();
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            val uri = data!!.data
            binding.ivCompetitorImg.setImageURI(uri)
            if (uri != null) {
                returnValue.add(uri.path!!)
            }
            Log.d(TAG, "onActivityResult: " + uri!!.path)
        }
    }

    fun checkboxStatus() {
        var result = "نوع الدعايا: "
        if (chk1!!.isChecked) {
            result += "\nجندولة"
            mCheckList!!.add(1)
            mListCheckeBox!!.add(CheckBoxId(1))
        }
        if (chk2!!.isChecked) {
            result += "\nفلور دسبلاي"
            mCheckList!!.add(2)
            mListCheckeBox!!.add(CheckBoxId(2))
        }
        if (chk3!!.isChecked) {
            result += "\nمنطقة العروض"
            mCheckList!!.add(3)
            mListCheckeBox!!.add(CheckBoxId(3))
        }
        if (chk4!!.isChecked) {
            result += "\nمجلة العميل "
            mCheckList!!.add(4)
            mListCheckeBox!!.add(CheckBoxId(4))
        }
        if (chk5!!.isChecked) {
            result += "\nفلاير"
            mCheckList!!.add(5)
            mListCheckeBox!!.add(CheckBoxId(5))
        }
        if (chk6!!.isChecked) {
            result += "\nرف"
            mCheckList!!.add(6)
            mListCheckeBox!!.add(CheckBoxId(6))
        }
    }
}
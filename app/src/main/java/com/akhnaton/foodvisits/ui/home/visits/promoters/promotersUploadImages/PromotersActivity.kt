package com.akhnaton.foodvisits.ui.home.visits.promoters.promotersUploadImages

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import cn.pedant.SweetAlert.SweetAlertDialog
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.data.statusValue.promoter.PromoterIntent
import com.akhnaton.foodvisits.data.statusValue.promoter.PromoterStatus
import com.akhnaton.foodvisits.databinding.ActivityPromotersBinding
import com.github.dhaval2404.imagepicker.ImagePicker.Companion.with
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody.*
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class PromotersActivity : AppCompatActivity() {

    private val TAG = "PromotersActivity"
    private val IMAGES_REQUEST_CODE = 100
    private var pDialog: SweetAlertDialog? = null
    lateinit var binding: ActivityPromotersBinding
    private val returnValue = ArrayList<String>()
    lateinit var code: String
    lateinit var party_site: String
    lateinit var token: String
    lateinit var employee_id: String
    private var customer_code: String? = null
    private var sharedpreferences: SharedPreferences? = null
    private var userType = "prom"
    private var promoterImageRecyclerAdapter: PromoterImageRecyclerAdapter? = null
    var viewModel = PromotersActivityViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_promoters)

        fetchUploadImage()
        getRequiredData()

        Log.d("date", formatCurrentDate())

        binding.tvPromoterTitleAddImages.setOnClickListener { v -> selectPromotersImages() }
        sendImages()

    }

    private fun showDialog() {
        pDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
        pDialog!!.progressHelper.barColor = Color.parseColor("#A5DC86");
        pDialog!!.titleText = "Loading";
        pDialog!!.setCancelable(false);
        pDialog!!.show();
    }

    private fun getRequiredData() {
        code = intent.getStringExtra("cust_code")!!
        party_site = intent.getStringExtra("party_site")!!
        token = intent.getStringExtra("token")!!
        employee_id = intent.getStringExtra("employee_id")!!
        customer_code = intent.getStringExtra("customer_code")
        Log.d(
            "TEEESTING", """cust_code: ${code}party_site : $party_site
 Employee_id: ${employee_id}UserType: $userType
 DATA: ${formatCurrentDate()}"""
        )
        val date = Date()
        date.month
    }

    private fun uploadImages(imagePathList: List<String>) {

        val appVersion = 1.0.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val apiToken = token.toRequestBody("text/plain".toMediaTypeOrNull())
        val uploadImages: Array<Part?> = arrayOfNulls<Part>(imagePathList.size)
        val employee = employee_id.toRequestBody("text/plain".toMediaTypeOrNull())
        val date = formatCurrentDate().toRequestBody("text/plain".toMediaTypeOrNull())
        val customerCode = customer_code!!.toRequestBody("text/plain".toMediaTypeOrNull()) // صيدليه عمرو طراد
        val partySite = party_site.toRequestBody("text/plain".toMediaTypeOrNull())
        val user_Type = userType.toRequestBody("text/plain".toMediaTypeOrNull())
        val PromoterImage1 = "1".toRequestBody("text/plain".toMediaTypeOrNull())

        for (i in imagePathList.indices) {
            val mSaveBit = File(imagePathList[i])
            val requestBody = mSaveBit.asRequestBody("image/jpeg".toMediaTypeOrNull())
            uploadImages[i] = Part.createFormData("image[$i]", mSaveBit.name, requestBody)

        }

        lifecycleScope.launch {
            viewModel.promoterIntent.send(
                PromoterIntent.UploadImages(
                    appVersion,
                    apiToken,
                    uploadImages,
                    employee,
                    date,
                    customerCode,
                    partySite,
                    user_Type,
                    PromoterImage1
                )
            )
        }

    }

    fun fetchUploadImage() {
        lifecycleScope.launch {
            viewModel.status.collect {
                when (it) {
                    is PromoterStatus.Idle -> Log.d(TAG, "fetchData (Idle): Idle")
                    is PromoterStatus.Loading -> {
                        showDialog()
                        Log.d(TAG, "fetchData (Loading): Loading")
                    }
                    is PromoterStatus.UploadImages -> {
                        Log.d(TAG, "onResponse (Success): " + it.response.toString())
                        pDialog!!.dismiss()
                        Toast.makeText(
                            this@PromotersActivity,
                            "Successfully uploaded images",
                            Toast.LENGTH_LONG
                        )
                            .show()
                        finish()
                    }
                    is PromoterStatus.Error -> {
                        Log.d(TAG, "fetchData (Error): ${it.error}")
                        Toast.makeText(
                            this@PromotersActivity,
                            "Error: ${it.error}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }

    }


    private fun sendImages() {
        binding.btnSendImages.setOnClickListener { v ->
            if (returnValue.size > 0) {

                uploadImages(returnValue)
            } else Toast.makeText(this, "Please add photos to send", Toast.LENGTH_LONG).show()
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

    private fun formatCurrentDate(): String {
        // 14-03-2021 17:31:02
        val c = Calendar.getInstance().time
        // System.out.println("Current time => " + c);
        val df = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.ENGLISH) /*
        String pattern = "dd-MM-yyyy HH:mm:ss";
        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);*/
        return df.format(c)
    }

    private fun initAdapter(imageList: List<String>) {
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.rvPromotersData.setLayoutManager(layoutManager)
        promoterImageRecyclerAdapter = PromoterImageRecyclerAdapter(imageList, this)
        binding.rvPromotersData.setAdapter(promoterImageRecyclerAdapter)
        promoterImageRecyclerAdapter!!.notifyDataSetChanged()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            val uri = data!!.data
            if (uri != null) {
                returnValue.add(uri.path!!)
            }
//            Log.d(TAG, "onActivityResult: " + uri!!.path)
        }
        initAdapter(returnValue)
    }


}
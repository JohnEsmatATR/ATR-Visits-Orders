package com.akhnaton.foodvisits.ui.home.visits.promoters.promoterDayDetails

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import cn.pedant.SweetAlert.SweetAlertDialog
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.data.statusValue.promoter.PromoterIntent
import com.akhnaton.foodvisits.data.statusValue.promoter.PromoterStatus
import com.akhnaton.foodvisits.databinding.ActivityPromoterDayDetailsBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class PromoterDayDetailsActivity : AppCompatActivity() {

    private val TAG = "PromDayDetailsActivity"
    private var code: String? = null
    private var party_site: String? = null
    private var token: String? = null
    private var employee_id: String? = null
    private var customer_code: String? = null
    private var pDialog: SweetAlertDialog? = null
    private var sharedpreferences: SharedPreferences? = null
    private var userType = "prom"
    var viewModel = DayDetailsViewModel()
    lateinit var binding: ActivityPromoterDayDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_promoter_day_details)


        getRequiredData()
        sendStockStatus()
        fetchsendDetails()
    }


    private fun getRequiredData() {
        code = intent.getStringExtra("cust_code")
        party_site = intent.getStringExtra("party_site")
        token = intent.getStringExtra("token")
        employee_id = intent.getStringExtra("employee_id")
        customer_code = intent.getStringExtra("customer_code")
    }

    private fun sendStockStatus() {
        binding.btnSendImages.setOnClickListener { v ->
            if (binding.etCustomerCalls.text.toString().trim().isEmpty() ||
                binding.etCustomerPositiveCalls.text.toString().trim().isEmpty()
            ) {
                Toast.makeText(
                    this@PromoterDayDetailsActivity,
                    "Please add all inputs!",
                    Toast.LENGTH_LONG
                ).show()
            } else if (binding.etCustomerCalls.text.toString().trim().toInt() <
                binding.etCustomerPositiveCalls.text.toString().trim().toInt()
            ) {
                Toast.makeText(
                    this@PromoterDayDetailsActivity,
                    "لا يمكن ان يكون عدد المكالمات الاجابية اكبر من عدد المكالمات اليومية",
                    Toast.LENGTH_LONG
                ).show()
            } else sendDetails()
        }
    }

    fun fetchsendDetails() {
        lifecycleScope.launch {
            viewModel.status.collect {
                when (it) {
                    is PromoterStatus.Idle -> Log.d(TAG, "fetchData: (Idle) Idle")
                    is PromoterStatus.Loading -> {
                        showDialog()
                        Log.d(TAG, "fetchData:(Loading) Loading")
                    }

                    is PromoterStatus.SendDetails -> {
                        Log.d(
                            TAG,
                            "onResponse: (success)" + it.response.data!![0].message.toString()
                        )
                        val messageSuccess: String = it.response.data!![0].message.toString()
                        Log.d("Message", messageSuccess)
                        val Statusm: Int = it.response.data!![0].status!!.toInt()
                        if (Statusm > 0) {
                            Toast.makeText(baseContext, messageSuccess, Toast.LENGTH_LONG).show()
                            finish()
                        } else {
                            Toast.makeText(baseContext, messageSuccess, Toast.LENGTH_LONG).show()
                        }
                        pDialog!!.dismiss()
                    }

                    is PromoterStatus.Error -> {
                        Log.d(TAG, "fetchData: (error) ${it.error}")
                        Toast.makeText(
                            this@PromoterDayDetailsActivity,
                            "Error: ${it.error}",
                            Toast.LENGTH_LONG
                        ).show()
                        pDialog!!.dismiss()
                    }
                }
            }
        }

    }

    private fun sendDetails() {
        lifecycleScope.launch {
            viewModel.promoterIntent.send(
                PromoterIntent.SendDetails(
                    1.0,
                    token!!,
                    employee_id!!.toInt(),
                    formatCurrentDate(),
                    party_site!!.toInt(),
                    customer_code!!.toInt(),
                    1,
                    binding.etCustomerCalls.text.toString().toInt(),
                    binding.etCustomerPositiveCalls.text.toString().toInt(),
                    1,
                    userType,
                    1,
                )
            )
        }
    }


    fun formatCurrentDate(): String {
        // 14-03-2021 17:31:02
        val pattern = "dd-MM-yyyy HH:mm:ss"
        @SuppressLint("SimpleDateFormat") val simpleDateFormat = SimpleDateFormat(pattern)
        return simpleDateFormat.format(Date())
    }

    private fun showDialog() {
        pDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
        pDialog!!.progressHelper.barColor = Color.parseColor("#A5DC86");
        pDialog!!.titleText = "Loading";
        pDialog!!.setCancelable(false);
        pDialog!!.show();
    }

}
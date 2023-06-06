package com.akhnaton.foodvisits.ui.home.visits.paymentType

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.akhnaton.foodvisits.BuildConfig
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.data.model.payment.PaymentTermCustomer
import com.akhnaton.foodvisits.data.model.payment.ordersourceId
import com.akhnaton.foodvisits.data.statusValue.payment.PaymentIntent
import com.akhnaton.foodvisits.data.statusValue.payment.PaymentStatus
import com.akhnaton.foodvisits.databinding.ActivityPaymentBinding
import com.akhnaton.foodvisits.shared.SharedPreferencesHelper
import com.akhnaton.foodvisits.shared.SpinnerHelper
import com.akhnaton.foodvisits.ui.home.visits.order.OrderActivity
import kotlinx.coroutines.launch

class PaymentActivity : AppCompatActivity(), View.OnClickListener {

    companion object {
        private const val TAG = "PaymentActivity"
    }

    private val versionName = BuildConfig.VERSION_NAME
    private lateinit var binding: ActivityPaymentBinding
    private val viewModel: PaymentViewModel by viewModels()
    private var mPaymentNameList: MutableList<String> = ArrayList()
    private var mOrderSourceNameList: MutableList<String> = ArrayList()
    private var mPaymentList: List<PaymentTermCustomer> = ArrayList()
    private var mOrderSourceList: List<ordersourceId> = ArrayList()
    private var mPaymentTypePosition: String = ""
    private var mOrderSourcePosition: String = ""
    private var mOrderSourceFlag: Int = -1
    private var customerPartySiteId = ""
    private var orderType = ""
    private var customerTypePosition = ""
    private var customerCode = ""
    private var customerName = ""
    private var visitId = ""
    private lateinit var loadingDialog: AlertDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_payment)

        initLoadingDialog()

        customerPartySiteId = intent.getStringExtra("customerPartySiteId").toString()
        orderType = intent.getStringExtra("orderType").toString()
        customerTypePosition = intent.getStringExtra("customerTypePosition").toString()
        customerCode = intent.getStringExtra("customer_code").toString()
        visitId = intent.getStringExtra("visitId").toString()
        customerName = intent.getStringExtra("customer_name").toString()

        lifecycleScope.launch {
            viewModel.paymentIntent.send(
                PaymentIntent.Payments(
                    versionName, SharedPreferencesHelper.getInstance().getUserToken(),
                    customerPartySiteId, orderType, customerTypePosition
                )
            )
            showLoadingDialog()
        }

        binding.paymentType.setOnItemClickListener { adapter: AdapterView<*>?, view: View?, position: Int, p3: Long ->
            mPaymentTypePosition = mPaymentList[position].payment_term_id.toString()
        }

        binding.spOrdersource.setOnItemClickListener { adapter: AdapterView<*>?, view: View?, position: Int, p3: Long ->
            mOrderSourcePosition = mOrderSourceList[position].id.toString()
            mOrderSourceFlag = mOrderSourceList[position].flag
        }

        binding.backBtn.setOnClickListener { onBackPressed() }
        binding.submit.setOnClickListener(this)

        fetchData()
    }

    private fun fetchData() {
        lifecycleScope.launch {
            viewModel.status.collect {
                when (it) {
                    is PaymentStatus.Idle -> Log.d(TAG, "fetchData: Idle")
                    is PaymentStatus.Loading -> Log.d(TAG, "fetchData: Loading")
                    is PaymentStatus.GetPayments -> {
                        mPaymentList = it.data.data.customer_payments_term
                        mOrderSourceList = it.data.data.ordersource_id

                        it.data.data.customer_payments_term.forEach { data ->
                            mPaymentNameList.add(data.payment_term_description)
                        }
                        it.data.data.ordersource_id.forEach { data ->
                            mOrderSourceNameList.add(data.name)
                        }
                        SpinnerHelper().setAutoCompleteSpinnerAdapter(
                            binding.paymentType,
                            mPaymentNameList,
                            this@PaymentActivity
                        )
                        SpinnerHelper().setAutoCompleteSpinnerAdapter(
                            binding.spOrdersource,
                            mOrderSourceNameList,
                            this@PaymentActivity
                        )
                        Log.d(TAG, "fetchData: $it")
                        dismissdialog()
                    }
                    is PaymentStatus.Error -> {
                        Log.d(TAG, "fetchData: $it")
                        dismissdialog()
                    }
                }
            }
        }
    }


    override fun onClick(p0: View?) {

        if (mPaymentTypePosition != "" && mOrderSourcePosition != "") {
            startActivity(
                Intent(this, OrderActivity::class.java)
                    .putExtra("customerPartySiteId", customerPartySiteId)
                    .putExtra("orderType", orderType)
                    .putExtra("customerTypePosition", customerTypePosition)
                    .putExtra("visitId", visitId)
                    .putExtra("customer_code", customerCode)
                    .putExtra("paymentTypePosition", mPaymentTypePosition)
                    .putExtra("orderSourcePosition", mOrderSourcePosition)
                    .putExtra("orderSourceFlag", mOrderSourceFlag)
                    .putExtra("customer_name", customerName)
            )
        } else {
            Toast.makeText(this, "Some data required not selected", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initLoadingDialog() {
        val builder = AlertDialog.Builder(this)

        builder.setTitle("Loading...")

        val progressBar = ProgressBar(this)
        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        progressBar.layoutParams = lp
        builder.setView(progressBar)

        builder.setCancelable(false)
        loadingDialog = builder.create()
    }

    private fun showLoadingDialog() {
        if (!loadingDialog.isShowing) {
            loadingDialog.show()
        }
    }

    fun dismissdialog() {
        loadingDialog.dismiss()
    }



}
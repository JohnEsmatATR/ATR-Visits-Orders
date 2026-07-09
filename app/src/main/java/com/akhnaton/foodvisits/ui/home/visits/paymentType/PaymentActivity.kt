package com.akhnaton.foodvisits.ui.home.visits.paymentType

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
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
import com.akhnaton.foodvisits.data.model.payment.OrderSourceId
import com.akhnaton.foodvisits.data.model.payment.PriceList
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
    private var mPriceListNameList: MutableList<String> = ArrayList()
    private var mPaymentList: List<PaymentTermCustomer> = ArrayList()
    private var mOrderSourceList: List<OrderSourceId> = ArrayList()
    private var mPriceListList: List<PriceList> = ArrayList()
    private var mPaymentTypePosition: String = ""
    private var mOrderSourcePosition: String = ""
    private var mOrderSourceFlag: Int = -1
    private var mPriceListIdPosition: Int = -1
    private var mPriceListDescriptionPosition: String = ""
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


        if (orderType == "Pharma") {
            binding.tilPriceList.visibility = View.VISIBLE
        } else {
            binding.tilPriceList.visibility = View.GONE
        }


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

        binding.spPriceList.setOnItemClickListener { adapter: AdapterView<*>?, view: View?, position: Int, p3: Long ->
            mPriceListIdPosition = mPriceListList[position].price_list_id
            mPriceListDescriptionPosition = mPriceListList[position].price_list_description
        }

        binding.backBtn.setOnClickListener {  onBackPressedDispatcher.onBackPressed() }
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
                        mPriceListList = it.data.data.price_list

                        it.data.data.customer_payments_term.forEach { data ->
                            mPaymentNameList.add(data.payment_term_description)
                        }
                        SpinnerHelper().setAutoCompleteSpinnerAdapter(
                            binding.paymentType,
                            mPaymentNameList,
                            this@PaymentActivity
                        )
                        it.data.data.ordersource_id.forEach { data ->
                            mOrderSourceNameList.add(data.name)
                        }
                        SpinnerHelper().setAutoCompleteSpinnerAdapter(
                            binding.spOrdersource,
                            mOrderSourceNameList,
                            this@PaymentActivity
                        )
                        it.data.data.price_list.forEach { data ->
                            mPriceListNameList.add(data.price_list_description)
                        }
                        SpinnerHelper().setAutoCompleteSpinnerAdapter(
                            binding.spPriceList,
                            mPriceListNameList,
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
                    .putExtra("priceListIdPosition", mPriceListIdPosition)
                    .putExtra("priceListDescriptionPosition", mPriceListDescriptionPosition)
                    .putExtra("customer_name", customerName)
                    .putExtra("isOrderSaved", false)
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

    override fun attachBaseContext(newBase: Context) {
        val configuration = Configuration(newBase.resources.configuration)
        configuration.fontScale = 1.0f

        val context = newBase.createConfigurationContext(configuration)
        super.attachBaseContext(context)
    }

}
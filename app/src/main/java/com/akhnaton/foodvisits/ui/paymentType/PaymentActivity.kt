package com.akhnaton.foodvisits.ui.paymentType

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.akhnaton.foodvisits.BuildConfig
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.data.model.payment.PaymentTermCustomer
import com.akhnaton.foodvisits.data.statusValue.payment.PaymentIntent
import com.akhnaton.foodvisits.data.statusValue.payment.PaymentStatus
import com.akhnaton.foodvisits.databinding.ActivityPaymentBinding
import com.akhnaton.foodvisits.shared.SharedPreferencesHelper
import com.akhnaton.foodvisits.shared.SpinnerHelper
import com.akhnaton.foodvisits.ui.order.OrderActivity
import kotlinx.coroutines.launch

class PaymentActivity : AppCompatActivity(), View.OnClickListener {

    companion object {
        private const val TAG = "PaymentActivity"
    }

    private val versionName = BuildConfig.VERSION_NAME
    private lateinit var binding: ActivityPaymentBinding
    private val viewModel: PaymentViewModel by viewModels()
    private var mPaymentNameList: MutableList<String> = ArrayList()
    private var mPaymentList: List<PaymentTermCustomer> = ArrayList()
    private var mPaymentTypePosition: String = ""
    private var customerPartySiteId = ""
    private var orderType = ""
    private var customerTypePosition = ""
    private var visitId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_payment)

        customerPartySiteId = intent.getStringExtra("customerPartySiteId").toString()
        orderType = intent.getStringExtra("orderType").toString()
        customerTypePosition = intent.getStringExtra("customerTypePosition").toString()
        visitId = intent.getStringExtra("visitId").toString()

        lifecycleScope.launch {
            viewModel.paymentIntent.send(
                PaymentIntent.Payments(
                    versionName, SharedPreferencesHelper.getInstance().getUserToken(),
                    customerPartySiteId, orderType, customerTypePosition
                )
            )
        }
        binding.paymentType.setOnItemClickListener { adapter: AdapterView<*>?, view: View?, position: Int, p3: Long ->
            mPaymentTypePosition = mPaymentList[position].payment_term_id.toString()
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

                        it.data.data.customer_payments_term.forEach { data ->
                            mPaymentNameList.add(data.payment_term_description)
                        }
                        SpinnerHelper().setAutoCompleteSpinnerAdapter(
                            binding.paymentType,
                            mPaymentNameList,
                            this@PaymentActivity
                        )
                        Log.d(TAG, "fetchData: $it")
                    }
                    is PaymentStatus.Error -> Log.d(TAG, "fetchData: $it")
                }
            }
        }
    }


    override fun onClick(p0: View?) {
        val turnOver: Boolean
        if (binding.turnOver.isChecked) {
            turnOver = true
            startActivity(
                Intent(this, OrderActivity::class.java)
                    .putExtra("turnOver", turnOver)
                    .putExtra("customerPartySiteId", customerPartySiteId)
                    .putExtra("orderType", orderType)
                    .putExtra("customerTypePosition", customerTypePosition)
                    .putExtra("visitId", visitId)
                    .putExtra("paymentTypePosition", mPaymentTypePosition)
            )
        } else {
            turnOver = false
            startActivity(
                Intent(this, OrderActivity::class.java)
                    .putExtra("turnOver", turnOver)
                    .putExtra("customerPartySiteId", customerPartySiteId)
                    .putExtra("orderType", orderType)
                    .putExtra("customerTypePosition", customerTypePosition)
                    .putExtra("visitId", visitId)
                    .putExtra("paymentTypePosition", mPaymentTypePosition)
            )
        }
    }


}
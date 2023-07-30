package com.akhnaton.foodvisits.ui.home.supervisor.creditLimit

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.akhnaton.foodvisits.databinding.ActivityCreditLimitFormBinding
import com.akhnaton.foodvisits.shared.SharedPreferencesHelper
import com.akhnaton.foodvisits.ui.home.MainActivity
import com.akhnaton.foodvisits.data.statusValue.supervisor.creditDetails.CreditLimitIntent
import com.akhnaton.foodvisits.data.statusValue.supervisor.creditDetails.CreditLimitState

class CreditLimitFormActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreditLimitFormBinding
    private val viewModel: CreditLimitViewModel by viewModels()
    private var orderNumber: String = ""
    private var orderType: String = ""
    private var customerId: String = ""
    private var orderTotalPrice: String = ""

    private var customerCurrentOpeningStatus: String = ""
    private var customerPreviousCreditLimit: String = ""
    private var customerNationalId: String = ""
    private var customerSecurityCheques: String = ""

    var customer_code: String = ""
    var customer_name: String = ""
    var customer_description: String = ""
    var customer_branch: String = ""
    var branch: String = ""
    var customer_method_payment: String = ""
    var customer_current_opening_status: String = ""
    var customer_commercial_register: String = ""
    var customer_previous_credit_limit: String = ""
    var customer_quarter_plan: String = ""
    var sell_client_to_date: String = ""
    var investigation_ratio: String = ""
    var customer_required_credit: String = ""
    var customer_number_branches: String = ""
    var customer_guarantee: String = ""
    var customer_current_limit: String = ""
    var customer_withdrwals_current_year: String = ""
    var customer_withdrwals_last_year: String = ""
    var customer_bounces_current_year_forward_transaction: String = ""
    var customer_overrun_money: String = ""
    var customer_insurance: String = ""
    var customer_entered_orders: String = ""
    var customer_booked_orders: String = ""
    var customer_check_remitted_amount: String = ""
    var customer_total_limit: String = ""
    var remaining_credit: String = ""
    var customer_national_id: String = ""
    var customer_security_cheques: String = ""
    private lateinit var loadingDialog: AlertDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreditLimitFormBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        supportActionBar!!.hide()

        orderNumber = intent.getStringExtra("orderNumber")!!
        orderType = intent.getStringExtra("orderType")!!
        customerId = intent.getStringExtra("customer_id")!!
        orderTotalPrice = intent.getStringExtra("order_total_price")!!

        getCreditLimit(
            "1.0",
            SharedPreferencesHelper.getInstance().getUserToken(),
            orderNumber,
            customerId,
            orderTotalPrice
        )

        initLoadingDialog()
        fetchCreditLimit()
        onClick()
    }

    private fun onClick() {
        binding.backBtn.setOnClickListener {
            onBackPressed()

        }

        binding.send.setOnClickListener { v: View? ->

            customer_code = binding.customerCode.text.toString()
            customer_name = binding.customerName.text.toString()
            customer_description = binding.customerDescription.text.toString()
            customer_branch = binding.customerBranch.text.toString()
            branch = binding.branch.text.toString()
            customer_method_payment = binding.customerMethodPayment.text.toString()
            customer_current_opening_status = customerCurrentOpeningStatus
            customer_commercial_register = binding.customerCommercialRegister.text.toString()
            customer_previous_credit_limit = customerPreviousCreditLimit
            customer_quarter_plan = binding.customerQuarterPlan.text.toString()
            sell_client_to_date = binding.sellClientToDate.text.toString()
            investigation_ratio = binding.investigationRatio.text.toString()
            customer_required_credit = binding.customerRequiredCredit.text.toString()
            customer_number_branches = binding.customerNumberBranches.text.toString()
            customer_guarantee = binding.customerGuarantee.text.toString()
            customer_current_limit = binding.customerCurrentLimit.text.toString()
            customer_withdrwals_current_year = binding.customerWithdrwalsCurrentYear.text.toString()
            customer_withdrwals_last_year = binding.customerWithdrwalsLastYear.text.toString()
            customer_bounces_current_year_forward_transaction = binding.customerBouncesCurrentYearForwardTransaction.text.toString()
            customer_overrun_money = binding.customerOverrunMoney.text.toString()
            customer_insurance = binding.customerInsurance.text.toString()
            customer_entered_orders = binding.customerEnteredOrders.text.toString()
            customer_booked_orders = binding.customerBookedOrders.text.toString()
            customer_check_remitted_amount = binding.customerCheckRemittedAmount.text.toString()
            customer_total_limit = binding.customerTotalLimit.text.toString()
            remaining_credit = binding.remainingCredit.text.toString()
            customer_national_id = customerNationalId
            customer_security_cheques = customerSecurityCheques


            startActivity(Intent(this, MainActivity::class.java))
            sendCreditLimit(
                "1.0",
                SharedPreferencesHelper.getInstance().getUserToken(),
                customer_code,
                customer_name,
                customer_description,
                customer_branch,
                branch,
                customer_method_payment,
                customer_current_opening_status,
                customer_commercial_register,
                customer_previous_credit_limit,
                customer_quarter_plan,
                sell_client_to_date,
                investigation_ratio,
                customer_required_credit,
                customer_number_branches,
                customer_guarantee,
                customer_current_limit,
                customer_withdrwals_current_year,
                customer_withdrwals_last_year,
                customer_bounces_current_year_forward_transaction,
                customer_overrun_money,
                customer_insurance,
                customer_entered_orders,
                customer_booked_orders,
                customer_check_remitted_amount,
                customer_total_limit,
                remaining_credit,
                customer_national_id,
                customer_security_cheques,
                orderNumber,
            )
        }
    }

    private fun getCreditLimit(
        app_version: String,
        api_token: String,
        orderNumber: String,
        customer_id: String,
        order_total_price: String
    ) {
        lifecycleScope.launchWhenCreated {
            viewModel.creditLimitIntent.send(
                CreditLimitIntent.GetCreditLimit(app_version, api_token, orderNumber, customer_id, order_total_price)
            )
        }
    }

    private fun sendCreditLimit(
        app_version: String,
        api_token: String,
        customer_code: String,
        customer_name: String,
        customer_description: String,
        customer_branch: String,
        branch: String,
        customer_method_payment: String,
        customer_current_opening_status: String,
        customer_commercial_register: String,
        customer_previous_credit_limit: String,
        customer_quarter_plan: String,
        sell_client_to_date: String,
        investigation_ratio: String,
        customer_required_credit: String,
        customer_number_branches: String,
        customer_guarantee: String,
        customer_current_limit: String,
        customer_withdrwals_current_year: String,
        customer_withdrwals_last_year: String,
        customer_bounces_current_year_forward_transaction: String,
        customer_overrun_money: String,
        customer_insurance: String,
        customer_entered_orders: String,
        customer_booked_orders: String,
        customer_check_remitted_amount: String,
        customer_total_limit: String,
        remaining_credit: String,
        customer_national_id: String,
        customer_security_cheques: String,
        order_number: String,
    ) {
        lifecycleScope.launchWhenCreated {
            viewModel.creditLimitIntent.send(
                CreditLimitIntent.SendCreditLimit(
                    app_version,
                    api_token,
                    customer_code,
                    customer_name,
                    customer_description,
                    customer_branch,
                    branch,
                    customer_method_payment,
                    customer_current_opening_status,
                    customer_commercial_register,
                    customer_previous_credit_limit,
                    customer_quarter_plan,
                    sell_client_to_date,
                    investigation_ratio,
                    customer_required_credit,
                    customer_number_branches,
                    customer_guarantee,
                    customer_current_limit,
                    customer_withdrwals_current_year,
                    customer_withdrwals_last_year,
                    customer_bounces_current_year_forward_transaction,
                    customer_overrun_money,
                    customer_insurance,
                    customer_entered_orders,
                    customer_booked_orders,
                    customer_check_remitted_amount,
                    customer_total_limit,
                    remaining_credit,
                    customer_national_id,
                    customer_security_cheques,
                    order_number,
                )
            )
        }
    }

    private fun fetchCreditLimit() {
        lifecycleScope.launchWhenCreated {
            viewModel.state.collect {
                when (it) {
                    CreditLimitState.Idle -> Log.d(TAG, "Idle: ")
                    CreditLimitState.Loading -> {
                        Log.d(TAG, "loading")
                        showLoadingDialog()
                    }
                    is CreditLimitState.GetCreditLimit -> {
                        dismissdialog()
//                        Log.d(TAG, "onCreditLimit: " + it.creditLimitDetails.data!!.overrun_money)
                        binding.customerName.setText(it.creditLimitDetails.data!!.customer_name)
                        binding.customerBranch.setText(it.creditLimitDetails.data!!.customer_branch)
                        binding.branch.setText(it.creditLimitDetails.data!!.branch)
                        binding.customerDescription.setText(it.creditLimitDetails.data!!.customer_description)
                        binding.customerCode.setText(it.creditLimitDetails.data!!.customer_code)
                        binding.customerCurrentLimit.setText(it.creditLimitDetails.data!!.customer_current_limit)
                        binding.customerCommercialRegister.setText(it.creditLimitDetails.data!!.customer_commercial_register)
                        binding.customerMethodPayment.setText(it.creditLimitDetails.data!!.customer_method_payment)
                        binding.customerWithdrwalsCurrentYear.setText(it.creditLimitDetails.data!!.customer_withdrwals_current_year)
                        binding.customerWithdrwalsLastYear.setText(it.creditLimitDetails.data!!.customer_withdrwals_last_year)
                        binding.customerBouncesCurrentYearForwardTransaction.setText(it.creditLimitDetails.data!!.customer_bounces_current_year_forward_transaction)
                        binding.customerOverrunMoney.setText(it.creditLimitDetails.data!!.customer_overrun_money)
                        binding.customerInsurance.setText(it.creditLimitDetails.data!!.customer_insurance)
                        binding.customerRequiredCredit.setText(it.creditLimitDetails.data!!.customer_required_credit)
                        binding.customerNumberBranches.setText(it.creditLimitDetails.data!!.customer_number_branches)
                        binding.customerGuarantee.setText(it.creditLimitDetails.data!!.customer_guarantee)
                        binding.customerQuarterPlan.setText(it.creditLimitDetails.data!!.customer_quarter_plan)
                        binding.sellClientToDate.setText(it.creditLimitDetails.data!!.sell_client_to_date)
                        binding.investigationRatio.setText(it.creditLimitDetails.data!!.investigation_ratio)
                        binding.remainingCredit.setText(it.creditLimitDetails.data!!.remaining_credit)
                        binding.customerCheckRemittedAmount.setText(it.creditLimitDetails.data!!.customer_check_remitted_amount)
                        binding.customerEnteredOrders.setText(it.creditLimitDetails.data!!.customer_entered_orders)
                        binding.customerBookedOrders.setText(it.creditLimitDetails.data!!.customer_booked_orders)
                        binding.customerTotalLimit.setText(it.creditLimitDetails.data!!.customer_total_limit)
                        customerCurrentOpeningStatus = it.creditLimitDetails.data!!.customer_current_opening_status!!
                        customerPreviousCreditLimit = it.creditLimitDetails.data!!.customer_previous_credit_limit!!
                        customerNationalId = it.creditLimitDetails.data!!.customer_national_id!!
                        customerSecurityCheques = it.creditLimitDetails.data!!.customer_security_cheques!!
                    }

                    is CreditLimitState.SendCreditLimit -> {
                        dismissdialog()
                        Log.d(TAG, "message: " + it.staticResponse.message)
                    }

                    is CreditLimitState.Error -> {
                        dismissdialog()
                        Log.d(TAG, "onError: ${it.error}")
                    }
                }
            }
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


    companion object {
        private const val TAG = "CreditLimitFormActivity"
    }
}
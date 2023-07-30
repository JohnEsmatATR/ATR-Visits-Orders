package com.akhnaton.foodvisits.ui.home.supervisor.superShowOrders

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.akhnaton.foodvisits.data.model.supervisor.showOrder.SuperOrderStatus
import com.akhnaton.foodvisits.data.statusValue.supervisor.showOrders.ShowOrdersIntent
import com.akhnaton.foodvisits.databinding.ActivitySuperShowOrdersBinding
import com.akhnaton.foodvisits.shared.SharedPreferencesHelper
import com.akhnaton.foodvisits.data.statusValue.supervisor.showOrders.ShowOrdersState
import com.akhnaton.foodvisits.ui.home.supervisor.creditLimit.CreditLimitFormActivity
import com.akhnaton.foodvisits.ui.home.supervisor.superOrderDetails.SuperOrderDetailsActivity
import kotlinx.coroutines.launch

class SuperShowOrdersActivity : AppCompatActivity(), ShowOrdersAdapter.OnOrderListener {
    var mBinding: ActivitySuperShowOrdersBinding? = null
    var mAdapter: ShowOrdersAdapter? = null
    private val viewModel: OrdersViewModel by viewModels()
    var orderNumber: String = ""
    var orderType: String = ""
    var quotaFlag: String = ""
    var customerId: String = ""
    var orderTotalPrice: String = ""
    private lateinit var loadingDialog: AlertDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivitySuperShowOrdersBinding.inflate(
            layoutInflater
        )
        setContentView(mBinding!!.root)
//        supportActionBar!!.hide()

        getShowOrders(
            "1.0",
            SharedPreferencesHelper.getInstance().getUserToken(),
            SharedPreferencesHelper.getInstance().getEmployeeId()
        )

        mBinding!!.backBtn.setOnClickListener {
                v: View? -> onBackPressed()
        }

        initLoadingDialog()
        fetchShowOrders()
        setupRecycler()
    }

    private fun getShowOrders(
        app_version: String,
        api_token: String,
        superId: String
    ) {
        lifecycleScope.launch {
            viewModel.showOrdersIntent.send(
                ShowOrdersIntent.GetOrders(app_version, api_token, superId)
            )
        }
    }

    private fun rejectOrder(
        app_version: String,
        api_token: String,
        orderNumber: String
    ) {
        lifecycleScope.launchWhenCreated {
            viewModel.showOrdersIntent.send(
                ShowOrdersIntent.RejectOrder(app_version, api_token, orderNumber)
            )
        }
    }

    private fun checkCreditLimit(
        app_version: String,
        api_token: String,
        orderNumber: String
    ) {
        lifecycleScope.launch {
            viewModel.showOrdersIntent.send(
                ShowOrdersIntent.CheckCreditLimit(app_version, api_token, orderNumber)
            )
        }
    }

    private fun checkQouta(
        app_version: String,
        api_token: String,
        orderNumber: String,
        superId: String,
    ) {
        lifecycleScope.launch {
            viewModel.showOrdersIntent.send(
                ShowOrdersIntent.CheckQouta(app_version, api_token, orderNumber, superId)
            )
        }
    }

    private fun approveOrder(
        app_version: String,
        api_token: String,
        orderNumber: String,
        superId: String,
    ) {
        lifecycleScope.launch {
            viewModel.showOrdersIntent.send(
                ShowOrdersIntent.ApproveOrder(app_version, api_token, orderNumber, superId)
            )
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


    private fun fetchShowOrders() {
        lifecycleScope.launch {
            viewModel.state.collect {
                when (it) {
                    ShowOrdersState.Idle -> Log.d(TAG, "Idle: ")
                    ShowOrdersState.Loading -> {
                        Log.d("dvjnjdnvkdndvd", "loading")
                        showLoadingDialog()
                    }
                    is ShowOrdersState.ShowOrders -> {
                        mAdapter!!.setList(it.superStatus.data)
                        dismissdialog()
                    }
                    is ShowOrdersState.RejectOrder -> {
                        if (it.superStatus.status == 200) {
                            Log.d(
                                "dvjnjdnvkdndvd",
                                "onRejectClickListener: " + it.superStatus.message
                            )

                            getShowOrders(
                                "1.0",
                                SharedPreferencesHelper.getInstance().getUserToken(),
                                SharedPreferencesHelper.getInstance().getEmployeeId()
                            )

                            mAdapter!!.notifyDataSetChanged()
                            dismissdialog()
                            Toast.makeText(
                                this@SuperShowOrdersActivity,
                                it.superStatus.message + "",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    is ShowOrdersState.CheckCreditLimit -> {
                        dismissdialog()
                        if (it.staticResponse.status == 200) {
                            Log.d("dvjnjdnvkdndvd", "Have Credit Limit: " + it.staticResponse.message)
                            startActivity(
                                Intent(
                                    this@SuperShowOrdersActivity,
                                    CreditLimitFormActivity::class.java
                                ).putExtra("orderNumber", orderNumber)
                                    .putExtra("orderType", orderType)
                                    .putExtra("quota_flag", quotaFlag)
                                    .putExtra("customer_id", customerId)
                                    .putExtra("order_total_price", orderTotalPrice)
                            )
                        } else if (it.staticResponse.status == 400) {
                            Log.d("dvjnjdnvkdndvd", "Not Credit Limit: " + it.staticResponse.message)
                            approveOrder(
                                "1.0",
                                SharedPreferencesHelper.getInstance().getUserToken(),
                                orderNumber!!,
                                SharedPreferencesHelper.getInstance().getEmployeeId()
                            )
                        }
                    }
                    is ShowOrdersState.CheckQouta -> {
                        dismissdialog()
                        if (it.staticResponse.status == 200) {
                            Log.d("dvjnjdnvkdndvd", "Have Qouta: " + it.staticResponse.message)
                            checkCreditLimit(
                                "1.0",
                                SharedPreferencesHelper.getInstance().getUserToken(),
                                orderNumber!!
                            )
                        } else if (it.staticResponse.status == 400) {
                            Log.d("dvjnjdnvkdndvd", "Not don't have Qouta: " + it.staticResponse.message)
                            Toast.makeText(this@SuperShowOrdersActivity, it.staticResponse.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                    is ShowOrdersState.ApproveOrder -> {
                        dismissdialog()
                        if (it.staticResponse.status != 400) {
                            Log.d("dvjnjdnvkdndvd", "Approved order Successfully: " + it.staticResponse.message)
                            Toast.makeText(this@SuperShowOrdersActivity, it.staticResponse.message+"", Toast.LENGTH_SHORT).show()
                        } else {
                            Log.d("dvjnjdnvkdndvd", "Faild to approve order: " + it.staticResponse.message)
                            Toast.makeText(this@SuperShowOrdersActivity, it.staticResponse.message+"", Toast.LENGTH_SHORT).show()
                        }
                        getShowOrders(
                            "1.0",
                            SharedPreferencesHelper.getInstance().getUserToken(),
                            SharedPreferencesHelper.getInstance().getEmployeeId()
                        )

                    }
                    is ShowOrdersState.Error -> {
                        dismissdialog()
                        Log.d("dvjnjdnvkdndvd", "onError: ${it.error}")
                        Toast.makeText(this@SuperShowOrdersActivity, it.error.toString()+"", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun setupRecycler() {
        if (mAdapter == null) {
            mAdapter = ShowOrdersAdapter(this, this)
            val linearLayoutManager = LinearLayoutManager(this)
            mBinding!!.ordersRecycler.layoutManager = linearLayoutManager
            mBinding!!.ordersRecycler.adapter = mAdapter
            mBinding!!.ordersRecycler.itemAnimator = DefaultItemAnimator()
        } else {
            mAdapter!!.notifyDataSetChanged()
        }
    }

    override fun onRejectClickListener(orderNumber: String?) {
        rejectOrder("1.0", SharedPreferencesHelper.getInstance().getUserToken(), orderNumber!!)
        Log.d(TAG, "onRejectClickListener: $orderNumber")
    }

    override fun onPendingClickListener(orderNumber: String?, orderType: String?, quotaFlag: String?, customerId: String?, orderTotalPrice: String?) {
        this.orderNumber = orderNumber!!
        this.orderType = orderType!!
        this.quotaFlag = quotaFlag!!
        this.customerId = customerId!!
        this.orderTotalPrice = orderTotalPrice!!
        if (quotaFlag == "1") {
            checkQouta(
                "1.0",
                SharedPreferencesHelper.getInstance().getUserToken(),
                orderNumber!!,
                SharedPreferencesHelper.getInstance().getEmployeeId()
            )
        } else if (quotaFlag == "0") {
            checkCreditLimit(
                "1.0",
                SharedPreferencesHelper.getInstance().getUserToken(),
                orderNumber!!
            )
        }
    }

    override fun onOrderClickListener(orderNumber: String?, orderTotalPrice: String?, customerId: String?) {
        startActivity(
            Intent(this, SuperOrderDetailsActivity::class.java)
                .putExtra("orderNumber", orderNumber)
                .putExtra("order_total_price", orderTotalPrice)
                .putExtra("customer_id", customerId)
        )
    }

    companion object {
        private const val TAG = "SuperShowOrderActivity"
    }
}
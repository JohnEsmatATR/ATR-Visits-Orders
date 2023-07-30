package com.akhnaton.foodvisits.ui.home.supervisor.superOrderDetails

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.akhnaton.foodvisits.data.model.supervisor.orderDetails.SuperOrder
import com.akhnaton.foodvisits.data.statusValue.supervisor.orderDetails.OrderDetailsIntent
import com.akhnaton.foodvisits.databinding.ActivitySuperOrderDetailsBinding
import com.akhnaton.foodvisits.shared.SharedPreferencesHelper
import com.akhnaton.foodvisits.data.statusValue.supervisor.orderDetails.OrderDetailsState

class SuperOrderDetailsActivity : AppCompatActivity() {
    var mBinding: ActivitySuperOrderDetailsBinding? = null
    private val viewModel: OrderDetailsViewModel by viewModels()
    var oAdapter: OrderDetailsAdapter? = null
    var rAdapter: ReturnDetailsAdapter? = null
    private lateinit var loadingDialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivitySuperOrderDetailsBinding.inflate(
            layoutInflater
        )
        setContentView(mBinding!!.root)
//        supportActionBar!!.hide()

        val extra = intent
        val orderNumber = extra.getStringExtra("orderNumber")
        val orderTotalPrice = extra.getStringExtra("order_total_price")
        val customerId = extra.getStringExtra("customer_id")

        initLoadingDialog()
        getOrderDetails("1.0", SharedPreferencesHelper.getInstance().getUserToken(), orderNumber!!, SharedPreferencesHelper.getInstance().getEmployeeId(), orderTotalPrice!!, customerId!!)
        fetchOrderDetails()

        setupRecycler()
    }

    private fun getOrderDetails(
        app_version: String,
        api_token: String,
        orderNumber: String,
        superId: String,
        order_total_price: String,
        customer_id:String,
    ) {
        lifecycleScope.launchWhenCreated {
            viewModel.orderDetailsIntent.send(
                OrderDetailsIntent.GetOrderDetails(
                    app_version,
                    api_token,
                    orderNumber,
                    superId,
                    order_total_price,
                    customer_id,
                )
            )
        }
    }

    private fun fetchOrderDetails() {
        lifecycleScope.launchWhenCreated {
            viewModel.state.collect {
                when (it) {
                    OrderDetailsState.Idle -> Log.d(TAG, "Idle: ")
                    OrderDetailsState.Loading -> {
                        Log.d(TAG, "loading")
                        showLoadingDialog()
                    }
                    is OrderDetailsState.OrderDetails -> {
                        dismissdialog()
                        Log.d(TAG, "mList: " + it.superOrder.data)
                        setData(it.superOrder)
                        oAdapter!!.setList(it.superOrder.data[0].orderDetails)
                        rAdapter!!.setList(it.superOrder.data[0].orderDetailsReturn)

                    }

                    is OrderDetailsState.Error -> {
                        dismissdialog()
                        Log.d(TAG, "onError: ${it.error}")
                    }
                }
            }
        }
    }

    private fun setupRecycler() {
        if (oAdapter == null) {
            oAdapter = OrderDetailsAdapter()
            val linearLayoutManager = LinearLayoutManager(this)
            mBinding!!.oDRec.layoutManager = linearLayoutManager
            mBinding!!.oDRec.adapter = oAdapter
            mBinding!!.oDRec.itemAnimator = DefaultItemAnimator()

            //Return RecyclerView
            val linerReturn = LinearLayoutManager(this)
            rAdapter = ReturnDetailsAdapter()
            mBinding!!.oDReturnRec.layoutManager = linerReturn
            mBinding!!.oDReturnRec.adapter = rAdapter
            mBinding!!.oDReturnRec.itemAnimator = DefaultItemAnimator()
        } else {
            oAdapter!!.notifyDataSetChanged()
        }
    }

    private fun setData(item: SuperOrder) {
        mBinding!!.customerName.text = item.data[0].customer_name
        mBinding!!.totalOrder.text = item.data[0].total_order
        mBinding!!.lastUpdate.text = item.data[0].last_updated_date
        mBinding!!.orderNumber.text = item.data[0].order_nmber
        mBinding!!.returnNumber.text = item.data[0].order_return_number
        if (item.data[0].order_return_number == "") {
            mBinding!!.returnNumberText.visibility = View.GONE
            mBinding!!.returnNumber.visibility = View.GONE
            mBinding!!.totalReturn.visibility = View.GONE
            mBinding!!.totalReturnText.visibility = View.GONE
            mBinding!!.returnText.visibility = View.GONE
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
        private const val TAG = "OrderDetailsActivity"
    }
}
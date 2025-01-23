package com.akhnaton.foodvisits.ui.home.visits.orderHistory

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.akhnaton.foodvisits.BuildConfig
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.data.statusValue.orderHistory.OrderHistoryIntent
import com.akhnaton.foodvisits.data.statusValue.orderHistory.OrderHistoryState
import com.akhnaton.foodvisits.databinding.ActivityOrderHistoryDetailsBinding
import com.akhnaton.foodvisits.shared.SharedPreferencesHelper

class OrderHistoryDetailsActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "OrderDetailsActivity"
    }

    private lateinit var binding: ActivityOrderHistoryDetailsBinding
    private val viewModel: OrderHistoryViewModel by viewModels()
    private var mAdapter = OrderDetailsAdapter()
    private var mReturnAdapter = OrderDetailsAdapter()
    private val version = BuildConfig.VERSION_NAME

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_order_history_details)

        val orderNumber = intent.getStringExtra("orderNumber")
        binding.orderNumber.text = orderNumber

        lifecycleScope.launchWhenCreated {
            viewModel.ordersIntent.send(
                OrderHistoryIntent.OrderHistoryDetails(
                    token = SharedPreferencesHelper.getInstance().getUserToken(),
                    version = version,
                    orderNumber = orderNumber ?: ""
                )
            )
        }

        binding.backBtn.setOnClickListener {
            onBackPressed()
            finish()
        }

        fetchData()
        setupRecycler()
    }

    private fun setupRecycler() {
        binding.oRdRecycler.adapter = mAdapter
        binding.oRdRecycler.apply {
            layoutManager = LinearLayoutManager(this@OrderHistoryDetailsActivity)
        }

        binding.ordReturnRecycler.adapter = mReturnAdapter
        binding.ordReturnRecycler.apply {
            layoutManager = LinearLayoutManager(this@OrderHistoryDetailsActivity)
        }
    }

    private fun fetchData() {
        lifecycleScope.launchWhenCreated {
            viewModel.status.collect {
                when (it) {
                    OrderHistoryState.Idle -> Log.d(TAG, "Idle: ")
                    OrderHistoryState.Loading -> Log.d(TAG, "loading")
                    is OrderHistoryState.GetOrdersHistoryDetails -> {
                        mAdapter.setOrderList(it.orders.data.order_details)
                        if (!it.orders.data.return_details.isNullOrEmpty()){
                            Log.d("jjdvndjnvjdnv", "fetchData: ${it.orders.data.return_details}")
                            binding.returnCard.visibility = View.VISIBLE
                            mReturnAdapter.setOrderList(it.orders.data.return_details)

                        }
                    }

                    is OrderHistoryState.Error -> {
                        binding.constrainLayout.visibility = View.INVISIBLE
                        binding.tryAgain.visibility = View.VISIBLE
                    }

                    else -> {}
                }
            }
        }
    }
}
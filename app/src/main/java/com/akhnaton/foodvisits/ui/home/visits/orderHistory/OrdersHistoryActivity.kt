package com.akhnaton.foodvisits.ui.home.visits.orderHistory

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.DatePicker
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.akhnaton.foodvisits.BuildConfig
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.data.model.orderHistory.OrderHistoryData
import com.akhnaton.foodvisits.data.statusValue.orderHistory.OrderHistoryIntent
import com.akhnaton.foodvisits.data.statusValue.orderHistory.OrderHistoryState
import com.akhnaton.foodvisits.databinding.ActivityOrdersHistoryBinding
import com.akhnaton.foodvisits.shared.ConvertDate
import com.akhnaton.foodvisits.shared.SharedPreferencesHelper
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class OrdersHistoryActivity : AppCompatActivity(),
    OrdersHistoryViewHolder.OnSelectOrderClickListener {

    companion object {
        private const val TAG = "OrdersHistoryActivity"
    }

    private lateinit var binding: ActivityOrdersHistoryBinding
    private val viewModel: OrderHistoryViewModel by viewModels()
    private val mAdapter = OrdersHistoryAdapter()
    private val version = BuildConfig.VERSION_NAME
    private var timeStampFrom = ""
    private var timeStampTo = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_orders_history)

        lifecycleScope.launch {
            viewModel.ordersIntent.send(
                OrderHistoryIntent.OrderHistory(
                    SharedPreferencesHelper.getInstance().getUserToken(),
                    version, ConvertDate.getDateTimeStamp(), ConvertDate.getDateTimeStamp()
                )
            )
        }

        binding.fromDate.setOnClickListener {
            val cldr = Calendar.getInstance()
            val day = cldr[Calendar.DAY_OF_MONTH]
            val month = cldr[Calendar.MONTH]
            val year = cldr[Calendar.YEAR]
            val picker = DatePickerDialog(
                this,
                { view1: DatePicker?, year1: Int, monthOfYear: Int, dayOfMonth: Int ->

                    val date = dayOfMonth.toString() + "/" + (monthOfYear + 1) + "/" + year1
                    cldr.set(year1, monthOfYear, dayOfMonth)
                    timeStampFrom = (cldr.timeInMillis / 1000).toString()

//                    Log.d("TAG", "Testing TimeStamp T:  ${ convertDateToLong(cldr.time)}")
                    binding.fromDate.setText(date)
                }, year, month, day
            )
            picker.show()
        }

        binding.toDate.setOnClickListener {
            val cldr = Calendar.getInstance()
            val day = cldr[Calendar.DAY_OF_MONTH]
            val month = cldr[Calendar.MONTH]
            val year = cldr[Calendar.YEAR]
            val picker = DatePickerDialog(
                this,
                { view1: DatePicker?, year1: Int, monthOfYear: Int, dayOfMonth: Int ->

                    val date = dayOfMonth.toString() + "/" + (monthOfYear + 1) + "/" + year1
                    cldr.set(year1, monthOfYear, dayOfMonth)
                    timeStampTo = (cldr.timeInMillis / 1000).toString()
                    Log.d("TAG", "Testing TimeStamp T:  $timeStampTo")

                    binding.toDate.setText(date)
                }, year, month, day
            )
            picker.show()
        }

        binding.toDateBtn.setOnClickListener {
            Log.d(
                TAG,
                "ConvertDate: $timeStampFrom + TimeStampTo: $timeStampTo"
            )

            lifecycleScope.launch {
                viewModel.ordersIntent.send(
                    OrderHistoryIntent.OrderHistory(
                        SharedPreferencesHelper.getInstance().getUserToken(),
                        version, timeStampFrom, timeStampTo
                    )
                )
            }
        }
        binding.backBtn.setOnClickListener { onBackPressed() }
        setupRecycler()
        fetchData()
    }

    private fun setupRecycler() {
        binding.recOrdersHistory.adapter = mAdapter
        binding.recOrdersHistory.apply {
            layoutManager = LinearLayoutManager(this@OrdersHistoryActivity)
        }
    }

    private fun fetchData() {
        lifecycleScope.launch {
            viewModel.status.collect {
                when (it) {
                    is OrderHistoryState.GetOrdersHistory -> {
                        if (it.orders.data.isEmpty()){
                            binding.blankOrders.visibility = View.VISIBLE
                        }
                        Log.d("TAG", "fetchData: ${it.orders}")
                        mAdapter.setOrdersList(it.orders.data, this@OrdersHistoryActivity)
                    }
                    is OrderHistoryState.Error -> Log.d(TAG, "fetchData: ${it.error}")
                    OrderHistoryState.Idle -> Log.d(TAG, "fetchData: $it")
                    OrderHistoryState.Loading -> Log.d(TAG, "fetchData: $it")
                }

            }
        }
    }

    override fun onSelectOrderClickListener(data: OrderHistoryData, position: Int) {
        val intent = Intent(this@OrdersHistoryActivity, OrderHistoryDetailsActivity::class.java)
        intent.putExtra("orderNumber", data.orig_sys_document_ref)
        startActivity(intent)
        Log.d("TAG", "onSelectOrderClickListener: ${data.customer_name}")
    }

    fun convertDateToLong(date: String): Long {
        val df = SimpleDateFormat("yyyy.MM.dd HH:mm")
        return df.parse(date).time
    }
}
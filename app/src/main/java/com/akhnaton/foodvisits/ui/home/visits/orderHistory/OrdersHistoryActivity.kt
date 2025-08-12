package com.akhnaton.foodvisits.ui.home.visits.orderHistory

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.DatePicker
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
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
import com.akhnaton.foodvisits.ui.home.visits.order.OrderActivity
import kotlinx.coroutines.launch
import java.text.ParseException
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
    private lateinit var loadingDialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_orders_history)

        initLoadingDialog()

        timeStampFrom = ConvertDate.getDateTimeStamp()
        timeStampTo = ConvertDate.getDateTimeStamp()

        lifecycleScope.launch {
            viewModel.ordersIntent.send(
                OrderHistoryIntent.OrderHistory(
                    SharedPreferencesHelper.getInstance().getUserToken(),
                    version, timeStampTo, timeStampFrom
                )
            )
        }
        showLoadingDialog()

        val sdf = SimpleDateFormat("dd/M/yyyy")
        val date = sdf.format(Date())
        binding.fromDate.setText(date)
        binding.toDate.setText(date)

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

                    binding.toDate.setText(date)

                }, year, month, day
            )
            picker.show()
        }

        binding.toDateBtn.setOnClickListener {
            Log.d("kjbkjbjkjknkn", "onCreate: $timeStampFrom || $timeStampTo")

            try {
                var dateFrom = Date(timeStampFrom.toLong() * 1000L)
                var dateTo = Date(timeStampTo.toLong() * 1000L)

                val calendarFrom = Calendar.getInstance().apply {
                    time = dateFrom
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                dateFrom = calendarFrom.time

                val calendarTo = Calendar.getInstance().apply {
                    time = dateTo
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                dateTo = calendarTo.time


                Log.d("kjbkjbjkjknkn", "onCreate: $dateFrom || $dateTo")
                if (dateFrom.before(dateTo) || dateFrom == dateTo) {
                    lifecycleScope.launch {
                        viewModel.ordersIntent.send(
                            OrderHistoryIntent.OrderHistory(
                                SharedPreferencesHelper.getInstance().getUserToken(),
                                version, timeStampTo, timeStampFrom
                            )
                        )
                    }
                    showLoadingDialog()
                } else {
                    Toast.makeText(this, "Time From is smallest than Time To", Toast.LENGTH_SHORT)
                        .show()
                }
            } catch (e: ParseException) {
                // Handle the ParseException here
                e.printStackTrace()
            }


        }
        binding.backBtn.setOnClickListener { onBackPressed() }
        setupRecycler()
        fetchData()
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
                        if (it.orders.data.isEmpty()) {
                            binding.blankOrders.visibility = View.VISIBLE
                        } else {
                            binding.blankOrders.visibility = View.GONE
                        }
                        Log.d("TAG", "fetchData: ${it.orders}")
                        mAdapter.setOrdersList(it.orders.data, this@OrdersHistoryActivity)
                        dismissdialog()
                    }

                    is OrderHistoryState.Error -> {
                        Log.d(TAG, "fetchData: ${it.error}")
                        dismissdialog()
                    }

                    OrderHistoryState.Idle -> Log.d(TAG, "fetchData: $it")
                    OrderHistoryState.Loading -> Log.d(TAG, "fetchData: $it")
                    else -> {}
                }

            }
        }
    }

    override fun onSelectOrderClickListener(data: OrderHistoryData, position: Int) {
        if (data.flag == "1") {
            val intent = Intent(this@OrdersHistoryActivity, OrderHistoryDetailsActivity::class.java)
            intent.putExtra("orderNumber", data.orig_sys_document_ref)
            startActivity(intent)
            Log.d("TAG", "onSelectOrderClickListener: ${data.customer_name}")
        } else {
            val intent = Intent(this@OrdersHistoryActivity, OrderActivity::class.java)
            intent.putExtra("customerPartySiteId", data.party_site_id)
            intent.putExtra("orderType", data.order_type)
            intent.putExtra("customerTypePosition", data.customer_type)
            intent.putExtra("customer_code", data.customer_code)
            intent.putExtra("paymentTypePosition", data.payment_term_id)
            intent.putExtra("orderSourcePosition", data.order_source_id)
            intent.putExtra("orderSourceFlag", data.order_source_flag)
            intent.putExtra("customer_name", data.customer_name)
            intent.putExtra("isOrderSaved", true)
            intent.putExtra("orderNumber", data.orig_sys_document_ref)
            startActivity(intent)
            Log.d("TAG", "onSelectOrderClickListener: ${data.customer_name}")

        }
    }


    fun convertDateToLong(date: String): Long {
        val df = SimpleDateFormat("yyyy.MM.dd HH:mm")
        return df.parse(date).time
    }
}
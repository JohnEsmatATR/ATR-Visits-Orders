package com.akhnaton.foodvisits.ui.home.printFood

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import cn.pedant.SweetAlert.BuildConfig
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.data.model.food.details.FoodInvoiceDetails
import com.akhnaton.foodvisits.data.model.food.order.FoodData
import com.akhnaton.foodvisits.data.statusValue.food.FoodIntent
import com.akhnaton.foodvisits.data.statusValue.food.FoodStatus
import com.akhnaton.foodvisits.databinding.ActivityFoodInvoiceBinding
import com.akhnaton.foodvisits.databinding.FoodOrderDetailsBinding
import com.akhnaton.foodvisits.shared.SharedPreferencesHelper
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.log

class FoodInvoiceActivity : AppCompatActivity(), View.OnClickListener {

    companion object {
        private const val TAG = "FoodInvoiceActivity"
    }

    private val versionName = BuildConfig.VERSION_NAME

    private lateinit var binding: ActivityFoodInvoiceBinding
    private val viewModel: FoodViewModel by viewModels()
    private var mLinearLayout: ViewGroup? = null
    var data = FoodData()
    lateinit var printMe:PrinterManager
    val dateFormat = SimpleDateFormat("yyyy-MMM-dd hh:mm:ss a", Locale.ENGLISH)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
            binding= ActivityFoodInvoiceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        data = intent.getSerializableExtra("foodOrder") as FoodData

        mLinearLayout = binding.itemListLayout
        printMe = PrinterManager(baseContext)

        binding.tvInvoiceNumber.text = data.orderSalesNumber
       // binding.tvDateTime.text = dateFormat.format(Date())

        data.orderSalesNumber.let {
            lifecycleScope.launch {
                viewModel.foodIntent.send(
                    FoodIntent.OrderDetails("1.19", SharedPreferencesHelper.getInstance().getUserToken(), orderNumber = data.orderSalesNumber)
                )
                viewModel.currentDateTime.collect {dataTime ->
                    binding.tvDateTime.text= dataTime
                }
            }
        }

        binding.btnPrint.setOnClickListener(this)


        fetchData()
    }


    private fun calculateTotalOrderPrice(orderList: List<FoodInvoiceDetails>): Float {
        var price = 0f
        for (order in orderList) {
            price += order.totalItemPrice?.toFloat()!!
        }
        return String.format(Locale.US, "%.2f", price).toFloat()
    }

    private fun calculateTotalOrderQuantity(orderList: List<FoodInvoiceDetails>): Int {
        var quanity = 0
        for (order in orderList) {
            quanity += order.orderQuantity?.toInt()!!
        }
        return quanity
    }

    private fun calculateTotalVAT(orderList: List<FoodInvoiceDetails>): Float {
        var vat = 0f
        for (order in orderList) {
            vat += order.taxValue?.toFloat()!!
            Log.d(TAG, "calculateTotalVAT: ${ vat}")
        }
        return String.format(Locale.US, "%.2f", vat).toFloat()
    }

    @SuppressLint("SetTextI18n")
    private fun fetchData() {
        lifecycleScope.launch {
            viewModel.status.collect {
                when (it) {
                    is FoodStatus.Idle -> {
                        Log.d(TAG, "fetchData: Idle")
                    }
                    is FoodStatus.Loading -> Log.d(TAG, "fetchData: Loading")

                    is FoodStatus.OrderDetails -> {
                        Log.d(TAG, "fetchData: Orders: ${it.data}")
                        binding.tvCustomerName.text = it.data.data.invoice_info.customer_name
                        binding.tvCustomerAddress.text = it.data.data.invoice_info.customer_address

                        when (data.orderType) {
                            "Food" -> {
                                binding.txtOrderType.visibility = View.VISIBLE
                                binding.txtBatchNumber.visibility = View.GONE
                                binding.txtInvoice.text = "فاتورة"
                            }

                            "Pharma" -> {
                                binding.txtOrderType.visibility = View.GONE
                                binding.txtBatchNumber.visibility = View.VISIBLE
                                binding.txtInvoice.text = "رقم امر البيع"
                            }

                            else -> {
                                binding.txtOrderType.visibility = View.GONE
                                binding.txtBatchNumber.visibility = View.GONE
                            }
                        }

                        for (order in it.data.data.invoice_details) {
                            addLayout(
                                order.itemDesc ?: "",
                                order.orderQuantity.toString() ?: "1",
                                order.listPrice.toString() ?: "1",
                                order.totalItemPrice.toString()
                            )
                        }

                        addLayout(
                            "الإجمالي",
                            calculateTotalOrderQuantity(orderList = it.data.data.invoice_details).toString(),
                            "",
                            calculateTotalOrderPrice(orderList = it.data.data.invoice_details).toString()
                        )
                        Log.d(TAG, "fetchData: ${it.data.data.invoice_details}")
                        binding.tvVat.text =
                            calculateTotalVAT(it.data.data.invoice_details).toString() + " جنيه"
                        binding.tvTotalInvoiceWithoutTax.text =
                            " ${it.data.data.invoice_info.invoice_total_value} جنيه "

                        binding.tvTotalInvoice.text =
                            " ${
                                (it.data.data.invoice_info.invoice_total_value) + calculateTotalVAT(
                                    it.data.data.invoice_details
                                )
                            } جنيه "

                        Toast.makeText(
                            baseContext,
                            it.data.data.invoice_info.customer_name,
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    is FoodStatus.DeliveryPrint -> {
                        Log.d(TAG, "DeliveryPrint: ${it.data.status}")
                        printMe.unbindService(baseContext)
                    }

                    is FoodStatus.Error -> {
                        Toast.makeText(
                            this@FoodInvoiceActivity,
                            "No Data Found",
                            Toast.LENGTH_SHORT
                        ).show()

                        Log.d(TAG, "fetchData: ${it.error.toString()}")
                        printMe.unbindService(baseContext)
                    }

                    else -> {
                        Log.d(TAG, "fetchData: ")
                        printMe.unbindService(baseContext)
                    }
                }
            }
        }
    }

    private fun addLayout(
        productName: String,
        productQuantity: String,
        batchNumber: String,
        productPrice: String
    ) {
        val binding =
            FoodOrderDetailsBinding.inflate(LayoutInflater.from(this), mLinearLayout, false)

        binding.tvProductName.text = productName
        binding.tvProductQty.text = productQuantity
        binding.tvBatchNumber.text = batchNumber
        binding.tvProductPrice.text = productPrice

        if (data.orderType == "Pharma") {
            binding.layoutBatchNumber.visibility = View.VISIBLE
        } else {
            binding.layoutBatchNumber.visibility = View.GONE
        }

        mLinearLayout?.addView(binding.root)
    }

    override fun onClick(p0: View?) {
//        PrintMe(this).sendViewToPrinter(binding.printMeLayout)

        lifecycleScope.launch {
            printMe.performTransactionPrintingSuspend(binding.printMeLayout)
        }

        lifecycleScope.launch {
            viewModel.foodIntent.send(
                FoodIntent.DeliveryPrint(
                    "1.19",
                    SharedPreferencesHelper.getInstance().getUserToken(),
                    orderNumber = data.orderSalesNumber
                )
            )
        }
    }
}
package com.akhnaton.foodvisits.ui.home.printFood

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.akhnaton.foodvisits.BuildConfig
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.data.model.food.details.FoodInvoiceDetails
import com.akhnaton.foodvisits.data.model.food.order.FoodData
import com.akhnaton.foodvisits.data.statusValue.food.FoodIntent
import com.akhnaton.foodvisits.data.statusValue.food.FoodStatus
import com.akhnaton.foodvisits.databinding.ActivityFoodInvoiceBinding
import com.akhnaton.foodvisits.databinding.FoodOrderDetailsBinding
import com.akhnaton.foodvisits.shared.ConvertDate
import com.akhnaton.foodvisits.shared.SharedPreferencesHelper
import com.sunmi.peripheral.printer.InnerPrinterCallback
import com.sunmi.peripheral.printer.InnerPrinterManager
import com.sunmi.peripheral.printer.SunmiPrinterService
import kotlinx.coroutines.launch
import java.util.Locale

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_food_invoice)

        data = intent.getSerializableExtra("foodOrder") as FoodData

        mLinearLayout = binding.itemListLayout
        printMe = PrinterManager(baseContext)

        binding.tvInvoiceNumber.text = data.orderSalesNumber
        binding.tvDateTime.text = ConvertDate.getDateAndTime()

        data.orderSalesNumber.let {
            lifecycleScope.launch {
                viewModel.foodIntent.send(
                    FoodIntent.OrderDetails(
                        versionName,
                        SharedPreferencesHelper.getInstance().getUserToken(),
                        orderNumber = data.orderSalesNumber
                    )
                )
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
                            }

                            "Pharma" -> {
                                binding.txtOrderType.visibility = View.GONE
                                binding.txtBatchNumber.visibility = View.VISIBLE
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
                                order.batchNumber.toString() ?: "1",
                                order.totalItemPrice.toString()
                            )
                        }

                        addLayout(
                            "الإجمالي",
                            calculateTotalOrderQuantity(orderList = it.data.data.invoice_details).toString(),
                            "",
                            calculateTotalOrderPrice(orderList = it.data.data.invoice_details).toString()
                        )

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
                    versionName,
                    SharedPreferencesHelper.getInstance().getUserToken(),
                    orderNumber = data.orderSalesNumber
                )
            )
        }
    }
}
package com.akhnaton.foodvisits.ui.home.visits.order

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import cn.pedant.SweetAlert.SweetAlertDialog
import com.akhnaton.foodvisits.BuildConfig
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.data.model.order.CardItem
import com.akhnaton.foodvisits.data.model.order.OrderItem
import com.akhnaton.foodvisits.data.model.order.ProductData
import com.akhnaton.foodvisits.data.model.order.ReturnItem
import com.akhnaton.foodvisits.data.model.order.SavedOrder
import com.akhnaton.foodvisits.data.statusValue.order.OrderIntent
import com.akhnaton.foodvisits.data.statusValue.order.OrderStatus
import com.akhnaton.foodvisits.databinding.ActivityReturnBinding
import com.akhnaton.foodvisits.shared.CreateOrderHelper
import com.akhnaton.foodvisits.shared.ProgressDialogHelper
import com.akhnaton.foodvisits.shared.SharedPreferencesHelper
import com.akhnaton.foodvisits.shared.SpinnerHelper
import com.akhnaton.foodvisits.ui.home.MainActivity
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class ReturnActivity : AppCompatActivity(), View.OnClickListener,
    OrderViewHolder.OnItemClickListener {
    companion object {
        private const val TAG = "ReturnActivity"
    }

    private lateinit var binding: ActivityReturnBinding
    private val viewModel: OrderViewModel by viewModels()
    private val versionName = BuildConfig.VERSION_NAME
    private val mAdapter = OrderAdapter()
    private var mReturnList: List<ProductData> = ArrayList()
    private var mReturnNameList: MutableList<String> = ArrayList()
    private var mAdapterCardsProduct: MutableList<CardItem> = ArrayList()
    private var mItemPositionSelected: Int = 0
    private var mItemsCardAdded: ArrayList<ReturnItem> = ArrayList()
    private lateinit var loadingDialog: AlertDialog

    private var totalReturn = 0.0

    private var orderList: MutableList<OrderItem> = ArrayList()
    var categoryName = ""
    var orderType = ""
    var orderNumber = ""
    var customerTypePosition = ""
    var paymentTypePosition = ""
    var orderSourcePosition = ""
    var priceListIdPosition: Int = -1
    var priceListDescriptionPosition: String = ""
    var customerPartySiteId = ""
    var customerCode = ""
    var customerName = ""
    var turnOver = false
    private var totalOrder = 0.0
    private var returnLimit = 0
    private lateinit var progressBar: SweetAlertDialog
    private var isOrderSaved = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_return)

        initLoadingDialog()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {

                progressBar.show()
            }
        })
        orderList = intent.getParcelableArrayListExtra<OrderItem>("orderList")!!
        categoryName = intent.getStringExtra("categoryName").toString()
        orderType = intent.getStringExtra("orderType").toString()
        orderNumber = intent.getStringExtra("orderNumber").toString()
        customerTypePosition = intent.getStringExtra("customerTypePosition").toString()
        paymentTypePosition = intent.getStringExtra("paymentTypePosition").toString()
        orderSourcePosition = intent.getStringExtra("orderSourcePosition").toString()
        priceListIdPosition = intent.getIntExtra("priceListIdPosition", -1)
        priceListDescriptionPosition = intent.getStringExtra("priceListDescriptionPosition").toString()
        customerPartySiteId = intent.getStringExtra("customerPartySiteId").toString()
        customerCode = intent.getStringExtra("customer_code").toString()
        customerName = intent.getStringExtra("customer_name").toString()
        turnOver = intent.getBooleanExtra("turnOver", false)
        totalOrder = intent.getDoubleExtra("total", 0.0)
        returnLimit = intent.getIntExtra("returnLimit", 0)
        binding.totalOrder.text = intent.getStringExtra("totalOrder").toString()
        orderNumber = intent.getStringExtra("orderNumber").toString()
        isOrderSaved = intent.getBooleanExtra("isOrderSaved", false)


        if (isOrderSaved) {
            lifecycleScope.launch {
                viewModel.orderIntent.send(
                    OrderIntent.SavedOrder(
                        appVersion = versionName,
                        apiToken = SharedPreferencesHelper.getInstance()
                            .getUserToken(),
                        orderNumber = orderNumber,
                    )
                )
                showLoadingDialog()
            }
        }


        lifecycleScope.launch {
            viewModel.orderIntent.send(
                OrderIntent.GetProducts(
                    app_version = versionName,
                    api_token = SharedPreferencesHelper.getInstance().getUserToken(),
                    orderType = orderType,
                    sub_category = categoryName,
                    customer_type = customerTypePosition.toInt(),
                    customer_code = customerCode.toInt(),
                    customer_party_site_id = customerPartySiteId.toInt(),
                    item_price_list = priceListIdPosition.toString()
                )
            )

            viewModel.orderIntent.send(
                OrderIntent.GetCategories(
                    versionName,
                    SharedPreferencesHelper.getInstance().getUserToken(),
                    orderType,
                    customerTypePosition
                )
            )
            showLoadingDialog()

        }
        binding.itemsSpinner.setOnItemClickListener { mItemPositionSelected = it }

        binding.addItems.setOnClickListener(this)
        binding.sendOrder.setOnClickListener(this)
        binding.btnSave.setOnClickListener(this)
        mAdapter.deleteProduct(this)
        binding.backArrow.setOnClickListener { progressBar.show() }
        binding.txtName.text = customerName
        fetchData()
        setupRecycler()
        initBackPressedDialog()
        selectedItemProduct()

    }


    private fun setSavedOrderItems(items: SavedOrder) {

        for (item in items.return_items) {
            val cardItem = CardItem(
                item.item_id,
                item.item_code,
                item.item_name,
                item.item_price,
                item.tax.toFloat(),
                item.quantity.toString(),
                item.items_price.toFloat(),
                "normal",
                item.price_list_id
            )

            val orderItem = ReturnItem(
                item.item_id,
                item.quantity.toString()
            )

            mAdapterCardsProduct.add(cardItem)
            mItemsCardAdded.add(orderItem)
        }
        mAdapter.addProduct(mAdapterCardsProduct)


        //Calculate Total Amount Text
        totalReturn += items.order_return_items_general_data.total_items_price
        binding.totalReturn.text = "${totalReturn.toFloat()} EGP"

        binding.itemsCount.text =
            "${mAdapterCardsProduct.size} Items"
    }

    private fun setupRecycler() {
        binding.recOrders.adapter = mAdapter
        binding.recOrders.apply {
            layoutManager = LinearLayoutManager(this@ReturnActivity)
        }
    }

    private fun fetchData() {
        lifecycleScope.launch {
            viewModel.status.collect {
                when (it) {
                    OrderStatus.Idle -> Log.d(TAG, "fetchData: Idle")
                    OrderStatus.Loading -> Log.d(TAG, "fetchData: Loading")
                    is OrderStatus.GetCategories -> {
                        SpinnerHelper().setNormalSpinnerAdapter(
                            binding.categorySpinner,
                            it.data.data.sub_categories.toMutableList(),
                            this@ReturnActivity
                        )
                        categoryName = binding.categorySpinner.selectedItem.toString()
                        Log.d(TAG, "fetchData: ${it.data.data.sub_categories}")

                        viewModel.orderIntent.send(
                            OrderIntent.GetProducts(
                                app_version = versionName,
                                api_token = SharedPreferencesHelper.getInstance().getUserToken(),
                                orderType = orderType,
                                sub_category = categoryName,
                                customer_type = customerTypePosition.toInt(),
                                customer_code = customerCode.toInt(),
                                customer_party_site_id = customerPartySiteId.toInt(),
                                item_price_list = priceListIdPosition.toString(),
                            )
                        )
                        dismissdialog()
                    }

                    is OrderStatus.GetProducts -> {

                        mReturnNameList.clear()
                        mReturnList = it.data.data.return_products
                        mReturnList.forEach { name ->
                            mReturnNameList.add(name.item_description)
                        }

                        binding.itemsSpinner.setItems(mReturnNameList.toTypedArray())
                        binding.itemsSpinner.setSelection(0)
                        Log.d(TAG, "fetchData: ${it.data.data.return_products}")

                        dismissdialog()
                    }

                    is OrderStatus.SendOrder -> {
                        if (it.data.status == 400) {
                            ProgressDialogHelper().orderLimitAlert(
                                this@ReturnActivity,
                                it.data.message
                            )
                            dismissdialog()
                        } else {
                            startActivity(Intent(this@ReturnActivity, MainActivity::class.java))
                            dismissdialog()
                            finishAffinity()
                        }
                    }

                    is OrderStatus.SaveOrderPending -> {
                        if (it.data.status == 400) {
                            ProgressDialogHelper().orderLimitAlert(
                                this@ReturnActivity,
                                it.data.message
                            )
                            dismissdialog()
                        } else {
                            dismissdialog()
                            startActivity(Intent(this@ReturnActivity, MainActivity::class.java))
                            finishAffinity()
                        }

                        Log.d(
                            TAG,
                            "fetchData: SaveOrder: ${it.data.message}"
                        )
                    }

                    is OrderStatus.SavedOrder -> {
                        Log.d("dvjdnjvndvdvd", "onCreate: ${it.data.status}")

                        if (it.data.status == 400) {
                            Toast.makeText(
                                this@ReturnActivity,
                                "Error, ${it.data.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                            dismissdialog()
                        } else {
                            setSavedOrderItems(it.data.data)
                            dismissdialog()

                        }

                        Log.d(
                            TAG,
                            "fetchData: SavedOrders: ${it.data.message}"
                        )
                    }

                    is OrderStatus.Error -> {
                        dismissdialog()
                        Log.d(TAG, "fetchData: Error $it")
                    }

                    else -> {}
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

    fun selectedItemProduct() {

        binding.itemsSpinner.setOnItemClickListener { position ->
            mItemPositionSelected = position
            binding.txtQuantity.text = ""
            binding.txtQuantity.text = mReturnList[position].item_availability.toString()
            binding.quantityED.setText("")

        }





        binding.categorySpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    categoryName = binding.categorySpinner.selectedItem.toString()
                    lifecycleScope.launch {
                        viewModel.orderIntent.send(
                            OrderIntent.GetProducts(
                                app_version = versionName,
                                api_token = SharedPreferencesHelper.getInstance().getUserToken(),
                                orderType = orderType,
                                sub_category = categoryName,
                                customer_type = customerTypePosition.toInt(),
                                customer_code = customerCode.toInt(),
                                customer_party_site_id = customerPartySiteId.toInt(),
                                item_price_list = priceListIdPosition.toString(),
                            )
                        )
                    }
                    binding.txtQuantity.text = ""
                    binding.itemsSpinner.setText("")
                    binding.quantityED.setText("")

                    showLoadingDialog()
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    Log.d(TAG, "onNothingSelected: ")
                }


            }
    }

    @SuppressLint("SetTextI18n")
    override fun onClick(v: View) {
        when (v.id) {
            binding.sendOrder.id -> {
                validationSendOrder("send")
            }

            binding.btnSave.id -> {
                validationSendOrder("save")
            }

            binding.addItems.id -> {

                if (binding.quantityED.text.toString()
                        .isEmpty() || binding.quantityED.text.toString().toInt() <= 0
                ) {
                    binding.quantityED.error = "Choose Quantity"
                } else if (binding.itemsSpinner.text.toString() == "") {
                    Toast.makeText(
                        this,
                        "no item selected",
                        Toast.LENGTH_SHORT
                    ).show()
                } else if (binding.quantityED.text.toString().toInt() == 100000
                    || binding.quantityED.text.toString().length > 6
                ) {
                    binding.quantityED.error = "Order quantity can't be more than 100,000"
                    binding.quantityED.isFocusable = true
                } else {
                    if (checkItemAddedBefore()) {

                        val mItem = mReturnList[mItemPositionSelected]

                        val returnItem = ReturnItem(
                            mItem.item_id,
                            binding.quantityED.text.toString()
                        )
                        mItemsCardAdded.add(returnItem)

                        //Calculate Total Amount Text
                        val total =
                            binding.quantityED.text.toString()
                                .toDouble() * (mItem.item_price + mItem.item_tax)
                        totalReturn += total
                        binding.totalReturn.text = "${totalReturn.toFloat()} EGP"
                        val cardItem = CardItem(
                            mItem.item_id,
                            mItem.item_code,
                            mItem.item_description,
                            mItem.item_price,
                            mItem.item_tax.toFloat(),
                            binding.quantityED.text.toString(),
                            total.toFloat(),
                            "",
                            mItem.item_price_list
                        )

                        mAdapterCardsProduct.add(cardItem)
                        mAdapter.addProduct(mAdapterCardsProduct)
                        binding.itemsCount.text =
                            "${mAdapterCardsProduct.size} Items"
                    }
                }
            }
        }
    }

    private fun validationSendOrder(flag: String) {
        if (mAdapterCardsProduct.isEmpty()) {
            Toast.makeText(this, "Select Item To Send", Toast.LENGTH_SHORT).show()
        } else {
            val orderPercent: Double = totalOrder / 100.0f * returnLimit
            Log.d(
                "jbvdjnjnkdvdv",
                "validationSendOrder: ${orderPercent} | ${totalOrder} | ${returnLimit}"
            )
            if (orderPercent < totalReturn) {
                ProgressDialogHelper().orderLimitAlert(
                    this,
                    "غير مسموح النسبة تتعدي $returnLimit%"
                )
            } else {
                if (flag == "send") {
                    lifecycleScope.launch {
                        viewModel.orderIntent.send(
                            OrderIntent.SendOrder(
                                CreateOrderHelper().addOrder(
                                    orderType,
                                    orderNumber,
                                    customerTypePosition,
                                    customerPartySiteId,
                                    paymentTypePosition,
                                    turnOver,
                                    orderList,
                                    mItemsCardAdded,
                                    orderSourcePosition.toInt(),
                                    priceListIdPosition,
                                    priceListDescriptionPosition,
                                )
                            )
                        )
                    }
                    showLoadingDialog()
                } else if (flag == "save") {
                    //Save order pending api
                    lifecycleScope.launch {
                        viewModel.orderIntent.send(
                            OrderIntent.SaveOrderPending(
                                CreateOrderHelper().addOrder(
                                    orderType,
                                    orderNumber,
                                    customerTypePosition,
                                    customerPartySiteId,
                                    paymentTypePosition,
                                    turnOver,
                                    orderList,
                                    mItemsCardAdded,
                                    orderSourcePosition.toInt(),
                                    priceListIdPosition,
                                    priceListDescriptionPosition,
                                )
                            )
                        )
                        showLoadingDialog()
                    }
                }
            }
        }
    }

    private fun checkItemAddedBefore(): Boolean {
        for (item in mAdapterCardsProduct) {
            if (item.item_code == mReturnList[mItemPositionSelected].item_code) {
                Toast.makeText(this, "This Item Added Before", Toast.LENGTH_SHORT).show()
                return false
            }
        }
        return true
    }

    @SuppressLint("SetTextI18n")
    override fun onDeleteClick(item: CardItem) {
        mAdapterCardsProduct.remove(item)
        val returnItemId = ReturnItem(
            item.item_id,
            item.quantity
        )
        totalReturn -= item.total
        binding.totalReturn.text = totalReturn.toFloat().roundToInt().toString() + " EGP"
        mItemsCardAdded.remove(returnItemId)
        mAdapter.addProduct(mAdapterCardsProduct)
        binding.itemsCount.text =
            "${mAdapterCardsProduct.size} Items"
    }




    private fun initBackPressedDialog() {

        progressBar = SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
        progressBar.setTitleText("تنبيه!...")
            .setContentText("عند الرجوع سيتم ازالة اصناف المرتجع")
            .setConfirmText("متأكد")
            .setConfirmClickListener { sDialog ->
                sDialog.dismissWithAnimation()
                finish()
            }

            .setCancelButton(
                "رجوع"
            ) { sDialog ->
                sDialog.dismissWithAnimation()
                progressBar.dismiss()
            }
        progressBar.setCancelable(false);

    }

}
package com.akhnaton.foodvisits.ui.home.visits.order

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import cn.pedant.SweetAlert.SweetAlertDialog
import com.akhnaton.foodvisits.BuildConfig
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.data.model.order.*
import com.akhnaton.foodvisits.data.statusValue.order.OrderIntent
import com.akhnaton.foodvisits.data.statusValue.order.OrderStatus
import com.akhnaton.foodvisits.databinding.ActivityOrderBinding
import com.akhnaton.foodvisits.shared.CreateOrderHelper
import com.akhnaton.foodvisits.shared.ProgressDialogHelper
import com.akhnaton.foodvisits.shared.SharedPreferencesHelper
import com.akhnaton.foodvisits.shared.SpinnerHelper
import com.akhnaton.foodvisits.ui.home.MainActivity
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class OrderActivity : AppCompatActivity(), View.OnClickListener,
    OrderViewHolder.OnItemClickListener {
    companion object {
        private const val TAG = "OrderActivity"
    }

    private val versionName = BuildConfig.VERSION_NAME
    private lateinit var binding: ActivityOrderBinding
    private val viewModel: OrderViewModel by viewModels()
    private val mAdapter = OrderAdapter()
    private lateinit var progressBar: SweetAlertDialog
    private lateinit var loadingDialog: AlertDialog


    private var orderLimit: Int = 0
    private var returnLimit: Int = 0
    private var mItemOrdersList: List<ProductData> = ArrayList()
    private var mItemReturnList: List<ProductData> = ArrayList()
    private var mItemsOrdersNameList: MutableList<String> = ArrayList()
    private var mItemsReturnNameList: MutableList<String> = ArrayList()
    private var mItemsCardAdded: ArrayList<OrderItem> = ArrayList()
    private var mItemPositionSelected: Int = 0

    private var mBonusList: List<BonusData> = ArrayList()
    private var mBonusNameList: MutableList<String> = ArrayList()
    private var mBonusPositionSelected: String = ""

    private var mAdapterCardsProduct: MutableList<CardItem> = ArrayList()
    private var mReturnItemCardAdded: MutableList<ReturnItem> = ArrayList()

    private var categoryName = ""
    private var orderNumber = ""
    private var customerPartySiteId = ""
    private var orderType = ""
    private var customerTypePosition = ""
    private var turnOver = false
    private var visitId = ""
    private var paymentTypePosition = ""
    private var orderSourcePosition = ""
    private var orderSourceFlag = -1
    private var customerCode = ""
    private var customerName = ""
    private var totalOrder: Double = 0.0
    private var flagProduct = false
    private var isOrderSaved = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_order)

        initLoadingDialog()

        customerPartySiteId = intent.getStringExtra("customerPartySiteId").toString()
        orderType = intent.getStringExtra("orderType").toString()
        customerTypePosition = intent.getStringExtra("customerTypePosition").toString()
//      turnOver = intent.getBooleanExtra("turnOver", false)
        visitId = intent.getStringExtra("visitId").toString()
        paymentTypePosition = intent.getStringExtra("paymentTypePosition").toString()
        orderSourcePosition = intent.getStringExtra("orderSourcePosition").toString()
        orderSourceFlag = intent.getIntExtra("orderSourceFlag", -1)
        customerCode = intent.getStringExtra("customer_code").toString()
        customerName = intent.getStringExtra("customer_name").toString()
        orderNumber = intent.getStringExtra("orderNumber").toString()
        isOrderSaved = intent.getBooleanExtra("isOrderSaved", false)

        lifecycleScope.launch {

            if (isOrderSaved) {
                viewModel.orderIntent.send(
                    OrderIntent.SavedOrder(
                        appVersion = versionName,
                        apiToken = SharedPreferencesHelper.getInstance()
                            .getUserToken(),
                        orderNumber = orderNumber,
                    )
                )
                viewModel.orderIntent.send(
                    OrderIntent.GetCategories(
                        app_version = versionName,
                        api_token = SharedPreferencesHelper.getInstance()
                            .getUserToken(),
                        orderType = orderType,
                        customer_type = customerTypePosition
                    )
                )

                showLoadingDialog()

            } else {
                viewModel.orderIntent.send(
                    OrderIntent.GenerateOrderNumber(
                        versionName,
                        SharedPreferencesHelper.getInstance().getUserToken(),
                        customerPartySiteId,
                        orderType,
                        customerTypePosition,
                        paymentTypePosition,
                        visitId
                    )
                )

                showLoadingDialog()
            }
            viewModel.orderIntent.send(OrderIntent.GetOrderLimit(versionName))
        }

        binding.txtName.text = customerName
        binding.itemsSpinner.setItems(mItemsOrdersNameList.toTypedArray())
        binding.addItems.setOnClickListener(this)
        binding.sendOrder.setOnClickListener(this)
        binding.returnOrder.setOnClickListener(this)
        binding.btnSave.setOnClickListener(this)
        binding.backArrow.setOnClickListener { onBackPressed() }
        mAdapter.deleteProduct(this)
        setupRecycler()
        selectedItemProduct()
        fetchData()
    }

    private fun setSavedOrderItems(items: SavedOrder) {

        for (item in items.order_items) {
            val cardItem = CardItem(
                item.item_id,
                item.item_code,
                item.item_name,
                item.item_price,
                item.tax.toFloat(),
                item.quantity.toString(),
                item.items_price.toFloat(),
                mBonusPositionSelected,
                item.price_list_id
            )

            val orderItem = OrderItem(
                "normal",
                item.item_id,
                item.quantity.toString(),
                item.price_list_id
            )



            mAdapterCardsProduct.add(cardItem)
            mItemsCardAdded.add(orderItem)
        }
        for (item in items.return_items) {
            val orderReturn = ReturnItem(
                item.item_id,
                item.quantity.toString()
            )
            mReturnItemCardAdded.add(orderReturn)

        }
            mAdapter.addProduct(mAdapterCardsProduct)

        //Calculate Total Amount Text
        totalOrder += items.order_items_general_data.total_items_price
        binding.totalAmount.text = "${totalOrder.toFloat()} EGP"

        binding.itemsCount.text =
            "${mAdapterCardsProduct.size} Items"
    }

    private fun fetchData() {
        lifecycleScope.launch {
            viewModel.status.collect {
                when (it) {
                    OrderStatus.Idle -> Log.d(TAG, "fetchData: Idle")
                    OrderStatus.Loading -> Log.d(TAG, "fetchData: Loading")
                    is OrderStatus.GetOrderNumber -> {
                        if (it.data.status == 200) {
                            orderNumber = it.data.data.order_number
                            viewModel.orderIntent.send(
                                OrderIntent.GetCategories(
                                    app_version = versionName,
                                    api_token = SharedPreferencesHelper.getInstance()
                                        .getUserToken(),
                                    orderType = orderType,
                                    customer_type = customerTypePosition
                                )
                            )
                        } else {
                            showAlertDialog(it.data.message)
                        }
                    }

                    is OrderStatus.GetCategories -> {
                        if (it.data.status == 200) {
                            SpinnerHelper().setNormalSpinnerAdapter(
                                binding.categorySpinner,
                                it.data.data.sub_categories.toMutableList(),
                                this@OrderActivity
                            )
                            categoryName = binding.categorySpinner.selectedItem.toString()

                            viewModel.orderIntent.send(
                                OrderIntent.GetProducts(
                                    app_version = versionName,
                                    api_token = SharedPreferencesHelper.getInstance()
                                        .getUserToken(),
                                    orderType = orderType,
                                    sub_category = categoryName,
                                    customer_type = customerTypePosition.toInt(),
                                    customer_code = customerCode.toInt(),
                                    customer_party_site_id = customerPartySiteId.toInt()
                                )
                            )

                        } else {
                            Toast.makeText(
                                this@OrderActivity,
                                "something went wrong",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    is OrderStatus.GetProducts -> {
                        if (it.data.status == 200) {
                            // Items Product
                            mItemsOrdersNameList.clear()
                            mBonusNameList.clear()
                            mItemOrdersList = it.data.data.order_products
                            mItemReturnList = it.data.data.return_products
                            it.data.data.order_products.forEach { item ->
                                mItemsOrdersNameList.add(item.item_description)
                            }

                            it.data.data.return_products.forEach { returnItems ->
                                mItemsReturnNameList.add(returnItems.item_description)
                            }

                            binding.itemsSpinner.setItems(mItemsOrdersNameList.toTypedArray())

                            //Bonus Spinner
                            mBonusList = it.data.data.products_bonus

                            it.data.data.products_bonus.forEach { bonus ->
                                mBonusNameList.add(bonus.item_description)
                            }
                            SpinnerHelper().setNormalSpinnerAdapter(
                                binding.bonusSpinner,
                                mBonusNameList,
                                this@OrderActivity
                            )
                            dismissdialog()
                            flagProduct = true
                        } else {
                            Toast.makeText(
                                this@OrderActivity,
                                "something went wrong",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    is OrderStatus.SendOrder -> {
                        if (it.data.status == 400) {
                            ProgressDialogHelper().orderLimitAlert(
                                this@OrderActivity,
                                it.data.message
                            )
                            dismissdialog()
                        } else {
                            dismissdialog()
                            startActivity(Intent(this@OrderActivity, MainActivity::class.java))
                            finishAffinity()
                        }

                        Log.d(
                            TAG,
                            "fetchData: SendOrder: ${it.data.message}"
                        )
                    }

                    is OrderStatus.SaveOrderPending -> {
                        if (it.data.status == 400) {
                            ProgressDialogHelper().orderLimitAlert(
                                this@OrderActivity,
                                it.data.message
                            )
                            dismissdialog()
                        } else {
                            dismissdialog()
                            startActivity(Intent(this@OrderActivity, MainActivity::class.java))
                            finishAffinity()
                        }

                        Log.d(
                            TAG,
                            "fetchData: SaveOrder: ${it.data.message}"
                        )
                    }

                    is OrderStatus.SavedOrder -> {

                        if (it.data.status == 400) {
                            Log.d("dvjdnjvndvdvd", "onCreate: ${it.data.status}")

                            Toast.makeText(
                                this@OrderActivity,
                                "Error, ${it.data.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                            dismissdialog()
                        } else {
                            Log.d("dvjdnjvndvdvd", "onCreate: ${it.data.status}")

                            setSavedOrderItems(it.data.data)
                            dismissdialog()

                        }

                        Log.d(
                            TAG,
                            "fetchData: SavedOrders: ${it.data.message}"
                        )
                    }

                    is OrderStatus.GetOrderLimit -> {
                        returnLimit = it.data.data.order_returns_limit_percentage
                        orderLimit = it.data.data.lowest_price_order
                    }

                    is OrderStatus.Error -> {
                        Log.d("dvjdnjvndvdvd", "onCreate: ${it.error.toString()}")

                        Log.d(TAG, "fetchData: Error $it")
                        dismissdialog()

                    }
                }
            }
        }
    }

    private fun showAlertDialog(message: String) {
        progressBar = SweetAlertDialog(this@OrderActivity, SweetAlertDialog.WARNING_TYPE)
        progressBar.setTitleText("تنبيه!...")
            .setContentText(message)
            .setConfirmText("OK")
            .setConfirmClickListener { sDialog ->
                sDialog.dismissWithAnimation()
                progressBar.dismiss()
                finish()
            }
        progressBar.setCancelable(true)
        progressBar.show()
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


    private fun selectedItemProduct() {
        binding.itemsSpinner.setOnItemClickListener { position ->
            mItemPositionSelected = position
            binding.txtQuantity.text = ""
            binding.txtQuantity.text = mItemOrdersList[position].item_availability.toString()
            binding.quantityED.setText("")

        }

        binding.bonusSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                mBonusPositionSelected = mBonusList[position].item_name
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                Log.d(TAG, "onNothingSelected: ")
            }


        }

        binding.categorySpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    if (flagProduct) {

                        categoryName = binding.categorySpinner.selectedItem.toString()
                        lifecycleScope.launch {
                            viewModel.orderIntent.send(
                                OrderIntent.GetProducts(
                                    app_version = versionName,
                                    api_token = SharedPreferencesHelper.getInstance()
                                        .getUserToken(),
                                    orderType = orderType,
                                    sub_category = categoryName,
                                    customer_type = customerTypePosition.toInt(),
                                    customer_code = customerCode.toInt(),
                                    customer_party_site_id = customerPartySiteId.toInt()
                                )
                            )
                        }

                        binding.txtQuantity.text = ""
                        binding.itemsSpinner.setText("")
                        binding.quantityED.setText("")

                        showLoadingDialog()
                    }

                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    Log.d(TAG, "onNothingSelected: ")
                }
            }

    }

    private fun setupRecycler() {
        binding.recOrders.adapter = mAdapter
        binding.recOrders.apply {
            layoutManager = LinearLayoutManager(this@OrderActivity)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.sendOrder.id -> {
                validationSendOrder("send")
            }

            binding.addItems.id -> {
                validationAddItem()
            }

            binding.returnOrder.id -> {
                if (mAdapterCardsProduct.isEmpty()) {
                    Toast.makeText(this, "Select Item To return", Toast.LENGTH_SHORT).show()
                } else if (totalOrder < 200.0) {
                    Toast.makeText(
                        this,
                        "Minimum total order should be more than 200 L.E",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    startActivity(
                        Intent(this@OrderActivity, ReturnActivity::class.java)
                            .putExtra("orderType", orderType)
                            .putExtra("categoryName", categoryName)
                            .putExtra("orderNumber", orderNumber)
                            .putExtra("customerTypePosition", customerTypePosition)
                            .putExtra("customerPartySiteId", customerPartySiteId)
                            .putExtra("paymentTypePosition", paymentTypePosition)
                            .putExtra("turnOver", turnOver)
                            .putExtra("total", totalOrder)
                            .putExtra("returnLimit", returnLimit)
                            .putExtra("totalOrder", binding.totalAmount.text.toString())
                            .putExtra("customer_code", customerCode)
                            .putExtra("customer_name", customerName)
                            .putExtra("orderSourcePosition", orderSourcePosition)
                            .putExtra("orderSourceFlag", orderSourceFlag)
                            .putExtra("orderNumber", orderNumber)
                            .putExtra("isOrderSaved", isOrderSaved)
                            .putParcelableArrayListExtra("orderList", mItemsCardAdded)
                    )
                }
            }

            binding.btnSave.id -> {
                validationSendOrder("save")
            }
        }
    }


    private fun validationSendOrder(flag: String) {
        if (mAdapterCardsProduct.isEmpty()) {
            Toast.makeText(this, "Select Item To Send", Toast.LENGTH_SHORT).show()
        } else if (totalOrder < 200.0) {
            Toast.makeText(
                this,
                "Minimum total order should be more than 200 L.E",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            if (orderLimit >= totalOrder) {
                ProgressDialogHelper().orderLimitAlert(
                    this,
                    "Total order must be at least $orderLimit LE"
                )
            } else {
                if (flag == "send") {
                    //Create order api
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
                                    mItemsCardAdded,
                                    mReturnItemCardAdded,
                                    orderSourcePosition.toInt()
                                )
                            )
                        )
                        showLoadingDialog()
                    }
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
                                    mItemsCardAdded,
                                    mReturnItemCardAdded,
                                    orderSourcePosition.toInt()
                                )
                            )
                        )
                        showLoadingDialog()
                    }
                }
            }


        }
    }

    private fun validationAddItem() {
        if (binding.quantityED.text.toString().isEmpty() || binding.quantityED.text.toString()
                .toInt() <= 0
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
        } else if (orderSourceFlag == 2 &&
            binding.quantityED.text.toString().toInt() > binding.txtQuantity.text.toString().toInt()
        ) {
            binding.quantityED.error =
                "Maximum Quantity you can add is ${binding.txtQuantity.text}"
            binding.quantityED.isFocusable = true

        } else if (orderSourceFlag == 0 &&
            binding.quantityED.text.toString().toInt() <= binding.txtQuantity.text.toString()
                .toInt()
        ) {
            binding.quantityED.error =
                "Minimum Quantity you can add is ${binding.txtQuantity.text.toString().toInt() + 1}"
            binding.quantityED.isFocusable = true

        } else {
            if (checkItemAddedBefore() && mItemOrdersList.isNotEmpty()) {

                val mItem = mItemOrdersList[mItemPositionSelected]

                val orderItem = OrderItem(
                    mBonusPositionSelected,
                    mItem.item_id,
                    binding.quantityED.text.toString(),
                    mItem.item_price_list
                )
                mItemsCardAdded.add(orderItem)

                //Calculate Total Amount Text
                val total =
                    binding.quantityED.text.toString()
                        .toDouble() * (mItem.item_price + mItem.item_tax)
                totalOrder += total
                binding.totalAmount.text = "${totalOrder.toFloat()} EGP"

                val cardItem = CardItem(
                    mItem.item_id,
                    mItem.item_code,
                    mItem.item_description,
                    mItem.item_price,
                    mItem.item_tax.toFloat(),
                    binding.quantityED.text.toString(),
                    total.toFloat(),
                    mBonusPositionSelected,
                    mItem.item_price_list
                )

                mAdapterCardsProduct.add(cardItem)
                mAdapter.addProduct(mAdapterCardsProduct)
                binding.itemsCount.text =
                    "${mAdapterCardsProduct.size} Items"
            }
        }
    }

    private fun checkItemAddedBefore(): Boolean {
        for (item in mAdapterCardsProduct) {
            if (item.item_code == mItemOrdersList[mItemPositionSelected].item_code) {
                Toast.makeText(this, "This Item Added Before", Toast.LENGTH_SHORT).show()
                return false
            }
        }
        return true
    }


    @SuppressLint("SetTextI18n")
    override fun onDeleteClick(item: CardItem) {
        mAdapterCardsProduct.remove(item)
        val orderItemId = OrderItem(
            item.bonus,
            item.item_id,
            item.quantity,
            item.item_price_list
        )

        totalOrder -= item.total
        binding.totalAmount.text = totalOrder.toFloat().roundToInt().toString() + " EGP"
        mItemsCardAdded.remove(orderItemId)
        mAdapter.addProduct(mAdapterCardsProduct)
        binding.itemsCount.text =
            "${mAdapterCardsProduct.size} Items"
    }

}
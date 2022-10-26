package com.akhnaton.foodvisits.ui.home.visits.order

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
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

class OrderActivity : AppCompatActivity(), View.OnClickListener,
    OrderViewHolder.OnItemClickListener {
    companion object {
        private const val TAG = "OrderActivity"
    }

    private val versionName = BuildConfig.VERSION_NAME
    private lateinit var binding: ActivityOrderBinding
    private val viewModel: OrderViewModel by viewModels()
    private val mAdapter = OrderAdapter()

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
    private var totalOrder: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_order)

        customerPartySiteId = intent.getStringExtra("customerPartySiteId").toString()
        orderType = intent.getStringExtra("orderType").toString()
        customerTypePosition = intent.getStringExtra("customerTypePosition").toString()
//      turnOver = intent.getBooleanExtra("turnOver", false)
        visitId = intent.getStringExtra("visitId").toString()
        paymentTypePosition = intent.getStringExtra("paymentTypePosition").toString()

        lifecycleScope.launch {
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

            viewModel.orderIntent.send(OrderIntent.GetOrderLimit(versionName))
        }

        binding.itemsSpinner.setItems(mItemsOrdersNameList.toTypedArray())
        binding.addItems.setOnClickListener(this)
        binding.sendOrder.setOnClickListener(this)
        binding.returnOrder.setOnClickListener(this)
        binding.backArrow.setOnClickListener { onBackPressed() }
        mAdapter.deleteProduct(this)
        setupRecycler()
        selectedItemProduct()
        fetchData()
    }

    private fun fetchData() {
        lifecycleScope.launch {
            viewModel.status.collect {
                when (it) {
                    OrderStatus.Idle -> Log.d(TAG, "fetchData: Idle")
                    OrderStatus.Loading -> Log.d(TAG, "fetchData: Loading")
                    is OrderStatus.GetOrderNumber -> {
                        orderNumber = it.data.data.order_number
                        viewModel.orderIntent.send(
                            OrderIntent.GetCategories(
                                app_version = versionName,
                                api_token = SharedPreferencesHelper.getInstance().getUserToken(),
                                orderType = orderType
                            )
                        )
                    }
                    is OrderStatus.GetCategories -> {
                        SpinnerHelper().setNormalSpinnerAdapter(
                            binding.categorySpinner,
                            it.data.data.sub_categories.toMutableList(),
                            this@OrderActivity
                        )
                        categoryName = binding.categorySpinner.selectedItem.toString()

                        viewModel.orderIntent.send(
                            OrderIntent.GetProducts(
                                app_version = versionName,
                                api_token = SharedPreferencesHelper.getInstance().getUserToken(),
                                orderType = orderType,
                                sub_category = categoryName,
                                customer_type = customerTypePosition.toInt(),
                                customer_party_site_id = customerPartySiteId.toInt()
                            )
                        )
                    }
                    is OrderStatus.GetProducts -> {
                        // Items Product
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
                    }
                    is OrderStatus.SendOrder -> {
                        if (it.data.status == 400) {
                            ProgressDialogHelper().orderLimitAlert(
                                this@OrderActivity,
                                it.data.message
                            )
                        } else {
                            ProgressDialogHelper().showAlertProgress(
                                this@OrderActivity,
                                "Order Sending.."
                            ).hide()
                            startActivity(Intent(this@OrderActivity, MainActivity::class.java))
                        }

                        Log.d(
                            TAG,
                            "fetchData: SendOrder: ${it.data.message}"
                        )
                    }
                    is OrderStatus.GetOrderLimit -> {
                        returnLimit = it.data.data.order_returns_limit_percentage
                        orderLimit = it.data.data.lowest_price_order
                    }
                    is OrderStatus.Error -> {
                        Log.d(TAG, "fetchData: Error $it")
                        ProgressDialogHelper().showAlertProgress(
                            this@OrderActivity,
                            "Order Sending.."
                        ).hide()

                    }
                }
            }
        }
    }

    private fun selectedItemProduct() {
        binding.itemsSpinner.setOnItemClickListener { position ->
            Log.d(TAG, "selectedItemProduct: ${mItemOrdersList[position].item_description}")
            mItemPositionSelected = position
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

            override fun onNothingSelected(parent: AdapterView<*>) {
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
                if (mAdapterCardsProduct.isEmpty()) {
                    Toast.makeText(this, "Select Item To Add", Toast.LENGTH_SHORT).show()
                } else {
                    if (orderLimit >= totalOrder) {
                        ProgressDialogHelper().orderLimitAlert(
                            this,
                            "Total order must be at least $orderLimit LE"
                        )
                    } else {
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
                                        mReturnItemCardAdded
                                    )
                                )
                            )
                        }
                        ProgressDialogHelper().showAlertProgress(this, "Order Sending..").show()
                    }


                }
            }

            binding.addItems.id -> {

                if (binding.quantityED.text.toString().isEmpty()) {
                    binding.quantityED.error = "Choose Quantity"
                    binding.quantityED.isFocusable = true
                } else {
                    if (checkItemAddedBefore()) {

                        val mItem = mItemOrdersList[mItemPositionSelected]

                        val orderItem = OrderItem(
                            mBonusPositionSelected,
                            mItem.item_id,
                            binding.quantityED.text.toString()
                        )
                        mItemsCardAdded.add(orderItem)

                        //Calculate Total Amount Text
                        val total =
                            binding.quantityED.text.toString().toDouble() * mItem.item_price
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
                            mBonusPositionSelected
                        )

                        mAdapterCardsProduct.add(cardItem)
                        mAdapter.addProduct(mAdapterCardsProduct)
                        binding.itemsCount.text =
                            "${mAdapterCardsProduct.size} Items"
                    }
                }
            }

            binding.returnOrder.id -> {
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
                        .putParcelableArrayListExtra("orderList", mItemsCardAdded)
                )
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onDeleteClick(item: CardItem) {
        mAdapterCardsProduct.remove(item)
        val orderItemId = OrderItem(
            item.bonus,
            item.item_id,
            item.quantity
        )
        totalOrder -= item.item_price * item.quantity.toFloat()
        binding.totalAmount.text = totalOrder.toFloat().toString() + "EGP"
        mItemsCardAdded.remove(orderItemId)
        mAdapter.addProduct(mAdapterCardsProduct)
        binding.itemsCount.text =
            "${mAdapterCardsProduct.size} Items"
    }

    private fun checkItemAddedBefore(): Boolean {
        var check = true
        for (item in mAdapterCardsProduct) {
            if (item.item_code == mItemOrdersList[mItemPositionSelected].item_code) {
                check = false
                Toast.makeText(this, "This Item Added Before", Toast.LENGTH_SHORT).show()
            } else {
                check = true
            }
        }
        return check
    }

}
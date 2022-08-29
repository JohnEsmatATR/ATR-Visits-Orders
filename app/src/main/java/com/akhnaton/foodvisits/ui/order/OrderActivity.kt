package com.akhnaton.foodvisits.ui.order

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.akhnaton.foodvisits.BuildConfig
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.data.model.order.BonusData
import com.akhnaton.foodvisits.data.model.order.CardItem
import com.akhnaton.foodvisits.data.model.order.Item
import com.akhnaton.foodvisits.data.model.order.ProductData
import com.akhnaton.foodvisits.data.statusValue.order.OrderIntent
import com.akhnaton.foodvisits.data.statusValue.order.OrderStatus
import com.akhnaton.foodvisits.databinding.ActivityOrderBinding
import com.akhnaton.foodvisits.shared.CreateOrderHelper
import com.akhnaton.foodvisits.shared.ProgressDialog
import com.akhnaton.foodvisits.shared.SharedPreferencesHelper
import com.akhnaton.foodvisits.shared.SpinnerHelper
import com.akhnaton.foodvisits.ui.MainActivity
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
    private var mItemList: List<ProductData> = ArrayList()
    private var mItemsNameList: MutableList<String> = ArrayList()
    private var mItemsCardAdded: MutableList<Item> = ArrayList()
    private var mItemPositionSelected: Int = 0

    private var mBonusList: List<BonusData> = ArrayList()
    private var mBonusNameList: MutableList<String> = ArrayList()
    private var mBonusPositionSelected: String = ""

    private var mAdapterCardsProduct: MutableList<CardItem> = ArrayList()

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
        turnOver = intent.getBooleanExtra("turnOver", false)
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


        binding.itemsSpinner.setItems(mItemsNameList.toTypedArray())
        binding.addItems.setOnClickListener(this)
        binding.sendOrder.setOnClickListener(this)
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
                                versionName,
                                SharedPreferencesHelper.getInstance().getUserToken(),
                                orderType
                            )
                        )
                    }
                    is OrderStatus.GetCategories -> {
                        SpinnerHelper().setNormalSpinnerAdapter(
                            binding.categorySpinner,
                            it.data.data.sub_categories.toMutableList(),
                            this@OrderActivity
                        )
                        Log.d(TAG, "fetchData: ${binding.categorySpinner.selectedItem}")

                        viewModel.orderIntent.send(
                            OrderIntent.GetProducts(
                                versionName,
                                SharedPreferencesHelper.getInstance().getUserToken(),
                                orderType,
                                "Instant",
                                customerTypePosition.toInt(),
                                customerPartySiteId.toInt()
                            )
                        )
                    }

                    is OrderStatus.GetProducts -> {
                        // Items Product
                        mItemList = it.data.data.products
                        it.data.data.products.forEach { item ->
                            mItemsNameList.add(item.item_description)
                        }

                        //Bonus Spinner
                        mBonusList = it.data.data.products_bonus
                        binding.itemsSpinner.setItems(mItemsNameList.toTypedArray())
                        it.data.data.products_bonus.forEach { bonus ->
                            mBonusNameList.add(bonus.item_description)
                        }
                        SpinnerHelper().setNormalSpinnerAdapter(
                            binding.bonusSpinner,
                            mBonusNameList,
                            this@OrderActivity
                        )

                    }
                    is OrderStatus.Error -> Log.d(TAG, "fetchData: Error $it")
                    is OrderStatus.SendOrder -> {
                        ProgressDialog().showAlertProgress(this@OrderActivity).hide()
                        startActivity(Intent(this@OrderActivity, MainActivity::class.java))
                        Log.d(
                            TAG,
                            "fetchData: SendOrder: ${it.data.message}"
                        )
                    }
                    is OrderStatus.GetOrderLimit -> orderLimit = it.data.data.lowest_price_order
                }
            }
        }
    }


    private fun selectedItemProduct() {
        binding.itemsSpinner.setOnItemClickListener { position ->
            Log.d(TAG, "selectedItemProduct: ${mItemList[position].item_description}")
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
                    if (orderLimit >= totalOrder){
                        ProgressDialog().orderLimitAlert(this,"Total order must be at least $orderLimit LE")
                    }
                    
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
                                        mItemsCardAdded
                                    )
                                )
                            )
                        }
                    ProgressDialog().showAlertProgress(this).show()
                }
            }

            binding.addItems.id -> {

                if (binding.quantityED.text.toString().isEmpty()) {
                    binding.quantityED.error = "Choose Quantity"
                    binding.quantityED.isFocusable = true
                } else {
                    if (checkItemAddedBefore()) {

                        val mItem = mItemList[mItemPositionSelected]

                        val item = Item(
                            mBonusPositionSelected,
                            mItem.item_id,
                            binding.quantityED.text.toString()
                        )
                        mItemsCardAdded.add(item)

                        //Calculate Total Amount Text
                        val total =
                            binding.quantityED.text.toString().toDouble() * mItem.item_price
                        totalOrder += total
                        binding.totalAmount.text = "Total: ${totalOrder.toFloat()} LE"

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
                    }
                }
            }
        }
    }

    override fun onDeleteClick(item: CardItem) {
        mAdapterCardsProduct.remove(item)
        val itemId = Item(
            item.bonus,
            item.item_id,
            item.quantity
        )
        totalOrder -= item.item_price * item.quantity.toFloat()
        binding.totalAmount.text = totalOrder.toFloat().toString()
        mItemsCardAdded.remove(itemId)
        mAdapter.addProduct(mAdapterCardsProduct)
    }

    private fun checkItemAddedBefore(): Boolean {
        var check = true
        for (item in mAdapterCardsProduct) {
            if (item.item_code == mItemList[mItemPositionSelected].item_code) {
                check = false
                Toast.makeText(this, "This Item Added Before", Toast.LENGTH_SHORT).show()
            } else {
                check = true
            }
        }
        return check
    }

}
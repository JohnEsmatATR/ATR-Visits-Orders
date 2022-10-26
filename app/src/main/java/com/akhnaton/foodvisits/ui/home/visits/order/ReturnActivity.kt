package com.akhnaton.foodvisits.ui.home.visits.order

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
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
import com.akhnaton.foodvisits.data.statusValue.order.OrderIntent
import com.akhnaton.foodvisits.data.statusValue.order.OrderStatus
import com.akhnaton.foodvisits.databinding.ActivityReturnBinding
import com.akhnaton.foodvisits.shared.CreateOrderHelper
import com.akhnaton.foodvisits.shared.ProgressDialogHelper
import com.akhnaton.foodvisits.shared.SharedPreferencesHelper
import com.akhnaton.foodvisits.shared.SpinnerHelper
import com.akhnaton.foodvisits.ui.home.MainActivity
import kotlinx.coroutines.launch

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

    private var totalReturn = 0.0

    private var orderList: MutableList<OrderItem> = ArrayList()
    var categoryName = ""
    var orderType = ""
    var orderNumber = ""
    var customerTypePosition = ""
    var paymentTypePosition = ""
    var customerPartySiteId = ""
    var turnOver = false
    private var totalOrder = 0.0
    private var returnLimit = 0
    private lateinit var progressBar: SweetAlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_return)

        orderList = intent.getParcelableArrayListExtra<OrderItem>("orderList")!!
        categoryName = intent.getStringExtra("categoryName").toString()
        orderType = intent.getStringExtra("orderType").toString()
        orderNumber = intent.getStringExtra("orderNumber").toString()
        customerTypePosition = intent.getStringExtra("customerTypePosition").toString()
        paymentTypePosition = intent.getStringExtra("paymentTypePosition").toString()
        customerPartySiteId = intent.getStringExtra("customerPartySiteId").toString()
        turnOver = intent.getBooleanExtra("turnOver", false)
        totalOrder = intent.getDoubleExtra("total", 0.0)
        returnLimit = intent.getIntExtra("returnLimit", 0)
        binding.totalOrder.text = intent.getStringExtra("totalOrder").toString()


        lifecycleScope.launch {
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

            viewModel.orderIntent.send(
                OrderIntent.GetCategories(
                    versionName,
                    SharedPreferencesHelper.getInstance().getUserToken(),
                    orderType.toString()
                )
            )

        }
        binding.itemsSpinner.setOnItemClickListener { mItemPositionSelected = it }

        binding.addItems.setOnClickListener(this)
        binding.sendOrder.setOnClickListener(this)
        mAdapter.deleteProduct(this)
        binding.backArrow.setOnClickListener { progressBar.show() }
        fetchData()
        setupRecycler()
        initBackPressedDialog()

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
                    is OrderStatus.GetProducts -> {
                        mReturnList = it.data.data.return_products
                        mReturnList.forEach { name ->
                            mReturnNameList.add(name.item_description)
                        }

                        binding.itemsSpinner.setItems(mReturnNameList.toTypedArray())
                        Log.d(TAG, "fetchData: ${it.data.data.return_products}")
                    }
                    is OrderStatus.GetCategories -> {
                        SpinnerHelper().setNormalSpinnerAdapter(
                            binding.categorySpinner,
                            it.data.data.sub_categories.toMutableList(),
                            this@ReturnActivity
                        )
                        Log.d(TAG, "fetchData: ${it.data.data.sub_categories}")
                    }
                    is OrderStatus.SendOrder -> {
                        ProgressDialogHelper().showAlertProgress(
                            this@ReturnActivity,
                            "Return Sending.."
                        ).hide()
                        startActivity(Intent(this@ReturnActivity, MainActivity::class.java))
                    }
                    is OrderStatus.Error -> Log.d(TAG, "fetchData: Error $it")
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onClick(v: View) {
        when (v.id) {
            binding.sendOrder.id -> {
                val orderPercent: Double = totalOrder / 100.0f * returnLimit
                if (orderPercent >= totalReturn) {
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
                                    mItemsCardAdded
                                )
                            )
                        )
                    }
                    ProgressDialogHelper().showAlertProgress(this, "Return Sending..").show()
                } else {
                    ProgressDialogHelper().orderLimitAlert(
                        this,
                        "غير مسموح النسبة تتعدي $returnLimit%"
                    )
                }
            }

            binding.addItems.id -> {

                if (binding.quantityED.text.toString().isEmpty()) {
                    binding.quantityED.error = "Choose Quantity"
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
                            binding.quantityED.text.toString().toDouble() * mItem.item_price
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
                            ""
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

    private fun checkItemAddedBefore(): Boolean {
        var check = true
        for (item in mAdapterCardsProduct) {
            if (item.item_code == mReturnList[mItemPositionSelected].item_code) {
                check = false
                Toast.makeText(this, "This Item Added Before", Toast.LENGTH_SHORT).show()
            } else {
                check = true
            }
        }
        return check
    }

    @SuppressLint("SetTextI18n")
    override fun onDeleteClick(item: CardItem) {
        mAdapterCardsProduct.remove(item)
        val returnItemId = ReturnItem(
            item.item_id,
            item.quantity
        )
        totalReturn -= item.item_price * item.quantity.toFloat()
        binding.totalReturn.text = totalReturn.toFloat().toString() + "EGP"
        mItemsCardAdded.remove(returnItemId)
        mAdapter.addProduct(mAdapterCardsProduct)
        binding.itemsCount.text =
            "${mAdapterCardsProduct.size} Items"
    }


    override fun onBackPressed() {
        progressBar.show()
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
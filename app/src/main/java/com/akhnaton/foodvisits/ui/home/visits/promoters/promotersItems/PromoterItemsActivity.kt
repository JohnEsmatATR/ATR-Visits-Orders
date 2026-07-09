package com.akhnaton.foodvisits.ui.home.visits.promoters.promotersItems

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import cn.pedant.SweetAlert.SweetAlertDialog
import com.akhnaton.foodvisits.BuildConfig
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.data.model.promoter.PromoterItem
import com.akhnaton.foodvisits.data.statusValue.promoter.PromoterIntent
import com.akhnaton.foodvisits.data.statusValue.promoter.PromoterStatus
import com.akhnaton.foodvisits.databinding.ActivityPromoterItemsBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class PromoterItemsActivity : AppCompatActivity(), PromotersDataAdapter.OnSubmitListener {


    private val TAG = "PromoterItemActivity"
    private var pDialog: SweetAlertDialog? = null
    private var code: String? = null
    private var listItems: List<PromoterItem>? = emptyList()
    private var party_site: String? = null
    private var token:String? = null
    private var employee_id:String? = null
    private var customer_code: String? = null
    private var binding: ActivityPromoterItemsBinding? = null
    private var promotersDataAdapter: PromotersDataAdapter? = null
    private var sharedpreferences: SharedPreferences? = null
    private var userType = "prom"
    private val getItemsViewModel: PromoterGetItemsViewModel by viewModels()
//    private val submitStockViewModel: PromoterSubmitStockViewModel by viewModels()
    private var adapterTextViewPosition: TextView? = null
    private var mItem: PromoterItem? = null
    var txt: View? = null
    private val versionName = BuildConfig.VERSION_NAME


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_promoter_items)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {

                closePage()
            }
        })

        binding!!.btnSendStock.setOnClickListener { v -> closePage() }
        getRequiredData()
        getCurrentDayStockItems()
        searchProducts()
        fetchItems()
    }

    private fun getRequiredData() {
        code = intent.getStringExtra("cust_code")
        party_site = intent.getStringExtra("party_site")
        token = intent.getStringExtra("token")
        employee_id = intent.getStringExtra("employee_id")
        customer_code = intent.getStringExtra("customer_code")
    }

    private fun searchProducts() {
        binding!!.svSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                if (newText.isEmpty()) {
                    initAdapter(listItems!!)
                } else {
                    val newListItems: MutableList<PromoterItem> = ArrayList()
                    var j = 0
                    for (i in listItems!!) {
                        j++
                        if (i.description != null) {
                            Log.d("dvdvdvdvdvdvdvdvdvdvd", "${listItems!!.size} $j onQueryTextChange: ${i.description}")
                            if (i.description!!.contains(newText)) {
                                newListItems.add(i)
                            }
                        }
                    }
                    initAdapter(newListItems)
                }
                return false
            }
        })
    }


    fun fetchItems() {
        lifecycleScope.launch {
            getItemsViewModel.status.collect {
                when (it) {
                    is PromoterStatus.Idle -> Log.d(TAG, "fetchData1: Idle")
                    is PromoterStatus.Loading -> {
                        showDialog()
                        Log.d(TAG, "fetchData1: Loading")
                    }
                    is PromoterStatus.GetCurrentStockItems -> {
                        Log.d(TAG, "onResponse1: " + it.response.data)
                        pDialog!!.dismiss()
                        try {
                            if (it.response.data!!.isNotEmpty()) {
                                listItems = it.response.data!!
                                initAdapter(listItems!!)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    is PromoterStatus.SubmitItems -> {
                        Log.d(TAG, "onResponse: " + it.response.data!![0].message)
                        pDialog!!.dismiss()
                        if (txt != null) {
                            (txt!!.findViewById<View>(R.id.tv_item_description) as TextView).setTextColor(
                                Color.GREEN
                            )
                        }

                        if (it.response.status!! > 0) {
                            Toast.makeText(
                                this@PromoterItemsActivity,
                                it.response.data!![0].message,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                    is PromoterStatus.Error -> {
                        Log.d(TAG, "fetchData1: ${it.error}")
                        Toast.makeText(
                            this@PromoterItemsActivity,
                            "Error: ${it.error}",
                            Toast.LENGTH_LONG
                        ).show()
                        pDialog!!.dismiss()
                    }

                    else -> {}
                }
            }
        }

    }




    fun getCurrentDayStockItems() {

        lifecycleScope.launch {
            getItemsViewModel.promoterIntent.send(
                PromoterIntent.GetCurrentStockItems(
                    versionName.toDouble(),
                    token!!,
                    employee_id!!.toInt(),
                    party_site!!.toInt(),
                    customer_code!!.toInt(),
                    formatCurrentDate()!!,
                    1,
                )
            )
        }

    }

    private fun initAdapter(itemList: List<PromoterItem>) {
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding!!.rvPromotersData.layoutManager = layoutManager
        promotersDataAdapter = PromotersDataAdapter(itemList, this, party_site!!, code!!, this)
        promotersDataAdapter!!.setHasStableIds(true)
        binding!!.rvPromotersData.adapter = promotersDataAdapter
        promotersDataAdapter!!.notifyDataSetChanged()
    }

    private fun showDialog() {
        pDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
        pDialog!!.progressHelper.barColor = Color.parseColor("#A5DC86");
        pDialog!!.titleText = "Loading";
        pDialog!!.setCancelable(false);
        pDialog!!.show();
    }

    fun formatCurrentDate(): String? {
        // 14-03-2021 17:31:02
        val pattern = "dd-MM-yyyy HH:mm:ss"
        @SuppressLint("SimpleDateFormat") val simpleDateFormat = SimpleDateFormat(pattern)
        return simpleDateFormat.format(Date())
    }

    private fun closePage() {
        val pp = SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
        pp.setTitleText("إغلاق الصفحة")
            .setContentText(" هل انت متأكد من اغلاق الصفحة ؟ ")
            .setConfirmText("نعم")
            .setConfirmClickListener { sDialog: SweetAlertDialog ->
                sDialog.dismissWithAnimation()
                finish()
            }
            .setCancelButton("الغاء") { obj: SweetAlertDialog -> obj.dismissWithAnimation() }
        pp.setCancelable(false)
        pp.show()
    }




    override fun onSubmitClickListener(position: Int, item: PromoterItem?, textView: View?) {
        adapterTextViewPosition = textView!!.findViewById(R.id.tv_item_description)
        mItem = item

        lifecycleScope.launch {
            getItemsViewModel.promoterIntent.send(
                PromoterIntent.SubmitStock(
                    1.0,
                    token,
                    employee_id!!.toInt(),
                    party_site!!.toInt(),
                    formatCurrentDate(),
                    item!!.inventoryItemId!!.toInt(),
                    item!!.returnQuantity!!.toInt(),
                    item!!.quantity!!.toInt(),
                    item!!.price!!.toDouble(),
                    customer_code!!.toInt(),
                    userType
                )
            )
            txt = textView
        }

    }

    override fun attachBaseContext(newBase: Context) {
        val configuration = Configuration(newBase.resources.configuration)
        configuration.fontScale = 1.0f

        val context = newBase.createConfigurationContext(configuration)
        super.attachBaseContext(context)
    }

}
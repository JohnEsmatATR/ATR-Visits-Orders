package com.akhnaton.foodvisits.ui.home.visits.promoters.promotersItems

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import cn.pedant.SweetAlert.SweetAlertDialog
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.data.model.promoter.PromoterItem
import com.akhnaton.foodvisits.data.statusValue.promoter.PromoterIntent
import com.akhnaton.foodvisits.data.statusValue.promoter.PromoterStatus
import com.akhnaton.foodvisits.databinding.ActivityPromoterItemsBinding
import com.akhnaton.foodvisits.ui.home.visits.VisitsViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class PromoterItemsActivity : AppCompatActivity(), PromotersDataAdapter.OnSubmitListener {


    private val TAG = "PromoterItemActivity"
    private var pDialog: SweetAlertDialog? = null
    private var code: String? = null
    private var party_site: String? = null
    private var employee_id: String? = null
    private var binding: ActivityPromoterItemsBinding? = null
    private var promotersDataAdapter: PromotersDataAdapter? = null
    private var sharedpreferences: SharedPreferences? = null
    private var userType = ""
    private val viewModel: PromoterItemsViewModel by viewModels()
    private var adapterTextViewPosition: TextView? = null
    private var mItem: PromoterItem? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_promoter_items)

//        sharedpreferences = getSharedPreferences(getString(R.string.sharedpref), MODE_PRIVATE)
//        userType = sharedpreferences.getString("user_type", "")!!

//        viewModel = ViewModelProviders.of(this).get(PromoterItemsViewModel::class.java)
//
//        viewModel.getErrorGetItem().observe(this) { s ->
//            if (!s.isEmpty()) {
//                pDialog!!.dismiss()
//                finish()
//                Toast.makeText(
//                    this@PromoterItemsActivity,
//                    "Connection Error, Please try again later: $s",
//                    Toast.LENGTH_LONG
//                ).show()
//            }
//        }

//        viewModel.getCurrentStockError().observe(this) { s ->
//            if (!s.equals("End of input at line 2 column 1 path $")) {
//                pDialog!!.dismiss()
//                finish()
//                Toast.makeText(
//                    this@PromoterItemsActivity,
//                    "Connection Error, Please try again later: $s",
//                    Toast.LENGTH_LONG
//                ).show()
//            } else {
//                pDialog!!.dismiss()
//            }
//        }

//        viewModel.getErrorSubmitStock().observe(this) { s ->
//            if (!s.isEmpty()) {
//                Log.d(PromoterItemsActivity.TAG, "SubmitStockError: $s")
//                (adapterTextViewPosition!!.findViewById<View>(R.id.tv_item_description) as TextView).setTextColor(
//                    Color.RED
//                )
//                val pp =
//                    SweetAlertDialog(this@PromoterItemsActivity, SweetAlertDialog.WARNING_TYPE)
//                pp.setTitleText("تنبيه!... تأكد من اتصالك بالانترنت")
//                    .setContentText(mItem.getDescription() + "\n" + "فشل ارسال المنتج ")
//                    .setConfirmText("اعادة المحاولة")
//                    .setConfirmClickListener { sDialog: SweetAlertDialog ->
//                        sDialog.dismissWithAnimation()
//                        viewModel.submitStock(
//                            employee_id,
//                            party_site,
//                            code,
//                            formatCurrentDate(),
//                            mItem.getInventoryItemId(),
//                            mItem.getQuantity(),
//                            mItem.getReturnQuantity(),
//                            mItem.getPrice(),
//                            "1"
//                        )
//                    }
//                    .setCancelButton(
//                        "الغاء"
//                    ) { obj: SweetAlertDialog -> obj.dismissWithAnimation() }
//                pp.setCancelable(false)
//                pp.show()
//            }
//        }

        binding!!.btnSendStock.setOnClickListener { v -> closePage() }
        getItems()
        getCurrentDayStockItems()
        getRequiredData()
        searchProducts()
        fetchGetItems()
        fetchGetCurrentDayStockItems()
    }

    private fun getRequiredData() {
        code = intent.getStringExtra("cust_code")
        party_site = intent.getStringExtra("party_site")
        employee_id = intent.getStringExtra("employee_id")
    }

    private fun searchProducts() {
        binding!!.svSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                if (promotersDataAdapter != null) promotersDataAdapter!!.getFilter().filter(newText)
                return false
            }
        })
    }

    fun fetchGetItems() {
        lifecycleScope.launch{
            viewModel.status.collect {
                when (it) {
                    is PromoterStatus.Idle -> Log.d(TAG, "fetchData: Idle")
                    is PromoterStatus.Loading -> {
                        showDialog()
                        Log.d(TAG, "fetchData: Loading")
                    }
                    is PromoterStatus.GetItems -> {
                        Log.d(TAG, "onResponse: " + it.data.toString())
                        initAdapter(it.data)
                        getCurrentDayStockItems()
                        pDialog!!.dismiss()
                    }
                    is PromoterStatus.Error -> {
                        Log.d(TAG, "fetchData: ${it.error}")
                        Toast.makeText(
                            this@PromoterItemsActivity,
                            "Error: ${it.error}",
                            Toast.LENGTH_LONG
                        ).show()
                        pDialog!!.dismiss()
                    }
                }
            }
        }

    }

    private fun getItems() {
        lifecycleScope.launch {
            viewModel.promoterIntent.send(
                PromoterIntent.GetItems(
                    "1",
                    "139413",
                    "11769",
                    "3699497",
                )
            )
        }
    }

    fun fetchGetCurrentDayStockItems() {
        lifecycleScope.launch{
            viewModel.status.collect {
                when (it) {
                    is PromoterStatus.Idle -> Log.d(TAG, "fetchData1: Idle")
                    is PromoterStatus.Loading -> {
                        showDialog()
                        Log.d(TAG, "fetchData1: Loading")
                    }
                    is PromoterStatus.GetCurrentStockItems -> {
                        Log.d(TAG, "onResponse1: " + it.data.toString())
                        pDialog!!.dismiss()
                        try {
                            if (it.data.isNotEmpty()) {
                                promotersDataAdapter!!.updateQuantity(it.data)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
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
                }
            }
        }

//        viewModel.getCurrentStockMutable(
//            employee_id,
//            party_site,
//            code,
//            formatCurrentDate(),
//            userType,
//            "1"
//        ).observe(
//            this
//        ) { items ->
//            pDialog!!.dismiss()
//            try {
//                if (items.size() > 0) {
//                    promotersDataAdapter!!.updateQuantity(items)
//                }
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }
    }

    fun getCurrentDayStockItems() {

        lifecycleScope.launch {
            viewModel.promoterIntent.send(
                PromoterIntent.GetCurrentStockItems(
                    "139413",
                    "3697642",
                    "10759",
                    "21-03-2023",
                    "prom",
                    "1",
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

    override fun onBackPressed() {
        closePage()
    }


    override fun onSubmitClickListener(position: Int, item: PromoterItem?, textView: View?) {
//        adapterTextViewPosition = textView.findViewById(R.id.tv_item_description)
//        mItem = item
//        viewModel.submitStock(
//            employee_id,
//            party_site,
//            code,
//            formatCurrentDate(),
//            item.inventoryItemId,
//            item.quantity,
//            item.returnQuantity,
//            item.price,
//            "1"
//        ).observe(
//            this
//        ) { sR ->
//            Log.d(TAG, "onSubmitClickListener: $sR")
//            val res: StaticResponse = sR.get(0)
//            val MessageSuccess: String = res.getMessageS()
//            Log.d("Message", MessageSuccess)
//            val Statusm: Int = res.getStatus()
//            if (Statusm > 0) {
//                Toast.makeText(this@PromoterItemsActivity, MessageSuccess, Toast.LENGTH_LONG).show()
//                (textView.findViewById<View>(R.id.tv_item_description) as TextView).setTextColor(
//                    Color.GREEN
//                )
//            }
//        }
    }
}
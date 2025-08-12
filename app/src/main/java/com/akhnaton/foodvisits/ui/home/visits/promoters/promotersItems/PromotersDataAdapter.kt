package com.akhnaton.foodvisits.ui.home.visits.promoters.promotersItems

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.data.model.promoter.PromoterItem
import com.akhnaton.foodvisits.shared.SharedPreferencesHelper
import java.util.*
import kotlin.collections.ArrayList

class PromotersDataAdapter(
    ordersList: List<PromoterItem>,
    private val context: Context,
    party_site_id: String,
    customer_code: String,
    onSubmitListener: OnSubmitListener
) :
    RecyclerView.Adapter<PromotersDataAdapter.ViewHolder>(), Filterable {
    private var ordersList: List<PromoterItem> = ArrayList<PromoterItem>()
    private var contactListFiltered: List<PromoterItem> = ArrayList<PromoterItem>()
    private val party_site_id: String
    private val customer_code: String
    private val employee_id: String?
    private val onSubmitListener: OnSubmitListener

    init {
        this.ordersList = ordersList
        this.customer_code = customer_code
        this.party_site_id = party_site_id
        contactListFiltered = this.ordersList
        this.onSubmitListener = onSubmitListener

        employee_id = SharedPreferencesHelper.getInstance().getUserToken()
    }

    fun updateQuantity(ordersList: List<PromoterItem>) {
        for (item in contactListFiltered) {
            for (item1 in ordersList) {
                if (item.inventoryItemId.equals(item1!!.inventoryItemId)) {
                    item.quantity = item1.quantity
                    item.price = item1.price
                    item.returnQuantity = item1.returnQuantity
                    contactListFiltered[contactListFiltered.indexOf(item)].quantity = item1.quantity
                }
            }
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_promoter_item, parent, false)
        return ViewHolder(view, onSubmitListener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item: PromoterItem = contactListFiltered[position]
        holder.bindView(item)
    }

    override fun getItemCount(): Int {
        return contactListFiltered.size
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charString = charSequence.toString()
                contactListFiltered = if (charString.isEmpty()) {
                    ordersList
                } else {
                    val filteredList: MutableList<PromoterItem> = ArrayList<PromoterItem>()
                    for (row in ordersList) {
                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        if (row.description!!.lowercase(Locale.ROOT).contains(
                                charString.lowercase(
                                    Locale.getDefault()
                                )
                            )
                            || row.inventoryItemId!!.contains(charSequence)
                            || row.itemCode!!.contains(charSequence)
                        ) {
                            filteredList.add(row)
                        }
                    }
                    filteredList
                }
                val filterResults = FilterResults()
                filterResults.values = contactListFiltered
                return filterResults
            }

            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
                try {
                    contactListFiltered = filterResults.values as ArrayList<PromoterItem>
                    notifyDataSetChanged()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    inner class ViewHolder(itemView: View, private val onSubmitListener: OnSubmitListener) :
        RecyclerView.ViewHolder(itemView) {
        private val itemCodeTV = itemView.findViewById<TextView>(R.id.tv_item_code)
        private val itemDescriptionTV = itemView.findViewById<TextView>(R.id.tv_item_description)

        //private final TextView itemQuantityET = itemView.findViewById(R.id.et_item_quantity);
        private val quantityView =
            itemView.findViewById<EditText>(R.id.quantityView_default)
        private val priceET = itemView.findViewById<EditText>(R.id.et_price)
        private val returnET = itemView.findViewById<EditText>(R.id.et_return_quantity)
        private val saveButton = itemView.findViewById<Button>(R.id.btn_save_item)
        @SuppressLint("SetTextI18n")
        fun bindView(item: PromoterItem) {
            itemCodeTV.text = item.inventoryItemId + "\n" + item.itemCode
            itemDescriptionTV.setText(item.description)
            quantityView.setText(item.quantity!!.toString())
            priceET.setText(item.price)
            returnET.setText(item.returnQuantity)
            item.price = priceET.text.toString().trim { it <= ' ' }
            item.returnQuantity = returnET.text.toString().trim { it <= ' ' }
            saveButton.setOnClickListener {
                item.price = priceET.text.toString().trim { it <= ' ' }
                item.returnQuantity = returnET.text.toString().trim { it <= ' ' }
                if (item.price.toString().isEmpty()) {
                    priceET.error = "يجب ادخال السعر"
                    priceET.requestFocus()
                }
                if (item.returnQuantity.toString().isEmpty()) {
                    returnET.error = "يجب ادخال المرتجع"
                    returnET.requestFocus()
                }
                if (item.price.toString().isNotEmpty() && item.returnQuantity.toString().isNotEmpty()) {
                    onSubmitListener.onSubmitClickListener(adapterPosition, item, itemView)
                }
            }
            quantityView.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    ordersList[ordersList.indexOf(item)].quantity = s.toString()
                    Log.d("Quantity", s.toString())
                }

                override fun afterTextChanged(s: Editable?) {
                    // Update item quantity when text changes
                    s?.let { editable ->
                        val builder = AlertDialog.Builder(
                            context
                        )
                        builder.setTitle("Change Quantity")
                        val inflate: View = LayoutInflater.from(context)
                            .inflate(R.layout.custom_dialog_change_quantity, null, false)
                        val et = inflate.findViewById<View>(R.id.et_qty) as EditText
                        et.setText(quantityView.text.toString())
                        et.addTextChangedListener(object : TextWatcher {
                            override fun beforeTextChanged(
                                s: CharSequence,
                                start: Int,
                                count: Int,
                                after: Int
                            ) {
                            }

                            override fun onTextChanged(
                                s: CharSequence,
                                start: Int,
                                before: Int,
                                count: Int
                            ) {
                                if (TextUtils.isEmpty(s)) return
                            }

                            override fun afterTextChanged(s: Editable) {}
                        })
                        builder.setView(inflate)
                        builder.setPositiveButton("Change",
                            DialogInterface.OnClickListener { dialog, which ->
                                val newQuantity = et.text.toString()
                                if (TextUtils.isEmpty(newQuantity)) return@OnClickListener
                                val intNewQuantity = newQuantity.toInt()
                                quantityView.setText(intNewQuantity)
                                ordersList[ordersList.indexOf(item)].quantity = newQuantity
                                //submitCurrentStockItems(item, itemView);
                            }).setNegativeButton("Cancel", null)
                        builder.show()


                    }
                }
            })

//            quantityView.onQuantityChangeListener = object : OnQuantityChangeListener {
//                override fun onQuantityChanged(
//                    oldQuantity: Int,
//                    newQuantity: Int,
//                    programmatically: Boolean
//                ) {
//                    ordersList[ordersList.indexOf(item)].quantity = newQuantity.toString()
//                    Log.d("Quantity", newQuantity.toString())
//                    //submitCurrentStockItems(item, itemView);
//                }
//
//                override fun onLimitReached() {}
//            }
//            quantityView.setQuantityClickListener(View.OnClickListener {
//                val builder = AlertDialog.Builder(
//                    context
//                )
//                builder.setTitle("Change Quantity")
//                val inflate: View = LayoutInflater.from(context)
//                    .inflate(R.layout.custom_dialog_change_quantity, null, false)
//                val et = inflate.findViewById<View>(R.id.et_qty) as EditText
//                et.setText(quantityView.quantity.toString())
//                et.addTextChangedListener(object : TextWatcher {
//                    override fun beforeTextChanged(
//                        s: CharSequence,
//                        start: Int,
//                        count: Int,
//                        after: Int
//                    ) {
//                    }
//
//                    override fun onTextChanged(
//                        s: CharSequence,
//                        start: Int,
//                        before: Int,
//                        count: Int
//                    ) {
//                        if (TextUtils.isEmpty(s)) return
//                        if (QuantityView.isValidNumber(s.toString())) {
//                            val intNewQuantity = s.toString().toInt()
//                            // quantityView.setQuantity(intNewQuantity);
//                        } else {
//                            Toast.makeText(context, "Enter valid integer", Toast.LENGTH_LONG).show()
//                        }
//                    }
//
//                    override fun afterTextChanged(s: Editable) {}
//                })
//                builder.setView(inflate)
//                builder.setPositiveButton("Change",
//                    DialogInterface.OnClickListener { dialog, which ->
//                        val newQuantity = et.text.toString()
//                        if (TextUtils.isEmpty(newQuantity)) return@OnClickListener
//                        val intNewQuantity = newQuantity.toInt()
//                        quantityView.quantity = intNewQuantity
//                        ordersList[ordersList.indexOf(item)].quantity = newQuantity
//                        //submitCurrentStockItems(item, itemView);
//                    }).setNegativeButton("Cancel", null)
//                builder.show()
//            })
//            quantityView.isQuantityDialog
        }
    }

    interface OnSubmitListener {
        fun onSubmitClickListener(position: Int, item: PromoterItem?, textView: View?)
    }
}
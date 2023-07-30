package com.akhnaton.foodvisits.ui.home.supervisor.superShowOrders

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.data.model.supervisor.showOrder.SuperOrderStatus

class ShowOrdersAdapter(private val context: Context, var orderListener: OnOrderListener) :
    RecyclerView.Adapter<ShowOrdersAdapter.ViewHolder?>() {
    private var mList: List<SuperOrderStatus> = ArrayList()
    private var lastPosition = -1
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v: View =
            LayoutInflater.from(parent.context).inflate(R.layout.list_orders_super, parent, false)
        return ViewHolder(v, orderListener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (mList[position].rets_name == " ") {
            holder.hasReturn.visibility = View.GONE
            holder.returnText.visibility = View.GONE
        } else {
            holder.hasReturn.visibility = View.VISIBLE
            holder.hasReturn.text = mList[position].rets_name
            holder.returnText.visibility = View.VISIBLE
        }
        holder.orderMadeBy.text = mList[position].up_dat
        holder.orderType.text = mList[position].ord_type
        holder.customerName.text = mList[position].custname
        holder.salesMan.text = mList[position].salesrep
        holder.superVisor.text = mList[position].supervisor
        holder.total.text = mList[position].total
        holder.line.text = mList[position].line_name
        holder.paymentMethod.text = mList[position].pay_nm
        holder.orderNumber.text = mList[position].nomorder


        //Animation When Scroll
        val animation = AnimationUtils.loadAnimation(
            context,
            if (holder.adapterPosition > lastPosition) R.anim.up_from_bottom else R.anim.down_from_top
        )
        lastPosition = holder.adapterPosition
        holder.itemView.startAnimation(animation)
    }

    fun setList(orders: List<SuperOrderStatus>) {
        mList = orders
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    inner class ViewHolder(itemView: View, orderListener: OnOrderListener) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val orderCard: CardView
        val orderMadeBy: TextView
        val orderType: TextView
        val customerName: TextView
        val salesMan: TextView
        val superVisor: TextView
        val total: TextView
        val line: TextView
        val paymentMethod: TextView
        val hasReturn: TextView
        val returnText: TextView
        val orderNumber: TextView
        val pending: Button
        val reject: Button
        val orderListener: OnOrderListener

        init {
            orderCard = itemView.findViewById<CardView>(R.id.orderCard)
            orderMadeBy = itemView.findViewById<TextView>(R.id.orderMadeBy)
            orderType = itemView.findViewById<TextView>(R.id.orderType)
            customerName = itemView.findViewById(R.id.customerName)
            salesMan = itemView.findViewById<TextView>(R.id.salesMan)
            superVisor = itemView.findViewById<TextView>(R.id.superVisor)
            total = itemView.findViewById(R.id.total)
            line = itemView.findViewById<TextView>(R.id.line)
            paymentMethod = itemView.findViewById<TextView>(R.id.paymentMethod)
            hasReturn = itemView.findViewById<TextView>(R.id.hasReturn)
            returnText = itemView.findViewById(R.id.returnText)
            orderNumber = itemView.findViewById(R.id.orderNumber)
            pending = itemView.findViewById<Button>(R.id.pending)
            reject = itemView.findViewById<Button>(R.id.reject)
            this.orderListener = orderListener
            reject.setOnClickListener(this)
            orderCard.setOnClickListener(this)
            pending.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            if (v.id == R.id.orderCard) {
                orderListener.onOrderClickListener(mList[adapterPosition].nomorder, mList[adapterPosition].total, mList[adapterPosition].customer_id)
            }
            if (v.id == R.id.pending) {
                orderListener.onPendingClickListener(mList[adapterPosition].nomorder, mList[adapterPosition].ord_type, mList[adapterPosition].quota_flag, mList[adapterPosition].customer_id, mList[adapterPosition].total)
            }
            if (v.id == R.id.reject) {
                orderListener.onRejectClickListener(mList[adapterPosition].nomorder)
            }
        }
    }

    interface OnOrderListener {
        fun onRejectClickListener(orderNumber: String?)
        fun onPendingClickListener(orderNumber: String?, orderType: String?, quotaFlag: String?, customerId: String?, orderTotalPrice: String?)
        fun onOrderClickListener(orderNumber: String?, orderTotalPrice: String?, customerId: String?)
    }
}
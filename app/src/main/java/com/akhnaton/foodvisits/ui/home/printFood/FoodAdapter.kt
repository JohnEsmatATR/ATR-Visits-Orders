package com.akhnaton.foodvisits.ui.home.printFood

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.akhnaton.foodvisits.data.model.food.order.Food
import com.akhnaton.foodvisits.data.model.food.order.FoodData
import com.akhnaton.foodvisits.databinding.FoodListBinding
import com.akhnaton.foodvisits.shared.SharedPreferencesHelper.Companion.context
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class FoodAdapter : RecyclerView.Adapter<FoodViewHolder>() {

    private var mFood = mutableListOf<FoodData>()
    private lateinit var mContext: Context

    fun setFood(mFood: Food, context: Context) {
        this.mFood = mFood.data.toMutableList()
        this.mContext = context
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = FoodListBinding.inflate(inflater, parent, false)
        return FoodViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        holder.bind(mFood[position])
    }

    override fun getItemCount(): Int {
        return mFood.size
    }
}

class FoodViewHolder(val binding: FoodListBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(data: FoodData) {
        binding.food = data
        binding.executePendingBindings()


        itemView.setOnClickListener {

            if (data.orderSalesNumber != null) {
                val intent = Intent(context, FoodInvoiceActivity::class.java)
                intent.flags = FLAG_ACTIVITY_NEW_TASK
                intent.putExtra("foodOrder", data)
                context.startActivity(
                    intent
                )
            } else {
                Toast.makeText(context, "No Items Found", Toast.LENGTH_SHORT).show()
            }

        }
    }
}
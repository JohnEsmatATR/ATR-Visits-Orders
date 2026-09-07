package com.akhnaton.atrDistribution.ui.collectorAndAccountant.distributionPlan

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.akhnaton.foodvisits.data.model.getCustomerData.CustomerAddres
import com.akhnaton.foodvisits.data.model.getCustomerData.TEL
import com.akhnaton.foodvisits.databinding.ItemCustomerPhoneNumberBinding
import com.akhnaton.foodvisits.ui.home.phoneVisit.CustomerDataAdapter.OnItemClickListener

class CustomerPhoneNumbersAdapter(
    private val phoneNumbers: List<TEL>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<CustomerPhoneNumbersAdapter.PhoneViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PhoneViewHolder {

        val binding =
            ItemCustomerPhoneNumberBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )

        return PhoneViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: PhoneViewHolder,
        position: Int
    ) {
        holder.bind(phoneNumbers[position])
    }

    override fun getItemCount(): Int =
        phoneNumbers.size

    inner class PhoneViewHolder(
        private val binding: ItemCustomerPhoneNumberBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(phone: TEL) {

            // Change these field names according to your TEL model
            binding.tvPhoneNumber.text =
                phone.TEL ?: "-"

            binding.btnCall.setOnClickListener {
                listener.onCallClick(phone)
            }
        }
    }

    interface OnItemClickListener {
        fun onCallClick(phone: TEL)
    }
}
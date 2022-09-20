package com.akhnaton.foodvisits.ui.visits

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.akhnaton.foodvisits.data.model.CustomerVisitPlan
import com.akhnaton.foodvisits.data.model.VisitsPlan
import com.akhnaton.foodvisits.data.model.VisitsPlaneDataDumy
import com.akhnaton.foodvisits.data.model.visits.CustomerSiteData
import com.akhnaton.foodvisits.databinding.ListPlanBinding

class PlanAdapter : RecyclerView.Adapter<PlanViewHolder>() {

    private var mPlan = mutableListOf<CustomerVisitPlan>()
    private lateinit var listener: PlanViewHolder.OnSelectEmployeeClickListener

    fun setPlan(
        plan: List<CustomerVisitPlan>, listener: PlanViewHolder.OnSelectEmployeeClickListener
    ) {
        this.mPlan = plan.toMutableList()
        this.listener = listener
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlanViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListPlanBinding.inflate(inflater, parent, false)
        return PlanViewHolder(binding, listener)
    }

    override fun onBindViewHolder(holder: PlanViewHolder, position: Int) {
        holder.bind(mPlan[position])
    }

    override fun getItemCount(): Int {
        return mPlan.size
    }
}

class PlanViewHolder(val binding: ListPlanBinding, val listener: OnSelectEmployeeClickListener) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(data: CustomerVisitPlan) {
        binding.plan = data
        binding.visitCard.setOnClickListener { listener.onSelectEmployeeClickListener(data,adapterPosition) }
        binding.executePendingBindings()
    }

    interface OnSelectEmployeeClickListener {
        fun onSelectEmployeeClickListener(data: CustomerVisitPlan,position: Int)
    }
}

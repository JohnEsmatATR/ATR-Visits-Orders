package com.akhnaton.foodvisits.ui.visits

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.akhnaton.foodvisits.data.model.VisitsPlaneData
import com.akhnaton.foodvisits.databinding.ListPlanBinding

class PlanAdapter : RecyclerView.Adapter<PlanViewHolder>() {

    private var mPlan = mutableListOf<VisitsPlaneData>()

    fun setPlan(plan: List<VisitsPlaneData>) {
        this.mPlan = plan.toMutableList()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlanViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListPlanBinding.inflate(inflater, parent, false)
        return PlanViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlanViewHolder, position: Int) {
        holder.bind(mPlan[position])
    }

    override fun getItemCount(): Int {
        return mPlan.size
    }
}

class PlanViewHolder(val binding: ListPlanBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(data: VisitsPlaneData) {
        binding.plan = data
        binding.executePendingBindings()
    }

}

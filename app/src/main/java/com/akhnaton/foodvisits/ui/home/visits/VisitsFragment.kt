package com.akhnaton.foodvisits.ui.home.visits

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.akhnaton.foodvisits.BuildConfig
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.data.model.CustomerVisitPlan
import com.akhnaton.foodvisits.data.statusValue.visit.VisitsIntent
import com.akhnaton.foodvisits.data.statusValue.visit.VisitsStatus
import com.akhnaton.foodvisits.databinding.FragmentVisitsBinding
import com.akhnaton.foodvisits.shared.ConvertDate
import com.akhnaton.foodvisits.shared.ProgressDialogHelper
import com.akhnaton.foodvisits.shared.SharedPreferencesHelper
import kotlinx.coroutines.launch


class VisitsFragment : Fragment(), PlanViewHolder.OnSelectEmployeeClickListener,
    View.OnClickListener {

    companion object {
        private const val TAG = "VisitsFragment"
    }

    private val viewModel: VisitsViewModel by viewModels()
    private lateinit var binding: FragmentVisitsBinding
    private val mAdapter: PlanAdapter = PlanAdapter()
    private var mList: List<CustomerVisitPlan> = ArrayList()
    private val versionName = BuildConfig.VERSION_NAME
    private lateinit var dialog: ProgressDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(layoutInflater, R.layout.fragment_visits, container, false)

        dialog = ProgressDialogHelper().showAlertProgress(
            requireActivity(),
            "Loading..."
        )

        binding.day.text = ConvertDate.getDay()
        binding.date.text = ConvertDate.getDate()

        lifecycleScope.launch {
            viewModel.visitsIntent.send(
                VisitsIntent.GetPlan(
                    versionName, SharedPreferencesHelper.getInstance().getUserToken()
                )
            )
        }
        binding.tryAgainButtons.tryAgain.setOnClickListener(this)

        setupRecycler()
        fetchData()
        return binding.root
    }


    private fun fetchData() {
        lifecycleScope.launch {
            viewModel.status.collect {
                when (it) {
                    is VisitsStatus.Idle -> dialog.show()
                    is VisitsStatus.Loading -> dialog.show()

                    //Get Order Type
                    is VisitsStatus.Plan -> {
                        dialog.hide()
                        setAdapterData(it.data.data.customer_visit_plan)
                        mList = it.data.data.customer_visit_plan
                        binding.tryAgainButtons.root.visibility = View.GONE
                    }

                    is VisitsStatus.Error -> {
                        dialog.hide()
                        binding.tryAgainButtons.root.visibility = View.VISIBLE
                    }
                }
            }
        }
    }


    private fun setupRecycler() {
        binding.recPlan.adapter = mAdapter
        binding.recPlan.apply {
            layoutManager = LinearLayoutManager(requireActivity())
        }
    }

    private fun setAdapterData(data: List<CustomerVisitPlan>) {
        mAdapter.setPlan(data, this)
    }

    override fun onSelectEmployeeClickListener(data: CustomerVisitPlan, position: Int) {
        val tsLong = System.currentTimeMillis() / 1000

        startActivity(
            Intent(
                requireActivity(),
                VisitsDetailsActivity::class.java
            )
                .putExtra("customerPartySiteId", mList[position].customer_party_site_id)
                .putExtra("time", tsLong.toString())
                .putExtra("customerSiteData", mList[position])
                .putExtra("orderType", mList[position].customer_order_type)
                .putExtra("customerTypePosition", mList[position].customer_type)
        )
    }

    override fun onClick(p0: View?) {
        lifecycleScope.launch {
            viewModel.visitsIntent.send(
                VisitsIntent.GetPlan(
                    versionName, SharedPreferencesHelper.getInstance().getUserToken()
                )
            )
        }
    }


}


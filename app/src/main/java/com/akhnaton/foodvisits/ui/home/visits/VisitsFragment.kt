package com.akhnaton.foodvisits.ui.home.visits

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.akhnaton.foodvisits.BuildConfig
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.data.model.CustomerVisitPlan
import com.akhnaton.foodvisits.data.statusValue.visit.VisitsIntent
import com.akhnaton.foodvisits.data.statusValue.visit.VisitsStatus
import com.akhnaton.foodvisits.databinding.FragmentVisitsBinding
import com.akhnaton.foodvisits.shared.ProgressDialogHelper
import com.akhnaton.foodvisits.shared.SharedPreferencesHelper
import kotlinx.coroutines.launch


class VisitsFragment : Fragment(), PlanViewHolder.OnSelectEmployeeClickListener,
    View.OnClickListener {

    companion object {
        private const val TAG = "VisitsFragment"
    }

    private lateinit var viewModel: VisitsViewModel
    private lateinit var binding: FragmentVisitsBinding
    private val mAdapter: PlanAdapter = PlanAdapter()
    private var mList: MutableList<CustomerVisitPlan> = ArrayList()
    private val versionName = BuildConfig.VERSION_NAME
    private lateinit var dialog: AlertDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding =
            DataBindingUtil.inflate(layoutInflater, R.layout.fragment_visits, container, false)
        viewModel =
            ViewModelProvider(this, VisitsViewModelFactory(context!!))[VisitsViewModel::class.java]

        dialog = ProgressDialogHelper().showAlertProgress(
            requireContext(),
            "Loading..."
        )


        binding.tryAgainButtons.tryAgain.setOnClickListener(this)

        setupRecycler()
        setupSearchView()
        fetchData()

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        mList.clear()
        lifecycleScope.launch {
            viewModel.visitsIntent.send(
                VisitsIntent.GetPlan(
                    versionName, SharedPreferencesHelper.getInstance().getUserToken()
                )
            )
        }
        binding.searchView.setQuery("", false)
    }


    private fun fetchData() {
        lifecycleScope.launch {
            viewModel.statusVisit.collect {
                when (it) {
                    is VisitsStatus.Idle -> dialog.show()
                    is VisitsStatus.Loading -> dialog.show()

                    //Get Order Type
                    is VisitsStatus.Plan -> {
                        dialog.hide()
                        mList.clear()
                        Log.d(TAG, "fetchData: ${it.data.data.customer_visit_plan}")
                        mList.addAll(it.data.data.customer_visit_plan)

                        binding.day.text = it.data.data.day
                        binding.date.text = it.data.data.date

                        val sortedList = mList.sortedBy { it.CUSTOMER_CODE }
                        mList.clear()
                        mList.addAll(sortedList)

                        if (mList.isEmpty()) {
                            binding.txtNoData.visibility = View.VISIBLE
                        } else {
                            binding.txtNoData.visibility = View.GONE
                            setAdapterData(mList)
                        }
                        binding.tryAgainButtons.root.visibility = View.GONE
                    }

                    is VisitsStatus.Error -> {
                        dialog.hide()
                        binding.tryAgainButtons.root.visibility = View.VISIBLE
                    }

                    else -> {}
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
            Intent(requireActivity(), VisitsDetailsActivity::class.java)
                .putExtra("customerPartySiteId", data.customer_party_site_id)
                .putExtra("time", tsLong.toString())
                .putExtra("customerSiteData", data)
                .putExtra("orderType", data.customer_order_type)
                .putExtra("customerTypePosition", data.customer_type)
                .putExtra("customer_name", data.customer_name)
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
    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val filteredList = mList.filter {
                    it.customer_name.contains(newText.orEmpty(), ignoreCase = true) ||
                            it.customer_party_site_id.contains(newText.orEmpty(), ignoreCase = true) ||
                            it.customer_order_type?.contains(newText.orEmpty(), ignoreCase = true) == true
                }
                setAdapterData(filteredList)
                return true
            }
        })
    }

}


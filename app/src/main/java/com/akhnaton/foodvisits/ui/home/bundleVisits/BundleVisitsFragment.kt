package com.akhnaton.foodvisits.ui.home.bundlevisits

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.data.model.bundleVisits.BundleVisit
import com.akhnaton.foodvisits.data.model.bundleVisits.RejectReason
import com.akhnaton.foodvisits.data.statusValue.bundlevisits.BundleVisitsIntent
import com.akhnaton.foodvisits.data.statusValue.bundlevisits.BundleVisitsStatus
import com.akhnaton.foodvisits.databinding.FragmentBundleVisitsBinding
import com.akhnaton.foodvisits.shared.DialogUtils
import com.akhnaton.foodvisits.shared.ProgressDialogHelper
import com.akhnaton.foodvisits.shared.SharedPreferencesHelper
import com.akhnaton.foodvisits.ui.auth.LoginActivity2
import com.google.gson.Gson
import kotlinx.coroutines.launch
import com.akhnaton.foodvisits.data.model.bundleVisits.Data as BundleData

class BundleVisitsFragment : Fragment() {

    companion object {
        private const val TAG = "BundleVisitsFragment"
    }

    private lateinit var binding: FragmentBundleVisitsBinding
    private lateinit var dialog: AlertDialog
    private val viewModel: BundleVisitsViewModel by viewModels()

    private var allVisits = listOf<BundleVisit>()
    private var rejectReasons = listOf<RejectReason>()
    private lateinit var adapter: BundleVisitsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            layoutInflater, R.layout.fragment_bundle_visits, container, false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog = ProgressDialogHelper().showAlertProgress(requireContext(), "Loading..")
        dialog.hide()

        adapter = BundleVisitsAdapter { openDetails(it) }
        binding.rv.layoutManager = LinearLayoutManager(requireContext())
        binding.rv.adapter = adapter

        binding.swipeRefresh.setColorSchemeColors(
            ContextCompat.getColor(requireContext(), R.color.colorPrimary)
        )
        binding.swipeRefresh.setOnRefreshListener {
            binding.etSearch.text?.clear()
            loadVisits()
        }

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.etSearch.addTextChangedListener {
            filterVisits(it.toString().trim())
        }

        fetchData()
        loadVisits()
    }

    private fun loadVisits() {
        lifecycleScope.launch {
            viewModel.intent.send(BundleVisitsIntent.GetTodayVisits)
        }
    }

    private fun filterVisits(keyword: String) {
        val filtered = allVisits.filter {
            keyword.isBlank() ||
                    it.customerName.contains(keyword, true) ||
                    it.customerCode.contains(keyword, true) ||
                    it.customerAddress.contains(keyword, true)
        }
        adapter.setList(filtered)
        binding.llZeroState.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
        binding.rv.visibility = if (filtered.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun openDetails(item: BundleVisit) {
        val bundle = Bundle().apply {
            putString("visitId", item.visitDetailId)
        }
        findNavController().navigate(R.id.toBundleVisitDetails, bundle)
    }

    private fun fetchData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.status.collect {
                    when (it) {
                        is BundleVisitsStatus.Idle -> {}

                        is BundleVisitsStatus.Loading -> {
                            if (!binding.swipeRefresh.isRefreshing) dialog.show()
                        }

                        is BundleVisitsStatus.GetTodayVisits -> {
                            dialog.dismiss()
                            binding.swipeRefresh.isRefreshing = false
                            if (it.data.status == 200) {
                                val data = Gson().fromJson(it.data.data, BundleData::class.java)
                                allVisits = data.visits
                                rejectReasons = data.rejectReasons
                                binding.tvVisitsCount.text = "إجمالي الزيارات: ${allVisits.size}"
                                filterVisits(binding.etSearch.text.toString().trim())
                            } else if (it.data.status == 401) {
                                lifecycleScope.launch {
                                    viewModel.intent.send(
                                        BundleVisitsIntent.RefreshToken(
                                            SharedPreferencesHelper.getInstance().getEmployeeId(),
                                            SharedPreferencesHelper.getInstance().getUserToken()
                                        )
                                    )
                                }
                            } else {
                                DialogUtils.showResultDialog(
                                    context = requireContext(),
                                    message = it.data.message,
                                    isSuccess = false,
                                    showOkButton = true,
                                    onOk = {}
                                )
                            }
                        }

                        is BundleVisitsStatus.RefreshToken -> {
                            dialog.dismiss()
                            binding.swipeRefresh.isRefreshing = false
                            if (it.data.status == 200) {
                                val data = Gson().fromJson(
                                    it.data.data,
                                    com.akhnaton.foodvisits.data.model.refreshToken.Data::class.java
                                )
                                SharedPreferencesHelper.getInstance().saveUserToken(data.TOKEN)
                                loadVisits()
                            } else {
                                DialogUtils.showResultDialog(
                                    context = requireContext(),
                                    message = it.data.message,
                                    isSuccess = false,
                                    showOkButton = true,
                                    onOk = {
                                        SharedPreferencesHelper.getInstance().logOut()
                                        startActivity(
                                            Intent(requireContext(), LoginActivity2::class.java)
                                        )
                                        requireActivity().finishAffinity()
                                    }
                                )
                            }
                        }

                        is BundleVisitsStatus.Error -> {
                            Log.d(TAG, "fetchData: ${it.error}")
                            dialog.dismiss()
                            binding.swipeRefresh.isRefreshing = false
                        }

                        else -> {}
                    }
                }
            }
        }
    }
}
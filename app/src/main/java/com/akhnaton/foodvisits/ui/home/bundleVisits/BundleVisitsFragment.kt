package com.akhnaton.foodvisits.ui.home.bundlevisits

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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

    private enum class VisitFilter {
        ALL,
        PENDING,
        ACCEPTED,
        REJECTED
    }

    private var selectedFilter = VisitFilter.ALL

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

        handleTopBottomKeyboard()

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

        setupFilters()

        fetchData()
        loadVisits()
    }

    private fun setupFilters() {

        binding.chipAll.setOnClickListener {
            selectedFilter = VisitFilter.ALL
            updateFilterChips()
            filterVisits(binding.etSearch.text.toString().trim())
        }

        binding.chipPending.setOnClickListener {
            selectedFilter = VisitFilter.PENDING
            updateFilterChips()
            filterVisits(binding.etSearch.text.toString().trim())
        }

        binding.chipAccepted.setOnClickListener {
            selectedFilter = VisitFilter.ACCEPTED
            updateFilterChips()
            filterVisits(binding.etSearch.text.toString().trim())
        }

        binding.chipRejected.setOnClickListener {
            selectedFilter = VisitFilter.REJECTED
            updateFilterChips()
            filterVisits(binding.etSearch.text.toString().trim())
        }

        updateFilterChips()
    }

    private fun updateFilterChips() {

        binding.chipAll.isChecked =
            selectedFilter == VisitFilter.ALL

        binding.chipPending.isChecked =
            selectedFilter == VisitFilter.PENDING

        binding.chipAccepted.isChecked =
            selectedFilter == VisitFilter.ACCEPTED

        binding.chipRejected.isChecked =
            selectedFilter == VisitFilter.REJECTED

        updateChipColors()
    }

    private fun updateChipColors() {

        // All
        applyChipColors(
            chip = binding.chipAll,
            selected = selectedFilter == VisitFilter.ALL,
            selectedColor = R.color.orange,
            unselectedColor = R.color.orange_bg
        )

        // Pending
        applyChipColors(
            chip = binding.chipPending,
            selected = selectedFilter == VisitFilter.PENDING,
            selectedColor = R.color.orange,
            unselectedColor = R.color.orange_bg
        )

        // Accepted
        applyChipColors(
            chip = binding.chipAccepted,
            selected = selectedFilter == VisitFilter.ACCEPTED,
            selectedColor = R.color.green,
            unselectedColor = R.color.green_bg
        )

        // Rejected
        applyChipColors(
            chip = binding.chipRejected,
            selected = selectedFilter == VisitFilter.REJECTED,
            selectedColor = R.color.red,
            unselectedColor = R.color.red_bg
        )
    }

    private fun applyChipColors(
        chip: com.google.android.material.chip.Chip,
        selected: Boolean,
        selectedColor: Int,
        unselectedColor: Int
    ) {

        val backgroundColor = ContextCompat.getColor(
            requireContext(),
            if (selected) selectedColor else unselectedColor
        )

        val textColor = ContextCompat.getColor(
            requireContext(),
            if (selected) {
                R.color.white
            } else {
                when (selectedColor) {
                    R.color.orange ->
                        R.color.orange

                    R.color.green ->
                        R.color.green

                    R.color.red ->
                        R.color.red

                    else ->
                        R.color.black
                }
            }
        )

        chip.setChipBackgroundColor(
            android.content.res.ColorStateList.valueOf(backgroundColor)
        )

        chip.setTextColor(textColor)
    }

    private fun handleTopBottomKeyboard() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())

            view.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                maxOf(imeInsets.bottom, systemBars.bottom)
            )
            insets
        }
        ViewCompat.requestApplyInsets(binding.root)
    }

    private fun loadVisits() {
        lifecycleScope.launch {
            viewModel.intent.send(BundleVisitsIntent.GetTodayVisits)
        }
    }

    private fun filterVisits(keyword: String) {

        val filtered = allVisits.filter { visit ->

            val matchesStatus = when (selectedFilter) {
                VisitFilter.ALL -> true
                VisitFilter.PENDING -> visit.status == 0
                VisitFilter.ACCEPTED -> visit.status == 1
                VisitFilter.REJECTED -> visit.status == 2
            }

            val matchesSearch =
                keyword.isBlank() ||
                        visit.custName.contains(keyword, true) ||
                        visit.custId.contains(keyword, true) ||
                        visit.custAddress.contains(keyword, true)

            matchesStatus && matchesSearch
        }

        adapter.setList(filtered)

        binding.llZeroState.visibility =
            if (filtered.isEmpty()) View.VISIBLE else View.GONE

        binding.rv.visibility =
            if (filtered.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun openDetails(item: BundleVisit) {
        val bundle = Bundle().apply {
            putString("visitId", item.visitId)
        }
        findNavController().navigate(R.id.toBundleVisitDetails, bundle)
    }

    private fun updateFilterCounts() {

        val pendingCount =
            allVisits.count { it.status == 0 }

        val acceptedCount =
            allVisits.count { it.status == 1 }

        val rejectedCount =
            allVisits.count { it.status == 2 }

        binding.chipAll.text =
            "${context?.getString(R.string.all)} ${allVisits.size}"

        binding.chipPending.text =
            "${context?.getString(R.string.pending)} $pendingCount"

        binding.chipAccepted.text =
            "${context?.getString(R.string.done)} $acceptedCount"

        binding.chipRejected.text =
            "${context?.getString(R.string.reject)} $rejectedCount"
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
//                                binding.tvVisitsCount.text = "إجمالي الزيارات: ${allVisits.size}"
                                updateFilterCounts()
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
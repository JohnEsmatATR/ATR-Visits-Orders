package com.akhnaton.foodvisits.ui.home.visitPlan


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.data.model.pendingVisits.PendingVisitItem
import com.akhnaton.foodvisits.data.model.pendingVisits.PendingVisitsData
import com.akhnaton.foodvisits.data.statusValue.visitPlan.VisitIntent
import com.akhnaton.foodvisits.data.statusValue.visitPlan.VisitStatus
import com.akhnaton.foodvisits.databinding.FragmentPendingVisitsBinding
import com.akhnaton.foodvisits.shared.DialogUtils
import com.akhnaton.foodvisits.shared.SharedPreferencesHelper
import com.akhnaton.foodvisits.ui.auth.LoginActivity2
import com.akhnaton.foodvisits.ui.home.visitiPlan.PendingVisitsAdapter
import com.google.gson.Gson
import kotlinx.coroutines.launch

class PendingVisitsFragment : Fragment() {

    companion object {
        private const val TAG = "PendingVisitsFragment"
        private const val PAGE_SIZE = 20
        private const val DECISION_APPROVE = 1
        private const val DECISION_REJECT = 0
    }

    private var _binding: FragmentPendingVisitsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: VisitPlanViewModel by viewModels()
    private lateinit var adapter: PendingVisitsAdapter

    private var allLoadedVisits: List<PendingVisitItem> = emptyList()
    private var currentPage = 1
    private var totalRows = 0
    private var isLoadingPage = false
    private var isFirstLoad = true

    private var searchQuery: String = ""

    private var pendingRetry: (() -> Unit)? = null
    private var hasRetriedAfterRefresh = false

    private var lastActionIds: List<String> = emptyList()
    private var lastDecision: Int = DECISION_APPROVE

    private val selectedVisitIds: MutableSet<String> = mutableSetOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPendingVisitsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecycler()
        setupListeners()
        observeStatus()
        loadFirstPage()
    }

    override fun onResume() {
        super.onResume()
        if (isFirstLoad) {
            isFirstLoad = false
        } else {
            loadFirstPage()
        }
    }

    private fun loadFirstPage() {
        currentPage = 1
        allLoadedVisits = emptyList()
        selectedVisitIds.clear()
        viewModel.visitIntent.trySend(
            VisitIntent.GetPendingVisits(page = currentPage, pageSize = PAGE_SIZE, isLoadMore = false)
        )
    }

    private fun loadNextPage() {
        if (isLoadingPage) return
        if (allLoadedVisits.size >= totalRows) return
        isLoadingPage = true
        currentPage += 1
        viewModel.visitIntent.trySend(
            VisitIntent.GetPendingVisits(page = currentPage, pageSize = PAGE_SIZE, isLoadMore = true)
        )
    }

    private fun setupRecycler() {
        adapter = PendingVisitsAdapter(
            emptyList(),
            onApproveClick = { item -> approveVisits(listOf(item.ID)) },
            onRejectClick = { item -> rejectVisits(listOf(item.ID)) },
            onSelectToggle = { item -> toggleVisitSelected(item.ID) },
        )
        binding.rvPendingVisits.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@PendingVisitsFragment.adapter
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                    if (!isLoadingPage &&
                        (visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 3 &&
                        firstVisibleItemPosition >= 0
                    ) {
                        loadNextPage()
                    }
                }
            })
        }
    }

    private fun setupListeners() {
        binding.etSearchPending.doAfterTextChanged { text ->
            searchQuery = text?.toString().orEmpty()
            applyFilterAndRender()
        }

        binding.cbSelectAll.setOnClickListener {
            val filtered = getFilteredVisits()
            val allSelected = filtered.isNotEmpty() && filtered.all { selectedVisitIds.contains(it.ID) }
            if (allSelected) {
                filtered.forEach { selectedVisitIds.remove(it.ID) }
            } else {
                filtered.forEach { selectedVisitIds.add(it.ID) }
            }
            applyFilterAndRender()
        }

        binding.btnBulkApprove.setOnClickListener {
            if (selectedVisitIds.isNotEmpty()) approveVisits(selectedVisitIds.toList())
        }

        binding.btnBulkReject.setOnClickListener {
            if (selectedVisitIds.isNotEmpty()) rejectVisits(selectedVisitIds.toList())
        }
    }

    private fun toggleVisitSelected(visitId: String) {
        if (selectedVisitIds.contains(visitId)) {
            selectedVisitIds.remove(visitId)
        } else {
            selectedVisitIds.add(visitId)
        }
        updateSelectionUI()
    }

    private fun approveVisits(ids: List<String>) {
        submitDecision(ids, DECISION_APPROVE)
    }

    private fun rejectVisits(ids: List<String>) {
        submitDecision(ids, DECISION_REJECT)
    }

    private fun submitDecision(ids: List<String>, decision: Int) {
        lastActionIds = ids
        lastDecision = decision
        viewModel.visitIntent.trySend(VisitIntent.ApproveVisits(ids, decision = decision))
    }

    private fun showDecisionToast() {
        val count = lastActionIds.size
        val messageRes = if (lastDecision == DECISION_APPROVE) {
            R.string.visits_approved_toast
        } else {
            R.string.visits_rejected_toast
        }
        Toast.makeText(requireContext(), getString(messageRes, count), Toast.LENGTH_SHORT).show()
    }

    private fun sendRefreshToken() {
        lifecycleScope.launch {
            viewModel.visitIntent.send(
                VisitIntent.RefreshToken(
                    SharedPreferencesHelper.getInstance().getEmployeeId(),
                    SharedPreferencesHelper.getInstance().getUserToken()
                )
            )
        }
    }

    private fun handleResponse(
        code: Int,
        message: String?,
        retry: () -> Unit,
        onSuccess: () -> Unit
    ) {
        Log.d(TAG, "response code=$code message=$message retried=$hasRetriedAfterRefresh")
        when (code) {
            200 -> {
                hasRetriedAfterRefresh = false
                onSuccess()
            }

            401 -> {
                if (hasRetriedAfterRefresh) {
                    hasRetriedAfterRefresh = false
                    pendingRetry = null
                    showSessionExpired(message)
                } else {
                    pendingRetry = retry
                    sendRefreshToken()
                }
            }

            else -> {
                hasRetriedAfterRefresh = false
                isLoadingPage = false
                binding.progressLoading.visibility = View.GONE
                DialogUtils.showResultDialog(
                    context = requireContext(),
                    message = message.orEmpty(),
                    isSuccess = false,
                    showOkButton = true,
                )
            }
        }
    }

    private fun showSessionExpired(message: String?) {
        DialogUtils.showResultDialog(
            context = requireContext(),
            message = message.orEmpty(),
            isSuccess = false,
            showOkButton = true,
            onOk = {
                SharedPreferencesHelper.getInstance().logOut()
                startActivity(Intent(requireContext(), LoginActivity2::class.java))
                requireActivity().finishAffinity()
            }
        )
    }

    private fun observeStatus() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.status.collect { status ->
                    when (status) {
                        is VisitStatus.Loading -> {
                            binding.progressLoading.visibility = View.VISIBLE
                        }

                        is VisitStatus.LoadingMore -> {
                        }

                        is VisitStatus.GetPendingVisits -> {
                            binding.progressLoading.visibility = View.GONE
                            isLoadingPage = false

                            handleResponse(
                                code = status.response.status,
                                message = status.response.message,
                                retry = {
                                    viewModel.visitIntent.trySend(
                                        VisitIntent.GetPendingVisits(currentPage, PAGE_SIZE, status.isLoadMore)
                                    )
                                }
                            ) {
                                val pendingData = Gson().fromJson(
                                    status.response.data,
                                    PendingVisitsData::class.java
                                )
                                totalRows = pendingData.pagination.total_rows

                                allLoadedVisits = if (status.isLoadMore) {
                                    allLoadedVisits + pendingData.visits
                                } else {
                                    pendingData.visits
                                }
                                applyFilterAndRender()
                            }
                        }

                        is VisitStatus.ApproveVisits -> {
                            binding.progressLoading.visibility = View.GONE
                            handleResponse(
                                code = status.response.status,
                                message = status.response.message,
                                retry = { submitDecision(lastActionIds, lastDecision) }
                            ) {
                                showDecisionToast()
                                selectedVisitIds.clear()
                                loadFirstPage()
                            }
                        }

                        is VisitStatus.RefreshToken -> {
                            binding.progressLoading.visibility = View.GONE
                            if (status.data.status == 200) {
                                val tokenData = Gson().fromJson(
                                    status.data.data,
                                    com.akhnaton.foodvisits.data.model.refreshToken.Data::class.java
                                )
                                SharedPreferencesHelper.getInstance().saveUserToken(tokenData.TOKEN)
                                hasRetriedAfterRefresh = true
                                val retry = pendingRetry
                                pendingRetry = null
                                retry?.invoke()
                            } else {
                                pendingRetry = null
                                hasRetriedAfterRefresh = false
                                showSessionExpired(status.data.message)
                            }
                        }

                        is VisitStatus.Error -> {
                            Log.d(TAG, "observeStatus: ${status.message}")
                            binding.progressLoading.visibility = View.GONE
                            isLoadingPage = false
                            DialogUtils.showResultDialog(
                                context = requireContext(),
                                message = status.message.orEmpty(),
                                isSuccess = false,
                                showOkButton = true,
                            )
                        }

                        else -> {}
                    }
                }
            }
        }
    }

    private fun getFilteredVisits(): List<PendingVisitItem> {
        val query = searchQuery.normalizeArabic()
        if (query.isBlank()) return allLoadedVisits
        return allLoadedVisits.filter { visit ->
            visit.CUSTOMER_NAME.normalizeArabic().contains(query) ||
                    visit.LAST_NAME.normalizeArabic().contains(query) ||
                    visit.ID.contains(query)
        }
    }

    private fun applyFilterAndRender() {
        val filtered = getFilteredVisits()
        adapter.updateList(filtered)
        adapter.setSelectedIds(selectedVisitIds.toSet())

        val allSelected = filtered.isNotEmpty() && filtered.all { selectedVisitIds.contains(it.ID) }
        binding.cbSelectAll.isChecked = allSelected

        val isEmpty = filtered.isEmpty()
        binding.rvPendingVisits.visibility = if (isEmpty) View.GONE else View.VISIBLE
        binding.tvZeroState.visibility = if (isEmpty) View.VISIBLE else View.GONE

        updateSelectionUI()
    }

    private fun updateSelectionUI() {
        val count = selectedVisitIds.size
        binding.llBulkActions.visibility = if (count > 0) View.VISIBLE else View.GONE
        binding.btnBulkApprove.text = getString(R.string.bulk_approve_format, count)
        binding.btnBulkReject.text = getString(R.string.bulk_reject_format, count)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        pendingRetry = null
        _binding = null
    }

    private fun String.normalizeArabic(): String {
        return this
            .replace("أ", "ا")
            .replace("إ", "ا")
            .replace("آ", "ا")
            .replace("ة", "ه")
            .replace("ى", "ي")
            .trim()
            .lowercase()
    }
}
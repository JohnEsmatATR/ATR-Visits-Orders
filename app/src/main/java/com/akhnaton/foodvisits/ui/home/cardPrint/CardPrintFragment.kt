package com.akhnaton.foodvisits.ui.home.cardPrint

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.data.model.cardPrint.CardPrintItem
import com.akhnaton.foodvisits.data.statusValue.cardPrint.CardPrintIntent
import com.akhnaton.foodvisits.data.statusValue.cardPrint.CardPrintStatus
import com.akhnaton.foodvisits.databinding.FragmentCardPrintBinding
import com.akhnaton.foodvisits.ui.home.cardPrint.CardPrintAdapter
import com.akhnaton.foodvisits.ui.home.cardPrint.CardPrintViewModel
import com.akhnaton.foodvisits.shared.DialogUtils
import com.akhnaton.foodvisits.shared.SharedPreferencesHelper
import com.akhnaton.foodvisits.ui.auth.LoginActivity2
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class CardPrintFragment : Fragment() {

    private var _binding: FragmentCardPrintBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CardPrintViewModel by viewModels()
    private lateinit var adapter: CardPrintAdapter
    private var fullList: List<CardPrintItem> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCardPrintBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecycler()
        observeStatus()
        setupListeners()
        setupSearch()
        setupScrollToTop()

        viewModel.cardPrintIntent.trySend(CardPrintIntent.GetPrintInvoicesList)
    }

    private fun setupListeners() {
        binding.btnBackContainer.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupScrollToTop() {
        binding.cardPrintRecycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as? LinearLayoutManager ?: return
                if (layoutManager.findFirstVisibleItemPosition() > 0) {
                    binding.btnScrollTop.visibility = View.VISIBLE
                } else {
                    binding.btnScrollTop.visibility = View.GONE
                }
            }
        })

        binding.btnScrollTop.setOnClickListener {
            binding.cardPrintRecycler.smoothScrollToPosition(0)
        }
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener { text ->
            filterList(text?.toString().orEmpty())
        }
    }

    private fun filterList(query: String) {
        if (query.isBlank()) {
            renderList(fullList)
            return
        }
        val normalizedQuery = query.normalizeArabic()
        val filtered = fullList.filter {
            it.customer_name.normalizeArabic().contains(normalizedQuery) ||
                    it.order_sales_number.toString().normalizeArabic().contains(normalizedQuery) ||
                    it.order_type.normalizeArabic().contains(normalizedQuery)
        }
        renderList(filtered)
    }

    private fun renderList(list: List<CardPrintItem>) {
        if (list.isEmpty()) {
            binding.imNoData.visibility = View.VISIBLE
        } else {
            binding.imNoData.visibility = View.GONE
        }
        adapter.updateList(list)
    }

    private fun setupRecycler() {
        adapter = CardPrintAdapter(emptyList()) { item ->
            findNavController().navigate(
                R.id.toCardPrintDetails,
                Bundle().apply {
                    putString("orderSalesNumber", item.order_sales_number.toString())
                }
            )
        }
        binding.cardPrintRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@CardPrintFragment.adapter
        }
    }

    private fun observeStatus() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.status.collect { status ->
                    when (status) {
                        is CardPrintStatus.Loading -> {
                            binding.tryAgainButtons.root.visibility = View.GONE
                            binding.imNoData.visibility = View.GONE
                        }
                        is CardPrintStatus.GetPrintInvoicesList -> {
                            Log.d("WHATstatus", status.response.status.toString())
                            if (status.response.status == 401) {
                                sendRefreshToken()
                            } else {
                                fullList = status.response.data
                                renderList(fullList)
                            }
                        }
                        is CardPrintStatus.RefreshToken -> {
                            if (status.data.status == 200) {
                                val tokenData = com.google.gson.Gson().fromJson(
                                    status.data.data,
                                    com.akhnaton.foodvisits.data.model.refreshToken.Data::class.java
                                )
                                SharedPreferencesHelper.getInstance().saveUserToken(tokenData.TOKEN)
                            } else {
                                DialogUtils.showResultDialog(
                                    context = requireContext(),
                                    message = status.data.message,
                                    isSuccess = false,
                                    showOkButton = true,
                                    onOk = {
                                        SharedPreferencesHelper.getInstance().logOut()
                                        startActivity(Intent(requireContext(), LoginActivity2::class.java))
                                        requireActivity().finishAffinity()
                                    })
                            }
                        }
                        is CardPrintStatus.Error -> {
                            binding.tryAgainButtons.root.visibility = View.VISIBLE
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    private fun sendRefreshToken() {
        lifecycleScope.launch {
            viewModel.cardPrintIntent.send(
                CardPrintIntent.RefreshToken(
                    SharedPreferencesHelper.getInstance().getEmployeeId(),
                    SharedPreferencesHelper.getInstance().getUserToken()
                )
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

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
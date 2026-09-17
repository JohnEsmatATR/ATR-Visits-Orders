package com.akhnaton.foodvisits.ui.home.visitPlan

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.data.model.visitPlan.LineItem
import com.akhnaton.foodvisits.data.statusValue.visitPlan.AddVisitIntent
import com.akhnaton.foodvisits.data.statusValue.visitPlan.AddVisitStatus
import com.akhnaton.foodvisits.databinding.FragmentAddVisitPlanBinding
import kotlinx.coroutines.launch

class AddVisitPlanFragment : Fragment() {

    private var _binding: FragmentAddVisitPlanBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AddVisitPlanViewModel by viewModels()

    private var salesTypes: List<String> = emptyList()
    private var selectedSaleType: String? = null

    private var lines: List<LineItem> = emptyList()
    private var selectedLine: LineItem? = null
    private lateinit var lineAdapter: LineAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddVisitPlanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
        setupExpandableSections()
        setupRoutesRecyclerView()
        observeStatus()
        getSalesTypes()
    }

    private fun setupRoutesRecyclerView() {
        lineAdapter = LineAdapter(
            items = lines,
            getSelectedCode = { selectedLine?.LINE_CODE },
            onLineClick = { line ->
                selectedLine = line
                lineAdapter.updateList(currentFilteredList())
                updateRouteHeader()
            }
        )
        binding.rvRoutes.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRoutes.adapter = lineAdapter

        binding.etSearchRoute.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                lineAdapter.updateList(currentFilteredList(s?.toString().orEmpty()))
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun currentFilteredList(query: String = binding.etSearchRoute.text?.toString().orEmpty()): List<LineItem> {
        if (query.isBlank()) return lines
        return lines.filter {
            it.LINE_NAME.contains(query, ignoreCase = true) ||
                    it.LINE_CODE.contains(query, ignoreCase = true)
        }
    }

    private fun getSalesTypes() {
        viewModel.addVisitPlanIntent.trySend(AddVisitIntent.GetSalesTypes)
    }

    private fun getLines(saleType: String) {
        viewModel.addVisitPlanIntent.trySend(AddVisitIntent.GetLines(saleType))
    }

    private fun observeStatus() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.status.collect { status ->
                    when (status) {
                        is AddVisitStatus.Loading -> {
                            binding.progressLoading.visibility = View.VISIBLE
                        }

                        is AddVisitStatus.GetSalesTypes -> {
                            binding.progressLoading.visibility = View.GONE
                            salesTypes = status.response.data.sales_types
                            buildSaleTypeGrid()
                        }

                        is AddVisitStatus.GetLines -> {
                            binding.progressLoading.visibility = View.GONE
                            lines = status.response.data.lines
                            lineAdapter.updateList(currentFilteredList())
                        }

                        is AddVisitStatus.Error -> {
                            binding.progressLoading.visibility = View.GONE
                            Toast.makeText(
                                requireContext(),
                                status.message ?: "خطأ",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        else -> {}
                    }
                }
            }
        }
    }

    private fun buildSaleTypeGrid() {
        binding.gridSaleTypes.removeAllViews()
        binding.gridSaleTypes.columnCount = 2

        val inflater = LayoutInflater.from(requireContext())

        salesTypes.forEach { type ->
            val itemView = inflater.inflate(
                R.layout.item_sale_option_card,
                binding.gridSaleTypes,
                false
            ) as TextView

            itemView.text = type

            val isSelected = type == selectedSaleType
            if (isSelected) {
                itemView.setBackgroundResource(R.drawable.bg_sale_type_selected)
                itemView.setTextColor(Color.WHITE)
            } else {
                itemView.setBackgroundResource(R.drawable.bg_sale_type_unselected)
                itemView.setTextColor(Color.BLACK)
            }

            itemView.setOnClickListener {
                selectedSaleType = type
                buildSaleTypeGrid()
                updateSaleTypeHeader()

                // تصفير اختيار الخط القديم عشان النوع اتغير
                selectedLine = null
                lines = emptyList()
                binding.etSearchRoute.text?.clear()
                lineAdapter.updateList(emptyList())
                resetRouteHeader()

                getLines(type)
            }

            val params = GridLayout.LayoutParams()
            params.width = 0
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            params.setMargins(6, 6, 6, 6)
            itemView.layoutParams = params

            binding.gridSaleTypes.addView(itemView)
        }
    }

    private fun updateSaleTypeHeader() {
        val type = selectedSaleType ?: return
        binding.tvSaleTypeSelected.text = type
        binding.tvSaleTypeSelected.visibility = View.VISIBLE
        binding.tvStepNumberSaleType.visibility = View.GONE
        binding.ivCheckSaleType.visibility = View.VISIBLE
        binding.statusIndicatorSaleType.setCardBackgroundColor(Color.parseColor("#2ECC71"))
    }

    private fun updateRouteHeader() {
        val line = selectedLine ?: return
        binding.tvRouteSelected.text = "${line.LINE_NAME} (كود: ${line.LINE_CODE})"
        binding.tvRouteSelected.visibility = View.VISIBLE
        binding.tvStepNumberRoute.visibility = View.GONE
        binding.ivCheckRoute.visibility = View.VISIBLE
        binding.statusIndicatorRoute.setCardBackgroundColor(Color.parseColor("#2ECC71"))
    }

    private fun resetRouteHeader() {
        binding.tvRouteSelected.visibility = View.GONE
        binding.tvStepNumberRoute.visibility = View.VISIBLE
        binding.ivCheckRoute.visibility = View.GONE
        binding.statusIndicatorRoute.setCardBackgroundColor(Color.parseColor("#FFEEDD"))
    }

    private fun setupClickListeners() {

        binding.btnBackContainer.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.btnSavePlan.setOnClickListener {
            Toast.makeText(requireContext(), "تم حفظ خطة الزيارة", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupExpandableSections() {
        binding.headerSaleType.setOnClickListener {
            toggleSection(binding.contentSaleType, binding.ivChevronSaleType)
        }

        binding.headerRoute.setOnClickListener {
            toggleSection(binding.contentRoute, binding.ivChevronRoute)
        }

        binding.headerCustomers.setOnClickListener {
            toggleSection(binding.contentCustomers, binding.ivChevronCustomers)
        }
    }

    private fun toggleSection(contentView: View, chevronView: ImageView) {
        val isExpanded = contentView.visibility == View.VISIBLE

        if (isExpanded) {
            contentView.visibility = View.GONE
            chevronView.animate().rotation(0f).setDuration(200).start()
        } else {
            contentView.visibility = View.VISIBLE
            chevronView.animate().rotation(180f).setDuration(200).start()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
package com.akhnaton.foodvisits.ui.home.visitPlan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.akhnaton.foodvisits.databinding.FragmentAddVisitPlanBinding

class AddVisitPlanFragment : Fragment() {

    private var _binding: FragmentAddVisitPlanBinding? = null
    private val binding get() = _binding!!

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
    }

    private fun setupClickListeners() {

        binding.btnBackContainer.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // زر حفظ الخطة
        binding.btnSavePlan.setOnClickListener {
            Toast.makeText(requireContext(), "تم حفظ خطة الزيارة", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupExpandableSections() {
        // فتح وإغلاق قسم نوع المبيعات مع تدوير السهم
        binding.headerSaleType.setOnClickListener {
            toggleSection(binding.contentSaleType, binding.ivChevronSaleType)
        }

        // فتح وإغلاق قسم الخطوط/المسارات مع تدوير السهم
        binding.headerRoute.setOnClickListener {
            toggleSection(binding.contentRoute, binding.ivChevronRoute)
        }

        // فتح وإغلاق قسم العملاء والأيام مع تدوير السهم
        binding.headerCustomers.setOnClickListener {
            toggleSection(binding.contentCustomers, binding.ivChevronCustomers)
        }
    }

    private fun toggleSection(contentView: View, chevronView: ImageView) {
        val isExpanded = contentView.visibility == View.VISIBLE

        if (isExpanded) {
            contentView.visibility = View.GONE
            chevronView.animate()
                .rotation(0f)
                .setDuration(200)
                .start()
        } else {
            contentView.visibility = View.VISIBLE
            chevronView.animate()
                .rotation(180f)
                .setDuration(200)
                .start()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
package com.akhnaton.foodvisits.ui.home.visitPlan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.core.content.ContextCompat
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.databinding.FragmentVisitPlanBinding
import com.akhnaton.foodvisits.shared.SharedPreferencesHelper
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class VisitPlanFragment : Fragment() {

    private var _binding: FragmentVisitPlanBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVisitPlanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handleTopBottomKeyboard()

        binding.btnBackContainer.setOnClickListener { findNavController().popBackStack() }

        val isSuper = SharedPreferencesHelper.getInstance().isSuper()
        binding.llTabs.visibility = if (isSuper) View.VISIBLE else View.GONE

        if (isSuper) {
            binding.tabMyVisits.setOnClickListener { showTab(pending = false) }
            binding.tabPending.setOnClickListener { showTab(pending = true) }
        }

        val current = childFragmentManager.findFragmentById(R.id.fragment_container)
        if (current == null || !isSuper) {
            showTab(pending = false)
        } else {
            updateTabsUi(pending = current is PendingVisitsFragment)
        }
    }

    private fun showTab(pending: Boolean) {
        val current = childFragmentManager.findFragmentById(R.id.fragment_container)
        val alreadyShown = if (pending) current is PendingVisitsFragment
        else current is MyVisitsPlanFragment
        if (!alreadyShown) {
            val target: Fragment =
                if (pending) PendingVisitsFragment() else MyVisitsPlanFragment()
            childFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, target)
                .commit()
        }
        updateTabsUi(pending)
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
    }


    private fun updateTabsUi(pending: Boolean) = with(binding) {
        val ctx = requireContext()
        tabMyVisits.setBackgroundResource(if (!pending) R.drawable.bg_tab_selected else 0)
        tabMyVisits.setTextColor(ContextCompat.getColor(ctx, if (!pending) R.color.white else R.color.gray))
        tabPending.setBackgroundResource(if (pending) R.drawable.bg_tab_selected else 0)
        tabPending.setTextColor(ContextCompat.getColor(ctx, if (pending) R.color.white else R.color.gray))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
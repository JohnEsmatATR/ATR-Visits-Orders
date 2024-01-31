package com.akhnaton.foodvisits.ui.home.printFood

import android.app.ProgressDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.akhnaton.foodvisits.BuildConfig
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.data.statusValue.food.FoodIntent
import com.akhnaton.foodvisits.data.statusValue.food.FoodStatus
import com.akhnaton.foodvisits.databinding.FragmentFoodBinding
import com.akhnaton.foodvisits.shared.ProgressDialogHelper
import com.akhnaton.foodvisits.shared.SharedPreferencesHelper
import kotlinx.coroutines.launch

class FoodFragment : Fragment(), View.OnClickListener {

    private val versionName = BuildConfig.VERSION_NAME
    private val viewModel: FoodViewModel by viewModels()
    private lateinit var binding: FragmentFoodBinding
    private var mAdapter = FoodAdapter()
    private lateinit var dialog: ProgressDialog


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_food, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog = ProgressDialogHelper().showAlertProgress(
            requireActivity(),
            "Loading..."
        )

        lifecycleScope.launch {
            viewModel.foodIntent.send(
                FoodIntent.Food(versionName, SharedPreferencesHelper.getInstance().getUserToken())
            )
        }

        binding.tryAgainButtons.tryAgain.setOnClickListener(this)
        fetchData()
        setupRecycler()
    }

    private fun setupRecycler() {
        binding.foodRecycler.adapter = mAdapter
        binding.foodRecycler.apply {
            layoutManager = LinearLayoutManager(requireActivity())
        }
    }

    private fun fetchData() {
        lifecycleScope.launch {
            viewModel.status.collect {
                when (it) {
                    is FoodStatus.Idle -> dialog.show()
                    is FoodStatus.Loading -> dialog.show()
                    is FoodStatus.FoodOrders -> {
                        dialog.hide()
                        if (it.data.data.isEmpty()) {
                            binding.imNoData.visibility = View.VISIBLE
                        } else {
                            binding.imNoData.visibility = View.GONE
                            mAdapter.setFood(it.data,requireActivity())
                        }

                        binding.tryAgainButtons.root.visibility = View.GONE
                    }
                    is FoodStatus.Error -> {
                        dialog.hide()
                        binding.tryAgainButtons.root.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    override fun onClick(p0: View?) {
        lifecycleScope.launch {
            viewModel.foodIntent.send(
                FoodIntent.Food(versionName, SharedPreferencesHelper.getInstance().getUserToken())
            )
        }
    }

}
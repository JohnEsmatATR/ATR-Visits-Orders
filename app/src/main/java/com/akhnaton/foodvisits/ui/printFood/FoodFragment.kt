package com.akhnaton.foodvisits.ui.printFood

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.akhnaton.foodvisits.BuildConfig
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.data.statusValue.food.FoodIntent
import com.akhnaton.foodvisits.data.statusValue.food.FoodStatus
import com.akhnaton.foodvisits.databinding.FragmentFoodBinding
import com.akhnaton.foodvisits.shared.SharedPreferencesHelper
import kotlinx.coroutines.launch

class FoodFragment : Fragment() {

    companion object {
        private const val TAG = "FoodFragment"
    }

    private val versionName = BuildConfig.VERSION_NAME
    private val viewModel: FoodViewModel by viewModels()
    private lateinit var binding: FragmentFoodBinding
    private var mAdapter = FoodAdapter()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_food, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch {
            viewModel.foodIntent.send(
                FoodIntent.Food(versionName,SharedPreferencesHelper.getInstance().getUserToken())
            )
        }

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
                    is FoodStatus.Idle -> Log.d(TAG, "fetchData: Idle")
                    is FoodStatus.Loading -> Log.d(TAG, "fetchData: Loading")
                    is FoodStatus.FoodOrders -> {
                        Log.d(TAG, "fetchData: Orders: ${it.data}")
                        mAdapter.setFood(it.data)
                    }
                    is FoodStatus.Error -> {
                        Toast.makeText(requireActivity(), "No Data Found", Toast.LENGTH_SHORT)
                            .show()
                        Log.d(TAG, "fetchData: ${it.error.toString()}")
                    }
                }
            }
        }
    }

}
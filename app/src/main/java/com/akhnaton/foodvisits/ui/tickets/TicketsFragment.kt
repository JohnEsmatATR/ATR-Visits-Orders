package com.akhnaton.foodvisits.ui.tickets

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.data.statusValue.tickets.TicketsIntent
import com.akhnaton.foodvisits.data.statusValue.tickets.TicketsStatus
import com.akhnaton.foodvisits.databinding.FragmentTicketsBinding
import com.akhnaton.foodvisits.shared.ProgressDialog
import kotlinx.coroutines.launch

class TicketsFragment : Fragment(), View.OnClickListener {

    companion object {
        private const val TAG = "TicketsFragment"
    }

    private val viewModel: TicketsViewModel by viewModels()
    private lateinit var binding: FragmentTicketsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(layoutInflater, R.layout.fragment_tickets, container, false)

        binding.sendTicket.setOnClickListener(this)
        fetchData()
        return binding.root
    }

    private fun fetchData() {
        lifecycleScope.launch {
            viewModel.state.collect() {
                when (it) {
                    is TicketsStatus.Idle -> Log.d(TAG, "fetchData: Idle")
                    is TicketsStatus.Loading -> ProgressDialog().showProgress(requireActivity())
                        .show()
                    is TicketsStatus.SendTickets -> {
                        Log.d(TAG, "fetchData: ${it.data}")
                        binding.error.visibility = View.GONE
                        ProgressDialog().showProgress(requireActivity()).hide()
                        requireActivity().onBackPressed()
                    }
                    is TicketsStatus.Error -> {
                        binding.error.text = it.error.toString()
                        binding.error.visibility = View.VISIBLE
                        Log.d(TAG, "fetchData: ${it.error}")
                    }
                }
            }
        }
    }

    override fun onClick(p0: View?) {

        lifecycleScope.launch {
            viewModel.ticketsIntent.send(
                TicketsIntent.Tickets(
                    1,
                    binding.ticketTextEd.text.toString(),
                    "23852"
                )
            )
        }
    }


}
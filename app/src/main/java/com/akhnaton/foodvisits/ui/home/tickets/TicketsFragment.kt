package com.akhnaton.foodvisits.ui.home.tickets

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.akhnaton.foodvisits.BuildConfig
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.data.statusValue.tickets.TicketsIntent
import com.akhnaton.foodvisits.data.statusValue.tickets.TicketsStatus
import com.akhnaton.foodvisits.databinding.FragmentTicketsBinding
import com.akhnaton.foodvisits.shared.ProgressDialogHelper
import com.akhnaton.foodvisits.shared.SharedPreferencesHelper
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class TicketsFragment : Fragment(), View.OnClickListener {

    companion object {
        private const val TAG = "TicketsFragment"
    }

    private val version = BuildConfig.VERSION_NAME
    private val viewModel: TicketsViewModel by viewModels()
    private lateinit var binding: FragmentTicketsBinding
    private lateinit var dialog: ProgressDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(layoutInflater, R.layout.fragment_tickets, container, false)

        dialog = ProgressDialogHelper().showAlertProgress(requireContext(), "Loading..")

        binding.sendTicket.setOnClickListener(this)
        fetchData()
        return binding.root
    }

    private fun fetchData() {
        lifecycleScope.launch {
            viewModel.state.collect() {
                when (it) {
                    is TicketsStatus.Idle -> Log.d(TAG, "fetchData: ")
                    is TicketsStatus.Loading -> dialog.show()
                    is TicketsStatus.SendTickets -> {
                        dialog.hide()
                        binding.error.visibility = View.GONE
                        binding.ticketTextEd.text?.clear()
                        requireActivity().onBackPressed()
                        val snackbar = Snackbar.make(binding.root, "تم ارسال طلبك بنجاح", Snackbar.LENGTH_LONG)
                        snackbar.setBackgroundTint(ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark))
                        snackbar.show()

                    }

                    is TicketsStatus.Error -> {
                        dialog.hide()
//                        binding.error.text = it.error.toString()
//                        binding.error.visibility = View.VISIBLE
                        Log.d(TAG, "fetchData: ${it.error}")
                    }
                }
            }
        }
    }

    override fun onClick(p0: View?) {
        if (binding.ticketTextEd.text!!.isNotEmpty()) {

            lifecycleScope.launch {
                viewModel.ticketsIntent.send(
                    TicketsIntent.Tickets(
                        version,
                        binding.ticketTextEd.text.toString(),
                        SharedPreferencesHelper.getInstance().getUserToken()
                    )
                )
            }
        } else {
            binding.ticketTextEd.error = "يجب كتابة الرسالة اولا"
            binding.ticketTextEd.isFocusable = true
        }
    }


}
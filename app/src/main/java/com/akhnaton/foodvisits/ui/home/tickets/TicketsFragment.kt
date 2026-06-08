package com.akhnaton.foodvisits.ui.home.tickets

import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
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
import com.google.android.datatransport.runtime.scheduling.persistence.EventStoreModule_PackageNameFactory.packageName
import com.google.android.gms.common.wrappers.Wrappers.packageManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class TicketsFragment : Fragment(), View.OnClickListener {

    companion object {
        private const val TAG = "TicketsFragment"
    }

    private val version = BuildConfig.VERSION_NAME
    private val viewModel: TicketsViewModel by viewModels()
    private lateinit var binding: FragmentTicketsBinding
    private lateinit var dialog: AlertDialog

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
                    is TicketsStatus.Idle -> {
                        dialog.hide()
                    }

                    is TicketsStatus.Loading -> {
                        dialog.show()
                    }

                    is TicketsStatus.SendTickets -> {
                        dialog.hide()
                        binding.error.visibility = View.GONE
                        binding.ticketTextEd.text?.clear()
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                        val snackbar =
                            Snackbar.make(binding.root, "تم ارسال طلبك بنجاح", Snackbar.LENGTH_LONG)
                        snackbar.setBackgroundTint(
                            ContextCompat.getColor(
                                requireContext(),
                                android.R.color.holo_green_dark
                            )
                        )
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

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onClick(p0: View?) {
        var message = binding.ticketTextEd.text.toString()
        if (binding.ticketTextEd.text!!.isNotEmpty()) {
            val appName = requireContext().applicationInfo
                .loadLabel(requireContext().packageManager)
                .toString()

            val context = requireContext()

            val packageInfo =
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    context.packageManager.getPackageInfo(
                        context.packageName,
                        android.content.pm.PackageManager.PackageInfoFlags.of(0)
                    )
                } else {
                    @Suppress("DEPRECATION")
                    context.packageManager.getPackageInfo(context.packageName, 0)
                }
            val versionName = packageInfo.versionName
            val versionCode = packageInfo.longVersionCode
            val appVersion = "$versionName ($versionCode)"

            val osInfo =
                "Android ${android.os.Build.VERSION.RELEASE} (SDK ${android.os.Build.VERSION.SDK_INT})"

            val deviceModel = "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}"

            val formattedMessage = message.replace("\n", "<br>")

            val finalMessage = """
                $formattedMessage<br><br>
                --- Support Info ---<br>
                App: $appName<br>
                Version: $appVersion<br>
                OS: $osInfo<br>
                Device: $deviceModel<br>
            """.trimIndent()

            message = finalMessage
            Log.d("WHAT", message)
            lifecycleScope.launch {
                viewModel.ticketsIntent.send(
                    TicketsIntent.Tickets(
                        version,
                        message,
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
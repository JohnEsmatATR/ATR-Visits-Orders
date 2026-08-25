package com.akhnaton.foodvisits.ui.home.tickets

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import com.akhnaton.foodvisits.BuildConfig
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.data.model.getStartOrderData.Data
import com.akhnaton.foodvisits.data.statusValue.tickets.TicketsIntent
import com.akhnaton.foodvisits.data.statusValue.tickets.TicketsStatus
import com.akhnaton.foodvisits.databinding.FragmentTicketsBinding
import com.akhnaton.foodvisits.shared.DialogUtils
import com.akhnaton.foodvisits.shared.ProgressDialogHelper
import com.akhnaton.foodvisits.shared.SharedPreferencesHelper
import com.akhnaton.foodvisits.shared.getMessage
import com.akhnaton.foodvisits.shared.toMultipart
import com.akhnaton.foodvisits.ui.auth.LoginActivity2
import com.akhnaton.foodvisits.ui.home.MainActivity
import com.akhnaton.foodvisits.ui.home.phoneVisit.Card3Adapter
import com.google.android.datatransport.runtime.scheduling.persistence.EventStoreModule_PackageNameFactory.packageName
import com.google.android.gms.common.wrappers.Wrappers.packageManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import kotlinx.coroutines.launch
import okhttp3.RequestBody.Companion.toRequestBody

class TicketsFragment : Fragment(), View.OnClickListener {

    companion object {
        private const val TAG = "TicketsFragment"
    }

    private val version = BuildConfig.VERSION_NAME
    private val viewModel: TicketsViewModel by viewModels()
    private lateinit var binding: FragmentTicketsBinding
    private lateinit var dialog: AlertDialog
    private var selectedUsers: String = ""

    private lateinit var cardAdapter: Card4Adapter
    private val selectedEmails = mutableListOf<String>()

    private lateinit var attachmentAdapter: AttachmentAdapter
    private val attachments = mutableListOf<Uri>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(layoutInflater, R.layout.fragment_tickets, container, false)

        setupKeyboardInsets()

        dialog = ProgressDialogHelper().showAlertProgress(requireContext(), "Loading..")

        MainActivity.binding.navView2.visibility = View.GONE

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnSendTicket.setOnClickListener {
            createTicket()
        }
//        binding.btnSendTicket.setOnClickListener(this)

        cardAdapter = Card4Adapter(
            object : Card4Adapter.OnItemClickListener {
                override fun onClose(item: String) {
                    selectedEmails.remove(item)
                    selectedUsers = selectedEmails.joinToString(";")
                    cardAdapter.setList(selectedEmails)
                }
            }
        )

        binding.rvCCs.layoutManager =
            GridLayoutManager(requireContext(), 2)

        binding.rvCCs.adapter = cardAdapter
        binding.rvCCs.itemAnimator = DefaultItemAnimator()

        val pickAttachments =
            registerForActivityResult(
                ActivityResultContracts.OpenMultipleDocuments()
            ) { uris ->

                uris.forEach {

                    requireContext().contentResolver.takePersistableUriPermission(
                        it,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )

                    if (!attachments.contains(it))
                        attachments.add(it)
                }

                attachmentAdapter.setList(attachments)
            }

        attachmentAdapter = AttachmentAdapter(

            object : AttachmentAdapter.OnItemClickListener {

                override fun onRemove(item: Uri) {

                    attachments.remove(item)

                    attachmentAdapter.setList(attachments)
                }
            }
        )

        binding.rvAttachments.layoutManager =
            GridLayoutManager(requireContext(), 2)

        binding.rvAttachments.adapter = attachmentAdapter

        binding.llAddAttachment.setOnClickListener {

            pickAttachments.launch(
                arrayOf(
                    "image/*",
                    "application/pdf",
                    "application/msword",
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                    "text/plain"
                )
            )
        }

        getAllUsers()
        fetchData()
        return binding.root
    }

    private fun getAllUsers() {
        lifecycleScope.launch {
            viewModel.ticketsIntent.send(
                TicketsIntent.GetAllUsers
            )
        }
    }

    private fun createTicket() {
        lifecycleScope.launch {
            viewModel.ticketsIntent.send(
                TicketsIntent.CreateTicket(
                    binding.etPhone.text.toString().toRequestBody(),
                    selectedUsers.toRequestBody(),
                    binding.etSubtitle.text.toString().toRequestBody(),
                    binding.etDescription.text.toString().toRequestBody(),
                    attachments.map { uri ->
                        uri.toMultipart(requireContext())
                    }
                )
            )
        }
    }

    private fun fetchData() {
        lifecycleScope.launch {
            viewModel.state.collect {
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
//                        binding.etTitle.text?.clear()
                        binding.etDescription.text?.clear()
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

                    is TicketsStatus.GetAllUsers -> {
                        dialog.hide()
                        if (it.data.status == 200) {
                            val users = it.data.data

                            val adapter = ArrayAdapter(
                                requireContext(),
                                android.R.layout.simple_dropdown_item_1line,
                                users.map { user -> user.EMAIL_ADDRESS }
                            )

                            binding.etCC.setAdapter(adapter)

                            binding.etCC.setOnItemClickListener { parent, _, position, _ ->

                                val selectedEmail = parent.getItemAtPosition(position) as String

                                val user = users.firstOrNull { user ->
                                    user.EMAIL_ADDRESS == selectedEmail
                                } ?: return@setOnItemClickListener

                                Log.d("WHATcc", user.EMAIL_ADDRESS)

                                if (!selectedEmails.contains(user.EMAIL_ADDRESS)) {
                                    selectedEmails.add(user.EMAIL_ADDRESS)
                                }

                                selectedUsers = selectedEmails.joinToString(";")

                                binding.etCC.setText("")

                                cardAdapter.setList(selectedEmails)
                            }

                        } else {
                            DialogUtils.showResultDialog(
                                context = requireContext(),
                                message = it.data.message,
                                isSuccess = false,
                                showOkButton = true,
                                onOk = {
                                    SharedPreferencesHelper.getInstance()
                                        .logOut()
                                    startActivity(
                                        Intent(
                                            requireContext(),
                                            LoginActivity2::class.java
                                        )
                                    )
                                    requireActivity().finishAffinity()
                                }
                            )
                        }
                    }

                    is TicketsStatus.CreateTicket -> {
                        dialog.hide()
                        if (it.data.status == 200) {
                            val data = Gson().fromJson(
                                it.data.data, Data::class.java
                            )
                            DialogUtils.showResultDialog(
                                context = requireContext(),
                                message = it.data.message.getMessage(),
                                isSuccess = true,
                                showOkButton = true,
                                onOk = {
                                    findNavController().popBackStack()
                                }
                            )
                        } else if (it.data.status == 400) {
                            DialogUtils.showResultDialog(
                                context = requireContext(),
                                message = it.data.message.getMessage(),
                                isSuccess = false,
                                showOkButton = true,
                            )
                        } else {
                            DialogUtils.showResultDialog(
                                context = requireContext(),
                                message = it.data.message.getMessage(),
                                isSuccess = false,
                                showOkButton = true,
                                onOk = {
                                    SharedPreferencesHelper.getInstance()
                                        .logOut()
                                    startActivity(
                                        Intent(
                                            requireContext(),
                                            LoginActivity2::class.java
                                        )
                                    )
                                    requireActivity().finishAffinity()
                                }
                            )
                        }
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

//    private fun setRecycler1(
//        users: String
//    ) {
//
//        var list = users.split(";")
//        val adapter =
//            Card4Adapter(
//                object : Card4Adapter.OnItemClickListener {
//
//                    override fun onClose(item: String) {
//
//                        Log.d("WHATclick", "HII CLICK")
//
//                    }
//                }
//            )
//
//        adapter.setList(list)
//
//        binding.rvCCs.layoutManager =
//            GridLayoutManager(
//                requireContext(),
//                2
//            )
//
//        binding.rvCCs.adapter = adapter
//        binding.rvCCs.itemAnimator = DefaultItemAnimator()
//    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onClick(p0: View?) {
        var message = binding.etDescription.text.toString()
        if (binding.etDescription.text!!.isNotEmpty()) {
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
            binding.error.error = "يجب كتابة الرسالة اولا"
            binding.error.isFocusable = true
        }
    }

    private fun setupKeyboardInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val imeInsets = insets.getInsets(
                WindowInsetsCompat.Type.ime()
            )
            val systemBars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
            )
            view.setPadding(
                view.paddingLeft,
                systemBars.top,
                view.paddingRight,
                maxOf(
                    imeInsets.bottom,
                    systemBars.bottom
                )
            )
            insets
        }
    }

}
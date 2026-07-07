package com.akhnaton.foodvisits.ui.home.more

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.akhnaton.foodvisits.BuildConfig
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.databinding.FragmentInvoiceBinding
import com.akhnaton.foodvisits.databinding.FragmentMoreBinding
import com.akhnaton.foodvisits.shared.DialogUtils
import com.akhnaton.foodvisits.shared.SharedPreferencesHelper
import com.akhnaton.foodvisits.ui.auth.LoginActivity2
import com.akhnaton.foodvisits.ui.home.MainActivity
import com.akhnaton.foodvisits.ui.home.WebOrderActivity
import com.akhnaton.foodvisits.ui.home.order.Order2ViewModel
import kotlin.getValue

class MoreFragment : Fragment() {

    companion object {
        private const val TAG = "MoreFragment"
    }

    private lateinit var binding: FragmentMoreBinding
    private lateinit var dialog: AlertDialog

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvName.setText(SharedPreferencesHelper.getInstance().getUsername())
        binding.tvVersionName.setText("V ${BuildConfig.VERSION_NAME}")

        MainActivity.binding.navView2.visibility = View.VISIBLE

        binding.cardSupport.setOnClickListener {
            findNavController().navigate(
                R.id.toTickets
            )
        }

        binding.cardPrint.setOnClickListener {
            findNavController().navigate(
                R.id.toPrint
            )
        }

        binding.cardOrders.setOnClickListener {
            requireContext().startActivity(Intent(requireActivity(), WebOrderActivity::class.java))
        }
        binding.btnLogout.setOnClickListener {
            DialogUtils.showResultDialog(
                context = requireContext(),
                message = "هل انت متأكد انك تريد تسجيل الخروج ؟",
                isSuccess = false,
                showYesNoButtons = true,
                onYes = {
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            layoutInflater, R.layout.fragment_more, container, false
        )
        return binding.root
    }

}
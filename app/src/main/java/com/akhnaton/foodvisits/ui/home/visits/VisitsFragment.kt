package com.akhnaton.foodvisits.ui.home.visits

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.akhnaton.foodvisits.BuildConfig
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.data.db.VisitDatabase
import com.akhnaton.foodvisits.data.model.CustomerVisitPlan
import com.akhnaton.foodvisits.data.statusValue.visit.VisitsIntent
import com.akhnaton.foodvisits.data.statusValue.visit.VisitsStatus
import com.akhnaton.foodvisits.databinding.FragmentVisitsBinding
import com.akhnaton.foodvisits.shared.ProgressDialogHelper
import com.akhnaton.foodvisits.shared.SharedPreferencesHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class VisitsFragment : Fragment(), PlanViewHolder.OnSelectEmployeeClickListener,
    View.OnClickListener {

    companion object {
        private const val TAG = "VisitsFragment"
    }

    private lateinit var viewModel: VisitsViewModel
    private lateinit var binding: FragmentVisitsBinding
    private val mAdapter: PlanAdapter = PlanAdapter()
    private var mList: MutableList<CustomerVisitPlan> = ArrayList()
    private val versionName = BuildConfig.VERSION_NAME
    private lateinit var dialog: AlertDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding =
            DataBindingUtil.inflate(layoutInflater, R.layout.fragment_visits, container, false)
        viewModel =
            ViewModelProvider(this, VisitsViewModelFactory(context!!))[VisitsViewModel::class.java]

        dialog = ProgressDialogHelper().showAlertProgress(
            requireContext(),
            "Loading..."
        )


        binding.tryAgainButtons.tryAgain.setOnClickListener(this)

        setupRecycler()
        setupSearchView()
        fetchData()


        return binding.root
    }

    override fun onResume() {
        super.onResume()
        mList.clear()
        showTextWhenHaveVisitActive()
        lifecycleScope.launch {
            viewModel.visitsIntent.send(
                VisitsIntent.GetPlan(
                    versionName, SharedPreferencesHelper.getInstance().getUserToken()
                )
            )
        }
        binding.searchView.setQuery("", false)
    }
    private fun showTextWhenHaveVisitActive() {
        lifecycleScope.launch {
            val db = VisitDatabase.getDatabase(requireContext())
            val dao = db.visitTimerDao()
            val activeVisit = dao.getActiveVisit()

            if (activeVisit != null) {
                binding.layoutActiveVisit.visibility = View.VISIBLE
                binding.ivCopyName.visibility = View.VISIBLE

                val fullText = "يوجد زياره مفتوحه الان للعميل :  ${activeVisit.name}"
                val nameStart = fullText.indexOf(activeVisit.name)
                val nameEnd = nameStart + activeVisit.name.length

                val spannable = SpannableString(fullText)
                val redColor = ContextCompat.getColor(requireContext(), android.R.color.holo_red_light)
                spannable.setSpan(
                    ForegroundColorSpan(redColor),
                    nameStart,
                    nameEnd,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                spannable.setSpan(
                    StyleSpan(Typeface.BOLD),
                    nameStart,
                    nameEnd,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                binding.textView4.text = spannable


                binding.ivCopyName.setOnClickListener {
                    val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("CustomerName", activeVisit.name)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(requireContext(), "تم نسخ اسم العميل", Toast.LENGTH_SHORT).show()
                }

            } else {
                binding.layoutActiveVisit.visibility = View.GONE
            }
        }
    }



    private fun fetchData() {
        lifecycleScope.launch {
            viewModel.statusVisit.collect {
                when (it) {
                    is VisitsStatus.Idle -> dialog.show()
                    is VisitsStatus.Loading -> dialog.show()

                    //Get Order Type
                    is VisitsStatus.Plan -> {
                        dialog.hide()
                        mList.clear()
                        Log.d(TAG, "fetchData: ${it.data.data.customer_visit_plan}")
                        mList.addAll(it.data.data.customer_visit_plan)

                        binding.day.text = it.data.data.day
                        binding.date.text = it.data.data.date

                        val sortedList = mList.sortedBy { it.CUSTOMER_CODE }
                        mList.clear()
                        mList.addAll(sortedList)

                        if (mList.isEmpty()) {
                            binding.txtNoData.visibility = View.VISIBLE
                        } else {
                            binding.txtNoData.visibility = View.GONE
                            setAdapterData(mList)
                        }
                        binding.tryAgainButtons.root.visibility = View.GONE
                    }

                    is VisitsStatus.Error -> {
                        dialog.hide()
                        binding.tryAgainButtons.root.visibility = View.VISIBLE
                    }

                    else -> {}
                }
            }
        }
    }


    private fun setupRecycler() {
        binding.recPlan.adapter = mAdapter
        binding.recPlan.apply {
            layoutManager = LinearLayoutManager(requireActivity())
        }
    }

    private fun setAdapterData(data: List<CustomerVisitPlan>) {
        mAdapter.setPlan(data, this)
    }

    override fun onSelectEmployeeClickListener(data: CustomerVisitPlan, position: Int) {
        val tsLong = System.currentTimeMillis() / 1000

        lifecycleScope.launch {
            val db = VisitDatabase.getDatabase(requireContext())
            val dao = db.visitTimerDao()

            val activeVisit = dao.getActiveVisit()

            if (activeVisit != null) {
                if (activeVisit.customerPartySiteId == data.customer_party_site_id) {

                    startActivity(
                        Intent(requireActivity(), VisitsDetailsActivity::class.java)
                            .putExtra("customerPartySiteId", data.customer_party_site_id)
                            .putExtra("time", tsLong.toString())
                            .putExtra("customerSiteData", data)
                            .putExtra("orderType", data.customer_order_type)
                            .putExtra("customerTypePosition", data.customer_type)
                            .putExtra("customer_name", data.customer_name)
                    )
                } else {

                    AlertDialog.Builder(requireContext())
                        .setTitle("تنبيه")
                        .setMessage("يوجد زيارة مفتوحة بالفعل للعميل: ${activeVisit.name}")
                        .setPositiveButton("حسناً") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .show()
                }
            } else {

                startActivity(
                    Intent(requireActivity(), VisitsDetailsActivity::class.java)
                        .putExtra("customerPartySiteId", data.customer_party_site_id)
                        .putExtra("time", tsLong.toString())
                        .putExtra("customerSiteData", data)
                        .putExtra("orderType", data.customer_order_type)
                        .putExtra("customerTypePosition", data.customer_type)
                        .putExtra("customer_name", data.customer_name)
                )
            }
        }
    }


    override fun onClick(p0: View?) {
        lifecycleScope.launch {
            viewModel.visitsIntent.send(
                VisitsIntent.GetPlan(
                    versionName, SharedPreferencesHelper.getInstance().getUserToken()
                )
            )
        }
    }
    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val filteredList = mList.filter {
                    it.customer_name.contains(newText.orEmpty(), ignoreCase = true) ||
                            it.customer_party_site_id.contains(newText.orEmpty(), ignoreCase = true) ||
                            it.customer_order_type?.contains(newText.orEmpty(), ignoreCase = true) == true
                }
                setAdapterData(filteredList)
                return true
            }
        })
    }

}


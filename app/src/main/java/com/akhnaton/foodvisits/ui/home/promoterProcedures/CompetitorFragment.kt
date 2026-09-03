package com.akhnaton.foodvisits.ui.home.promoterProcedures

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.akhnaton.foodvisits.data.model.promoter.GetCompetitor
import com.akhnaton.foodvisits.data.model.promoter.GetCompetitorTypes
import com.akhnaton.foodvisits.data.model.promoter.GetPromotionTypes
import com.akhnaton.foodvisits.data.statusValue.promoter.PromoterIntent
import com.akhnaton.foodvisits.data.statusValue.promoter.PromoterStatus
import com.akhnaton.foodvisits.databinding.FragmentCompetitorsBinding
import com.akhnaton.foodvisits.shared.SharedPreferencesHelper
import com.akhnaton.foodvisits.ui.home.visits.promoters.promoterCompetitorsActivity.PromoterCompetitorsViewModel
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.datepicker.CalendarConstraints
import java.util.TimeZone

class CompetitorFragment : Fragment() {

    private var promotionTypesList: List<GetPromotionTypes> = emptyList()
    private var competitorsList: List<GetCompetitor> = emptyList()
    private var competitorTypesList: List<GetCompetitorTypes> = emptyList()

    private val promotionCheckBoxes = mutableListOf<Pair<CheckBox, GetPromotionTypes>>()

    private var _binding: FragmentCompetitorsBinding? = null
    private val binding get() = _binding!!

    private lateinit var imagesAdapter: SelectedImagesAdapter

    private val viewModel: PromoterCompetitorsViewModel by viewModels()

    private var selectedCompetitorId: String? = null
    private var selectedTypeId: String? = null

    private val pickImagesLauncher = registerForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            val currentImages = imagesAdapter.getImages().toMutableList()
            currentImages.addAll(uris)
            imagesAdapter.setImages(currentImages)
            binding.rvImages.visibility = View.VISIBLE
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCompetitorsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupImagesRecyclerView()
        observeStatus()

        binding.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnAddImages.setOnClickListener {
            pickImagesLauncher.launch("image/*")
        }

        viewModel.promoterIntent.trySend(
            PromoterIntent.GetCompetitorList(appVersion = 1.0)
        )

        binding.btnSave.setOnClickListener {
            onSaveClicked()
        }
        binding.etOfferDate.setOnClickListener {
            showOfferDatePicker()
        }

        binding.layoutOfferDate.setEndIconOnClickListener {
            showOfferDatePicker()
        }
    }

    private fun setupImagesRecyclerView() {
        imagesAdapter = SelectedImagesAdapter(
            onAddMoreClick = { pickImagesLauncher.launch("image/*") },
            onRemoveClick = { position ->
                val currentImages = imagesAdapter.getImages().toMutableList()
                currentImages.removeAt(position)
                imagesAdapter.setImages(currentImages)
                binding.rvImages.visibility =
                    if (currentImages.isEmpty()) View.GONE else View.VISIBLE
            }
        )

        binding.rvImages.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.HORIZONTAL,
            false
        )
        binding.rvImages.adapter = imagesAdapter
    }

    private fun setupPromotionTypeCheckboxes(items: List<GetPromotionTypes>) {
        val container = binding.llPromotionTypesContainer
        container.removeAllViews()
        promotionCheckBoxes.clear()

        val rowSpacingPx = (8 * resources.displayMetrics.density).toInt()
        var index = 0

        while (index < items.size) {
            val rowLayout = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutDirection = View.LAYOUT_DIRECTION_RTL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { if (index > 0) topMargin = rowSpacingPx }
            }

            for (offset in 0 until 2) {
                val itemIndex = index + offset
                if (itemIndex >= items.size) {
                    rowLayout.addView(View(requireContext()), LinearLayout.LayoutParams(0, 0, 1f))
                    continue
                }
                val item = items[itemIndex]
                val checkBox = CheckBox(requireContext()).apply {
                    text = item.name.trim()
                    layoutDirection = View.LAYOUT_DIRECTION_RTL
                    gravity = Gravity.CENTER_VERTICAL or Gravity.RIGHT
                    layoutParams = LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
                    )
                }
                promotionCheckBoxes.add(checkBox to item)
                rowLayout.addView(checkBox)
            }

            container.addView(rowLayout)
            index += 2
        }
    }

    private fun observeStatus() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.status.collect { status ->
                    when (status) {
                        is PromoterStatus.GetCompetitorList -> {
                            competitorTypesList = status.response.data.get_competitor_types
                            competitorsList = status.response.data.get_competitor
                            promotionTypesList = status.response.data.get_promotion_types

                            setupPromotionTypeCheckboxes(promotionTypesList)

                            val types = competitorTypesList.map { it.type_name }
                            val companies = competitorsList.map { it.competitor_name }

                            binding.actvCategory.setAdapter(
                                ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, types)
                            )
                            binding.actvCategory.setOnItemClickListener { _, _, position, _ ->
                                selectedTypeId = competitorTypesList[position].id
                            }

                            binding.actvCompany.setAdapter(
                                ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, companies)
                            )
                            binding.actvCompany.setOnItemClickListener { _, _, position, _ ->
                                selectedCompetitorId = competitorsList[position].id
                            }

                            viewModel.resetStatus()
                        }

                        is PromoterStatus.SendCompetitors -> {
                            Toast.makeText(requireContext(), "تم حفظ المنافس بنجاح", Toast.LENGTH_SHORT).show()
                            viewModel.resetStatus()
                            findNavController().popBackStack()
                        }

                        is PromoterStatus.Error -> {
                            Toast.makeText(requireContext(), status.error ?: "حدث خطأ", Toast.LENGTH_SHORT).show()
                            viewModel.resetStatus()
                        }

                        else -> {}
                    }
                }
            }
        }
    }

    private fun onSaveClicked() {

        if (offerDateForApi.isBlank()) {
            Toast.makeText(requireContext(), "من فضلك اختر تاريخ العرض", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedTypeId == null || selectedCompetitorId == null) {
            Toast.makeText(requireContext(), "من فضلك اختر الفئة والشركة المنافسة", Toast.LENGTH_SHORT).show()
            return
        }

        fun String.toBody(): RequestBody = this.toRequestBody("text/plain".toMediaTypeOrNull())

        val checkedIds = promotionCheckBoxes
            .filter { it.first.isChecked }
            .map { it.second.id }

        val promTypeJson = checkedIds.joinToString(",", "[", "]") { "{\"id\":\"$it\"}" }

        val imagePart = imagesAdapter.getImages()
            .firstOrNull()
            ?.let { uriToMultipart(it) }

        if (imagePart == null) {
            Toast.makeText(requireContext(), "من فضلك اختر صورة", Toast.LENGTH_SHORT).show()
            return
        }

        val apiToken = SharedPreferencesHelper.getInstance().getUserToken()
            ?: requireActivity().intent?.getStringExtra("token") ?: ""

        val employeeId = SharedPreferencesHelper.getInstance().getEmployeeId()
            ?: requireActivity().intent?.getStringExtra("employee_id") ?: ""

        val customerCode = arguments?.getString("customerCode")
            ?: requireActivity().intent?.getStringExtra("customer_code") ?: ""

        val partySiteId = arguments?.getString("customerPartySiteId")
            ?: requireActivity().intent?.getStringExtra("party_site") ?: ""

        Log.d("CUSTOMER_DEBUG", "Fragment customer_code = '$customerCode'")
        Log.d("TOKEN_DEBUG", "api_token = '$apiToken' (length=${apiToken.length})")

        val creationDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        viewModel.promoterIntent.trySend(
            PromoterIntent.SendCompetitors(
                appVersion = "1.0".toBody(),
                apiToken = apiToken.toBody(),
                image = imagePart,
                created_by = employeeId.toBody(),
                creation_date = creationDate.toBody(),
                party_site_id = partySiteId.toBody(),
                customer_code = customerCode.toBody(),
                product_id = "".toBody(),
                price = binding.etPriceBefore.text.toString().toBody(),
                price_after_disc = binding.etPriceAfter.text.toString().toBody(),
                product_name = binding.etProductName.text.toString().toBody(),
                weight = binding.etUnitSize.text.toString().toBody(),
                discount_rate = binding.etDiscount.text.toString().toBody(),
                prom_type = promTypeJson.toBody(),
                prom_date = offerDateForApi.toBody(),
                user_type = "".toBody(),
                PromoterCompetitorCompress = binding.etProductSize.text.toString().toBody(),
                competitor_id = selectedCompetitorId!!.toBody(),
                type_id = selectedTypeId!!.toBody(),
            )
        )
    }

    private fun uriToMultipart(uri: Uri): MultipartBody.Part? {
        val contentResolver = requireContext().contentResolver
        val inputStream = contentResolver.openInputStream(uri) ?: return null
        val tempFile = File.createTempFile("competitor_img_", ".jpg", requireContext().cacheDir)
        FileOutputStream(tempFile).use { output ->
            inputStream.copyTo(output)
        }
        val requestFile = tempFile.asRequestBody("image/*".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData("image", tempFile.name, requestFile)
    }
    private var offerDateForApi: String = ""

    private fun showOfferDatePicker() {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("اختر تاريخ العرض")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        datePicker.addOnPositiveButtonClickListener { selectionMillis ->
            val calendar = java.util.Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            calendar.timeInMillis = selectionMillis

            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            offerDateForApi = sdf.format(calendar.time)

            binding.etOfferDate.setText(offerDateForApi)
        }

        datePicker.show(childFragmentManager, "OFFER_DATE_PICKER")
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
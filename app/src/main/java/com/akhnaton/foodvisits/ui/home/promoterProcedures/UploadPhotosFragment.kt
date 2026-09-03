package com.akhnaton.foodvisits.ui.home.promoterProcedures

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.data.statusValue.promoter.PromoterIntent
import com.akhnaton.foodvisits.data.statusValue.promoter.PromoterStatus
import com.akhnaton.foodvisits.databinding.FragmentUploadPhotosBinding
import com.akhnaton.foodvisits.shared.SharedPreferencesHelper
import com.akhnaton.foodvisits.ui.home.visits.promoters.promoterCompetitorsActivity.PromoterCompetitorsViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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

class UploadPhotosFragment : Fragment() {

    private lateinit var binding: FragmentUploadPhotosBinding
    private val selectedImages = mutableListOf<Uri>()
    private lateinit var selectedImagesAdapter: SelectedImagesAdapter

    private val viewModel: PromoterCompetitorsViewModel by viewModels()

    private val galleryPicker =
        registerForActivityResult(
            ActivityResultContracts.GetMultipleContents()
        ) { uris ->

            if (uris.isNotEmpty()) {

                selectedImages.addAll(uris)

                selectedImagesAdapter.setImages(selectedImages)

                updateImagesUI()
            }
        }

    private var cameraImageUri: Uri? = null

    private val cameraLauncher =
        registerForActivityResult(
            ActivityResultContracts.TakePicture()
        ) { success ->

            if (success) {

                cameraImageUri?.let { uri ->

                    selectedImages.add(uri)

                    selectedImagesAdapter.setImages(selectedImages)

                    updateImagesUI()
                }
            }
        }

    private val cameraPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->

            if (granted) {
                openCamera()
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->

            val systemBars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
            )

            view.setPadding(
                view.paddingLeft,
                systemBars.top,
                view.paddingRight,
                systemBars.bottom
            )

            insets
        }

        setupViews()
        observeStatus()

    }

    private fun observeStatus() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.status.collect { status ->
                    when (status) {
                        is PromoterStatus.UploadImages -> {
                            Toast.makeText(requireContext(), "تم رفع الصور بنجاح", Toast.LENGTH_SHORT).show()
                            findNavController().popBackStack()
                        }

                        is PromoterStatus.Error -> {
                            Toast.makeText(requireContext(), status.error ?: "حدث خطأ", Toast.LENGTH_SHORT).show()
                        }

                        else -> {}
                    }
                }
            }
        }
    }

    private fun setupViews() {

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.cardUpload.setOnClickListener {
            showImageSourceDialog()
        }

        binding.tvUpload.setOnClickListener {
            showImageSourceDialog()
        }

        binding.ivUpload.setOnClickListener {
            showImageSourceDialog()
        }

        binding.btnUpload.setOnClickListener {
            Log.d("UPLOAD_DEBUG", "uploadImages called, images count = ${selectedImages.size}")
            uploadImages()
        }

        setupImagesRecycler()

        updateImagesUI()
    }

    private fun setupImagesRecycler() {

        selectedImagesAdapter = SelectedImagesAdapter(
            onAddMoreClick = {
                showImageSourceDialog()
            },
            onRemoveClick = { imagePosition ->

                if (imagePosition in selectedImages.indices) {
                    selectedImages.removeAt(imagePosition)

                    selectedImagesAdapter.setImages(selectedImages)

                    updateImagesUI()
                }
            }
        )

        binding.recyclerImages.apply {

            layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                true
            )

            adapter = selectedImagesAdapter

            setHasFixedSize(true)

            clipToPadding = false
        }
    }

    private fun updateImagesUI() {

        val count = selectedImages.size
        val hasImages = count > 0

        binding.cardUpload.visibility =
            if (hasImages) View.GONE else View.VISIBLE

        binding.recyclerImages.visibility =
            if (hasImages) View.VISIBLE else View.GONE

        // Update description
        binding.tvDescription.text =
            if (hasImages) {
                when (count) {
                    1 -> "تم اختيار صورة واحدة"
                    2 -> "تم اختيار صورتين"
                    else -> "تم اختيار $count صور"
                }
            } else {
                "يرجى إضافة الصور المطلوبة للمخزون"
            }

        // Upload button
        binding.btnUpload.isEnabled = hasImages

        binding.btnUpload.backgroundTintList =
            if (hasImages) {
                ContextCompat.getColorStateList(requireContext(), R.color.orange)
            } else {
                ContextCompat.getColorStateList(requireContext(), R.color.gray)
            }

        // Update adapter
        if (::selectedImagesAdapter.isInitialized) {
            selectedImagesAdapter.notifyDataSetChanged()
        }
    }

    private fun openGallery() {
        galleryPicker.launch("image/*")
    }

    private fun uploadImages() {

        fun String.toBody(): RequestBody = this.toRequestBody("text/plain".toMediaTypeOrNull())

        if (selectedImages.isEmpty()) {
            Toast.makeText(requireContext(), "من فضلك اختر صور أولاً", Toast.LENGTH_SHORT).show()
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

        val creationDate = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.ENGLISH).format(Date())

        val imageParts: Array<MultipartBody.Part?> = arrayOfNulls(selectedImages.size)

        for (i in selectedImages.indices) {
            imageParts[i] = uriToMultipart(selectedImages[i], i)
        }

        lifecycleScope.launch {
            viewModel.promoterIntent.send(
                PromoterIntent.UploadImages(
                    appVersion = "1.0".toBody(),
                    apiToken = apiToken.toBody(),
                    image = imageParts,
                    created_by = employeeId.toBody(),
                    creation_date = creationDate.toBody(),
                    customer_code = customerCode.toBody(),
                    party_site_id = partySiteId.toBody(),
                    user_type = "prom".toBody(),
                    funNum = "1".toBody()
                )
            )
        }
    }

    private fun uriToMultipart(uri: Uri, index: Int): MultipartBody.Part? {
        val contentResolver = requireContext().contentResolver
        val inputStream = contentResolver.openInputStream(uri) ?: return null
        val tempFile = File.createTempFile("upload_img_", ".jpg", requireContext().cacheDir)
        FileOutputStream(tempFile).use { output ->
            inputStream.copyTo(output)
        }
        val requestFile = tempFile.asRequestBody("image/*".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData("image[$index]", tempFile.name, requestFile)
    }

    private fun updateUploadButton() {

        val hasImages = selectedImages.isNotEmpty()

        binding.btnUpload.isEnabled = hasImages

        binding.btnUpload.backgroundTintList =
            if (hasImages) {
                ContextCompat.getColorStateList(requireContext(), R.color.orange)
            } else {
                ContextCompat.getColorStateList(requireContext(), R.color.gray)
            }
    }

    private fun createImageUri(): Uri {

        val timeStamp = SimpleDateFormat(
            "yyyyMMdd_HHmmss",
            Locale.getDefault()
        ).format(Date())

        val imageFile = File.createTempFile(
            "IMG_${timeStamp}_",
            ".jpg",
            requireContext().cacheDir
        )

        return FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.provider",
            imageFile
        )
    }

    private fun openCamera() {

        cameraImageUri = createImageUri()

        cameraLauncher.launch(cameraImageUri)
    }

    private fun showImageSourceDialog() {

        val options = arrayOf(
            "المعرض",
            "الكاميرا"
        )

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("اختيار الصور")
            .setItems(options) { _, which ->

                when (which) {

                    0 -> {
                        openGallery()
                    }

                    1 -> {
                        checkCameraPermission()
                    }
                }
            }
            .show()
    }

    private fun checkCameraPermission() {

        if (
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {

            openCamera()

        } else {

            cameraPermissionLauncher.launch(
                Manifest.permission.CAMERA
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        binding = DataBindingUtil.inflate(
            layoutInflater, R.layout.fragment_upload_photos, container, false
        )
        return binding.root
    }

}
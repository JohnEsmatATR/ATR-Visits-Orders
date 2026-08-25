package com.akhnaton.foodvisits.ui.home.promoterProcedures

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.databinding.FragmentUploadPhotosBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UploadPhotosFragment : Fragment() {

    private lateinit var binding: FragmentUploadPhotosBinding
    private val selectedImages = mutableListOf<Uri>()
    private lateinit var selectedImagesAdapter: SelectedImagesAdapter

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

        // Show/hide upload area
        binding.cardUpload.visibility =
            if (hasImages) View.GONE else View.VISIBLE

        // Show/hide RecyclerView
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
                requireContext().getColorStateList(R.color.orange)
            } else {
                requireContext().getColorStateList(R.color.gray)
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
        // We will add the API upload here.
    }

    private fun updateUploadButton() {

        val hasImages = selectedImages.isNotEmpty()

        binding.btnUpload.isEnabled = hasImages

        binding.btnUpload.backgroundTintList =
            if (hasImages) {
                requireContext().getColorStateList(R.color.orange)
            } else {
                requireContext().getColorStateList(R.color.gray)
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
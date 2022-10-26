package com.akhnaton.foodvisits.ui.home.profile

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.databinding.ActivitySetupProfileBinding
import com.akhnaton.foodvisits.shared.FirebaseProfileClient
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.canhub.cropper.CropImage
import com.github.dhaval2404.imagepicker.ImagePicker.Companion.with
import com.google.firebase.firestore.FirebaseFirestore

class SetupProfileActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivitySetupProfileBinding

    private lateinit var mainImageURI: Uri
    var downloadUri: Uri? = null
    private val user_id: String = ""
    private var isChanged = false
    private var mFire = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_setup_profile)


        Glide.with(applicationContext)
            .load(FirebaseProfileClient().getProfileImgSetup(binding, this@SetupProfileActivity))
            .placeholder(R.drawable.addprofile)
            .error(R.drawable.addprofile)
            .apply(RequestOptions.circleCropTransform())
            .into(binding.imgProfile)


        binding.saveImg.setOnClickListener(this)

        binding.imgProfile.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(
                        this@SetupProfileActivity,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        this@SetupProfileActivity,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        1
                    )
                } else {
                    bringImagePicker()
                }
            } else {
                bringImagePicker()
            }
        }

    }

    private fun bringImagePicker() {
        with(this)
            .crop()
            .compress(300)
            .maxResultSize(
                1080,
                1080
            ) //Final image resolution will be less than 1080 x 1080(Optional)
            .start()
    }


    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            val uri = data!!.data
            if (uri != null) {
                mainImageURI = uri
            }
            binding.imgProfile.setImageURI(mainImageURI)
            isChanged = true


//            binding.imgProfile.setImageURI(uri)

            Log.d(
                "TAG",
                "onActivityResult: " + uri!!.path
            )
        } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
            Log.d("TAG", "onActivityResult:")
        }
    }

    override fun onClick(p0: View?) {
        FirebaseProfileClient().setProfileImgSetup(mainImageURI,this@SetupProfileActivity)
    }
}
package com.akhnaton.foodvisits.ui.home.profile

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.akhnaton.foodvisits.BuildConfig
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.databinding.ActivityProfileBinding
import com.akhnaton.foodvisits.shared.FirebaseProfileClient
import com.akhnaton.foodvisits.shared.SharedPreferencesHelper
import com.akhnaton.foodvisits.ui.auth.LoginActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions


class ProfileActivity : AppCompatActivity(), View.OnClickListener {

    companion object {
        private const val TAG = "ProfileFragment"
    }

    private lateinit var binding: ActivityProfileBinding
    private val versionName = BuildConfig.VERSION_NAME


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupBinding()
    }

    private fun setupBinding() {
        binding =
            DataBindingUtil.setContentView(this, R.layout.activity_profile)
        binding.usernameText.text = SharedPreferencesHelper.getInstance().getUsername()
        binding.versionApp.text = "V $versionName Latest 1"

        binding.logout.setOnClickListener(this)
        binding.editProfile.setOnClickListener(this)

        Glide.with(applicationContext)
            .load(FirebaseProfileClient().getProfileImage(binding, this@ProfileActivity))
            .placeholder(R.drawable.addprofile)
            .error(R.drawable.addprofile)
            .apply(RequestOptions.circleCropTransform())
            .into(binding.profileImg)

        binding.btnBack.setOnClickListener {
            finish()
        }
    }


    override fun onClick(p0: View) {

        if (p0.id == R.id.editProfile) {
            startActivity(Intent(this@ProfileActivity, SetupProfileActivity::class.java))
        }

        if (p0.id == R.id.logout) {
            SharedPreferencesHelper.getInstance().logOut()
            startActivity(Intent(this@ProfileActivity, LoginActivity::class.java))
            finishAffinity()
        }
    }
}



package com.akhnaton.foodvisits.ui.profile

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.databinding.ActivityProfileBinding
import com.akhnaton.foodvisits.databinding.ActivitySetupProfileBinding
import com.akhnaton.foodvisits.shared.FirebaseProfileClient
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SetupProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySetupProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_setup_profile)

        Glide.with(applicationContext)
            .load(FirebaseProfileClient().getProfileImgSetup(binding, this@SetupProfileActivity))
            .apply(RequestOptions.circleCropTransform())
            .into(binding.imgProfile)

    }

}
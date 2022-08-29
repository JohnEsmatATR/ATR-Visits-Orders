package com.akhnaton.foodvisits.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.databinding.ActivityMainBinding
import com.akhnaton.foodvisits.shared.SharedPreferencesHelper
import com.akhnaton.foodvisits.ui.auth.LoginActivity
import com.akhnaton.foodvisits.ui.profile.ProfileActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.appcheck.safetynet.SafetyNetAppCheckProviderFactory
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupBinding()
    }

    private fun setupBinding() {

        val binding: ActivityMainBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.lifecycleOwner = this
        binding.executePendingBindings()

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.main_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        setupWithNavController(binding.navView, navController)

        binding.floating.setOnClickListener {
            val nav = findNavController(navHostFragment)
            nav.navigateUp() || super.onSupportNavigateUp()
        }
//        FirebaseApp.initializeApp(/*context=*/this)
//        val firebaseAppCheck = FirebaseAppCheck.getInstance()
//        firebaseAppCheck.installAppCheckProviderFactory(
//            PlayIntegrityAppCheckProviderFactory.getInstance()
//        )
//        getProfileImage(binding)

        binding.profileBtn.setOnClickListener(this)
    }

    override fun onStart() {
        super.onStart()
        if (!SharedPreferencesHelper().isLogged()) {
            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
        }
    }

    private fun getProfileImage(binding: ActivityMainBinding) {
        val db = Firebase.firestore

        db.collection("Users").document("146070").get()
            .addOnCompleteListener { task: Task<DocumentSnapshot> ->
                if (task.isSuccessful) {
                    if (task.result.exists()) {
                        val image = task.result.getString("image")
                        Log.d("TAG", "onFireStoreImage: $image")
                        Glide.with(applicationContext).load(image)
                            .apply(RequestOptions.circleCropTransform())
                            .into(binding.profileImg)
                    }
                } else {
                    val error = task.exception!!.message
                    Toast.makeText(
                        this@MainActivity,
                        "(FIRESTORE Retrieve Error) : $error",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    override fun onClick(p0: View?) {
        startActivity(Intent(this@MainActivity, ProfileActivity::class.java))
    }

}
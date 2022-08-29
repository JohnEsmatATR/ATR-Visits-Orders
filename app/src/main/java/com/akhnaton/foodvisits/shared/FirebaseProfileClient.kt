package com.akhnaton.foodvisits.shared

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.akhnaton.foodvisits.databinding.ActivityProfileBinding
import com.akhnaton.foodvisits.databinding.ActivitySetupProfileBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class FirebaseProfileClient {

    private val db = Firebase.firestore


    fun getProfileImage(binding: ActivityProfileBinding, context: Context) {

        db.collection("Users").document("146070").get()
            .addOnCompleteListener { task: Task<DocumentSnapshot> ->
                if (task.isSuccessful) {
                    if (task.result.exists()) {
                        val image = task.result.getString("image")
                        Log.d("TAG", "onFireStoreImage: $image")

                        Glide.with(context).load(image)
                            .apply(RequestOptions.circleCropTransform())
                            .into(binding.profileImg)
                    }
                } else {
                    val error = task.exception!!.message
                    Log.d("TAG", "getProfileImage: $error")
                }
            }
    }

    fun getProfileImgSetup(binding: ActivitySetupProfileBinding, context: Context) {

        db.collection("Users").document("146070").get()
            .addOnCompleteListener { task: Task<DocumentSnapshot> ->
                if (task.isSuccessful) {
                    if (task.result.exists()) {
                        val image = task.result.getString("image")
                        Log.d("TAG", "onFireStoreImage: $image")

                        Glide.with(context).load(image)
                            .apply(RequestOptions.circleCropTransform())
                            .into(binding.imgProfile)
                    }
                } else {
                    val error = task.exception!!.message
                    Log.d("TAG", "getProfileImage: $error")
                }
            }
    }


}
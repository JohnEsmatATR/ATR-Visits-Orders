package com.akhnaton.foodvisits.shared

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.akhnaton.foodvisits.databinding.ActivityProfileBinding
import com.akhnaton.foodvisits.databinding.ActivitySetupProfileBinding
import com.akhnaton.foodvisits.ui.MainActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.OnProgressListener
import com.google.firebase.storage.UploadTask
import java.io.File
import java.util.*

class FirebaseProfileClient {

    private val db = Firebase.firestore
    private val storage = FirebaseStorage.getInstance().reference


    fun getProfileImage(binding: ActivityProfileBinding, context: Context) {

        db.collection("Users").document(SharedPreferencesHelper.getInstance().getUserToken()).get()
            .addOnCompleteListener { task: Task<DocumentSnapshot> ->
                if (task.isSuccessful) {
                    if (task.result.exists()) {
                        val image = task.result.getString("image")
                        Log.d("TAG", "onFireStoreImage: $image")

                        Glide.with(context.applicationContext).load(image)
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

        db.collection("Users").document(SharedPreferencesHelper.getInstance().getUserToken()).get()
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

    fun setProfileImgSetup(uri: Uri, context: Context) {
        val progressDialog = ProgressDialog(context)
        progressDialog.setTitle("Uploading...")
        progressDialog.show()
        val newImageFile = File(uri.path!!)

        val imagePath: UploadTask =
            storage.child("profile_images").child(
                SharedPreferencesHelper.getInstance().getUserToken() + ".jpg"
            ).putBytes(newImageFile.readBytes())

        imagePath.addOnProgressListener { taskSnapshot: UploadTask.TaskSnapshot ->
            val progress = (100.0
                    * taskSnapshot.bytesTransferred
                    / taskSnapshot.totalByteCount)
            progressDialog.setMessage(
                "Uploaded "
                        + progress.toInt() + "%"
            )
        }
        imagePath.continueWithTask { task: Task<UploadTask.TaskSnapshot?> ->
            if (!task.isSuccessful) {
                throw Objects.requireNonNull(task.exception)!!
            }
            imagePath.getResult().getMetadata()?.getReference()?.getDownloadUrl()

        }.addOnCompleteListener { task: Task<Uri?> ->
            if (task.isSuccessful) {

                val userMap: MutableMap<String, String> = HashMap()
                userMap["name"] = SharedPreferencesHelper.getInstance().getUserToken()
                userMap["image"] = task.result.toString()

                db.collection("Users")
                    .document(SharedPreferencesHelper.getInstance().getUserToken())
                    .set(userMap)
                    .addOnCompleteListener { task1: Task<Void?> ->
                        if (task1.isSuccessful) {
                            context.startActivity(Intent(context, MainActivity::class.java))

                            Toast.makeText(
                                context,
                                "The user Settings are updated.",
                                Toast.LENGTH_LONG
                            ).show()
                            progressDialog.dismiss()

                        } else {
                            val error = task1.exception!!.message
                            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                            progressDialog.dismiss()
                        }
                    }
                Log.d("TAG", "setProfileImgSetup: ${task.result}")
            } else {
                val error = task.exception!!.message
                Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            }
        }

    }


}
package com.akhnaton.foodvisits.shared

import android.content.Context
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

fun uriToPart(context: Context, uri: Uri): MultipartBody.Part {
    val inputStream = context.contentResolver.openInputStream(uri)!!
    val bytes = inputStream.readBytes()

    val requestBody = bytes.toRequestBody(
        context.contentResolver.getType(uri)?.toMediaTypeOrNull()
    )

    return MultipartBody.Part.createFormData(
        "attachments[]",
        "file",
        requestBody
    )
}
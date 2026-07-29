package com.akhnaton.foodvisits.shared

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

fun Uri.toMultipart(
    context: Context,
    partName: String = "attachments[]"
): MultipartBody.Part {

    val resolver = context.contentResolver

    var fileName = "attachment"

    resolver.query(this, null, null, null, null)?.use { cursor ->
        val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (cursor.moveToFirst() && index != -1) {
            fileName = cursor.getString(index)
        }
    }

    val tempFile = File(context.cacheDir, fileName)

    resolver.openInputStream(this)?.use { input ->
        FileOutputStream(tempFile).use { output ->
            input.copyTo(output)
        }
    }

    val mimeType = resolver.getType(this) ?: "application/octet-stream"

    val requestBody = tempFile.asRequestBody(
        mimeType.toMediaTypeOrNull()
    )

    return MultipartBody.Part.createFormData(
        partName,
        fileName,
        requestBody
    )
}
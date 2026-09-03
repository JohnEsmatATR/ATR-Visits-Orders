package com.akhnaton.foodvisits.shared

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

fun getFileFromUri(context: Context, uri: Uri): File {
    val fileName = getFileName(context, uri)
    val file = File(context.cacheDir, fileName)

    context.contentResolver.openInputStream(uri)?.use { inputStream ->
        FileOutputStream(file).use { outputStream ->
            inputStream.copyTo(outputStream)
        }
    }

    return file
}
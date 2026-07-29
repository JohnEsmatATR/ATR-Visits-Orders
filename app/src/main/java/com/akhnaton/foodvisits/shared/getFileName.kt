package com.akhnaton.foodvisits.shared

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns

fun getFileName(
    context: Context,
    uri: Uri
): String {

    var name = "Attachment"

    val cursor = context.contentResolver.query(
        uri,
        null,
        null,
        null,
        null
    )

    cursor?.use {

        val index =
            it.getColumnIndex(OpenableColumns.DISPLAY_NAME)

        if (it.moveToFirst())
            name = it.getString(index)
    }

    return name
}
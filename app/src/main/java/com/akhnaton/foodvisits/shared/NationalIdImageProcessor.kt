package com.akhnaton.foodvisits.shared

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.FileOutputStream
import kotlin.math.roundToInt

object NationalIdImageProcessor {

    private const val TARGET_ASPECT_RATIO = 1.586f
    private const val JPEG_QUALITY = 95

    fun process(
        context: Context,
        inputFile: File
    ): File {

        val bitmap =
            decodeCorrectlyRotatedBitmap(inputFile)

        val croppedBitmap =
            cropToNationalIdRatio(bitmap)

        val outputFile = File(
            context.externalCacheDir,
            "national_id_${System.currentTimeMillis()}.jpg"
        )

        FileOutputStream(outputFile).use { outputStream ->

            croppedBitmap.compress(
                Bitmap.CompressFormat.JPEG,
                JPEG_QUALITY,
                outputStream
            )
        }

        if (croppedBitmap !== bitmap) {
            croppedBitmap.recycle()
        }

        bitmap.recycle()

        return outputFile
    }

    private fun decodeCorrectlyRotatedBitmap(
        file: File
    ): Bitmap {

        val bitmap =
            BitmapFactory.decodeFile(file.absolutePath)
                ?: throw IllegalStateException(
                    "Unable to decode image"
                )

        val exif =
            ExifInterface(file.absolutePath)

        val orientation =
            exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )

        return when (orientation) {

            ExifInterface.ORIENTATION_ROTATE_90 ->
                bitmap.rotate(90f)

            ExifInterface.ORIENTATION_ROTATE_180 ->
                bitmap.rotate(180f)

            ExifInterface.ORIENTATION_ROTATE_270 ->
                bitmap.rotate(270f)

            ExifInterface.ORIENTATION_FLIP_HORIZONTAL ->
                bitmap.flip(horizontal = true)

            ExifInterface.ORIENTATION_FLIP_VERTICAL ->
                bitmap.flip(horizontal = false)

            else ->
                bitmap
        }
    }

    private fun cropToNationalIdRatio(
        source: Bitmap
    ): Bitmap {

        val sourceWidth = source.width
        val sourceHeight = source.height

        val sourceRatio =
            sourceWidth.toFloat() / sourceHeight.toFloat()

        return if (sourceRatio > TARGET_ASPECT_RATIO) {

            // Image is too wide.
            // Keep the full height and crop the sides.

            val cropWidth =
                (sourceHeight * TARGET_ASPECT_RATIO)
                    .roundToInt()

            val left =
                (sourceWidth - cropWidth) / 2

            Bitmap.createBitmap(
                source,
                left,
                0,
                cropWidth,
                sourceHeight
            )

        } else {

            // Image is too tall.
            // Keep the full width and crop top/bottom.

            val cropHeight =
                (sourceWidth / TARGET_ASPECT_RATIO)
                    .roundToInt()

            val top =
                (sourceHeight - cropHeight) / 2

            Bitmap.createBitmap(
                source,
                0,
                top,
                sourceWidth,
                cropHeight
            )
        }
    }

    private fun Bitmap.rotate(
        degrees: Float
    ): Bitmap {

        val matrix =
            android.graphics.Matrix()

        matrix.postRotate(degrees)

        return Bitmap.createBitmap(
            this,
            0,
            0,
            width,
            height,
            matrix,
            true
        )
    }

    private fun Bitmap.flip(
        horizontal: Boolean
    ): Bitmap {

        val matrix =
            android.graphics.Matrix()

        matrix.preScale(
            if (horizontal) -1f else 1f,
            if (horizontal) 1f else -1f
        )

        return Bitmap.createBitmap(
            this,
            0,
            0,
            width,
            height,
            matrix,
            true
        )
    }
}
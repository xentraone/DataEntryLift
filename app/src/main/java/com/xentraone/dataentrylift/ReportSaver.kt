package com.xentraone.dataentrylift

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

/** Saves report PNGs into the phone's own storage so they stay on the device:
 *  Pictures/DataEntryLift (visible in Gallery/Files) on Android 10+,
 *  the app's picture folder on older phones. */
object ReportSaver {

    fun save(context: Context, bmp: Bitmap, name: String): Uri {
        return if (Build.VERSION.SDK_INT >= 29) {
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, name)
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(
                    MediaStore.Images.Media.RELATIVE_PATH,
                    Environment.DIRECTORY_PICTURES + "/DataEntryLift"
                )
            }
            val uri = context.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values
            ) ?: throw IllegalStateException("Could not create image")
            context.contentResolver.openOutputStream(uri)!!.use { out ->
                bmp.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            uri
        } else {
            val dir = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "reports")
            dir.mkdirs()
            val file = File(dir, name)
            FileOutputStream(file).use { out ->
                bmp.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            FileProvider.getUriForFile(context, context.packageName + ".fileprovider", file)
        }
    }
}

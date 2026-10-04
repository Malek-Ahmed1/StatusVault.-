package com.kaboas.statusvault.utils

import android.content.ContentValues
import android.content.Context
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import com.kaboas.statusvault.R
import com.kaboas.statusvault.data.MediaType
import java.io.File

object DownloadHelper {

    fun downloadFile(context: Context, source: File, mediaType: MediaType) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                saveWithMediaStore(context, source, mediaType)
            } else {
                saveDirect(context, source, mediaType)
            }
        } catch (e: Exception) {
            Toast.makeText(context, context.getString(R.string.message_failed), Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveWithMediaStore(context: Context, source: File, mediaType: MediaType) {
        val folderName = if (mediaType == MediaType.IMAGE) "StatusVault/Images" else "StatusVault/Videos"
        val mimeType = if (mediaType == MediaType.IMAGE) "image/jpeg" else "video/mp4"

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, source.name)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/$folderName")
        }

        val collection = if (mediaType == MediaType.IMAGE)
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        else
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI

        val uri = context.contentResolver.insert(collection, contentValues) ?: return

        context.contentResolver.openOutputStream(uri)?.use { output ->
            source.inputStream().use { input -> input.copyTo(output) }
        }

        Toast.makeText(context, "Saved to Pictures/$folderName", Toast.LENGTH_SHORT).show()
    }

    private fun saveDirect(context: Context, source: File, mediaType: MediaType) {
        val folderName = if (mediaType == MediaType.IMAGE) "StatusVault/Images" else "StatusVault/Videos"
        val destDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            folderName
        )
        if (!destDir.exists()) destDir.mkdirs()

        val destFile = File(destDir, source.name)
        source.inputStream().use { input ->
            destFile.outputStream().use { output -> input.copyTo(output) }
        }

        MediaScannerConnection.scanFile(context, arrayOf(destFile.absolutePath), null, null)
        Toast.makeText(context, "Saved to Pictures/$folderName", Toast.LENGTH_SHORT).show()
    }
}

package com.kaboas.statusvault.utils

import android.content.Context
import android.os.Build
import android.provider.MediaStore
import java.io.File

object DeleteHelper {

    fun deleteFile(context: Context, file: File): Boolean {
        if (file.exists() && file.delete()) return true

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return try {
                val resolver = context.contentResolver
                val collection = if (file.extension.lowercase() in listOf("mp4", "mkv", "3gp", "avi"))
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                else
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI

                val selection = "${MediaStore.MediaColumns.DATA} = ?"
                val selectionArgs = arrayOf(file.absolutePath)
                resolver.delete(collection, selection, selectionArgs) > 0
            } catch (e: Exception) {
                false
            }
        }
        return false
    }
}

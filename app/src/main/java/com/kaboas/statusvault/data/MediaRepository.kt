package com.kaboas.statusvault.data

import android.os.Environment
import java.io.File

object MediaRepository {

    private var cachedDirs: List<File>? = null

    fun findStatusDirs(): List<File> {
        cachedDirs?.let { return it }

        val results = mutableListOf<File>()
        val knownPaths = listOf(
            "/WhatsApp/Media/.Statuses",
            "/WhatsApp Business/Media/.Statuses",
            "/Android/media/com.whatsapp/WhatsApp/Media/.Statuses",
            "/Android/media/com.whatsapp.w4b/WhatsApp Business/Media/.Statuses",
            "/WhatsApp/Media/Statuses",
            "/Android/media/com.whatsapp/WhatsApp/Media/Statuses"
        )
        knownPaths.forEach { path ->
            val dir = File(Environment.getExternalStorageDirectory(), path)
            if (dir.exists() && dir.isDirectory) results.add(dir)
        }

        if (results.isEmpty()) {
            try {
                File("/storage/emulated/0").walkTopDown()
                    .maxDepth(6)
                    .filter { it.isDirectory && (it.name == ".Statuses" || it.name == "Statuses") }
                    .take(10)
                    .forEach { results.add(it) }
            } catch (_: Exception) { }
        }

        val unique = results.distinct()
        cachedDirs = unique
        return unique
    }

    fun listMedia(type: MediaType): List<File> {
        val result = mutableListOf<File>()
        val extensions = when (type) {
            MediaType.IMAGE -> listOf("jpg", "jpeg", "png", "webp")
            MediaType.VIDEO -> listOf("mp4", "mkv", "3gp", "avi")
        }
        findStatusDirs().forEach { dir ->
            dir.listFiles()?.forEach { file ->
                if (file.isFile && file.extension.lowercase() in extensions) {
                    result.add(file)
                }
            }
        }
        return result.sortedByDescending { it.lastModified() }
    }

    fun clearCache() {
        cachedDirs = null
    }
}

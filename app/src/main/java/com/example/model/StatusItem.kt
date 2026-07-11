package com.example.model

import android.net.Uri

data class StatusItem(
    val id: String,
    val uriString: String,
    val fileName: String,
    val isVideo: Boolean,
    val fileSize: Long,
    val dateModified: Long,
    val isSaved: Boolean = false
) {
    val uri: Uri get() = Uri.parse(uriString)
    
    val formattedSize: String
        get() {
            val kb = fileSize / 1024.0
            val mb = kb / 1024.0
            return when {
                mb >= 1.0 -> String.format("%.1f MB", mb)
                else -> String.format("%.1f KB", kb)
            }
        }
}

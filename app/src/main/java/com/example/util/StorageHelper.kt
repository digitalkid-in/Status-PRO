package com.example.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.FileProvider
import androidx.documentfile.provider.DocumentFile
import com.example.model.StatusItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL

object StorageHelper {
    private const val TAG = "StorageHelper"
    const val DIRECTORY_NAME = "StatusSaver"

    // High quality Unsplash URLs for the Demo Mode
    val demoImages = listOf(
        "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=800&auto=format&fit=crop&q=80" to "Nature Escape.jpg",
        "https://images.unsplash.com/photo-1513836279014-a89f7a76ae86?w=800&auto=format&fit=crop&q=80" to "Forest Light.jpg",
        "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?w=800&auto=format&fit=crop&q=80" to "Foggy Mountains.jpg",
        "https://images.unsplash.com/photo-1501854140801-50d01698950b?w=800&auto=format&fit=crop&q=80" to "Sunset Peak.jpg",
        "https://images.unsplash.com/photo-1447752875215-b2761acb3c5d?w=800&auto=format&fit=crop&q=80" to "Autumn Path.jpg",
        "https://images.unsplash.com/photo-1472214222541-d510753a4907?w=800&auto=format&fit=crop&q=80" to "Green Meadow.jpg"
    )

    // Public stable short mp4 video URLs for the Demo Mode
    val demoVideos = listOf(
        "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4" to "Firefighter Demo.mp4",
        "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4" to "Outdoor Escape.mp4",
        "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4" to "Holiday Fun.mp4"
    )

    /**
     * Checks whether WhatsApp is installed on the device.
     */
    fun isWhatsAppInstalled(context: Context): Boolean {
        val pm = context.packageManager
        val packages = listOf("com.whatsapp", "com.whatsapp.w4b")
        for (pkg in packages) {
            try {
                pm.getPackageInfo(pkg, 0)
                return true
            } catch (e: Exception) {
                // Ignore
            }
        }
        return false
    }

    /**
     * Get a list of mock statuses for the Demo mode.
     */
    fun getDemoStatuses(context: Context): List<StatusItem> {
        val list = mutableListOf<StatusItem>()
        // Generate Image Statuses
        demoImages.forEachIndexed { index, (url, name) ->
            list.add(
                StatusItem(
                    id = "demo_img_$index",
                    uriString = url,
                    fileName = name,
                    isVideo = false,
                    fileSize = (1024 * 350 + (index * 4235)).toLong(), // Simulated size
                    dateModified = System.currentTimeMillis() - (index * 3600 * 1000), // Staggered time
                    isSaved = isDemoFileSaved(context, name)
                )
            )
        }
        // Generate Video Statuses
        demoVideos.forEachIndexed { index, (url, name) ->
            list.add(
                StatusItem(
                    id = "demo_vid_$index",
                    uriString = url,
                    fileName = name,
                    isVideo = true,
                    fileSize = (1024 * 1024 * 3 + (index * 135242)).toLong(), // Simulated size (~3MB)
                    dateModified = System.currentTimeMillis() - ((index + 6) * 3600 * 1000),
                    isSaved = isDemoFileSaved(context, name)
                )
            )
        }
        return list
    }

    private fun isDemoFileSaved(context: Context, fileName: String): Boolean {
        val savedList = getSavedStatuses(context)
        return savedList.any { it.fileName == fileName }
    }

    /**
     * Queries statuses from WhatsApp directory using SAF (Storage Access Framework).
     */
    fun getRecentStatuses(context: Context, grantedUriString: String?): List<StatusItem> {
        if (grantedUriString.isNullOrEmpty()) {
            return emptyList()
        }
        
        val list = mutableListOf<StatusItem>()
        try {
            val rootUri = Uri.parse(grantedUriString)
            val documentFile = DocumentFile.fromTreeUri(context, rootUri)
            if (documentFile != null && documentFile.exists() && documentFile.isDirectory) {
                val targetFolder = documentFile.findFile(".Statuses") ?: documentFile
                val files = targetFolder.listFiles()
                val savedList = getSavedStatuses(context)
                for (file in files) {
                    if (file.isFile && file.name != null) {
                        val name = file.name!!
                        val lowerName = name.lowercase()
                        val isVideo = lowerName.endsWith(".mp4")
                        val isImage = lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg") || lowerName.endsWith(".png")
                        
                        if (isVideo || isImage) {
                            val isSaved = savedList.any { it.fileName == name }
                            list.add(
                                StatusItem(
                                    id = file.uri.toString(),
                                    uriString = file.uri.toString(),
                                    fileName = name,
                                    isVideo = isVideo,
                                    fileSize = file.length(),
                                    dateModified = file.lastModified(),
                                    isSaved = isSaved
                                )
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error listing SAF statuses: ", e)
        }
        return list.sortedByDescending { it.dateModified }
    }

    /**
     * Handles the direct download of a status media item to the user's local device gallery.
     */
    suspend fun downloadMediaToGallery(context: Context, statusItem: StatusItem): Boolean {
        return saveStatus(context, statusItem)
    }

    /**
     * Saves a status item to the app's local cache directory.
     * Supports both real local URIs and web URIs (Demo Mode).
     */
    suspend fun saveStatus(context: Context, statusItem: StatusItem): Boolean = withContext(Dispatchers.IO) {
        try {
            val isDemo = statusItem.id.startsWith("demo_")
            val inputUri = statusItem.uri
            
            val cacheDir = File(context.cacheDir, DIRECTORY_NAME)
            if (!cacheDir.exists()) {
                cacheDir.mkdirs()
            }
            
            val targetFile = File(cacheDir, statusItem.fileName)
            if (targetFile.exists()) {
                return@withContext true
            }

            val success = try {
                targetFile.outputStream().use { outStream ->
                    if (isDemo) {
                        // Download from Unsplash or Google Storage
                        downloadUrlToStream(statusItem.uriString, outStream)
                    } else {
                        // Copy from SAF URI
                        context.contentResolver.openInputStream(inputUri)?.use { inStream ->
                            copyStream(inStream, outStream)
                        } ?: return@withContext false
                    }
                }
                true
            } catch (e: Exception) {
                Log.e(TAG, "Failed to write status file: ", e)
                if (targetFile.exists()) {
                    targetFile.delete()
                }
                false
            }

            success
        } catch (e: Exception) {
            Log.e(TAG, "saveStatus error: ", e)
            false
        }
    }

    /**
     * Scans the app's local cache directory to find files previously saved.
     */
    fun getSavedStatuses(context: Context): List<StatusItem> {
        val list = mutableListOf<StatusItem>()
        val cacheDir = File(context.cacheDir, DIRECTORY_NAME)
        
        if (!cacheDir.exists() || !cacheDir.isDirectory) {
            return list
        }
        
        val files = cacheDir.listFiles() ?: return list
        
        for (file in files) {
            if (file.isFile) {
                val name = file.name
                val lowerName = name.lowercase()
                val isVideo = lowerName.endsWith(".mp4")
                val isImage = lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg") || lowerName.endsWith(".png")
                
                if (isVideo || isImage) {
                    list.add(
                        StatusItem(
                            id = file.absolutePath,
                            uriString = Uri.fromFile(file).toString(),
                            fileName = name,
                            isVideo = isVideo,
                            fileSize = file.length(),
                            dateModified = file.lastModified(),
                            isSaved = true
                        )
                    )
                }
            }
        }
        
        return list.sortedByDescending { it.dateModified }
    }

    private fun parseCursor(cursor: Cursor, isVideo: Boolean): List<StatusItem> {
        val items = mutableListOf<StatusItem>()
        val idCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
        val nameCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
        val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)
        val dateCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED)

        val baseUri = if (isVideo) MediaStore.Video.Media.EXTERNAL_CONTENT_URI else MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        while (cursor.moveToNext()) {
            val idVal = cursor.getLong(idCol)
            val nameVal = cursor.getString(nameCol)
            val sizeVal = cursor.getLong(sizeCol)
            // Date modified in MediaStore is in seconds, convert to ms
            val dateVal = cursor.getLong(dateCol) * 1000 
            val itemUri = Uri.withAppendedPath(baseUri, idVal.toString())

            items.add(
                StatusItem(
                    id = itemUri.toString(),
                    uriString = itemUri.toString(),
                    fileName = nameVal ?: "Unknown",
                    isVideo = isVideo,
                    fileSize = sizeVal,
                    dateModified = dateVal,
                    isSaved = true
                )
            )
        }
        return items
    }

    /**
     * Delete a saved status from the public Gallery.
     */
    fun deleteSavedStatus(context: Context, statusItem: StatusItem): Boolean {
        return try {
            val file = File(statusItem.id) // In getSavedStatuses, we set id to absolutePath
            if (file.exists() && file.isFile) {
                file.delete()
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete saved status: ", e)
            false
        }
    }

    /**
     * Shares a status item. If it is a web URL (Demo Mode), it downloads it to a cache file
     * and shares that file so other apps can render the actual media!
     */
    suspend fun shareStatus(context: Context, statusItem: StatusItem, onReadyToShare: (Intent) -> Unit) = withContext(Dispatchers.IO) {
        try {
            val isDemo = statusItem.id.startsWith("demo_")
            val shareUri: Uri

            if (isDemo) {
                // Download to temporary cache file for sharing
                val cacheDir = File(context.cacheDir, "shared_statuses")
                if (!cacheDir.exists()) cacheDir.mkdirs()
                
                val tempFile = File(cacheDir, statusItem.fileName)
                FileOutputStream(tempFile).use { out ->
                    downloadUrlToStream(statusItem.uriString, out)
                }
                
                shareUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    tempFile
                )
            } else if (statusItem.uri.scheme == "file") {
                val file = File(statusItem.uri.path!!)
                shareUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
            } else {
                shareUri = statusItem.uri
            }

            val mimeType = if (statusItem.isVideo) "video/mp4" else "image/jpeg"
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, shareUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            withContext(Dispatchers.Main) {
                onReadyToShare(Intent.createChooser(shareIntent, "Share Status"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error preparing share: ", e)
        }
    }

    private fun copyStream(input: InputStream, output: OutputStream) {
        val buffer = ByteArray(8192)
        var bytesRead: Int
        while (input.read(buffer).also { bytesRead = it } != -1) {
            output.write(buffer, 0, bytesRead)
        }
    }

    private fun downloadUrlToStream(urlString: String, output: OutputStream) {
        var connection: HttpURLConnection? = null
        var input: InputStream? = null
        try {
            val url = URL(urlString)
            connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 15000
            connection.readTimeout = 15000
            connection.doInput = true
            connection.connect()

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                input = connection.inputStream
                copyStream(input, output)
            } else {
                throw Exception("HTTP error code: ${connection.responseCode}")
            }
        } finally {
            input?.close()
            connection?.disconnect()
        }
    }
}

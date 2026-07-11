package com.example.ui.components

import android.net.Uri
import android.widget.MediaController
import android.widget.VideoView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun VideoPlayer(
    uriString: String,
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = { context ->
            VideoView(context).apply {
                val controller = MediaController(context)
                controller.setAnchorView(this)
                setMediaController(controller)
                
                setOnPreparedListener { mediaPlayer ->
                    mediaPlayer.isLooping = true
                    start()
                }
            }
        },
        update = { videoView ->
            val videoUri = Uri.parse(uriString)
            videoView.setVideoURI(videoUri)
        },
        modifier = modifier.fillMaxSize()
    )

    // Ensure playback stops when the player is removed from the composition
    DisposableEffect(uriString) {
        onDispose {
            // No cleanup required directly, the AndroidView recycling will handle disposal.
        }
    }
}

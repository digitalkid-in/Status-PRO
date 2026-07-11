package com.example.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.graphicsLayer
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.model.StatusItem
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculateCentroidSize
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.input.pointer.PointerInputScope
import kotlin.math.abs

@Composable
fun MediaPreviewDialog(
    initialIndex: Int,
    items: List<StatusItem>,
    onDismiss: () -> Unit,
    onSave: (StatusItem) -> Unit,
    onShare: (StatusItem) -> Unit,
    onDelete: (StatusItem) -> Unit,
    onShareToPlatform: (StatusItem, String) -> Unit
) {
    val context = LocalContext.current
    val pagerState = rememberPagerState(initialPage = initialIndex, pageCount = { items.size })
    val scope = rememberCoroutineScope()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black // Force full-screen cinema black for media previews
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    val statusItem = items[page]
                    // Main Media Container
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 120.dp), // Leave space for controls at the bottom
                        contentAlignment = Alignment.Center
                    ) {
                        if (statusItem.isVideo) {
                            if (page == pagerState.currentPage) {
                                VideoPlayer(uriString = statusItem.uriString)
                            } else {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(statusItem.uri)
                                        .apply {
                                            decoderFactory(coil.decode.VideoFrameDecoder.Factory())
                                        }
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Video Thumbnail",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        } else {
                            var scale by remember { mutableStateOf(1f) }
                            var offsetX by remember { mutableStateOf(0f) }
                            var offsetY by remember { mutableStateOf(0f) }

                            LaunchedEffect(pagerState.currentPage) {
                                if (pagerState.currentPage != page) {
                                    scale = 1f
                                    offsetX = 0f
                                    offsetY = 0f
                                }
                            }

                            BoxWithConstraints(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                val width = constraints.maxWidth.toFloat()
                                val height = constraints.maxHeight.toFloat()

                                val maxOffsetX = ((scale - 1f) * width / 2f).coerceAtLeast(0f)
                                val maxOffsetY = ((scale - 1f) * height / 2f).coerceAtLeast(0f)

                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(statusItem.uri)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Status Preview",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .pointerInput(scale) {
                                            detectTapGestures(
                                                onDoubleTap = { tapOffset ->
                                                    if (scale > 1f) {
                                                        scale = 1f
                                                        offsetX = 0f
                                                        offsetY = 0f
                                                    } else {
                                                        val targetScale = 2.5f
                                                        val centerX = size.width / 2f
                                                        val centerY = size.height / 2f
                                                        val targetMaxOffsetX = ((targetScale - 1f) * width / 2f).coerceAtLeast(0f)
                                                        val targetMaxOffsetY = ((targetScale - 1f) * height / 2f).coerceAtLeast(0f)
                                                        scale = targetScale
                                                        offsetX = ((centerX - tapOffset.x) * (targetScale - 1f)).coerceIn(-targetMaxOffsetX, targetMaxOffsetX)
                                                        offsetY = ((centerY - tapOffset.y) * (targetScale - 1f)).coerceIn(-targetMaxOffsetY, targetMaxOffsetY)
                                                    }
                                                }
                                            )
                                        }
                                        .pointerInput(scale) {
                                            detectTransformGesturesCustom(
                                                onGesture = { _, pan, zoom ->
                                                    scale = (scale * zoom).coerceIn(1f, 4f)
                                                    val currentMaxOffsetX = ((scale - 1f) * width / 2f).coerceAtLeast(0f)
                                                    val currentMaxOffsetY = ((scale - 1f) * height / 2f).coerceAtLeast(0f)
                                                    offsetX = (offsetX + pan.x).coerceIn(-currentMaxOffsetX, currentMaxOffsetX)
                                                    offsetY = (offsetY + pan.y).coerceIn(-currentMaxOffsetY, currentMaxOffsetY)
                                                },
                                                canConsume = { scale > 1f }
                                            )
                                        }
                                        .graphicsLayer(
                                            scaleX = scale,
                                            scaleY = scale,
                                            translationX = offsetX,
                                            translationY = offsetY
                                        ),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }
                    }
                }

                // Left Arrow
                if (pagerState.currentPage > 0) {
                    IconButton(
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(8.dp)
                            .size(48.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Previous",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                // Right Arrow
                if (pagerState.currentPage < items.size - 1) {
                    IconButton(
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(8.dp)
                            .size(48.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Next",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                val currentItem = items[pagerState.currentPage]

                // Top Bar HUD overlay
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Back / Close button
                    IconButton(
                        onClick = onDismiss,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = Color.Black.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Preview",
                            tint = Color.White
                        )
                    }

                    // Metadata Text overlay
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Black.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${pagerState.currentPage + 1}/${items.size}",
                                color = Color.White,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = currentItem.fileName,
                                color = Color.White,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.widthIn(max = 140.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "(${currentItem.formattedSize})",
                                color = Color.LightGray,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                // Bottom Overlay Action Buttons HUD
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.75f))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Quick Share Button
                            FloatingActionButton(
                                onClick = { onShare(currentItem) },
                                containerColor = MaterialTheme.colorScheme.secondary,
                                contentColor = Color.White,
                                shape = CircleShape,
                                modifier = Modifier.size(52.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share Status",
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            // Save Button (only visible if NOT saved yet)
                            androidx.compose.animation.AnimatedContent(targetState = currentItem.isSaved, label = "SaveDeleteAnim") { isSaved ->
                                if (!isSaved) {
                                    FloatingActionButton(
                                        onClick = { onSave(currentItem) },
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = Color.White,
                                        shape = CircleShape,
                                        modifier = Modifier.size(60.dp) // Larger save button as primary action
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Save,
                                            contentDescription = "Save Status to Gallery",
                                            modifier = Modifier.size(26.dp)
                                        )
                                    }
                                } else {
                                    // Saved indicator state or Delete Option
                                    FloatingActionButton(
                                        onClick = { onDelete(currentItem) },
                                        containerColor = MaterialTheme.colorScheme.error,
                                        contentColor = Color.White,
                                        shape = CircleShape,
                                        modifier = Modifier.size(52.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete Saved Status",
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Social direct share row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "SHARE TO:",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = Color.LightGray,
                                modifier = Modifier.padding(end = 4.dp)
                            )
                            listOf(
                                "WHATSAPP" to "PERSONAL",
                                "WA BUSINESS" to "BUSINESS",
                                "INSTAGRAM" to "INSTAGRAM",
                                "FACEBOOK" to "FACEBOOK",
                                "TWITTER" to "TWITTER"
                            ).forEach { (platform, displayName) ->
                                Box(
                                    modifier = Modifier
                                        .background(Color.White.copy(alpha = 0.15f), shape = RoundedCornerShape(100.dp))
                                        .clickable { onShareToPlatform(currentItem, platform) }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = displayName,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

suspend fun PointerInputScope.detectTransformGesturesCustom(
    onGesture: (centroid: androidx.compose.ui.geometry.Offset, pan: androidx.compose.ui.geometry.Offset, zoom: Float) -> Unit,
    canConsume: () -> Boolean
) {
    awaitEachGesture {
        var zoom = 1f
        var pan = androidx.compose.ui.geometry.Offset.Zero
        var pastTouchSlop = false
        val touchSlop = viewConfiguration.touchSlop
        
        awaitFirstDown(requireUnconsumed = false)
        do {
            val event = awaitPointerEvent()
            val canceled = event.changes.any { it.isConsumed }
            if (!canceled) {
                val zoomChange = event.calculateZoom()
                val panChange = event.calculatePan()

                if (!pastTouchSlop) {
                    zoom *= zoomChange
                    pan += panChange

                    val centroid = event.calculateCentroid(useCurrent = false)
                    val centroidSize = event.calculateCentroidSize(useCurrent = false)
                    val panMotion = pan.getDistance()
                    val zoomMotion = abs(1 - zoom) * centroidSize

                    if (panMotion > touchSlop || zoomMotion > touchSlop) {
                        pastTouchSlop = true
                    }
                }

                if (pastTouchSlop) {
                    val centroid = event.calculateCentroid(useCurrent = false)
                    if (zoomChange != 1f || panChange != androidx.compose.ui.geometry.Offset.Zero) {
                        onGesture(centroid, panChange, zoomChange)
                    }
                    if (canConsume() || event.changes.size > 1) {
                        event.changes.forEach {
                            if (it.positionChanged()) {
                                it.consume()
                            }
                        }
                    }
                }
            }
        } while (event.changes.any { it.pressed })
    }
}

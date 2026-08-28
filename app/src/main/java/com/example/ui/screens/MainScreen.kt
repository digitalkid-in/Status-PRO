package com.example.ui.screens
import android.os.Build
import androidx.compose.material3.pulltorefresh.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.verticalScroll
import androidx.compose.animation.core.Animatable

import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.ui.graphics.graphicsLayer

import android.content.Context
import android.provider.DocumentsContract
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.geometry.Offset
import androidx.compose.runtime.saveable.rememberSaveable
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.videoFrameMillis
import com.example.model.StatusItem
import com.example.ui.components.MediaPreviewDialog
import com.example.ui.components.OnboardingTutorial
import com.example.ui.theme.*
import com.example.viewmodel.StatusViewModel
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: StatusViewModel,
    isDarkTheme: Boolean,
    onToggleTheme: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val autoCleanupDays by viewModel.autoCleanupDays.collectAsState()

    // ViewModel States
    val recentImages by viewModel.recentImages.collectAsState()
    val recentVideos by viewModel.recentVideos.collectAsState()
    val savedStatuses by viewModel.savedStatuses.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isDemoMode by viewModel.isDemoMode.collectAsState()
    val grantedTreeUri by viewModel.grantedTreeUri.collectAsState()
    val grantedTreeUriBusiness by viewModel.grantedTreeUriBusiness.collectAsState()
    val isBusinessMode by viewModel.isBusinessMode.collectAsState()
    val lastSavedItem by viewModel.lastSavedItem.collectAsState()
    val showTutorial by viewModel.showTutorial.collectAsState()
    val themePreference by viewModel.themePreference.collectAsState()
    val selectedStatusForPreview by viewModel.selectedStatusForPreview.collectAsState()
    val currentPreviewList by viewModel.currentPreviewList.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()

    var activeTab by remember { mutableIntStateOf(0) } // 0: Images, 1: Videos, 2: Saved, 3: Settings/About
    var isLinkingBusiness by remember { mutableStateOf(false) }
    var isBottomBarVisible by rememberSaveable { mutableStateOf(true) }
    val hazeState = remember { HazeState() }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (available.y < -10f) {
                    isBottomBarVisible = false
                } else if (available.y > 10f) {
                    isBottomBarVisible = true
                }
                return Offset.Zero
            }
        }
    }

    LaunchedEffect(activeTab) {
        isBottomBarVisible = true
    }

    // SAF Document tree selection launcher
    val safLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            try {
                // Request persistable directory URI permission
                val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(uri, takeFlags)
                if (isLinkingBusiness) {
                    viewModel.setBusinessTreeUri(uri.toString())
                } else {
                    viewModel.setTreeUri(uri.toString())
                }
            } catch (e: Exception) {
                Log.e("MainScreen", "Failed to persist SAF permission", e)
                Toast.makeText(context, "Link failed: Try granting access inside Android/media folder", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Toast listener
    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    // Auto-prompt directory permission if active folder is not linked and tutorial is done
    LaunchedEffect(showTutorial, isBusinessMode, grantedTreeUri, grantedTreeUriBusiness) {
        if (!showTutorial) {
            val activeUri = if (isBusinessMode) grantedTreeUriBusiness else grantedTreeUri
            if (activeUri.isNullOrEmpty()) {
                isLinkingBusiness = isBusinessMode
                try {
                    safLauncher.launch(getInitialUri(isBusinessMode))
                } catch (e: Exception) {
                    Log.e("MainScreen", "Failed to launch safLauncher directly", e)
                    safLauncher.launch(null)
                }
            }
        }
    }

    // Render Tutorial Dialog if enabled
    if (showTutorial) {
        OnboardingTutorial(
            onDismiss = { viewModel.completeTutorial() }
        )
    }

    // Render Media Preview Dialog
    selectedStatusForPreview?.let { item ->
        val list = currentPreviewList
        val initialIndex = Math.max(0, list.indexOf(item))
        MediaPreviewDialog(
            initialIndex = initialIndex,
            items = if (list.isEmpty()) listOf(item) else list,
            onDismiss = { viewModel.selectItemForPreview(null) },
            onSave = { viewModel.saveItem(it) },
            onShare = { itemToShare ->
                viewModel.shareItem(itemToShare) { intent ->
                    context.startActivity(intent)
                }
            },
            onDelete = { viewModel.deleteItem(it) },
            onShareToPlatform = { itemToShare, platform ->
                viewModel.shareToPlatform(itemToShare, platform) { intent ->
                    context.startActivity(intent)
                }
            }
        )
    }

    // Direct platform post-save popup dialog
    lastSavedItem?.let { item ->
        Dialog(
            onDismissRequest = { viewModel.clearLastSavedItem() },
            properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(24.dp)
                    )
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(48.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "STATUS SAVED!",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Share it instantly on your favorite social platforms:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Direct share row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val socialPlatforms = listOf(
                            "WHATSAPP" to "PERSONAL",
                            "WA BUSINESS" to "BUSINESS",
                            "INSTAGRAM" to "INSTAGRAM",
                            "FACEBOOK" to "FACEBOOK",
                            "TWITTER" to "TWITTER"
                        )
                        socialPlatforms.forEach { (platform, displayName) ->
                            Box(
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(12.dp))
                                    .clickable {
                                        viewModel.shareToPlatform(item, platform) { intent ->
                                            context.startActivity(intent)
                                        }
                                        viewModel.clearLastSavedItem()
                                    }
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = displayName,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // General Share Option
                    Button(
                        onClick = {
                            viewModel.shareItem(item) { intent ->
                                context.startActivity(intent)
                            }
                            viewModel.clearLastSavedItem()
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("OTHER APPS", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Close Button
                    Text(
                        text = "CLOSE",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clickable { viewModel.clearLastSavedItem() }
                            .padding(8.dp)
                    )
                }
            }
        }
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .statusBarsPadding()
                    .padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 8.dp)
            ) {
                // Header row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "STATUS PRO",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        var expanded by remember { mutableStateOf(false) }
                        
                        Box {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .clickable { expanded = true }
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isBusinessMode) Icons.Default.Business else Icons.Default.Person,
                                    contentDescription = null,
                                    tint = if (isBusinessMode) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isBusinessMode) "BUSINESS" else "PERSONAL",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Select Mode",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            androidx.compose.material3.DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                modifier = Modifier.background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
                            ) {
                                androidx.compose.material3.DropdownMenuItem(
                                    text = { Text("Personal Mode", fontWeight = FontWeight.Medium) },
                                    onClick = {
                                        viewModel.setBusinessMode(false)
                                        expanded = false
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.Person, contentDescription = null, tint = if (!isBusinessMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                )
                                androidx.compose.material3.DropdownMenuItem(
                                    text = { Text("Business Mode", fontWeight = FontWeight.Medium) },
                                    onClick = {
                                        viewModel.setBusinessMode(true)
                                        expanded = false
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.Business, contentDescription = null, tint = if (isBusinessMode) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                )
                            }
                        }

                        // Theme switch button
                        val rotation = remember { Animatable(0f) }
                        val coroutineScope = rememberCoroutineScope()
                        
                        IconButton(
                            onClick = {
                                val nextPref = when (themePreference) {
                                    "system" -> "light"
                                    "light" -> "dark"
                                    else -> "system"
                                }
                                viewModel.setTheme(nextPref)
                                coroutineScope.launch {
                                    rotation.animateTo(
                                        targetValue = rotation.value + 180f,
                                        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
                                    )
                                }
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant, shape = CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Loop,
                                contentDescription = "Toggle Theme ($themePreference)",
                                tint = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier
                                    .size(18.dp)
                            )
                        }
                    }
                }

                // Demo Banner (if active)
                AnimatedVisibility(
                    visible = isDemoMode,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.95f),
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f)
                                    )
                                ),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Demo Mode",
                                    tint = Color.Yellow,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Demo Mode active. Connect a real folder.",
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    isLinkingBusiness = isBusinessMode
                                    safLauncher.launch(null)
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White,
                                    contentColor = MaterialTheme.colorScheme.primary
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text(
                                    text = "Connect",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(nestedScrollConnection)
        ) {
            // Main content sits behind the floating nav bar so it can be seen (blurred) through it
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = innerPadding.calculateTopPadding())
                    .hazeSource(hazeState)
            ) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                androidx.compose.animation.Crossfade(targetState = activeTab, label = "TabTransition") { targetTab ->
                    when (targetTab) {
                        0 -> StatusGrid(
                            statuses = recentImages,
                            onItemClick = { viewModel.selectItemForPreview(it, recentImages) },
                            onSaveClick = { viewModel.saveItem(it) },
                            onShareClick = { itemToShare ->
                                viewModel.shareItem(itemToShare) { intent ->
                                    context.startActivity(intent)
                                }
                            },
                            emptyStateMessage = "The app will automatically open the correct folder if you click allow access.\n\nSimply tap 'USE THIS FOLDER' at the bottom of the screen and select 'Allow'.",
                            isDemoMode = isDemoMode,
                            onActionClicked = {
                                isLinkingBusiness = isBusinessMode
                                safLauncher.launch(getInitialUri(isBusinessMode))
                            },
                            onDownloadAllClick = { viewModel.downloadAll(recentImages) },
                            isRefreshing = isLoading,
                            onRefresh = { viewModel.refresh() }
                        )
                        1 -> StatusGrid(
                            statuses = recentVideos,
                            onItemClick = { viewModel.selectItemForPreview(it, recentVideos) },
                            onSaveClick = { viewModel.saveItem(it) },
                            onShareClick = { itemToShare ->
                                viewModel.shareItem(itemToShare) { intent ->
                                    context.startActivity(intent)
                                }
                            },
                            emptyStateMessage = "The app will automatically open the correct folder if you click allow access.\n\nSimply tap 'USE THIS FOLDER' at the bottom of the screen and select 'Allow'.",
                            isDemoMode = isDemoMode,
                            onActionClicked = {
                                isLinkingBusiness = isBusinessMode
                                safLauncher.launch(getInitialUri(isBusinessMode))
                            },
                            onDownloadAllClick = { viewModel.downloadAll(recentVideos) },
                            isRefreshing = isLoading,
                            onRefresh = { viewModel.refresh() }
                        )
                        2 -> SavedGalleryTab(
                            statuses = savedStatuses,
                            onItemClick = { viewModel.selectItemForPreview(it, savedStatuses) },
                            onShareClick = { itemToShare ->
                                viewModel.shareItem(itemToShare) { intent ->
                                    context.startActivity(intent)
                                }
                            },
                            emptyStateMessage = "Your Saved Gallery is empty! Tap Save on any status to save it here forever.",
                            isRefreshing = isLoading,
                            onRefresh = { viewModel.refresh() }
                        )
                        3 -> SettingsTab(
                            viewModel = viewModel,
                            grantedTreeUri = grantedTreeUri,
                            grantedTreeUriBusiness = grantedTreeUriBusiness,
                            isDemoMode = isDemoMode,
                            onLinkFolderClick = {
                                isLinkingBusiness = false
                                safLauncher.launch(getInitialUri(false))
                            },
                            onLinkBusinessFolderClick = {
                                isLinkingBusiness = true
                                safLauncher.launch(getInitialUri(true))
                            },
                            themePreference = themePreference
                        )
                    }
                }
            }
            }

            // Floating bottom bar overlay: tutorial banner + frosted nav bar with auto-hide on scroll down
            AnimatedVisibility(
                visible = isBottomBarVisible,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(durationMillis = 200)),
                exit = slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing)
                ) + fadeOut(animationSpec = tween(durationMillis = 200)),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                ) {
                    // Dismissible Quick Tutorial banner
                    val showBannerTutorial by viewModel.showQuickTutorialBanner.collectAsState()
                    AnimatedVisibility(
                        visible = showBannerTutorial,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    shape = RoundedCornerShape(24.dp)
                                )
                                .padding(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .background(MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(14.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Save,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "Quick Tutorial",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                        Text(
                                            text = "Tap any status to preview or share instantly.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "DISMISS",
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .clickable { viewModel.dismissQuickTutorialBanner() }
                                        .padding(8.dp)
                                )
                            }
                        }
                    }

                    // Navigation Bar — frosted glass with translucent depth and smooth blur
                    val isDark = isSystemInDarkTheme() || themePreference == "dark"
                    val navBarTintColor = if (isDark) {
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.68f)
                    } else {
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.72f)
                    }

                    NavigationBar(
                        containerColor = Color.Transparent,
                        tonalElevation = 0.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(12.dp, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                            .hazeEffect(state = hazeState) {
                                blurRadius = 16.dp
                                tints = listOf(HazeTint(navBarTintColor))
                                noiseFactor = 0.03f
                            }
                            .border(
                                width = 1.dp,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        if (isDark) Color.White.copy(alpha = 0.22f) else Color.White.copy(alpha = 0.55f),
                                        Color.Transparent
                                    )
                                ),
                                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                            )
                    ) {
                        val items = listOf(
                            Triple("IMAGES", Icons.Default.Image, 0),
                            Triple("VIDEOS", Icons.Default.PlayCircle, 1),
                            Triple("SAVED", Icons.Default.Folder, 2),
                            Triple("SETTINGS", Icons.Default.Settings, 3)
                        )

                        items.forEach { (label, icon, index) ->
                            NavigationBarItem(
                                selected = activeTab == index,
                                onClick = { activeTab = index },
                                icon = { 
                                    Icon(
                                        imageVector = icon, 
                                        contentDescription = label,
                                        modifier = Modifier.size(24.dp)
                                    ) 
                                },
                                label = { 
                                    Text(
                                        text = label, 
                                        style = MaterialTheme.typography.labelSmall, 
                                        fontWeight = if (activeTab == index) FontWeight.ExtraBold else FontWeight.SemiBold
                                    ) 
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f),
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusGrid(
    statuses: List<StatusItem>,
    onItemClick: (StatusItem) -> Unit,
    onSaveClick: (StatusItem) -> Unit,
    onShareClick: (StatusItem) -> Unit,
    emptyStateMessage: String,
    isDemoMode: Boolean,
    onActionClicked: () -> Unit,
    onDownloadAllClick: (() -> Unit)? = null,
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = {}
) {
    if (statuses.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f), shape = CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Explore,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "NOTHING HERE YET",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = emptyStateMessage,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(32.dp))
            if (onActionClicked != {}) {
                Button(
                    onClick = onActionClicked,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FolderOpen,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Allow Access", fontWeight = FontWeight.Bold)
                }
            }
        }
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            if (onDownloadAllClick != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${statuses.size} ITEMS DETECTED",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )

                    Button(
                        onClick = onDownloadAllClick,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Download All",
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "DOWNLOAD ALL",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = onRefresh,
                modifier = Modifier.fillMaxSize().weight(1f)
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 140.dp),
                    contentPadding = PaddingValues(start = 12.dp, top = 12.dp, end = 12.dp, bottom = 120.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                items(statuses, key = { it.id }) { item ->
                    StatusCard(
                        statusItem = item,
                        onClick = { onItemClick(item) },
                        onSave = { onSaveClick(item) },
                        onShare = { onShareClick(item) }
                    )
                }
            }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedGalleryTab(
    statuses: List<StatusItem>,
    onItemClick: (StatusItem) -> Unit,
    onShareClick: (StatusItem) -> Unit,
    emptyStateMessage: String,
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = {}
) {
    if (statuses.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(Color(0xFF4CAF50).copy(alpha = 0.1f), shape = CircleShape)
                    .border(2.dp, Color(0xFF4CAF50).copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.FolderOpen,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = Color(0xFF4CAF50)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "NO SAVED ITEMS",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = emptyStateMessage,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        val now = System.currentTimeMillis()
        val twentyFourHoursAgo = now - (24 * 60 * 60 * 1000)
        
        // Filter elements saved in the last 24 hours
        val savedRecently = statuses.filter { it.dateModified in twentyFourHoursAgo..now }

        Column(modifier = Modifier.fillMaxSize()) {
            if (savedRecently.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                // Section Title with AccessTime Icon and Active Green Dot Indicator
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = "Recent Saves Icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SAVED RECENTLY (LAST 24H)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(Color(0xFF4CAF50), shape = CircleShape)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "• ${savedRecently.size} items",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Bold
                    )
                }

                // Horizontal Carousel of Recently Saved Items
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(savedRecently, key = { "recent_${it.id}" }) { item ->
                        Box(
                            modifier = Modifier
                                .width(135.dp)
                        ) {
                            StatusCard(
                                statusItem = item,
                                onClick = { onItemClick(item) },
                                onSave = {}, // already saved
                                onShare = { onShareClick(item) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Header for all saved items
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = "Gallery Icon",
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ALL SAVED GALLERY",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "• ${statuses.size} items",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Standard grid containing all saved items
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = onRefresh,
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 140.dp),
                    contentPadding = PaddingValues(start = 12.dp, top = 12.dp, end = 12.dp, bottom = 120.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                items(statuses, key = { it.id }) { item ->
                    StatusCard(
                        statusItem = item,
                        onClick = { onItemClick(item) },
                        onSave = {}, // already saved
                        onShare = { onShareClick(item) }
                    )
                }
            }
            }
        }
    }
}

@Composable
fun StatusCard(
    statusItem: StatusItem,
    onClick: () -> Unit,
    onSave: () -> Unit,
    onShare: () -> Unit
) {
    val context = LocalContext.current

    val alpha = remember { Animatable(0f) }
    val offsetY = remember { Animatable(50f) }

    LaunchedEffect(statusItem.id) {
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 500)
        )
    }
    LaunchedEffect(statusItem.id) {
        offsetY.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                this.alpha = alpha.value
                this.translationY = offsetY.value
            }
            .aspectRatio(3f / 4f)
            .shadow(6.dp, shape = RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Main media thumbnail using Coil (for web images or local/SAF content URIs)
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(statusItem.uri)
                    .apply {
                        if (statusItem.isVideo) {
                            decoderFactory(coil.decode.VideoFrameDecoder.Factory())
                        }
                    }
                    .crossfade(true)
                    .build(),
                contentDescription = "Status Thumbnail",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Gradient Overlay for bottom text readability
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.5f)
                    .align(Alignment.BottomCenter)
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f)
                            )
                        )
                    )
            )

            // Bottom left type badge (SAVED only — PHOTO/VIDEO type label removed)
            if (statusItem.isSaved) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                        .background(Color(0xFF4CAF50), shape = RoundedCornerShape(100.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(12.dp).padding(end = 4.dp)
                        )
                        Text(
                            text = "SAVED",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            // Top right actions tray
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Share action (plain Box, not IconButton, so its size isn't
                // expanded by Material3's 48dp minimum touch target — that
                // expansion was what made the two circles appear to overlap)
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color.Black.copy(alpha = 0.6f), shape = CircleShape)
                        .clickable(onClick = onShare),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Save action
                androidx.compose.animation.Crossfade(targetState = statusItem.isSaved, label = "SaveStatus") { isSaved ->
                    if (!isSaved) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(MaterialTheme.colorScheme.primary, shape = CircleShape)
                                .clickable(onClick = onSave),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Save",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color(0xFF4CAF50), shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Saved",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsTab(
    viewModel: StatusViewModel,
    grantedTreeUri: String?,
    grantedTreeUriBusiness: String?,
    isDemoMode: Boolean,
    onLinkFolderClick: () -> Unit,
    onLinkBusinessFolderClick: () -> Unit,
    themePreference: String
) {
    val context = LocalContext.current
    val autoCleanupDays by viewModel.autoCleanupDays.collectAsState()
    val showFolderTip by viewModel.showSettingsFolderTip.collectAsState()

    if (showFolderTip) {
        AlertDialog(
            onDismissRequest = { /* Must tap "Got it" — an accidental outside tap shouldn't silently mark this permanently seen */ },
            properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
            title = { Text("Folder Setup Required", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "To automatically detect statuses, link your WhatsApp status folder:\n\n" +
                        "1. Tap \"Link Folder\" on the Personal Directory card below.\n" +
                        "2. Navigate to: Internal Storage → Android → media → com.whatsapp → WhatsApp → Media → .Statuses\n" +
                        "3. Tap \"Use this folder\" to grant access.\n\n" +
                        "For WhatsApp Business, use the Business Directory card and select the com.whatsapp.w4b folder instead."
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissSettingsFolderTip() }) {
                    Text("Got it")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 120.dp)
    ) {
        Text(
            text = "Settings & Directory Setup",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // 1a. Storage Folder Access Panel - Personal
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isSystemInDarkTheme() || themePreference == "dark") DarkCardBg else MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .shadow(2.dp, RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.FolderOpen,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Personal Directory",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (grantedTreeUri.isNullOrEmpty()) {
                        "No folder linked yet. Link your Personal status folder to automatically load your seen statuses."
                    } else {
                        "Linked! Scoping path:\n$grantedTreeUri"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onLinkFolderClick,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Link,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (grantedTreeUri.isNullOrEmpty()) "Link Folder" else "Change Folder")
                    }
                }
            }
        }

        // 1b. Storage Folder Access Panel - Business
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isSystemInDarkTheme() || themePreference == "dark") DarkCardBg else MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .shadow(2.dp, RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.FolderOpen,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Business Directory",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (grantedTreeUriBusiness.isNullOrEmpty()) {
                        "No folder linked yet. Link your Business status folder to load business statuses."
                    } else {
                        "Linked! Scoping path:\n$grantedTreeUriBusiness"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onLinkBusinessFolderClick,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Link,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (grantedTreeUriBusiness.isNullOrEmpty()) "Link Business Folder" else "Change Folder")
                    }
                }
            }
        }

        // 3. App Theme Panel
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isSystemInDarkTheme() || themePreference == "dark") DarkCardBg else MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .shadow(2.dp, RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LightMode,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "App Theme Profile",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val themes = listOf("system" to "System", "light" to "Light", "dark" to "Dark")
                    themes.forEach { (pref, title) ->
                        val isSelected = themePreference == pref
                        OutlinedButton(
                            onClick = { viewModel.setTheme(pref) },
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                contentColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 4.dp)
                        ) {
                            Text(text = title, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }

        // 4. Auto Cleanup Panel
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isSystemInDarkTheme() || themePreference == "dark") DarkCardBg else MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .shadow(2.dp, RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Auto Cleanup Saved Statuses",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Automatically delete old saved statuses to free up storage.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val durations = listOf(0 to "Off", 7 to "7 Days", 30 to "1 Month", 90 to "3 Months")
                    durations.forEach { (days, title) ->
                        val isSelected = autoCleanupDays == days
                        OutlinedButton(
                            onClick = { viewModel.setAutoCleanupDays(days) },
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.errorContainer else Color.Transparent,
                                contentColor = if (isSelected) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 2.dp)
                            ,
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                        ) {
                            Text(text = title, style = MaterialTheme.typography.labelSmall, maxLines = 1)
                        }
                    }
                }
            }
        }

        // 5. Showcase Demo Mode Panel
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isSystemInDarkTheme() || themePreference == "dark") DarkCardBg else MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .shadow(2.dp, RoundedCornerShape(16.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Showcase Demo Mode",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Fill status list with premium mock photos and videos.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = isDemoMode,
                    onCheckedChange = { checked ->
                        viewModel.toggleDemoMode(checked)
                    }
                )
            }
        }

        // 4. Help & Feedback Panel
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isSystemInDarkTheme() || themePreference == "dark") DarkCardBg else MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .shadow(2.dp, RoundedCornerShape(16.dp))
                .clickable {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = android.net.Uri.parse("mailto:support@digitalkid.in")
                        putExtra(Intent.EXTRA_SUBJECT, "Feedback for Status Pro App")
                    }
                    try {
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        android.widget.Toast.makeText(context, "No email client found", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Help,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Help & Feedback",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Contact support or send us your feedback.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Folder instructions guide text
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Folder Setup Instructions:",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "The app will automatically open the correct folder for your Android version.\n\nSimply tap 'USE THIS FOLDER' at the bottom of the screen and select 'Allow'.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

fun getInitialUri(isBusiness: Boolean): Uri {
    val basePath = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        if (isBusiness) {
            "primary%3AAndroid%2Fmedia%2Fcom.whatsapp.w4b%2FWhatsApp%20Business%2FMedia"
        } else {
            "primary%3AAndroid%2Fmedia%2Fcom.whatsapp%2FWhatsApp%2FMedia"
        }
    } else {
        if (isBusiness) {
            "primary%3AWhatsApp%20Business%2FMedia"
        } else {
            "primary%3AWhatsApp%2FMedia"
        }
    }
    val treeUri = Uri.parse("content://com.android.externalstorage.documents/tree/$basePath")
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        try {
            val docId = Uri.decode(basePath)
            DocumentsContract.buildDocumentUriUsingTree(treeUri, docId)
        } catch (e: Exception) {
            treeUri
        }
    } else {
        treeUri
    }
}

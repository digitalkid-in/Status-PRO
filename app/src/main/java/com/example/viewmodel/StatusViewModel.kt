package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.StatusItem
import com.example.util.StorageHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StatusViewModel(application: Application) : AndroidViewModel(application) {
    private val context: Context get() = getApplication()
    private val prefs = context.getSharedPreferences("status_saver_prefs", Context.MODE_PRIVATE)

    // UI States
    private val _recentImages = MutableStateFlow<List<StatusItem>>(emptyList())
    val recentImages: StateFlow<List<StatusItem>> = _recentImages.asStateFlow()

    private val _recentVideos = MutableStateFlow<List<StatusItem>>(emptyList())
    val recentVideos: StateFlow<List<StatusItem>> = _recentVideos.asStateFlow()

    private val _savedStatuses = MutableStateFlow<List<StatusItem>>(emptyList())
    val savedStatuses: StateFlow<List<StatusItem>> = _savedStatuses.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isDemoMode = MutableStateFlow(false)
    val isDemoMode: StateFlow<Boolean> = _isDemoMode.asStateFlow()

    private val _grantedTreeUri = MutableStateFlow<String?>(null)
    val grantedTreeUri: StateFlow<String?> = _grantedTreeUri.asStateFlow()

    private val _grantedTreeUriBusiness = MutableStateFlow<String?>(null)
    val grantedTreeUriBusiness: StateFlow<String?> = _grantedTreeUriBusiness.asStateFlow()

    private val _isBusinessMode = MutableStateFlow(false)
    val isBusinessMode: StateFlow<Boolean> = _isBusinessMode.asStateFlow()

    private val _lastSavedItem = MutableStateFlow<StatusItem?>(null)
    val lastSavedItem: StateFlow<StatusItem?> = _lastSavedItem.asStateFlow()

    private val _showTutorial = MutableStateFlow(true)
    val showTutorial: StateFlow<Boolean> = _showTutorial.asStateFlow()

    private val _showQuickTutorialBanner = MutableStateFlow(true)
    val showQuickTutorialBanner: StateFlow<Boolean> = _showQuickTutorialBanner.asStateFlow()

    private val _showSettingsFolderTip = MutableStateFlow(true)
    val showSettingsFolderTip: StateFlow<Boolean> = _showSettingsFolderTip.asStateFlow()

    // 3-way Theme Selection: "system", "light", "dark"
    private val _themePreference = MutableStateFlow("system")
    val themePreference: StateFlow<String> = _themePreference.asStateFlow()

    private val _autoCleanupDays = MutableStateFlow(0)
    val autoCleanupDays: StateFlow<Int> = _autoCleanupDays.asStateFlow()

    private val _selectedStatusForPreview = MutableStateFlow<StatusItem?>(null)
    val selectedStatusForPreview: StateFlow<StatusItem?> = _selectedStatusForPreview.asStateFlow()

    private val _currentPreviewList = MutableStateFlow<List<StatusItem>>(emptyList())
    val currentPreviewList: StateFlow<List<StatusItem>> = _currentPreviewList.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    init {
        // Load configurations
        _grantedTreeUri.value = prefs.getString("granted_tree_uri", null)
        _grantedTreeUriBusiness.value = prefs.getString("granted_tree_uri_business", null)
        _isBusinessMode.value = prefs.getBoolean("is_business_mode", false)
        _showTutorial.value = prefs.getBoolean("show_tutorial_v2", true)
        _showQuickTutorialBanner.value = prefs.getBoolean("show_quick_tutorial_banner", true)
        _showSettingsFolderTip.value = prefs.getBoolean("show_settings_folder_tip", true)
        _themePreference.value = prefs.getString("theme_pref", "system") ?: "system"
        _autoCleanupDays.value = prefs.getInt("auto_cleanup_days", 0)

        _isDemoMode.value = if (_isBusinessMode.value) _grantedTreeUriBusiness.value.isNullOrEmpty() else _grantedTreeUri.value.isNullOrEmpty()

        refresh()
    }

    /**
     * Refreshes the lists of statuses.
     */
    fun refresh() {
        triggerAutoCleanup()
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Fetch saved gallery files (always real on-device files)
                val saved = withContext(Dispatchers.IO) {
                    StorageHelper.getSavedStatuses(context)
                }
                _savedStatuses.value = saved

                if (_isDemoMode.value) {
                    // Populate with beautiful mock statuses
                    val demoList = withContext(Dispatchers.IO) {
                        StorageHelper.getDemoStatuses(context)
                    }
                    _recentImages.value = demoList.filter { !it.isVideo }
                    _recentVideos.value = demoList.filter { it.isVideo }
                } else {
                    // Fetch real statuses using SAF Tree URI
                    val activeUri = if (_isBusinessMode.value) _grantedTreeUriBusiness.value else _grantedTreeUri.value
                    val realList = withContext(Dispatchers.IO) {
                        StorageHelper.getRecentStatuses(context, activeUri)
                    }
                    _recentImages.value = realList.filter { !it.isVideo }
                    _recentVideos.value = realList.filter { it.isVideo }
                }
            } catch (e: Exception) {
                Log.e("StatusViewModel", "Error refreshing status lists: ", e)
                showToast("Failed to reload list")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Handles the direct download of a status media item to the user's local device gallery.
     */
    fun downloadMediaToGallery(item: StatusItem) {
        saveItem(item)
    }

    /**
     * Saves a status permanently into the public Gallery.
     */
    fun saveItem(item: StatusItem) {
        viewModelScope.launch {
            _isLoading.value = true
            val success = StorageHelper.saveStatus(context, item)
            _isLoading.value = false
            if (success) {
                showToast("Saved to your Gallery! 🎉")
                _lastSavedItem.value = item
                refresh()
                // Update selected item preview state if open
                val currentPreview = _selectedStatusForPreview.value
                if (currentPreview != null && currentPreview.id == item.id) {
                    _selectedStatusForPreview.value = currentPreview.copy(isSaved = true)
                }
            } else {
                showToast("Could not save status")
            }
        }
    }

    /**
     * Downloads (saves) all unsaved items of a specific type with one click.
     */
    fun downloadAll(items: List<StatusItem>) {
        val unsaved = items.filter { !it.isSaved }
        if (unsaved.isEmpty()) {
            showToast("All items are already saved! 👍")
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            var successCount = 0
            withContext(Dispatchers.IO) {
                unsaved.forEach { item ->
                    val success = StorageHelper.saveStatus(context, item)
                    if (success) {
                        successCount++
                    }
                }
            }
            _isLoading.value = false
            if (successCount > 0) {
                showToast("Successfully saved $successCount items to Gallery! 🎉")
                refresh()
            } else {
                showToast("Failed to save some statuses.")
            }
        }
    }

    /**
     * Share a status to a specific social media platform (Instagram, Facebook, Twitter).
     */
    fun shareToPlatform(item: StatusItem, platform: String, onStartIntent: (Intent) -> Unit) {
        viewModelScope.launch {
            showToast("Preparing to share... ⏳")
            StorageHelper.shareStatus(context, item) { baseIntent ->
                val pkg = when (platform.uppercase()) {
                    "INSTAGRAM" -> "com.instagram.android"
                    "FACEBOOK" -> "com.facebook.katana"
                    "TWITTER" -> "com.twitter.android"
                    "WHATSAPP" -> "com.whatsapp"
                    "WA BUSINESS", "BUSINESS WA" -> "com.whatsapp.w4b"
                    else -> null
                }
                if (pkg != null) {
                    baseIntent.`package` = pkg
                }
                try {
                    onStartIntent(baseIntent)
                } catch (e: Exception) {
                    // Fallback to standard share chooser
                    baseIntent.`package` = null
                    val displayName = when(platform.uppercase()) {
                        "WHATSAPP" -> "Personal Client"
                        "WA BUSINESS" -> "Business Client"
                        else -> platform
                    }
                    onStartIntent(Intent.createChooser(baseIntent, "Share to $displayName"))
                    showToast("$displayName is not installed. Using standard share.")
                }
            }
        }
    }

    fun clearLastSavedItem() {
        _lastSavedItem.value = null
    }

    /**
     * Deletes a saved status from the public Gallery.
     */
    fun deleteItem(item: StatusItem) {
        viewModelScope.launch {
            val success = withContext(Dispatchers.IO) {
                StorageHelper.deleteSavedStatus(context, item)
            }
            if (success) {
                showToast("Removed from Saved Gallery")
                if (_selectedStatusForPreview.value?.id == item.id) {
                    _selectedStatusForPreview.value = null // Close preview if deleted
                }
                refresh()
            } else {
                showToast("Could not delete file")
            }
        }
    }

    /**
     * Shares a status item.
     */
    fun shareItem(item: StatusItem, onReadyToShare: (Intent) -> Unit) {
        viewModelScope.launch {
            showToast("Preparing to share... ⏳")
            StorageHelper.shareStatus(context, item, onReadyToShare)
        }
    }

    /**
     * Triggers folder permissions linked through SAF.
     */
    fun setTreeUri(uri: String) {
        prefs.edit().putString("granted_tree_uri", uri).apply()
        _grantedTreeUri.value = uri
        _isDemoMode.value = false
        showToast("Personal folder linked! 🔗")
        refresh()
    }

    fun setBusinessTreeUri(uri: String) {
        prefs.edit().putString("granted_tree_uri_business", uri).apply()
        _grantedTreeUriBusiness.value = uri
        _isDemoMode.value = false
        showToast("Business folder linked! 🔗")
        refresh()
    }

    fun setBusinessMode(isBusiness: Boolean) {
        prefs.edit().putBoolean("is_business_mode", isBusiness).apply()
        _isBusinessMode.value = isBusiness

        _isDemoMode.value = if (isBusiness) _grantedTreeUriBusiness.value.isNullOrEmpty() else _grantedTreeUri.value.isNullOrEmpty()

        showToast(if (isBusiness) "Switched to Business Mode 💼" else "Switched to Personal Mode 💬")
        refresh()
    }

    /**
     * Toggles Demo Mode on and off.
     */
    fun toggleDemoMode(enable: Boolean) {
        _isDemoMode.value = enable
        refresh()
    }

    /**
     * Completes and saves the tutorial status.
     */
    fun completeTutorial() {
        prefs.edit().putBoolean("show_tutorial_v2", false).apply()
        _showTutorial.value = false
    }

    /**
     * Reset/Show tutorial again.
     */
    fun showTutorialAgain() {
        prefs.edit().putBoolean("show_tutorial_v2", true).apply()
        _showTutorial.value = true
    }

    /**
     * Dismisses the small "Quick Tutorial" banner permanently.
     */
    fun dismissQuickTutorialBanner() {
        prefs.edit().putBoolean("show_quick_tutorial_banner", false).apply()
        _showQuickTutorialBanner.value = false
    }

    /**
     * Dismisses the first-time Settings folder setup tip permanently.
     */
    fun dismissSettingsFolderTip() {
        prefs.edit().putBoolean("show_settings_folder_tip", false).apply()
        _showSettingsFolderTip.value = false
    }

    /**
     * Sets the application theme preference.
     */
    fun setAutoCleanupDays(days: Int) {
        prefs.edit().putInt("auto_cleanup_days", days).apply()
        _autoCleanupDays.value = days
        triggerAutoCleanup()
    }

    private fun triggerAutoCleanup() {
        val days = _autoCleanupDays.value
        if (days > 0) {
            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    val saved = StorageHelper.getSavedStatuses(context)
                    val cutoff = System.currentTimeMillis() - (days * 24L * 60L * 60L * 1000L)
                    val toDelete = saved.filter { it.dateModified < cutoff }
                    var deletedAny = false
                    for (item in toDelete) {
                        try {
                            if (StorageHelper.deleteSavedStatus(context, item)) {
                                deletedAny = true
                            }
                        } catch (e: Exception) {
                            Log.e("StatusViewModel", "Failed to auto-clean status", e)
                        }
                    }
                    if (deletedAny) {
                        // Reload saved statuses after cleanup
                        val newSaved = StorageHelper.getSavedStatuses(context)
                        _savedStatuses.value = newSaved
                    }
                }
            }
        }
    }

    fun setTheme(theme: String) {
        prefs.edit().putString("theme_pref", theme).apply()
        _themePreference.value = theme
    }

    fun selectItemForPreview(item: StatusItem?, list: List<StatusItem> = emptyList()) {
        _selectedStatusForPreview.value = item
        if (item != null) {
            _currentPreviewList.value = list
        } else {
            _currentPreviewList.value = emptyList()
        }
    }

    private fun showToast(msg: String) {
        _toastMessage.value = msg
    }

    fun clearToast() {
        _toastMessage.value = null
    }
}

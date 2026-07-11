#!/bin/bash
sed -i '/val themePreference: StateFlow<String> = _themePreference.asStateFlow()/a\
\
    private val _autoCleanupDays = MutableStateFlow(0)\
    val autoCleanupDays: StateFlow<Int> = _autoCleanupDays.asStateFlow()' app/src/main/java/com/example/viewmodel/StatusViewModel.kt

sed -i '/_themePreference.value = prefs.getString("theme_pref", "system") ?: "system"/a\
        _autoCleanupDays.value = prefs.getInt("auto_cleanup_days", 0)' app/src/main/java/com/example/viewmodel/StatusViewModel.kt

sed -i '/fun refresh() {/a\
        triggerAutoCleanup()' app/src/main/java/com/example/viewmodel/StatusViewModel.kt

sed -i '/fun setTheme(theme: String) {/i\
    fun setAutoCleanupDays(days: Int) {\
        prefs.edit().putInt("auto_cleanup_days", days).apply()\
        _autoCleanupDays.value = days\
        triggerAutoCleanup()\
    }\
\
    private fun triggerAutoCleanup() {\
        val days = _autoCleanupDays.value\
        if (days > 0) {\
            viewModelScope.launch {\
                withContext(Dispatchers.IO) {\
                    val saved = StorageHelper.getSavedStatuses(context)\
                    val cutoff = System.currentTimeMillis() - (days * 24L * 60L * 60L * 1000L)\
                    val toDelete = saved.filter { it.dateModified < cutoff }\
                    var deletedAny = false\
                    for (item in toDelete) {\
                        try {\
                            if (StorageHelper.deleteSavedStatus(context, item)) {\
                                deletedAny = true\
                            }\
                        } catch (e: Exception) {\
                            Log.e("StatusViewModel", "Failed to auto-clean status", e)\
                        }\
                    }\
                    if (deletedAny) {\
                        // Reload saved statuses after cleanup\
                        val newSaved = StorageHelper.getSavedStatuses(context)\
                        _savedStatuses.value = newSaved\
                    }\
                }\
            }\
        }\
    }\
' app/src/main/java/com/example/viewmodel/StatusViewModel.kt

# Codebase Index — Status PRO

## 📌 Project Overview
**Status PRO** is a native Android application built with **Kotlin** and **Jetpack Compose** (Material Design 3). It enables users to view, save, organize, and share status media (images and videos) cached by instant messaging apps (WhatsApp / WhatsApp Business) via Android's **Storage Access Framework (SAF)**, with a fallback **Demo Mode** using remote media assets.

---

## 📁 Repository Directory Structure

```
Status-PRO/
├── app/                                  # Android Application Module
│   ├── src/main/
│   │   ├── AndroidManifest.xml          # Manifest file (Permissions, Activities)
│   │   ├── java/com/example/
│   │   │   ├── MainActivity.kt          # Entry activity & edge-to-edge theme wrapper
│   │   │   ├── model/
│   │   │   │   └── StatusItem.kt        # Core status media data model
│   │   │   ├── util/
│   │   │   │   └── StorageHelper.kt     # SAF, File IO, Demo media & Gallery helper
│   │   │   ├── viewmodel/
│   │   │   │   └── StatusViewModel.kt   # App state management & business logic
│   │   │   └── ui/
│   │   │       ├── screens/
│   │   │       │   ├── MainScreen.kt    # Main UI screen with tabs, search, pull-refresh
│   │   │       │   └── TestPTR.kt       # Pull-to-refresh test component
│   │   │       ├── components/
│   │   │       │   ├── MediaPreviewDialog.kt  # Fullscreen image/video viewer modal
│   │   │       │   ├── VideoPlayer.kt         # ExoPlayer wrapper for video playback
│   │   │       │   └── OnboardingTutorial.kt  # Interactive onboarding dialogs
│   │   │       └── theme/
│   │   │           ├── Color.kt         # Modern color palette definition
│   │   │           ├── Theme.kt         # Material3 theme setup & dark mode rules
│   │   │           └── Type.kt          # Typography styles
│   └── build.gradle.kts                 # App module dependencies & build config
├── gradle/                              # Gradle wrapper files
├── build.gradle.kts                     # Root build configuration
├── settings.gradle.kts                  # Gradle project settings
├── gradle.properties                    # Gradle configuration properties
├── metadata.json                        # App metadata configuration
├── PLAY_STORE_LISTING.md                # Store listing descriptions & copy
├── README.md                            # Setup guide & local execution instructions
├── Status PRO Prototype (Standalone).html # Interactive HTML/JS standalone prototype
├── patch.kt                             # Kotlin patch script snippet
├── fix_imports.py                       # Python script for automated import fixing
├── fix_mainscreen.py                    # Python script for MainScreen code fixes
├── patch_460.py                         # Python patch utility script
├── patch_mainscreen.py                  # Python patch tool for MainScreen layout updates
├── patch_animation.sh                   # Shell script for animation adjustments
├── patch_mainscreen.sh                  # Shell script for updating MainScreen UI logic
└── patch_viewmodel.sh                  # Shell script for ViewModel refactoring
```

---

## 🧩 Key Architecture & Components

### 1. Main Entry & Configuration
* [MainActivity.kt](file:///c:/Users/anon/Documents/GitHub/Status-PRO/app/src/main/java/com/example/MainActivity.kt): Initializes Coil image loader with video frame decoder factory, splash screen API, edge-to-edge layout support, theme preference observation, and renders `MainScreen`.
* [AndroidManifest.xml](file:///c:/Users/anon/Documents/GitHub/Status-PRO/app/src/main/AndroidManifest.xml): Configures Android components, FileProvider authorities for sharing, permissions, and splash screen settings.

### 2. State & Business Logic Layer
* [StatusViewModel.kt](file:///c:/Users/anon/Documents/GitHub/Status-PRO/app/src/main/java/com/example/viewmodel/StatusViewModel.kt): Manages application state flows (`StateFlow`):
  * `recentImages`, `recentVideos`, `savedStatuses`
  * `isDemoMode`, `isBusinessMode`, `grantedTreeUri`
  * `themePreference` ("system", "light", "dark")
  * `autoCleanupDays` (automatic background file purging)
  * `selectedStatusForPreview`, `toastMessage`, `showTutorial`
* [StatusItem.kt](file:///c:/Users/anon/Documents/GitHub/Status-PRO/app/src/main/java/com/example/model/StatusItem.kt): Data class representing status media:
  * `id`, `uriString`, `fileName`, `isVideo`, `fileSize`, `dateModified`, `isSaved`
  * `formattedSize` calculated helper property.

### 3. Storage & I/O Engine
* [StorageHelper.kt](file:///c:/Users/anon/Documents/GitHub/Status-PRO/app/src/main/java/com/example/util/StorageHelper.kt): Singleton object executing filesystem operations:
  * SAF DocumentFile tree parsing for `.Statuses` folder.
  * Demo Mode fallback status generation using remote Unsplash images & Google Storage videos.
  * Async downloads and copy operations (`saveStatus`, `downloadMediaToGallery`).
  * FileProvider URI generation and social share intent dispatching (`shareStatus`).
  * Local cache storage and deletion (`deleteSavedStatus`).

### 4. User Interface Layer (Jetpack Compose)
* [MainScreen.kt](file:///c:/Users/anon/Documents/GitHub/Status-PRO/app/src/main/java/com/example/ui/screens/MainScreen.kt): Core composable screen featuring:
  * Navigation bar: Images, Videos, Saved Gallery tabs.
  * Personal vs Business WhatsApp directory toggle.
  * Search/filter bar, batch download button ("Save All").
  * Theme switcher & Auto-cleanup configuration sheet.
  * Pull-to-refresh mechanism.
* [MediaPreviewDialog.kt](file:///c:/Users/anon/Documents/GitHub/Status-PRO/app/src/main/java/com/example/ui/components/MediaPreviewDialog.kt): High-resolution fullscreen media dialog supporting multi-touch pinch-to-zoom, swipe navigation, saving, sharing, and deletion.
* [VideoPlayer.kt](file:///c:/Users/anon/Documents/GitHub/Status-PRO/app/src/main/java/com/example/ui/components/VideoPlayer.kt): Video engine integrating AndroidX Media3 / ExoPlayer for in-app video status playback.
* [OnboardingTutorial.kt](file:///c:/Users/anon/Documents/GitHub/Status-PRO/app/src/main/java/com/example/ui/components/OnboardingTutorial.kt): Interactive guide explaining SAF folder permissions and app features.

### 5. Theme & Styling
* [Color.kt](file:///c:/Users/anon/Documents/GitHub/Status-PRO/app/src/main/java/com/example/ui/theme/Color.kt): Deep purple/indigo & vibrant accent color palette.
* [Theme.kt](file:///c:/Users/anon/Documents/GitHub/Status-PRO/app/src/main/java/com/example/ui/theme/Theme.kt): Material3 Light & Dark color schemes and dynamic color logic.
* [Type.kt](file:///c:/Users/anon/Documents/GitHub/Status-PRO/app/src/main/java/com/example/ui/theme/Type.kt): Material3 Typography configuration.

---

## 🛠️ Utility Scripts & Assets
* **Standalone Interactive Prototype**: [Status PRO Prototype (Standalone).html](file:///c:/Users/anon/Documents/GitHub/Status-PRO/Status%20PRO%20Prototype%20(Standalone).html) — Fully functional single-file web prototype demonstrating app features in browser.
* **Store Assets**: [PLAY_STORE_LISTING.md](file:///c:/Users/anon/Documents/GitHub/Status-PRO/PLAY_STORE_LISTING.md) — Optimized marketing title, short description, key feature bullet points, and full store copy.
* **Code Refactoring Automation**:
  * [fix_imports.py](file:///c:/Users/anon/Documents/GitHub/Status-PRO/fix_imports.py)
  * [fix_mainscreen.py](file:///c:/Users/anon/Documents/GitHub/Status-PRO/fix_mainscreen.py)
  * [patch_mainscreen.py](file:///c:/Users/anon/Documents/GitHub/Status-PRO/patch_mainscreen.py)
  * [patch_mainscreen.sh](file:///c:/Users/anon/Documents/GitHub/Status-PRO/patch_mainscreen.sh)
  * [patch_viewmodel.sh](file:///c:/Users/anon/Documents/GitHub/Status-PRO/patch_viewmodel.sh)

---

## ⚡ Core Technical Features Matrix

| Feature | Implementation | Key Files |
| :--- | :--- | :--- |
| **Storage Access Framework (SAF)** | DocumentFile tree access for `.Statuses` | [`StorageHelper.kt`](file:///c:/Users/anon/Documents/GitHub/Status-PRO/app/src/main/java/com/example/util/StorageHelper.kt) |
| **Demo Mode** | Unsplash & Google Storage remote fallback | [`StorageHelper.kt`](file:///c:/Users/anon/Documents/GitHub/Status-PRO/app/src/main/java/com/example/util/StorageHelper.kt) |
| **State Management** | StateFlow & ViewModel coroutine scope | [`StatusViewModel.kt`](file:///c:/Users/anon/Documents/GitHub/Status-PRO/app/src/main/java/com/example/viewmodel/StatusViewModel.kt) |
| **Video Playback** | Media3 ExoPlayer & Coil VideoFrameDecoder | [`VideoPlayer.kt`](file:///c:/Users/anon/Documents/GitHub/Status-PRO/app/src/main/java/com/example/ui/components/VideoPlayer.kt), [`MainActivity.kt`](file:///c:/Users/anon/Documents/GitHub/Status-PRO/app/src/main/java/com/example/MainActivity.kt) |
| **Dynamic Theme** | Light/Dark/System preferences | [`Theme.kt`](file:///c:/Users/anon/Documents/GitHub/Status-PRO/app/src/main/java/com/example/ui/theme/Theme.kt), [`MainActivity.kt`](file:///c:/Users/anon/Documents/GitHub/Status-PRO/app/src/main/java/com/example/MainActivity.kt) |
| **Social Sharing** | Intent.ACTION_SEND with FileProvider URIs | [`StorageHelper.kt`](file:///c:/Users/anon/Documents/GitHub/Status-PRO/app/src/main/java/com/example/util/StorageHelper.kt) |
| **Auto Cleanup** | Scheduled file purging based on modification age | [`StatusViewModel.kt`](file:///c:/Users/anon/Documents/GitHub/Status-PRO/app/src/main/java/com/example/viewmodel/StatusViewModel.kt) |

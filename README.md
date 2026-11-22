# IPTV Player App

A modern Android IPTV player built with **Kotlin**, **Jetpack Compose**, **ExoPlayer (Media3)**, and **MVVM architecture**.

## Features

- 📺 **M3U/M3U8 Playlist Support** - Load playlists from URL, local file, or pasted content
- 🎬 **ExoPlayer Integration** - Hardware-accelerated video playback with adaptive streaming (HLS, DASH)
- 🔍 **Search & Filter** - Real-time channel search and category filtering
- ⭐ **Favorites** - Mark channels as favorites for quick access
- 🕒 **Recent Channels** - Quick access to recently watched channels
- 🎨 **Modern UI** - Material 3 design with dark theme (TV-friendly)
- 📱 **Picture-in-Picture** - Continue watching while using other apps
- 🔊 **Audio/Subtitle Tracks** - Multi-track support (UI ready)
- 📂 **File Import** - Import M3U files via system file picker

## Tech Stack

| Component | Technology |
|-----------|------------|
| Language | Kotlin 1.9+ |
| UI Framework | Jetpack Compose (Material 3) |
| Architecture | MVVM + Repository Pattern |
| Video Player | ExoPlayer (Media3) 1.4+ |
| Async | Coroutines + Flow |
| DI | Manual (ViewModel Factory) |
| Image Loading | Coil 2.6+ |
| Serialization | Kotlinx Serialization |
| Navigation | Navigation Compose |

## Project Structure

```
app/
├── src/main/java/com/example/iptvapp/
│   ├── IPTVApplication.kt          # Application class + ViewModel Factory
│   ├── model/
│   │   └── Channel.kt              # Data models (Channel, Playlist, PlaylistSource)
│   ├── parser/
│   │   └── M3UParser.kt            # M3U playlist parser
│   ├── data/
│   │   └── PlaylistRepository.kt   # Repository for playlist management
│   ├── ui/
│   │   ├── MainViewModel.kt        # Main screen ViewModel
│   │   ├── PlayerViewModel.kt      # Player screen ViewModel
│   │   ├── MainActivity.kt         # Main activity + Navigation
│   │   ├── screen/
│   │   │   ├── ChannelListScreen.kt # Channel list with search/categories
│   │   │   └── PlayerScreen.kt     # Full-screen player with controls
│   │   └── theme/
│   │       ├── Color.kt            # Color schemes (Light/Dark)
│   │       ├── Theme.kt            # App theme composition
│   │       └── Type.kt             # Typography
│   └── ...
├── src/main/res/
│   ├── values/strings.xml
│   ├── values/themes.xml
│   ├── values/colors.xml
│   └── xml/ (backup rules)
├── build.gradle.kts
└── proguard-rules.pro
```

## Getting Started

### Prerequisites

- **Android Studio** Koala (2024.1.1) or later
- **JDK 17** (included with Android Studio)
- **Android SDK** API 34 (Android 14)
- Minimum device/emulator: **API 24 (Android 7.0)**

### Build & Run

1. **Clone/Open the project:**
   ```bash
   cd IPTVApp
   ```

2. **Open in Android Studio:**
   - File → Open → Select `IPTVApp` folder
   - Wait for Gradle sync to complete

3. **Run on device/emulator:**
   - Connect Android device (USB debugging on) or create emulator
   - Click **Run** (▶️) or press `Shift+F10`

### Testing with Sample Playlist

A sample `sample_playlist.m3u` is included with test streams from Mux.

**To load it:**
1. Tap the **"+" (LiveTv)** icon in the top app bar
2. Select the `sample_playlist.m3u` file from the file picker
3. Or paste the content manually (feature can be added)

**Or load via URL in code:**
```kotlin
// In MainViewModel or Repository
repository.loadFromUrl("https://your-server.com/playlist.m3u")
```

## Key Components

### M3U Parser (`M3UParser.kt`)
Parses standard M3U/M3U8 playlists with support for:
- `#EXTINF` attributes (tvg-id, tvg-name, tvg-logo, group-title, etc.)
- `#EXTGRP` for grouping
- Catch-up, audio-track, aspect-ratio extensions

### Repository (`PlaylistRepository.kt`)
Manages playlist state with:
- Multiple source support (URL, file, pasted content)
- Category extraction & filtering
- Search functionality
- Favorites & recent channels tracking
- Loading/error states via StateFlow

### Player (`PlayerScreen.kt` + `PlayerViewModel.kt`)
Full-featured player with:
- ExoPlayer integration via `PlayerView`
- Play/pause, seek (±10s), progress slider
- Volume control with mute
- Buffered progress indication
- Channel info overlay
- Settings menu (audio, subtitles, quality - UI ready)
- Error handling with retry
- Auto-hiding controls

### UI Screens

**ChannelListScreen:**
- Top app bar with search & category menu
- Horizontal category chips
- LazyColumn with channel cards (logo, name, group, favorite)
- Pull-to-refresh ready (can be added)
- Empty/loading/error states

**PlayerScreen:**
- Full-screen immersive video
- Tap to toggle controls
- Double-tap to seek forward 10s
- Top bar with back, info, settings, PiP
- Bottom controls with progress, time, play/pause, seek, volume
- Channel info dialog
- Settings dropdown

## Adding Features

### EPG (Electronic Program Guide)
```kotlin
// 1. Add EPG parser (XMLTV format)
// 2. Extend Channel model with EPG data
// 3. Add EPG screen with timeline view
// 4. Integrate with player for current/next program
```

### Xtream Codes / Stalker Portal
```kotlin
// 1. Add API client for Xtream Codes
// 2. Authentication flow
// 3. Live TV, VOD, Series categories
// 4. EPG from portal
```

### Chromecast Support
```kotlin
// 1. Add Cast SDK dependency
// 2. Implement MediaRouteButton
// 3. Create CastReceiver/OptionsProvider
// 4. Handle session management
```

### DRM Support (Widevine/PlayReady)
```kotlin
// In ExoPlayer setup:
val drmSessionManager = DefaultDrmSessionManager.Builder()
    .setKeyRequestParameters(...)
    .build()
exoPlayer.setMediaItem(mediaItem, drmSessionManager)
```

### Offline Download
```kotlin
// Use ExoPlayer's DownloadService + DownloadTracker
// Add DownloadHelper for HLS/DASH
// Manage downloads in background service
```

## Configuration

### Gradle Properties (`gradle.properties`)
```properties
org.gradle.jvmargs=-Xmx4g -Dfile.encoding=UTF-8
android.useAndroidX=true
android.enableJetifier=true
kotlin.code.style=official
```

### Build Variants
```kotlin
// In app/build.gradle.kts
buildTypes {
    debug { isDebuggable = true }
    release {
        isMinifyEnabled = true
        isShrinkResources = true
        proguardFiles(...)
    }
}
```

## Permissions

| Permission | Purpose |
|------------|---------|
| `INTERNET` | Stream playback |
| `ACCESS_NETWORK_STATE` | Network awareness |
| `FOREGROUND_SERVICE_MEDIA_PLAYBACK` | Background playback |
| `WAKE_LOCK` | Keep screen on during playback |
| `READ_MEDIA_VIDEO` | Import local M3U files |
| `POST_NOTIFICATIONS` | Media controls in notification |

## Known Limitations / TODO

- [ ] Add "Add Playlist from URL" dialog
- [ ] EPG integration (XMLTV parser)
- [ ] Xtream Codes / Stalker Portal support
- [ ] Chromecast sender support
- [ ] Picture-in-Picture implementation
- [ ] Audio/Subtitle track selection UI
- [ ] Playback speed control
- [ ] Quality selector (ABR)
- [ ] Settings screen (theme, player options)
- [ ] Room database for persistent favorites/history
- [ ] DataStore for app preferences
- [ ] MediaSession/Notification for background playback
- [ ] Unit & UI tests
- [ ] Crashlytics/Analytics integration

## License

This project is for educational purposes. Ensure you have rights to stream any content you play.

## Contributing

1. Fork the repository
2. Create feature branch
3. Commit changes
4. Push and create Pull Request

---

**Built with ❤️ using modern Android development practices**
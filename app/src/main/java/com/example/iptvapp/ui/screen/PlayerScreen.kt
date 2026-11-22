package com.example.iptvapp.ui.screen

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.PictureInPicture
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.AudioTrack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.window.PopupProperties
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.MediaItemProcessor
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.exoplayer.trackselection.MappingTrackSelector
import androidx.media3.exoplayer.ui.PlayerView
import androidx.media3.ui.AspectRatioFrameLayout
import com.example.iptvapp.model.Channel
import com.example.iptvapp.ui.theme.IPTVTheme
import kotlinx.coroutines.flow.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.iptvapp.ui.PlayerViewModel
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class, UnstableApi::class)
@Composable
fun PlayerScreen(
    channel: Channel,
    viewModel: PlayerViewModel = viewModel(),
    onBackPress: () -> Unit
) {
    val context = LocalContext.current
    val player = remember { createExoPlayer(context, channel) }
    val exoPlayer = remember { player }

    // Initialize player with channel
    LaunchedEffect(channel) {
        viewModel.initializePlayer(exoPlayer, channel)
    }

    // Cleanup on dispose
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    // Observe player state
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val currentPosition by viewModel.currentPosition.collectAsStateWithLifecycle()
    val duration by viewModel.duration.collectAsStateWithLifecycle()
    val bufferedPosition by viewModel.bufferedPosition.collectAsStateWithLifecycle()
    val playbackError by viewModel.playbackError.collectAsStateWithLifecycle()
    val showControls by viewModel.showControls.collectAsStateWithLifecycle()

    var controlsVisible by remember { mutableStateOf(true) }
    var showSettingsMenu by remember { mutableStateOf(false) }
    var volume by remember { mutableStateOf(1.0f) }
    var isMuted by remember { mutableStateOf(false) }
    var isFullscreen by remember { mutableStateOf(true) }
    var showInfo by remember { mutableStateOf(false) }

    // Auto-hide controls
    LaunchedEffect(controlsVisible, isPlaying) {
        if (controlsVisible && isPlaying) {
            try {
                Thread.sleep(3000)
                controlsVisible = false
            } catch (e: InterruptedException) {
                // Ignore
            }
        }
    }

    IPTVTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { controlsVisible = !controlsVisible },
                        onDoubleTap = { viewModel.seekForward(10000) }
                    )
                }
        ) {
            // ExoPlayer View
            AndroidPlayerView(
                player = exoPlayer,
                modifier = Modifier.fillMaxSize(),
                channel = channel
            )

            // Top App Bar
            if (controlsVisible || showInfo) {
                TopAppBar(
                    modifier = Modifier.fillMaxWidth(),
                    title = {
                        Text(
                            channel.name,
                            color = Color.White,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.TextOverflow.Ellipsis
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackPress) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    actions = {
                        if (!showInfo) {
                            IconButton(onClick = { showInfo = true }) {
                                Icon(Icons.Filled.Info, contentDescription = "Channel Info", tint = Color.White)
                            }
                            IconButton(onClick = { showSettingsMenu = true }) {
                                Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = Color.White)
                            }
                            IconButton(onClick = { viewModel.togglePiP() }) {
                                Icon(Icons.Filled.PictureInPicture, contentDescription = "PiP", tint = Color.White)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White
                    )
                )
            }

            // Bottom Controls
            if (controlsVisible) {
                PlayerControls(
                    isPlaying = isPlaying,
                    currentPosition = currentPosition,
                    duration = duration,
                    bufferedPosition = bufferedPosition,
                    onPlayPauseClick = { viewModel.playPause() },
                    onSeekBackClick = { viewModel.seekBackward(10000) },
                    onSeekForwardClick = { viewModel.seekForward(10000) },
                    onPositionChange = { pos -> viewModel.seekTo(pos) },
                    volume = volume,
                    isMuted = isMuted,
                    onVolumeChange = { vol ->
                        volume = vol
                        exoPlayer.volume = vol
                    },
                    onMuteClick = {
                        isMuted = !isMuted
                        exoPlayer.volume = if (isMuted) 0f else volume
                    }
                )
            }

            // Channel Info Overlay
            if (showInfo) {
                ChannelInfoOverlay(
                    channel = channel,
                    onDismiss = { showInfo = false },
                    onClose = { showInfo = false }
                )
            }

            // Settings Menu
            if (showSettingsMenu) {
                SettingsMenu(
                    onDismiss = { showSettingsMenu = false },
                    player = exoPlayer,
                    channel = channel
                )
            }

            // Error Overlay
            playbackError?.let { error ->
                ErrorOverlay(
                    message = error,
                    onRetry = { viewModel.retry() },
                    onDismiss = { viewModel.clearError() }
                )
            }
        }
    }
}

@Composable
fun AndroidPlayerView(
    player: ExoPlayer,
    modifier: Modifier,
    channel: Channel
) {
    val context = LocalContext.current
    androidView(
        factory = { ctx ->
            val playerView = PlayerView(ctx).apply {
                this.player = player
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                useController = false
                layoutParams = android.widget.FrameLayout.LayoutParams(
                    android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                    android.widget.FrameLayout.LayoutParams.MATCH_PARENT
                )
            }
            playerView
        },
        update = { playerView ->
            playerView.player = player
            playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
        },
        modifier = modifier
    )
}

@Composable
fun PlayerControls(
    isPlaying: Boolean,
    currentPosition: Long,
    duration: Long,
    bufferedPosition: Long,
    onPlayPauseClick: () -> Unit,
    onSeekBackClick: () -> Unit,
    onSeekForwardClick: () -> Unit,
    onPositionChange: (Long) -> Unit,
    volume: Float,
    isMuted: Boolean,
    onVolumeChange: (Float) -> Unit,
    onMuteClick: () -> Unit
) {
    val progress = if (duration > 0) currentPosition.toFloat() / duration.toFloat() else 0f
    val bufferedProgress = if (duration > 0) bufferedPosition.toFloat() / duration.toFloat() else 0f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontal,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Progress Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(32.dp)
                .fillMaxWidth()
        ) {
            // Buffered progress
            androidx.compose.foundation.Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                if (bufferedProgress > 0) {
                    drawRect(
                        color = Color.White.copy(alpha = 0.3f),
                        topLeft = androidx.compose.ui.geometry.Offset.Zero,
                        size = androidx.compose.ui.geometry.Size(
                            size.width * bufferedProgress,
                            size.height
                        )
                    )
                }
            }

            // Main progress
            Slider(
                value = progress,
                onValueChange = { newProgress ->
                    val newPosition = (newProgress * duration).toLong()
                    onPositionChange(newPosition)
                },
                onValueChangeFinished = { /* seek done */ },
                modifier = Modifier.fillMaxSize(),
                colors = androidx.compose.material3.SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = Color.White,
                    inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                ),
                thumbSize = 16.dp
            )
        }

        // Time labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                formatTime(currentPosition),
                color = Color.White,
                fontSize = 12.sp
            )
            Text(
                formatTime(duration),
                color = Color.White,
                fontSize = 12.sp
            )
        }

        // Control Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Seek Back
            IconButton(onClick = onSeekBackClick) {
                Icon(Icons.Filled.Replay10, contentDescription = "Rewind 10s", tint = Color.White, modifier = Modifier.size(32.dp))
            }

            // Play/Pause
            IconButton(onClick = onPlayPauseClick) {
                Icon(
                    imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" : "Play",
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }

            // Seek Forward
            IconButton(onClick = onSeekForwardClick) {
                Icon(Icons.Filled.Forward10, contentDescription = "Forward 10s", tint = Color.White, modifier = Modifier.size(32.dp))
            }
        }

        // Volume Control
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onMuteClick) {
                Icon(
                    imageVector = if (isMuted || volume == 0f) Icons.Filled.VolumeOff else Icons.Filled.VolumeUp,
                    contentDescription = if (isMuted) "Unmute" : "Mute",
                    tint = Color.White
                )
            }
            Slider(
                value = if (isMuted) 0f else volume,
                onValueChange = onVolumeChange,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                enabled = !isMuted,
                colors = androidx.compose.material3.SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = Color.White,
                    inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                )
            )
        }
    }
}

@Composable
fun ChannelInfoOverlay(
    channel: Channel,
    onDismiss: () -> Unit,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .clickable(onClick = onDismiss)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .height(300.dp),
            colors = CardDefaults.cardColors(
                containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface,
                contentColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
            ),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Channel Information", fontSize = 20.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    IconButton(onClick = onClose) {
                        Icon(Icons.Filled.Close, contentDescription = "Close")
                    }
                }
                Divider()
                InfoRow("Name", channel.name)
                InfoRow("URL", channel.url)
                channel.group?.let { InfoRow("Group", it) }
                channel.tvgId?.let { InfoRow("TVG ID", it) }
                channel.tvgCountry?.let { InfoRow("Country", it) }
                channel.tvgLanguage?.let { InfoRow("Language", it) }
                channel.catchup?.let { InfoRow("Catch-up", it) }
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
        Text(value, fontSize = 14.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Medium, maxLines = 1, overflow = androidx.compose.ui.text.TextOverflow.Ellipsis)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsMenu(
    onDismiss: () -> Unit,
    player: ExoPlayer,
    channel: Channel
) {
    val popupProperties = remember { PopupProperties(focusable = true) }
    val context = LocalContext.current

    androidx.compose.material3.DropdownMenu(
        expanded = true,
        onDismissRequest = onDismiss,
        properties = popupProperties
    ) {
        DropdownMenuItem(
            text = { Text("Audio Tracks") },
            leadingIcon = { Icon(Icons.Filled.AudioTrack, contentDescription = "Audio") },
            onClick = {
                // TODO: Show audio track selection
                onDismiss()
            }
        )
        DropdownMenuItem(
            text = { Text("Subtitles") },
            leadingIcon = { Icon(Icons.Filled.ClosedCaption, contentDescription = "Subtitles") },
            onClick = {
                // TODO: Show subtitle selection
                onDismiss()
            }
        )
        DropdownMenuItem(
            text = { Text("Playback Speed") },
            leadingIcon = { Icon(Icons.Filled.Settings, contentDescription = "Speed") },
            onClick = {
                // TODO: Show speed selector
                onDismiss()
            }
        )
        DropdownMenuItem(
            text = { Text("Quality") },
            leadingIcon = { Icon(Icons.Filled.Settings, contentDescription = "Quality") },
            onClick = {
                // TODO: Show quality selector
                onDismiss()
            }
        )
        androidx.compose.material3.Divider()
        DropdownMenuItem(
            text = { Text("Report Issue", color = androidx.compose.material3.MaterialTheme.colorScheme.error) },
            leadingIcon = { Icon(Icons.Filled.Info, contentDescription = "Report", tint = androidx.compose.material3.MaterialTheme.colorScheme.error) },
            onClick = {
                // TODO: Report broken stream
                onDismiss()
            }
        )
    }
}

@Composable
fun ErrorOverlay(
    message: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f))
            .clickable(onClick = onDismiss)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = androidx.compose.material3.MaterialTheme.colorScheme.errorContainer,
                contentColor = androidx.compose.material3.MaterialTheme.colorScheme.onErrorContainer
            ),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontal,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                androidx.compose.material.icons.filled.Error()
                Text("Playback Error", fontSize = 20.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                Text(message, fontSize = 14.sp, textAlign = androidx.compose.ui.text.TextAlign.Center)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    androidx.compose.material3.OutlinedButton(onClick = onRetry) {
                        Text("Retry")
                    }
                    androidx.compose.material3.TextButton(onClick = onDismiss) {
                        Text("Dismiss")
                    }
                }
            }
        }
    }
}

fun formatTime(timeMs: Long): String {
    if (timeMs < 0) return "0:00"
    val totalSeconds = timeMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}
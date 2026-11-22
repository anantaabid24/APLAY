package com.example.iptvapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class PlayerViewModel : ViewModel() {

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow().distinctUntilChanged()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition = _currentPosition.asStateFlow().distinctUntilChanged()

    private val _duration = MutableStateFlow(0L)
    val duration = _duration.asStateFlow().distinctUntilChanged()

    private val _bufferedPosition = MutableStateFlow(0L)
    val bufferedPosition = _bufferedPosition.asStateFlow().distinctUntilChanged()

    private val _playbackError = MutableStateFlow<String?>(null)
    val playbackError = _playbackError.asStateFlow().distinctUntilChanged()

    private val _showControls = MutableStateFlow(true)
    val showControls = _showControls.asStateFlow().distinctUntilChanged()

    private var player: ExoPlayer? = null
    private var positionUpdateJob: kotlinx.coroutines.Job? = null

    fun initializePlayer(exoPlayer: ExoPlayer, channel: com.example.iptvapp.model.Channel) {
        player = exoPlayer
        _playbackError.value = null

        // Set up media item
        val mediaItem = com.google.android.exoplayer2.MediaItem.fromUri(channel.url)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()

        // Start playback
        exoPlayer.playWhenReady = true

        // Add listener for state updates
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                _isPlaying.value = (playbackState == Player.STATE_READY && exoPlayer.playWhenReady)
            }

            override fun onPlayerError(error: PlaybackException) {
                _playbackError.value = error.message ?: "Playback error occurred"
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
            }
        })

        // Start position updates
        startPositionUpdates()
    }

    private fun startPositionUpdates() {
        positionUpdateJob?.cancel()
        positionUpdateJob = viewModelScope.launch {
            while (true) {
                player?.let { p ->
                    _currentPosition.value = p.currentPosition
                    _duration.value = p.duration
                    _bufferedPosition.value = p.bufferedPosition
                }
                try {
                    Thread.sleep(500)
                } catch (e: InterruptedException) {
                    break
                }
            }
        }
    }

    fun playPause() {
        player?.let { p ->
            p.playWhenReady = !p.playWhenReady
            _isPlaying.value = p.playWhenReady
        }
    }

    fun seekTo(position: Long) {
        player?.seekTo(position)
    }

    fun seekForward(amount: Long) {
        player?.let { p ->
            val newPosition = (p.currentPosition + amount).coerceAtMost(p.duration)
            p.seekTo(newPosition)
        }
    }

    fun seekBackward(amount: Long) {
        player?.let { p ->
            val newPosition = (p.currentPosition - amount).coerceAtLeast(0)
            p.seekTo(newPosition)
        }
    }

    fun retry() {
        player?.let { p ->
            _playbackError.value = null
            p.prepare()
            p.playWhenReady = true
        }
    }

    fun clearError() {
        _playbackError.value = null
    }

    fun togglePiP() {
        // TODO: Implement Picture-in-Picture
        // Requires Android 8.0+ and proper setup in manifest
    }

    override fun onCleared() {
        positionUpdateJob?.cancel()
        player?.release()
        super.onCleared()
    }
}
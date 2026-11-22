package com.example.iptvapp

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.example.iptvapp.data.PlaylistRepository
import com.example.iptvapp.parser.M3UParser
import com.example.iptvapp.ui.MainViewModel
import com.example.iptvapp.ui.PlayerViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class IPTVApplication : Application() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        // Initialize any global components here
    }

    override fun onTerminate() {
        scope.cancel()
        super.onTerminate()
    }
}

// ViewModel Factory
class ViewModelFactory(
    private val playlistRepository: PlaylistRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        return when (modelClass) {
            MainViewModel::class.java -> MainViewModel(playlistRepository) as T
            PlayerViewModel::class.java -> PlayerViewModel() as T
            else -> throw IllegalArgumentException("Unknown ViewModel class: $modelClass")
        }
    }
}

// Repository Module
object RepositoryModule {
    private var playlistRepository: PlaylistRepository? = null

    fun getPlaylistRepository(application: Application): PlaylistRepository {
        return playlistRepository ?: synchronized(this) {
            playlistRepository ?: PlaylistRepository(M3UParser()).also { playlistRepository = it }
        }
    }
}
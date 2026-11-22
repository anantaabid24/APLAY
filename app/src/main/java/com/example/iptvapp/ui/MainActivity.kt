package com.example.iptvapp.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.iptvapp.model.Channel
import com.example.iptvapp.ui.screen.ChannelListScreen
import com.example.iptvapp.ui.screen.PlayerScreen
import com.example.iptvapp.ui.theme.IPTVTheme

class MainActivity : ComponentActivity() {

    private val pickFileLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                readM3UFromUri(uri)
            }
        }
    }

    private val viewModel: MainViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            IPTVTheme {
                Surface(
                    modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val navHostController = navController

                    NavHost(navHostController, startDestination = "channel_list") {
                        composable("channel_list") {
                            ChannelListScreen(
                                viewModel = viewModel,
                                onChannelClick = { channel ->
                                    navHostController.navigate("player/${channel.id}")
                                },
                                onAddPlaylistClick = { showAddPlaylistDialog() }
                            )
                        }
                        composable(
                            route = "player/{channelId}",
                            arguments = listOf(androidx.navigation.navArgument("channelId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val channelId = backStackEntry.getString() ?: ""
                            val channel = viewModel.channels.value?.firstOrNull { it.id == channelId }
                            channel?.let { ch ->
                                PlayerScreen(
                                    channel = ch,
                                    onBackPress = { navHostController.popBackStack() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun showAddPlaylistDialog() {
        // Show dialog with options: URL input, File picker, Paste M3U
        // For now, just show file picker
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "text/plain"
            addCategory(Intent.CATEGORY_OPENABLE)
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("text/plain", "application/x-mpegurl", "application/vnd.apple.mpegurl"))
        }
        pickFileLauncher.launch(intent)
    }

    private fun readM3UFromUri(uri: Uri) {
        contentResolver.openInputStream(uri)?.use { inputStream ->
            val content = inputStream.readText()
            viewModel.loadPlaylistFromContent(content, "Imported Playlist")
        }
    }
}
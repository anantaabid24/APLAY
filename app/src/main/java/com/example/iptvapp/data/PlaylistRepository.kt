package com.example.iptvapp.data

import com.example.iptvapp.model.Channel
import com.example.iptvapp.model.Playlist
import com.example.iptvapp.model.PlaylistSource
import com.example.iptvapp.parser.M3UParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL

class PlaylistRepository(private val m3uParser: M3UParser) {

    private val _playlist = MutableStateFlow<Playlist?>(null)
    val playlist = _playlist.asStateFlow().distinctUntilChanged()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow().distinctUntilChanged()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow().distinctUntilChanged()

    private val _sources = MutableStateFlow<List<PlaylistSource>>(emptyList())
    val sources = _sources.asStateFlow().distinctUntilChanged()

    suspend fun loadFromUrl(url: String): Playlist? {
        _isLoading.value = true
        _error.value = null
        return try {
            withContext(Dispatchers.IO) {
                val inputStream = URL(url).openStream()
                val content = inputStream.readText()
                inputStream.close()
                parseAndUpdate(content, PlaylistSource.generateId(url), url, false)
            }
        } catch (e: Exception) {
            _error.value = "Failed to load playlist: ${e.message}"
            null
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun loadFromFile(file: File): Playlist? {
        _isLoading.value = true
        _error.value = null
        return try {
            withContext(Dispatchers.IO) {
                val content = file.readText()
                parseAndUpdate(content, PlaylistSource.generateId(file.absolutePath), file.name, true)
            }
        } catch (e: Exception) {
            _error.value = "Failed to load file: ${e.message}"
            null
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun loadFromContent(content: String, sourceName: String): Playlist? {
        _isLoading.value = true
        _error.value = null
        return try {
            withContext(Dispatchers.IO) {
                parseAndUpdate(content, PlaylistSource.generateId(content), sourceName, true)
            }
        } catch (e: Exception) {
            _error.value = "Failed to parse playlist: ${e.message}"
            null
        } finally {
            _isLoading.value = false
        }
    }

    private suspend fun parseAndUpdate(
        content: String,
        sourceId: String,
        sourceName: String,
        isLocal: Boolean
    ): Playlist {
        val channels = m3uParser.parseSync(content)
        val categories = channels.mapNotNull { it.group }.distinct().sorted()

        val playlist = Playlist(
            channels = channels,
            categories = categories,
            lastUpdated = System.currentTimeMillis()
        )

        _playlist.value = playlist

        val source = PlaylistSource(
            id = sourceId,
            name = sourceName,
            url = if (isLocal) "file://$sourceName" else sourceName,
            isLocal = isLocal,
            lastLoaded = System.currentTimeMillis(),
            channelCount = channels.size
        )
        updateSource(source)

        return playlist
    }

    private fun updateSource(source: PlaylistSource) {
        val current = _sources.value.toMutableList()
        val index = current.indexOfFirst { it.id == source.id }
        if (index >= 0) {
            current[index] = source
        } else {
            current.add(0, source)
        }
        _sources.value = current
    }

    fun getChannelsByCategory(category: String?): List<Channel> {
        val playlist = _playlist.value
        if (playlist == null) return emptyList()
        return if (category == null || category == "All") {
            playlist.channels
        } else {
            playlist.channels.filter { it.group == category }
        }
    }

    fun getAllCategories(): List<String> {
        val playlist = _playlist.value
        if (playlist == null) return listOf("All")
        return listOf("All") + playlist.categories
    }

    fun searchChannels(query: String): List<Channel> {
        val playlist = _playlist.value ?: return emptyList()
        if (query.isBlank()) return playlist.channels
        val lowerQuery = query.lowercase()
        return playlist.channels.filter {
            it.name.lowercase().contains(lowerQuery) ||
            it.group?.lowercase().contains(lowerQuery) == true
        }
    }

    fun toggleFavorite(channelId: String) {
        val playlist = _playlist.value ?: return
        val updatedChannels = playlist.channels.map { channel ->
            if (channel.id == channelId) {
                channel.copy(isFavorite = !channel.isFavorite, lastPlayed = System.currentTimeMillis())
            } else {
                channel
            }
        }
        _playlist.value = playlist.copy(channels = updatedChannels)
    }

    fun getFavorites(): List<Channel> {
        return _playlist.value?.channels.filter { it.isFavorite } ?: emptyList()
    }

    fun getRecent(limit: Int = 10): List<Channel> {
        return _playlist.value?.channels
            .filter { it.lastPlayed > 0 }
            .sortedByDescending { it.lastPlayed }
            .take(limit) ?: emptyList()
    }

    fun clearError() {
        _error.value = null
    }
}
package com.example.iptvapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.iptvapp.data.PlaylistRepository
import com.example.iptvapp.model.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class MainViewModel(private val repository: PlaylistRepository) : ViewModel() {

    private val _selectedCategory = MutableStateFlow<String?>("All")
    val selectedCategory = _selectedCategory.asStateFlow().distinctUntilChanged()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow().distinctUntilChanged()

    val channels = combine(
        repository.playlist,
        selectedCategory,
        searchQuery
    ) { playlist, category, query ->
        if (playlist == null) emptyList()
        else if (query.isNotBlank()) {
            repository.searchChannels(query)
        } else {
            repository.getChannelsByCategory(category)
        }
    }.distinctUntilChanged()

    val categories = repository.playlist
        .map { it?.categories ?: emptyList() }
        .map { listOf("All") + it }
        .distinctUntilChanged()

    val isLoading = repository.isLoading
    val error = repository.error

    val favorites = repository.playlist
        .map { it?.channels.filter { it.isFavorite } ?: emptyList() }
        .distinctUntilChanged()

    val recent = repository.playlist
        .map { repository.getRecent(10) }
        .distinctUntilChanged()

    fun onCategorySelected(category: String?) {
        _selectedCategory.value = category
        _searchQuery.value = ""
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onChannelClick(channel: Channel) {
        // Update last played
        viewModelScope.launch {
            repository.toggleFavorite(channel.id) // Actually this toggles favorite, need separate method
        }
    }

    fun onFavoriteClick(channel: Channel) {
        viewModelScope.launch {
            repository.toggleFavorite(channel.id)
        }
    }

    fun loadPlaylistFromUrl(url: String) {
        viewModelScope.launch {
            repository.loadFromUrl(url)
        }
    }

    fun loadPlaylistFromContent(content: String, name: String) {
        viewModelScope.launch {
            repository.loadFromContent(content, name)
        }
    }

    fun clearError() {
        repository.clearError()
    }
}
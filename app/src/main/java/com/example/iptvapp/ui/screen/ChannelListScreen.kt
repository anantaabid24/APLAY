package com.example.iptvapp.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.image.coilAsyncImage
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.text.input.KeyboardActions
import androidx.compose.ui.text.input.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.PopupProperties
import com.example.iptvapp.model.Channel
import com.example.iptvapp.ui.theme.IPTVTheme
import coil.request.crossfade
import kotlinx.coroutines.flow.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.iptvapp.ui.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChannelListScreen(
    viewModel: MainViewModel = viewModel(),
    onChannelClick: (Channel) -> Unit,
    onAddPlaylistClick: () -> Unit
) {
    val channels by viewModel.channels.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    var selectedCategory by remember { mutableStateOf(categories.firstOrNull() ?: "All") }
    var showSearch by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }
    var showCategoryMenu by remember { mutableStateOf(false) }

    IPTVTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Top
            ) {
                // Top App Bar
                TopAppBar(
                    title = { Text("IPTV Player", fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface,
                        titleContentColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
                    ),
                    navigationIcon = {
                        if (showSearch) {
                            IconButton(onClick = { showSearch = false; searchText = "" }) {
                                Icon(Icons.Filled.Close, contentDescription = "Close search")
                            }
                        } else {
                            IconButton(onClick = { showCategoryMenu = true }) {
                                Icon(Icons.Filled.Menu, contentDescription = "Categories")
                            }
                        }
                    },
                    actions = {
                        if (!showSearch) {
                            IconButton(onClick = { showSearch = true }) {
                                Icon(Icons.Filled.Search, contentDescription = "Search")
                            }
                            IconButton(onClick = onAddPlaylistClick) {
                                Icon(Icons.Filled.LiveTv, contentDescription = "Add Playlist")
                            }
                        }
                    }
                )

                // Search Bar
                if (showSearch) {
                    SearchBar(
                        query = searchText,
                        onQueryChange = { newText ->
                            searchText = newText
                            viewModel.onSearchQueryChanged(newText)
                        },
                        onClear = {
                            searchText = ""
                            viewModel.onSearchQueryChanged("")
                        }
                    )
                    Divider()
                }

                // Category Chips
                if (!showSearch && categories.isNotEmpty()) {
                    CategoryChips(
                        categories = categories,
                        selectedCategory = selectedCategory,
                        onCategoryClick = { category ->
                            selectedCategory = category
                            viewModel.onCategorySelected(category)
                        }
                    )
                    Divider()
                }

                // Error Message
                error?.let { msg ->
                    ErrorBanner(message = msg, onDismiss = { viewModel.clearError() })
                    Divider()
                }

                // Channel List
                if (isLoading && channels.isEmpty()) {
                    LoadingIndicator()
                } else if (channels.isEmpty()) {
                    EmptyState(onAddPlaylistClick = onAddPlaylistClick)
                } else {
                    ChannelList(
                        channels = channels,
                        onChannelClick = onChannelClick,
                        onFavoriteClick = { channel -> viewModel.onFavoriteClick(channel) }
                    )
                }
            }

            // Category Dropdown Menu
            if (showCategoryMenu) {
                CategoryDropdownMenu(
                    categories = categories,
                    selectedCategory = selectedCategory,
                    onCategoryClick = { category ->
                        selectedCategory = category
                        viewModel.onCategorySelected(category)
                        showCategoryMenu = false
                    },
                    onDismiss = { showCategoryMenu = false }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit
) {
    val context = LocalContext.current
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        placeholder = { Text("Search channels...") },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = onClear) {
                    Icon(Icons.Filled.Close, contentDescription = "Clear")
                }
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { /* handled by onValueChange */ }),
        colors = TextFieldDefaults.textFieldColors(
            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerHighest
        )
    )
}

@Composable
fun CategoryChips(
    categories: List<String>,
    selectedCategory: String,
    onCategoryClick: (String) -> Unit
) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.forEach { category ->
            androidx.compose.material3.FilterChip(
                selected = (category == selectedCategory),
                onClick = { onCategoryClick(category) },
                label = { Text(category) },
                modifier = Modifier.padding(vertical = 4.dp),
                colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
                    selectedContainerColor = androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = androidx.compose.material3.MaterialTheme.colorScheme.onPrimaryContainer,
                    containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerHighest,
                    labelColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDropdownMenu(
    categories: List<String>,
    selectedCategory: String,
    onCategoryClick: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val popupProperties = remember { PopupProperties(focusable = true) }
    androidx.compose.material3.DropdownMenu(
        expanded = true,
        onDismissRequest = onDismiss,
        properties = popupProperties
    ) {
        categories.forEach { category ->
            DropdownMenuItem(
                text = { Text(category, fontWeight = if (category == selectedCategory) FontWeight.Bold else FontWeight.Normal) },
                onClick = { onCategoryClick(category) },
                leadingIcon = {
                    if (category == selectedCategory) {
                        Icon(Icons.Filled.Star, contentDescription = "Selected")
                    }
                }
            )
        }
    }
}

@Composable
fun ErrorBanner(message: String, onDismiss: () -> Unit) {
    androidx.compose.material3.Banner(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        leadingIcon = { Icon(Icons.Filled.Error, contentDescription = "Error", tint = androidx.compose.material3.MaterialTheme.colorScheme.onErrorContainer) },
        content = { Text(message, color = androidx.compose.material3.MaterialTheme.colorScheme.onErrorContainer) },
        actions = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text("Dismiss")
            }
        },
        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.errorContainer
    )
}

@Composable
fun LoadingIndicator() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
            strokeWidth = 4.dp
        )
    }
}

@Composable
fun EmptyState(onAddPlaylistClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontal,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.LiveTv,
                contentDescription = "No channels",
                tint = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp)
            )
            Text(
                "No channels found",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
            )
            Text(
                "Add an M3U playlist to start watching",
                fontSize = 14.sp,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.TextAlign.Center
            )
            androidx.compose.material3.Button(
                onClick = onAddPlaylistClick,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("Add Playlist")
            }
        }
    )
}

@Composable
fun ChannelList(
    channels: List<Channel>,
    onChannelClick: (Channel) -> Unit,
    onFavoriteClick: (Channel) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(channels) { channel ->
            ChannelItem(
                channel = channel,
                onClick = { onChannelClick(channel) },
                onFavoriteClick = { onFavoriteClick(channel) }
            )
        }
    }
}

@Composable
fun ChannelItem(
    channel: Channel,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    val cardColors = CardDefaults.cardColors(
        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface,
        contentColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
    )

    Card(
        onClick = onClick,
        colors = cardColors,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Channel Logo
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerHighest)
            ) {
                channel.logo?.let { logoUrl ->
                    androidx.compose.foundation.image.coilAsyncImage(
                        data = logoUrl,
                        contentDescription = channel.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(8.dp)),
                        crossfade = true
                    )
                } ?: Icon(
                    imageVector = Icons.Filled.LiveTv,
                    contentDescription = "Default channel icon",
                    tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentSize(Alignment.Center)
                )
            }

            // Channel Info
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = channel.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.TextOverflow.Ellipsis
                )
                channel.group?.let { group ->
                    Text(
                        text = group,
                        fontSize = 12.sp,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.TextOverflow.Ellipsis
                    )
                }
            }

            // Favorite Button
            IconButton(onClick = onFavoriteClick) {
                Icon(
                    imageVector = if (channel.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = if (channel.isFavorite) "Remove from favorites" : "Add to favorites",
                    tint = if (channel.isFavorite) Color.Red else androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Play Indicator
            Icon(
                imageVector = Icons.Filled.PlayCircle,
                contentDescription = "Play",
                tint = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}
package com.example.iptvapp.model

import kotlinx.serialization.Serializable

@Serializable
data class Channel(
    val id: String,
    val name: String,
    val url: String,
    val logo: String? = null,
    val group: String? = null,
    val tvgId: String? = null,
    val tvgName: String? = null,
    val tvgLogo: String? = null,
    val tvgShift: String? = null,
    val tvgCountry: String? = null,
    val tvgLanguage: String? = null,
    val tvgUrl: String? = null,
    val catchup: String? = null,
    val catchupSource: String? = null,
    val audioTrack: String? = null,
    val aspectRatio: String? = null,
    val isFavorite: Boolean = false,
    val lastPlayed: Long = 0
) {
    companion object {
        fun generateId(name: String, url: String): String {
            return "${name.hashCode()}_${url.hashCode()}".replace("-", "")
        }
    }
}

@Serializable
data class Playlist(
    val channels: List<Channel>,
    val categories: List<String>,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Serializable
data class PlaylistSource(
    val id: String,
    val name: String,
    val url: String,
    val isLocal: Boolean = false,
    val lastLoaded: Long = 0,
    val channelCount: Int = 0
) {
    companion object {
        fun generateId(url: String): String = url.hashCode().toString().replace("-", "")
    }
}
package com.example.iptvapp.parser

import com.example.iptvapp.model.Channel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class M3UParser {

    suspend fun parse(content: String): List<Channel> = withContext(Dispatchers.IO) {
        val channels = mutableListOf<Channel>()
        val lines = content.lines().iterator()
        var currentExtInf: String? = null
        var currentAttributes = mutableMapOf<String, String>()

        while (lines.hasNext()) {
            val line = lines.next().trim()

            if (line.startsWith("#EXTINF:")) {
                currentExtInf = line
                currentAttributes = parseExtInfAttributes(line)
            } else if (line.startsWith("#EXTGRP:")) {
                currentAttributes["group"] = line.substringAfter("#EXTGRP:").trim()
            } else if (line.isNotBlank() && !line.startsWith("#")) {
                // This is the URL line
                val url = line.trim()
                if (url.isNotEmpty() && currentExtInf != null) {
                    val channel = createChannel(currentAttributes, url)
                    channels.add(channel)
                    currentExtInf = null
                    currentAttributes.clear()
                }
            } else if (line.startsWith("#EXTM3U")) {
                // Header, ignore
            }
        }
        channels
    }

    private fun parseExtInfAttributes(extInfLine: String): MutableMap<String, String> {
        val attrs = mutableMapOf<String, String>()
        val content = extInfLine.substringAfter("#EXTINF:").trim()

        // Parse attributes like tvg-id="..." tvg-name="..." tvg-logo="..." group-title="..."
        val attrRegex = """(\w+(?:-\w+)?)\s*=\s*"([^"]*)"""".toRegex()
        attrRegex.findAll(content).forEach { match ->
            val key = match.groupValues[1]
            val value = match.groupValues[2]
            attrs[key] = value
        }

        // Extract channel name (after the last comma)
        val name = content.substringAfterLast(",").trim()
        if (name.isNotEmpty()) {
            attrs["name"] = name
        }

        return attrs
    }

    private fun createChannel(attrs: Map<String, String>, url: String): Channel {
        val name = attrs["name"] ?: attrs["tvg-name"] ?: "Unknown Channel"
        val id = Channel.generateId(name, url)

        return Channel(
            id = id,
            name = name,
            url = url,
            logo = attrs["tvg-logo"]?.takeIf { it.isNotEmpty() },
            group = attrs["group-title"]?.takeIf { it.isNotEmpty() } ?: attrs["group"]?.takeIf { it.isNotEmpty() },
            tvgId = attrs["tvg-id"]?.takeIf { it.isNotEmpty() },
            tvgName = attrs["tvg-name"]?.takeIf { it.isNotEmpty() },
            tvgLogo = attrs["tvg-logo"]?.takeIf { it.isNotEmpty() },
            tvgShift = attrs["tvg-shift"]?.takeIf { it.isNotEmpty() },
            tvgCountry = attrs["tvg-country"]?.takeIf { it.isNotEmpty() },
            tvgLanguage = attrs["tvg-language"]?.takeIf { it.isNotEmpty() },
            tvgUrl = attrs["tvg-url"]?.takeIf { it.isNotEmpty() },
            catchup = attrs["catchup"]?.takeIf { it.isNotEmpty() },
            catchupSource = attrs["catchup-source"]?.takeIf { it.isNotEmpty() },
            audioTrack = attrs["audio-track"]?.takeIf { it.isNotEmpty() },
            aspectRatio = attrs["aspect-ratio"]?.takeIf { it.isNotEmpty() }
        )
    }

    // Synchronous version for simple usage
    fun parseSync(content: String): List<Channel> {
        val channels = mutableListOf<Channel>()
        val lines = content.lines().iterator()
        var currentAttributes = mutableMapOf<String, String>()

        while (lines.hasNext()) {
            val line = lines.next().trim()

            if (line.startsWith("#EXTINF:")) {
                currentAttributes = parseExtInfAttributes(line)
            } else if (line.startsWith("#EXTGRP:")) {
                currentAttributes["group"] = line.substringAfter("#EXTGRP:").trim()
            } else if (line.isNotBlank() && !line.startsWith("#")) {
                val url = line.trim()
                if (url.isNotEmpty() && currentAttributes.isNotEmpty()) {
                    val channel = createChannel(currentAttributes, url)
                    channels.add(channel)
                    currentAttributes.clear()
                }
            }
        }
        return channels
    }
}
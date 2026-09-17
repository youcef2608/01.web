package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.Badge
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class RoomConverters {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    @TypeConverter
    fun fromBadgeList(badges: List<Badge>?): String {
        return badges?.let { json.encodeToString(it) } ?: ""
    }

    @TypeConverter
    fun toBadgeList(badgesJson: String?): List<Badge> {
        if (badgesJson.isNullOrBlank()) return emptyList()
        return try {
            json.decodeFromString(badgesJson)
        } catch (e: Exception) {
            emptyList()
        }
    }
}

package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.HelpCall
import com.example.data.model.HelpCallStatus
import com.example.data.model.HelpCategory
import com.example.data.model.UrgencyLevel

@Entity(tableName = "help_calls")
data class HelpCallEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val category: String,
    val urgency: String,
    val status: String,
    val latitude: Double,
    val longitude: Double,
    val locationName: String,
    val requesterId: String,
    val authorName: String,
    val authorPhone: String,
    val volunteerId: String?,
    val helperName: String?,
    val timestamp: Long,
    val completedAt: Long?
) {
    fun toHelpCall(): HelpCall {
        return HelpCall(
            id = id,
            title = title,
            description = description,
            category = runCatching { HelpCategory.valueOf(category) }.getOrDefault(HelpCategory.OTHER),
            urgency = runCatching { UrgencyLevel.valueOf(urgency) }.getOrDefault(UrgencyLevel.MEDIUM),
            status = runCatching { HelpCallStatus.valueOf(status) }.getOrDefault(HelpCallStatus.OPEN),
            latitude = latitude,
            longitude = longitude,
            locationName = locationName,
            requesterId = requesterId,
            authorName = authorName,
            authorPhone = authorPhone,
            volunteerId = volunteerId,
            helperName = helperName,
            timestamp = timestamp,
            completedAt = completedAt
        )
    }

    companion object {
        fun fromHelpCall(call: HelpCall): HelpCallEntity {
            return HelpCallEntity(
                id = call.id,
                title = call.title,
                description = call.description,
                category = call.category.name,
                urgency = call.urgency.name,
                status = call.status.name,
                latitude = call.latitude,
                longitude = call.longitude,
                locationName = call.locationName,
                requesterId = call.requesterId,
                authorName = call.authorName,
                authorPhone = call.authorPhone,
                volunteerId = call.volunteerId,
                helperName = call.helperName,
                timestamp = call.timestamp,
                completedAt = call.completedAt
            )
        }
    }
}

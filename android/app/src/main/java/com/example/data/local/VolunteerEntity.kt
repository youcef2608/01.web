package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.Badge
import com.example.data.model.Volunteer

@Entity(tableName = "volunteers")
data class VolunteerEntity(
    @PrimaryKey val id: String,
    val name: String,
    val phoneNumber: String,
    val wilaya: String,
    val baladiya: String,
    val neighborhood: String,
    val echoPoints: Int,
    val impactStreak: Int,
    val givenHelpsCount: Int,
    val receivedHelpsCount: Int,
    val badges: List<Badge>,
    val recentStory: String,
    val avatarUrl: String = "",
    val registrationDate: Long = 0L
) {
    fun toVolunteer(): Volunteer {
        return Volunteer(
            id = id,
            name = name,
            phoneNumber = phoneNumber,
            wilaya = wilaya,
            baladiya = baladiya,
            neighborhood = neighborhood,
            echoPoints = echoPoints,
            impactStreak = impactStreak,
            givenHelpsCount = givenHelpsCount,
            receivedHelpsCount = receivedHelpsCount,
            badges = badges.ifEmpty { Volunteer.defaultBadges() },
            recentStory = recentStory,
            avatarUrl = avatarUrl,
            registrationDate = if (registrationDate > 0) registrationDate else System.currentTimeMillis()
        )
    }

    companion object {
        fun fromVolunteer(vol: Volunteer): VolunteerEntity {
            return VolunteerEntity(
                id = vol.id,
                name = vol.name,
                phoneNumber = vol.phoneNumber,
                wilaya = vol.wilaya,
                baladiya = vol.baladiya,
                neighborhood = vol.neighborhood,
                echoPoints = vol.echoPoints,
                impactStreak = vol.impactStreak,
                givenHelpsCount = vol.givenHelpsCount,
                receivedHelpsCount = vol.receivedHelpsCount,
                badges = vol.badges,
                recentStory = vol.recentStory,
                avatarUrl = vol.avatarUrl,
                registrationDate = vol.registrationDate
            )
        }
    }
}

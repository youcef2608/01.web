package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.EchoAction
import com.example.data.model.HelpCategory

@Entity(tableName = "echo_actions")
data class EchoActionEntity(
    @PrimaryKey val id: String,
    val helpCallId: String,
    val helperId: String,
    val helperName: String,
    val beneficiaryName: String,
    val category: String,
    val generatedStory: String,
    val echoPointsAwarded: Int,
    val timestamp: Long
) {
    fun toEchoAction(): EchoAction {
        return EchoAction(
            id = id,
            helpCallId = helpCallId,
            helperId = helperId,
            helperName = helperName,
            beneficiaryName = beneficiaryName,
            category = runCatching { HelpCategory.valueOf(category) }.getOrDefault(HelpCategory.OTHER),
            generatedStory = generatedStory,
            echoPointsAwarded = echoPointsAwarded,
            timestamp = timestamp
        )
    }

    companion object {
        fun fromEchoAction(action: EchoAction): EchoActionEntity {
            return EchoActionEntity(
                id = action.id,
                helpCallId = action.helpCallId,
                helperId = action.helperId,
                helperName = action.helperName,
                beneficiaryName = action.beneficiaryName,
                category = action.category.name,
                generatedStory = action.generatedStory,
                echoPointsAwarded = action.echoPointsAwarded,
                timestamp = action.timestamp
            )
        }
    }
}

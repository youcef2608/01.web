package com.example.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * نتيجة التصنيف الذكي للنص بواسطة Gemini
 */
@Serializable
data class GeminiClassification(
    @SerialName("category")
    val category: String = "أخرى",
    @SerialName("urgency")
    val urgency: String = "متوسط",
    @SerialName("suggested_title")
    val suggestedTitle: String = "نداء مساعدة مجتمعي"
) {
    fun toHelpCategory(): HelpCategory = HelpCategory.fromArabic(category)
    fun toUrgencyLevel(): UrgencyLevel = UrgencyLevel.fromArabic(urgency)

    companion object {
        fun defaultFallback(text: String): GeminiClassification {
            val previewTitle = if (text.length > 25) text.take(25) + "..." else text.ifBlank { "نداء مساعدة مجتمعي" }
            val inferredCategory = HelpCategory.fromArabic(text).labelArabic
            val inferredUrgency = if (text.contains("عاجل") || text.contains("طوارئ")) "عاجل" else "متوسط"
            return GeminiClassification(
                category = inferredCategory,
                urgency = inferredUrgency,
                suggestedTitle = previewTitle
            )
        }
    }
}

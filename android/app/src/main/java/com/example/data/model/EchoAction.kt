package com.example.data.model

import kotlinx.serialization.Serializable

/**
 * نموذج عملية تطوع منجزة تسجل الأثر وتولد قصة ملهمة عبر Gemini
 * لا يحتوي على أي حقول أسعار أو مبالغ أو دفع
 */
@Serializable
data class EchoAction(
    val id: String = "",
    val helpCallId: String = "",
    val helperId: String = "",
    val helperName: String = "",
    val beneficiaryName: String = "",
    val category: HelpCategory = HelpCategory.OTHER,
    val generatedStory: String = "",
    val echoPointsAwarded: Int = 50,
    val timestamp: Long = System.currentTimeMillis()
)

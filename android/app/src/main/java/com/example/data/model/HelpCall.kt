package com.example.data.model

import kotlinx.serialization.Serializable
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * فئة النداء (نوع المساعدة)
 */
enum class HelpCategory(val labelArabic: String, val iconName: String) {
    DELIVERY("توصيل", "LocalShipping"),
    COMPANIONSHIP("مرافقة", "People"),
    EDUCATION("مساعدة تعليمية", "School"),
    MOVING("نقل أغراض", "Inventory2"),
    OTHER("أخرى", "VolunteerActivism");

    companion object {
        fun fromArabic(value: String?): HelpCategory {
            if (value == null) return OTHER
            return entries.firstOrNull { it.labelArabic.equals(value.trim(), ignoreCase = true) }
                ?: when {
                    value.contains("توصيل") || value.contains("دواء") || value.contains("طعام") -> DELIVERY
                    value.contains("مرافقة") || value.contains("كبير") || value.contains("طبيب") -> COMPANIONSHIP
                    value.contains("تعليم") || value.contains("درس") || value.contains("دراسة") -> EDUCATION
                    value.contains("نقل") || value.contains("حمل") || value.contains("أغراض") -> MOVING
                    else -> OTHER
                }
        }
    }
}

/**
 * درجة إلحاح النداء
 */
enum class UrgencyLevel(val labelArabic: String) {
    LOW("منخفض"),
    MEDIUM("متوسط"),
    URGENT("عاجل");

    companion object {
        fun fromArabic(value: String?): UrgencyLevel {
            if (value == null) return MEDIUM
            return entries.firstOrNull { it.labelArabic.equals(value.trim(), ignoreCase = true) }
                ?: when {
                    value.contains("عاجل") || value.contains("طارئ") || value.contains("urgent", true) -> URGENT
                    value.contains("منخفض") || value.contains("low", true) -> LOW
                    else -> MEDIUM
                }
        }
    }
}

/**
 * حالة النداء في الوقت الفعلي
 */
enum class HelpCallStatus(val labelArabic: String) {
    OPEN("مفتوح للجميع"),
    IN_PROGRESS("قيد التنفيذ"),
    PENDING_CONFIRMATION("في انتظار التأكيد"),
    COMPLETED("مكتمل وتم إحداث الأثر"),
    CANCELLED("تم الإلغاء")
}

/**
 * نموذج نداء المساعدة المجتمعي (بدون أي مقابل مالي إطلاقاً)
 */
@Serializable
data class HelpCall(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val category: HelpCategory = HelpCategory.OTHER,
    val urgency: UrgencyLevel = UrgencyLevel.MEDIUM,
    val status: HelpCallStatus = HelpCallStatus.OPEN,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val locationName: String = "",
    val requesterId: String = "", // صاحب النداء (هو الوحيد المخول يؤكد)
    val authorName: String = "",
    val authorPhone: String = "",
    val volunteerId: String? = null, // المتطوع اللي ضغط "سأساعد"
    val helperName: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
) {
    /**
     * حساب المسافة الجغرافية بالكيلومتر باستخدام معادلة Haversine محلياً دون أي API مدفوع
     */
    fun distanceTo(userLat: Double, userLon: Double): Double {
        if (latitude == 0.0 && longitude == 0.0) return 0.0
        val earthRadiusKm = 6371.0

        val dLat = Math.toRadians(latitude - userLat)
        val dLon = Math.toRadians(longitude - userLon)

        val lat1 = Math.toRadians(userLat)
        val lat2 = Math.toRadians(latitude)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                sin(dLon / 2) * sin(dLon / 2) * cos(lat1) * cos(lat2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadiusKm * c
    }

    /**
     * تنسيق المسافة بصيغة مقروءة
     */
    fun formatDistance(userLat: Double, userLon: Double): String {
        val dist = distanceTo(userLat, userLon)
        return if (dist < 1.0) {
            "${(dist * 1000).toInt()} م"
        } else {
            String.format(java.util.Locale.US, "%.1f كم", dist)
        }
    }
}

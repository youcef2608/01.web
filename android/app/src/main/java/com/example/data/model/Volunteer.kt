package com.example.data.model

import kotlinx.serialization.Serializable

/**
 * شارة تقديرية غير مادية للعمل التطوعي
 */
@Serializable
data class Badge(
    val id: String,
    val titleArabic: String,
    val descriptionArabic: String,
    val iconName: String,
    val isUnlocked: Boolean = false,
    val progress: Int = 0,
    val maxProgress: Int = 10
)

/**
 * نموذج المتطوع / المستخدم
 * يركز حصرياً على الأثر الاجتماعي ونقاط لمّة التكافلية الرمزية بدون أي مقابل مالي
 */
@Serializable
data class Volunteer(
    val id: String = "",
    val name: String = "",
    val phoneNumber: String = "",
    val wilaya: String = "",               // الولاية
    val baladiya: String = "",             // البلدية
    val neighborhood: String = "",         // الحي
    val echoPoints: Int = 0,               // نقاط لمّة (رمزية وغير مالية إطلاقاً)
    val impactStreak: Int = 0,             // سلسلة الأثر المستمر
    val givenHelpsCount: Int = 0,          // عداد التطوع العكسي: عدد المرات التي كان فيها معطي
    val receivedHelpsCount: Int = 0,       // عداد التطوع العكسي: عدد المرات التي كان فيها مستفيد
    val badges: List<Badge> = defaultBadges(),
    val recentStory: String = "انضم حديثاً إلى مجتمع لمّة للتكافل والمساعدة.",
    val avatarUrl: String = "",
    val registrationDate: Long = System.currentTimeMillis()
) {
    companion object {
        fun defaultBadges(): List<Badge> = listOf(
            Badge(
                id = "guardian",
                titleArabic = "حارس الحي",
                descriptionArabic = "إتمام 10 عمليات تطوع في حيك",
                iconName = "Shield",
                isUnlocked = false,
                progress = 0,
                maxProgress = 10
            ),
            Badge(
                id = "monthly_echo",
                titleArabic = "صدى الشهر",
                descriptionArabic = "إحداث أثر ملموس في صدارة الشهر",
                iconName = "EmojiEvents",
                isUnlocked = false,
                progress = 0,
                maxProgress = 1
            ),
            Badge(
                id = "hope_bringer",
                titleArabic = "باعث الأمل",
                descriptionArabic = "إنجاز 3 نداءات عاجلة بنجاح",
                iconName = "Favorite",
                isUnlocked = false,
                progress = 0,
                maxProgress = 3
            ),
            Badge(
                id = "community_pillar",
                titleArabic = "عماد التكافل",
                descriptionArabic = "تحقيق توازن ملهم بين العطاء والاستفادة",
                iconName = "Balance",
                isUnlocked = false,
                progress = 0,
                maxProgress = 5
            )
        )
    }
}

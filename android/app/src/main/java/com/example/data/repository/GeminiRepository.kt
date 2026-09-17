package com.example.data.repository

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.GeminiClassification
import com.example.data.remote.GeminiApiClient
import com.example.data.remote.GeminiContent
import com.example.data.remote.GeminiGenerationConfig
import com.example.data.remote.GeminiPart
import com.example.data.remote.GeminiRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

/**
 * مستودع التواصل مع Gemini API لتصنيف النداءات وتوليد قصص الأثر
 */
class GeminiRepository(
    private val apiClient: GeminiApiClient = GeminiApiClient(),
    private val json: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }
) {
    companion object {
        private const val TAG = "GeminiRepository"
    }

    /**
     * استدعاء Gemini API لتحليل نص نداء المساعدة وإرجاع تصنيف مهيكل (Category, Urgency, Suggested Title)
     * مع معالجة كاملة للأخطاء وقيمة افتراضية عند فشل الاستدعاء
     */
    suspend fun classifyHelpCall(inputText: String): GeminiClassification = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w(TAG, "Gemini API key is not configured. Returning fallback classification.")
            return@withContext GeminiClassification.defaultFallback(inputText)
        }

        val prompt = """
            أنت مساعد ذكي لتطبيق العمل التطوعي "لمّة".
            قم بتحليل نص نداء المساعدة التالي واستخرج المعلومات بصيغة JSON حصراً بدون أي كود ماركداون إضافي:
            النص: "$inputText"
            
            يجب أن يكون الرد عبارة عن كائن JSON بالصيغة التالية تماماً:
            {
              "category": "توصيل" أو "مرافقة" أو "مساعدة تعليمية" أو "نقل أغراض" أو "أخرى",
              "urgency": "منخفض" أو "متوسط" أو "عاجل",
              "suggested_title": "عنوان قصير جذاب للنداء (أقل من 6 كلمات)"
            }
        """.trimIndent()

        val request = GeminiRequest(
            contents = listOf(
                GeminiContent(parts = listOf(GeminiPart(text = prompt)))
            ),
            generationConfig = GeminiGenerationConfig(
                temperature = 0.2f,
                responseMimeType = "application/json"
            )
        )

        try {
            val response = apiClient.generateContent(apiKey, request)
            val rawText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim()

            if (!rawText.isNullOrBlank()) {
                val cleanedJson = cleanJsonString(rawText)
                return@withContext json.decodeFromString<GeminiClassification>(cleanedJson)
            } else {
                GeminiClassification.defaultFallback(inputText)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error calling Gemini API for classification", e)
            GeminiClassification.defaultFallback(inputText)
        }
    }

    /**
     * توليد جملة قصيرة ملهمة باللغة العربية تصف أثر عملية التطوع
     * مثال: "أحمد ساعد 3 أشخاص هذا الأسبوع في التنقل والوصول إلى موعدهم بأمان"
     */
    suspend fun generateImpactStory(
        helperName: String,
        beneficiaryName: String,
        category: String,
        helpCount: Int
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        val defaultStory = "$helperName قدم العون لـ $beneficiaryName في $category بكل محبة وإخلاص."

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext defaultStory
        }

        val prompt = """
            أنت محفز مجتمعي في تطبيق "لمّة" للعمل التطوعي غير المادي.
            اكتب جملة واحدة قصيرة وملهمة ودافئة باللغة العربية تصف أثر هذه المساعدة التطوعية:
            - المتطوع: $helperName
            - المستفيد: $beneficiaryName
            - نوع المساعدة: $category
            - إجمالي عمليات التطوع المنجزة: $helpCount
            
            الشروط:
            1. جملة واحدة فقط دافئة ومشجعة بدون مبالغة وبدون أي ذكر لأي أموال أو أسعار.
            2. ركز على التكافل وصدى الخير والأثر الإنساني في الحي.
        """.trimIndent()

        val request = GeminiRequest(
            contents = listOf(
                GeminiContent(parts = listOf(GeminiPart(text = prompt)))
            ),
            generationConfig = GeminiGenerationConfig(temperature = 0.7f)
        )

        try {
            val response = apiClient.generateContent(apiKey, request)
            val story = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim()
            if (!story.isNullOrBlank()) {
                story.replace("\"", "").replace("\n", " ").trim()
            } else {
                defaultStory
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error generating impact story with Gemini", e)
            defaultStory
        }
    }

    private fun cleanJsonString(raw: String): String {
        var result = raw.trim()
        if (result.startsWith("```json")) {
            result = result.removePrefix("```json")
        } else if (result.startsWith("```")) {
            result = result.removePrefix("```")
        }
        if (result.endsWith("```")) {
            result = result.removeSuffix("```")
        }
        return result.trim()
    }
}

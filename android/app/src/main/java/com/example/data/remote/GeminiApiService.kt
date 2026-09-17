package com.example.data.remote

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

@Serializable
data class GeminiPart(
    val text: String? = null
)

@Serializable
data class GeminiContent(
    val role: String? = "user",
    val parts: List<GeminiPart>
)

@Serializable
data class GeminiGenerationConfig(
    val temperature: Float? = 0.2f,
    val topP: Float? = 0.95f,
    val responseMimeType: String? = null
)

@Serializable
data class GeminiRequest(
    val contents: List<GeminiContent>,
    val generationConfig: GeminiGenerationConfig? = null
)

@Serializable
data class GeminiCandidate(
    val content: GeminiContent? = null
)

@Serializable
data class GeminiResponse(
    val candidates: List<GeminiCandidate>? = null
)

class GeminiApiClient(
    private val json: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }
) {
    companion object {
        private const val TAG = "GeminiApiClient"
        private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent"
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(45, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(45, TimeUnit.SECONDS)
        .build()

    suspend fun generateContent(apiKey: String, requestPayload: GeminiRequest): GeminiResponse = withContext(Dispatchers.IO) {
        val url = "$BASE_URL?key=$apiKey"
        val requestJson = json.encodeToString(requestPayload)
        val body = requestJson.toRequestBody("application/json; charset=utf-8".toMediaType())

        val request = Request.Builder()
            .url(url)
            .post(body)
            .header("Content-Type", "application/json")
            .build()

        client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                Log.e(TAG, "Gemini API error code: ${response.code}, body: $responseBody")
                return@withContext GeminiResponse()
            }
            try {
                json.decodeFromString<GeminiResponse>(responseBody)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse Gemini response: $responseBody", e)
                GeminiResponse()
            }
        }
    }
}

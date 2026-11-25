package com.example.criptotreck.data.remote

import com.example.criptotreck.BuildConfig
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Клиент Gemini API (free tier, Google AI Studio).
 *
 * Ключ берётся из BuildConfig.GEMINI_API_KEY, который подставляется из local.properties,
 * поэтому в исходниках и в git ключа нет.
 *
 * Важно: free-квота Google используется для улучшения продуктов Google, а промпты могут
 * просматриваться ревьюерами. Не отправляйте сюда персональные данные и секреты.
 */
class GeminiClient(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build(),
    private val json: Json = Json { ignoreUnknownKeys = true },
) {
    suspend fun generateText(prompt: String, model: String = DEFAULT_MODEL): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        require(apiKey.isNotBlank()) {
            "GEMINI_API_KEY не задан. Добавьте GEMINI_API_KEY=... в local.properties"
        }

        val payload = GeminiRequest(
            contents = listOf(GeminiContent(role = "user", parts = listOf(GeminiPart(text = prompt))))
        )
        val body = json.encodeToString(GeminiRequest.serializer(), payload)
            .toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("$BASE_URL/v1beta/models/$model:generateContent")
            .addHeader("x-goog-api-key", apiKey)
            .post(body)
            .build()

        client.newCall(request).execute().use { response ->
            val raw = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IOException("Gemini API ${response.code}: ${raw.take(300)}")
            }
            val parsed = json.decodeFromString(GeminiResponse.serializer(), raw)
            return parsed.candidates
                ?.firstOrNull()
                ?.content
                ?.parts
                ?.joinToString("") { it.text.orEmpty() }
                ?.trim()
                ?: throw IOException("Gemini вернул пустой ответ")
        }
    }

    companion object {
        const val BASE_URL = "https://generativelanguage.googleapis.com/"
        const val DEFAULT_MODEL = "gemini-3.8-flash"
    }
}

@Serializable
private data class GeminiRequest(val contents: List<GeminiContent>)

@Serializable
private data class GeminiContent(val role: String, val parts: List<GeminiPart>)

@Serializable
private data class GeminiPart(val text: String? = null)

@Serializable
private data class GeminiResponse(val candidates: List<GeminiCandidate>? = null)

@Serializable
private data class GeminiCandidate(val content: GeminiContent? = null)
package com.example.smartirrigation.data.repositories

import android.util.Log
import com.example.smartirrigation.data.network.ApiConfig
import com.example.smartirrigation.data.network.dto.chatbot.ChatRequest
import com.example.smartirrigation.data.network.dto.chatbot.ChatResponse
import com.example.smartirrigation.data.network.dto.chatbot.StreamToken
import com.example.smartirrigation.domain.repositories.ChatBotRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.preparePost
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.headers
import io.ktor.utils.io.readUTF8Line
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json

/**
 * Implementation of ChatBotRepository that communicates with the FastAPI backend.
 * Supports both SSE streaming and sync responses.
 */
class ChatBotRepoImpl(
    private val httpClient: HttpClient
) : ChatBotRepository {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        isLenient = true
    }

    companion object {
        private const val TAG = "ChatBotRepoImpl"
    }

    override fun sendMessage(request: ChatRequest): Flow<StreamToken> = flow {
        try {
            httpClient.preparePost("${ApiConfig.CHATBOT_BASE_URL}/chat") {
                contentType(ContentType.Application.Json)
                headers {
                    append(HttpHeaders.Accept, "text/event-stream")
                    append(HttpHeaders.CacheControl, "no-cache")
                }
                setBody(request)
            }.execute { response ->
                val channel = response.bodyAsChannel()

                while (!channel.isClosedForRead) {
                    val line = channel.readUTF8Line() ?: continue
                    val trimmed = line.trim()

                    // Skip empty lines
                    if (trimmed.isEmpty()) continue

                    // Skip SSE comments
                    if (trimmed.startsWith(":")) continue

                    // Check for done marker
                    if (trimmed.contains("[DONE]")) {
                        Log.d(TAG, "Stream completed")
                        break
                    }

                    // Parse event lines
                    when {
                        trimmed.startsWith("event:") -> {
                            // Event type line, skip (we handle data lines)
                            continue
                        }
                        trimmed.startsWith("data:") -> {
                            val jsonData = trimmed.removePrefix("data:").trim()
                            
                            // Skip [DONE] in data field
                            if (jsonData == "[DONE]") {
                                Log.d(TAG, "Stream completed via data field")
                                break
                            }

                            try {
                                val streamToken = json.decodeFromString<StreamToken>(jsonData)
                                Log.d(TAG, "Received token: ${streamToken.token}")
                                emit(streamToken)
                            } catch (e: Exception) {
                                Log.e(TAG, "Failed to parse token: $jsonData", e)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in sendMessage: ${e.message}", e)
            // Emit error token
            emit(StreamToken(token = "Error: ${e.message ?: "Unknown error"}"))
        }
    }

    override suspend fun sendMessageSync(request: ChatRequest): ChatResponse? {
        return try {
            val response: ChatResponse = httpClient.post("${ApiConfig.CHATBOT_BASE_URL}/chat/sync") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()
            
            Log.d(TAG, "Sync response: ${response.response.take(50)}...")
            response
        } catch (e: Exception) {
            Log.e(TAG, "Error in sendMessageSync: ${e.message}", e)
            null
        }
    }
}

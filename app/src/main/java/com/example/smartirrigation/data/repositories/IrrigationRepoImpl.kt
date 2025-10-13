package com.example.smartirrigation.data.repositories

import android.util.Log
import androidx.compose.ui.autofill.ContentType
import com.example.smartirrigation.data.network.dto.IrrigatorInfo
import com.example.smartirrigation.data.network.dto.ResponseMessage
import com.example.smartirrigation.data.network.dto.Threshold
import com.example.smartirrigation.domain.repositories.IrrigationRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.timeout
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.prepareGet
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.headers
import io.ktor.utils.io.readUTF8Line
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.retryWhen
import kotlinx.serialization.json.Json

class IrrigationRepoImpl(val httpClient : HttpClient) : IrrigationRepository {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        isLenient = true
    }

    override suspend fun getStatus(): Flow<IrrigatorInfo?> = flow {
        try {
            httpClient.prepareGet("http://192.168.1.150/sse") {
                headers {
                    append(HttpHeaders.Accept, "text/event-stream")
                    append(HttpHeaders.CacheControl, "no-cache")
                }

                timeout {
                    requestTimeoutMillis = Long.MAX_VALUE
                    socketTimeoutMillis = Long.MAX_VALUE
                }
            }.execute { response ->
                    val channel = response.bodyAsChannel()

                    while (!channel.isClosedForRead) {
                        val line = channel.readUTF8Line() ?: continue

                        if (line.startsWith("data: ")) {
                            val jsonData = line.removePrefix("data: ")
                            val irrigatorInfo = json.decodeFromString<IrrigatorInfo>(jsonData)
                            Log.d("IrrigationRepoImpl", "Received data: $irrigatorInfo")
                            emit(irrigatorInfo)
                        }
                    }
                }
        } catch (e: Exception) {

            Log.d("IrrigationRepoImpl", "Error: ${e.message}")
            emit(null)
        }
    }.retryWhen { cause, attempt ->
        if (attempt < 5) {
            delay(2000)
            true
        } else {
            false
        }
    }


    override suspend fun setThreshold(threshold: Int): Boolean {
        return try {
            val response : ResponseMessage? = httpClient.post("http://192.168.1.150/setThreshold") {
                header("Authorization", "Bearer myStrongAdminKey123")
                contentType(io.ktor.http.ContentType.Application.Json)
                setBody(Threshold(threshold))
            }.body()
            if (response != null && response.status == "ok") {
                Log.d("IrrigationRepoImpl", "Threshold set successfully: ${response.threshold}")
                true
            } else {
                Log.d("IrrigationRepoImpl", "Failed to set threshold, response: $response")
                false
            }
        }catch (e: Exception) {
            Log.d("IrrigationRepoImpl", "Error setting threshold: ${e.message}")
            false
        }
    }


    



}
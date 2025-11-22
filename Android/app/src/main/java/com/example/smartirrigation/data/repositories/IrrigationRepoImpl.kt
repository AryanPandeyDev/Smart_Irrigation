package com.example.smartirrigation.data.repositories

import android.util.Log
import com.example.smartirrigation.data.network.dto.IrrigatorInfo
import com.example.smartirrigation.data.network.dto.Mode
import com.example.smartirrigation.data.network.dto.ModeResponse
import com.example.smartirrigation.data.network.dto.PumpStatus
import com.example.smartirrigation.data.network.dto.PumpStatusResponse
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
                    // Set socket timeout above 15s heartbeat so idle read only times out on real stalls
                    socketTimeoutMillis = 20_000
                }
            }.execute { response ->
                val channel = response.bodyAsChannel()

                while (!channel.isClosedForRead) {
                    val line = channel.readUTF8Line() ?: continue
                    val trimmed = line.trim()

                    // Ignore empty lines and SSE comment/heartbeat lines
                    if (trimmed.isEmpty()) continue
                    if (trimmed.startsWith(":")) {
                        // Comment/heartbeat: e.g., ": ping" from server
                        continue
                    }
                    if (trimmed.startsWith("event: ping", ignoreCase = true)) {
                        // Named heartbeat event; ignore
                        continue
                    }

                    if (trimmed.startsWith("data:")) {
                        val jsonData = trimmed.removePrefix("data:").trimStart()
                        val irrigatorInfo = json.decodeFromString<IrrigatorInfo>(jsonData)
                        Log.d("IrrigationRepoImpl", "Received data: $irrigatorInfo")
                        emit(irrigatorInfo)
                    }
                }
                // Channel closed or response ended: signal disconnect
                Log.d("IrrigationRepoImpl", "SSE channel closed or response ended. Emitting disconnect (null)")
                emit(null)
            }
        } catch (e: Exception) {

            Log.d("IrrigationRepoImpl", "Error: ${e.message}")
            e.printStackTrace()
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

    override suspend fun setControlMode(isManual: Boolean): Boolean {
        return try {
            val response: ModeResponse? = httpClient.post("http://192.168.1.150/setMode") {
                header("Authorization", "Bearer myStrongAdminKey123")
                contentType(io.ktor.http.ContentType.Application.Json)
                setBody(Mode(isManual))
            }.body()
            if (response != null && response.status == "ok") {
                Log.d("IrrigationRepoImpl", "Control mode set successfully: $isManual")
                true
            } else {
                Log.d("IrrigationRepoImpl", "Failed to set control mode, response: $response")
                false
            }
        }catch (e: Exception) {
            Log.d("IrrigationRepoImpl", "Error setting control mode: ${e.message}")
            false
        }
    }

    override suspend fun turnOnPump(pumpStatus: Boolean): Boolean {
        return try {
            val response: PumpStatusResponse? = httpClient.post("http://192.168.1.150/setPump") {
                header("Authorization", "Bearer myStrongAdminKey123")
                contentType(io.ktor.http.ContentType.Application.Json)
                setBody(PumpStatus(pumpStatus))
            }.body()
            if (response != null && response.status == "ok") {
                Log.d("IrrigationRepoImpl", "Pump status set successfully: $pumpStatus")
                true
            } else {
                Log.d("IrrigationRepoImpl", "Failed to set pump status, response: $response")
                false
            }
        }catch (e: Exception) {
            Log.d("IrrigationRepoImpl", "Error setting pump status: ${e.message}")
            false
        }
    }


}
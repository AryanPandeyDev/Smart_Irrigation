package com.example.smartirrigation.data.repositories

import android.R.attr.text
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.smartirrigation.data.network.dto.IrrigatorInfo
import com.example.smartirrigation.domain.repositories.IrrigationRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText

class IrrigationRepoImpl(val httpClient : HttpClient,val context: Context) : IrrigationRepository {

    override suspend fun getStatus(): IrrigatorInfo? {
        try {
            val status = httpClient.get("http://192.168.1.150/status").bodyAsText()
            if (status.isNotEmpty()) {
                val values = status.lines().map { line ->
                    line.split(":", limit = 2)[1].trim()
                }
                Log.d("IrrigationRepoImpl", status)
                return IrrigatorInfo(
                    threshold = values[0].toInt(),
                    soilMoisture = values[1].toInt(),
                    relayStatus = (values[2] == "ON")
                )
            }
            return null
        } catch (e: Exception) {
            Log.d("IrrigationRepoImpl", "Error fetching status: ${e.message}")
            Toast.makeText(context, "Error fetching status: ${e.message}", Toast.LENGTH_LONG).show()
            return null
        }
    }
}
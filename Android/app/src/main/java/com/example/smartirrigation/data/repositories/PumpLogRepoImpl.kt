package com.example.smartirrigation.data.repositories

import com.example.smartirrigation.data.local.model.PumpLogEntry
import com.example.smartirrigation.data.local.preferences.DatastoreManager
import com.example.smartirrigation.domain.repositories.PumpLogRepository
import kotlinx.coroutines.flow.Flow

class PumpLogRepoImpl(
    private val datastoreManager: DatastoreManager
) : PumpLogRepository {

    override suspend fun addLogEntry(entry: PumpLogEntry) {
        datastoreManager.addPumpLogEntry(entry)
    }

    override fun getLogEntries(): Flow<List<PumpLogEntry>> {
        return datastoreManager.pumpLogsFlow
    }

    override suspend fun clearLogs() {
        datastoreManager.clearPumpLogs()
    }
}

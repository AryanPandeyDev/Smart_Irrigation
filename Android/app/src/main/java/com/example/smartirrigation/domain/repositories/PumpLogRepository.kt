package com.example.smartirrigation.domain.repositories

import com.example.smartirrigation.data.local.model.PumpLogEntry
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for pump history logs.
 */
interface PumpLogRepository {
    
    /**
     * Add a new log entry when pump status changes.
     */
    suspend fun addLogEntry(entry: PumpLogEntry)
    
    /**
     * Get all log entries as a flow.
     */
    fun getLogEntries(): Flow<List<PumpLogEntry>>
    
    /**
     * Clear all log entries.
     */
    suspend fun clearLogs()
}

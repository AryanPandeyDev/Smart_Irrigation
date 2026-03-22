package com.example.smartirrigation.data.local.preferences


import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.smartirrigation.data.local.model.PumpLogEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_settings")

class DatastoreManager(private val context: Context) {

    private val json = Json {
        prettyPrint = false
        ignoreUnknownKeys = true
    }

    companion object {
        private val PLANT_NAME = stringPreferencesKey("plant_name")
        private val ASKED_PERMISSION = booleanPreferencesKey("asked_permission")
        private val USER_LOCATION = stringPreferencesKey("user_location")
        private val NOTIFICATION_ENABLED = booleanPreferencesKey("notification_enabled")
        private val DARK_MODE_ENABLED = booleanPreferencesKey("dark_mode_enabled")
        private val PUMP_LOGS = stringPreferencesKey("pump_logs")
    }
    suspend fun changeAskedPermission(askedPermission: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[ASKED_PERMISSION] = askedPermission
        }
    }
    val askedPermissionFlow: Flow<Boolean> =
        context.dataStore.data.map { prefs ->
            prefs[ASKED_PERMISSION] ?: false
        }


    suspend fun savePlantName(plant: String) {
        context.dataStore.edit { prefs ->
            prefs[PLANT_NAME] = plant
        }
    }

    suspend fun getPlantInfo(): String? {
        return context.dataStore.data.first()[PLANT_NAME]
    }

    suspend fun saveUserLocation(location: String) {
        context.dataStore.edit { prefs ->
            prefs[USER_LOCATION] = location
        }
    }

    suspend fun getUserLocation(): String? {
        return context.dataStore.data.first()[USER_LOCATION]
    }

    suspend fun saveNotificationPreference(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[NOTIFICATION_ENABLED] = enabled
        }
    }

    suspend fun getNotificationPreference(): Boolean {
        return context.dataStore.data.first()[NOTIFICATION_ENABLED] ?: false
    }

    suspend fun saveDarkModePreference(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[DARK_MODE_ENABLED] = enabled
        }
    }

    suspend fun getDarkModePreference(): Boolean {
        return context.dataStore.data.first()[DARK_MODE_ENABLED] ?: false
    }

    // Pump Logs functions
    suspend fun addPumpLogEntry(entry: PumpLogEntry) {
        context.dataStore.edit { prefs ->
            val currentLogs = prefs[PUMP_LOGS]?.let {
                try {
                    json.decodeFromString<List<PumpLogEntry>>(it).toMutableList()
                } catch (e: Exception) {
                    mutableListOf()
                }
            } ?: mutableListOf()
            
            // Prevent duplicate logs - check if last entry has same pump status
            val lastEntry = currentLogs.firstOrNull()
            if (lastEntry != null && lastEntry.pumpStatus == entry.pumpStatus) {
                // Skip - same status already logged
                return@edit
            }
            
            currentLogs.add(0, entry) // Add to beginning (newest first)
            
            // Keep only last 100 entries
            val trimmedLogs = currentLogs.take(100)
            prefs[PUMP_LOGS] = json.encodeToString(trimmedLogs)
        }
    }

    val pumpLogsFlow: Flow<List<PumpLogEntry>> =
        context.dataStore.data.map { prefs ->
            prefs[PUMP_LOGS]?.let {
                try {
                    json.decodeFromString<List<PumpLogEntry>>(it)
                } catch (e: Exception) {
                    emptyList()
                }
            } ?: emptyList()
        }

    suspend fun clearPumpLogs() {
        context.dataStore.edit { prefs ->
            prefs.remove(PUMP_LOGS)
        }
    }
}


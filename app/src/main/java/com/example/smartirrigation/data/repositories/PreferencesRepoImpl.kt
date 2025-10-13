package com.example.smartirrigation.data.repositories

import android.util.Log
import com.example.smartirrigation.data.local.preferences.DatastoreManager
import com.example.smartirrigation.domain.repositories.PreferencesRepository

class PreferencesRepoImpl(val datastoreManager : DatastoreManager) : PreferencesRepository {
    override suspend fun savePlantInfo(plantName: String): Boolean {
        try {
            datastoreManager.savePlantName(plantName)
            return true
        } catch (e: Exception) {
            Log.d("PreferencesRepoImpl", "Error saving plant info: ${e.message}")
            return false
        }
    }

    override suspend fun getPlantInfo(): String? {
        try {
            return datastoreManager.getPlantInfo()
        } catch (e: Exception) {
            Log.d("PreferencesRepoImpl", "Error retrieving plant info: ${e.message}")
            return null
        }
    }
}
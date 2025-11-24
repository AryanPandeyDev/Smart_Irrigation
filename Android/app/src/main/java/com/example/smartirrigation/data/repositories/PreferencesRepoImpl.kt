package com.example.smartirrigation.data.repositories

import android.util.Log
import com.example.smartirrigation.data.local.preferences.DatastoreManager
import com.example.smartirrigation.domain.repositories.PreferencesRepository
import kotlinx.coroutines.flow.Flow

class PreferencesRepoImpl(
    val datastoreManager : DatastoreManager,
    override val askedPermissionFLow: Flow<Boolean> = datastoreManager.askedPermissionFlow,
) : PreferencesRepository {
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

    override suspend fun changeAskedPermission(askedPermission: Boolean) {
        try {
            datastoreManager.changeAskedPermission(askedPermission)
        } catch (e: Exception) {
            Log.d("PreferencesRepoImpl", "Error changing asked permission: ${e.message}")
        }
    }

    override suspend fun saveUserLocation(location: String) {
        try {
            datastoreManager.saveUserLocation(location)
        } catch (e: Exception) {
            Log.d("PreferencesRepoImpl", "Error saving user location: ${e.message}")
        }
    }

    override suspend fun getUserLocation(): String? {
        return try {
            datastoreManager.getUserLocation()
        } catch (e: Exception) {
            Log.d("PreferencesRepoImpl", "Error getting user location: ${e.message}")
            null
        }
    }
}
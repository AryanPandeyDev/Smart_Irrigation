package com.example.smartirrigation.domain.repositories

import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {

    suspend fun changeAskedPermission(askedPermission : Boolean)

    val askedPermissionFLow : Flow<Boolean>

    suspend fun savePlantInfo(plantName : String) : Boolean

    suspend fun getPlantInfo() : String?

    suspend fun saveUserLocation(location: String)

    suspend fun getUserLocation(): String?
    suspend fun saveNotificationPreference(enabled: Boolean)
    suspend fun getNotificationPreference(): Boolean
    suspend fun saveDarkModePreference(enabled: Boolean)
    suspend fun getDarkModePreference(): Boolean
}
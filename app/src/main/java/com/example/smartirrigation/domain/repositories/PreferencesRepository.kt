package com.example.smartirrigation.domain.repositories

interface PreferencesRepository {

    suspend fun savePlantInfo(plantName : String) : Boolean

    suspend fun getPlantInfo() : String?

}
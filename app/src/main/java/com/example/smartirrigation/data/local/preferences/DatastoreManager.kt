package com.example.smartirrigation.data.local.preferences


import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map


private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_settings")

class DatastoreManager(private val context: Context) {

    companion object {
        private val PLANT_NAME = stringPreferencesKey("plant_name")
        private val ASKED_PERMISSION = booleanPreferencesKey("asked_permission")
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
}

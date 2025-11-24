package com.example.smartirrigation

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.example.smartirrigation.common.AppState
import com.example.smartirrigation.data.local.preferences.DatastoreManager
import com.example.smartirrigation.domain.repositories.PreferencesRepository
import com.example.smartirrigation.presentation.dashboard.foreground_service.PumpStatusService
import com.example.smartirrigation.presentation.navigation.Navigation
import com.example.smartirrigation.presentation.ui.theme.AppTheme
import com.example.smartirrigation.presentation.utils.LocationHelper
import com.example.smartirrigation.presentation.utils.hasNotificationPermission
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var locationHelper: LocationHelper

    @Inject
    lateinit var preferencesRepository: PreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            AppTheme(AppState.isDarkMode) {
                Navigation()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (hasNotificationPermission(
                this,
                permission = Manifest.permission.POST_NOTIFICATIONS
            )) {
            Intent(this, PumpStatusService::class.java).also {
                startService(it)
            }
        }
        
        checkAndSaveLocation()
    }

    private fun checkAndSaveLocation() {
        lifecycleScope.launch {
            val savedLocation = preferencesRepository.getUserLocation()
            if (savedLocation.isNullOrBlank()) {
                if (locationHelper.hasLocationPermission()) {
                    withContext(Dispatchers.IO) {
                        val city = locationHelper.getCurrentCity()
                        if (city != null) {
                            preferencesRepository.saveUserLocation(city)
                            Log.d("LocationCity", "Location saved: $city")
                        } else {
                            Log.d("LocationCity", "Could not fetch city")
                        }
                    }
                }
            }
        }
    }
}
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
    lateinit var preferencesRepository: PreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        
        lifecycleScope.launch(Dispatchers.Main) {
            val isDarkMode = preferencesRepository.getDarkModePreference()
            AppState.isDarkMode.value = isDarkMode
        }

        setContent {
            AppTheme(AppState.isDarkMode.value) {
                Navigation()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            val hasPermission = hasNotificationPermission(
                this@MainActivity,
                permission = Manifest.permission.POST_NOTIFICATIONS
            )

            if (!hasPermission) {
                // Permission revoked in settings, update DataStore and stop service
                preferencesRepository.saveNotificationPreference(false)
                Intent(this@MainActivity, PumpStatusService::class.java).also {
                    stopService(it)
                }
            } else {
                // Permission granted, respect user preference
                if (preferencesRepository.getNotificationPreference()) {
                    Intent(this@MainActivity, PumpStatusService::class.java).also {
                        startService(it)
                    }
                } else {
                    // Permission granted but toggle is OFF, ensure service is stopped
                    Intent(this@MainActivity, PumpStatusService::class.java).also {
                        stopService(it)
                    }
                }
            }
        }
    }
}

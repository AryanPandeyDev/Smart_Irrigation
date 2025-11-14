package com.example.smartirrigation

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
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
import com.example.smartirrigation.presentation.dashboard.foreground_service.PumpStatusService
import com.example.smartirrigation.presentation.navigation.Navigation
import com.example.smartirrigation.presentation.ui.theme.AppTheme
import com.example.smartirrigation.presentation.utils.hasNotificationPermission
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

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

    }
}
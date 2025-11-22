package com.example.smartirrigation.presentation.settings.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.smartirrigation.presentation.navigation.Routes

@Composable
fun SettingsScreen() {
    Box (
        Modifier.fillMaxSize()
    ){
        Text(
            modifier = Modifier.align(Alignment.Center),
            text = "Settings Screen"
        )
    }
}
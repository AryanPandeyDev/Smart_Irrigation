package com.example.smartirrigation.presentation.dashboard.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun QuickActionCardsCombined(
    manualControlToggle: () -> Unit,
    historyLogsToggle: () -> Unit,
) {
    Text(
        text = "Quick Actions",
        fontSize = 19.sp,
        style = MaterialTheme.typography.bodyLarge.copy(
            fontWeight = FontWeight.SemiBold
        ),
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(vertical = 8.dp)
    )

    QuickActionCard(
        title = "Set Control",
        icon = Icons.Default.Tune,
        onClick = manualControlToggle

    )

    QuickActionCard(
        title = "History & Logs",
        icon = Icons.Default.History,
        onClick = historyLogsToggle
    )
}
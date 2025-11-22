package com.example.smartirrigation.presentation.dashboard.components


import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.example.smartirrigation.presentation.ui.theme.AppTheme
import com.primex.core.ExperimentalToolkitApi
import com.primex.core.blur.legacyBackgroundBlur

@OptIn(ExperimentalToolkitApi::class)
@Composable
fun InfoDisplayCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    info: String,
    iconType: IconType = IconType.MOISTURE,
) {
    val iconColor = when (iconType) {
        IconType.MOISTURE -> MaterialTheme.colorScheme.primary
        IconType.THRESHOLD -> MaterialTheme.colorScheme.secondary
    }

    Box(
        modifier = modifier
            .height(135.dp)
            .clip(RoundedCornerShape(24.dp))
            .border(2.dp,
                MaterialTheme.colorScheme.outline,
                RoundedCornerShape(24.dp))
            .legacyBackgroundBlur(
                radius = 23f,
                downsample = 0.1f
            )
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(0.2f)),
    ) {
        Column (
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Row(
                modifier = modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Icon container
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = iconColor.copy(alpha = 0.1f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (iconType) {
                            IconType.MOISTURE -> Icons.Default.WaterDrop
                            IconType.THRESHOLD -> Icons.Default.Thermostat
                        },
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Value
                Text(
                    text = value,
                    modifier = Modifier.padding(horizontal = 5.dp),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Medium,
                    fontSize = 24.sp,
                    color = iconColor.copy(0.7f)
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
                Text(
                    text = info,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    fontSize = 11.sp
                )
            }
        }

    }
}

enum class IconType {
    MOISTURE,
    THRESHOLD
}

@Preview(
    showSystemUi = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun SimpleInfoDisplayCardPreview() {
    AppTheme {
        InfoDisplayCard(
            title = "Moisture Level",
            value = "750",
            info = "Current soil moisture",
            iconType = IconType.MOISTURE
        )
    }
}

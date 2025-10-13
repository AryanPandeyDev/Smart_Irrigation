package com.example.smartirrigation.presentation.setup.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.smartirrigation.presentation.setup.components.AppButton
import com.example.smartirrigation.presentation.setup.components.AppTextField
import com.example.smartirrigation.presentation.setup.state.PlantSetupUiState
import com.example.smartirrigation.presentation.setup.viewmodel.PlantSetupViewModel
import com.example.smartirrigation.presentation.ui.theme.AppTheme


@Composable
fun SetPreferencesScreen(
    viewModel: PlantSetupViewModel = hiltViewModel(),
    context: Context = LocalContext.current,
    onSave: (PlantSetupUiState) -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()



    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary)
    ) {
        Box(
            modifier = Modifier
                .wrapContentHeight()
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(20.dp)
                .background(
                    MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(24.dp)
                ),
        ) {
            Column(
                modifier = Modifier
                    .wrapContentHeight()
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Plant Setup",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Spacer(modifier = Modifier.padding(10.dp))

                AppTextField(
                    value = state.plantType,
                    onValueChange = viewModel::updatePlantType,
                    label = "Enter the type of plant you are growing",
                    modifier = Modifier.width(300.dp),
                    placeholder = "e.g., Tomato, Cactus",
                    supportingText = state.errors.plantError ?: "",
                    keyboardOptions = KeyboardOptions.Default
                )
                Spacer(modifier = Modifier.padding(4.dp))

                AppTextField(
                    value = state.threshold,
                    onValueChange = viewModel::updateThreshold,
                    label = "Set the soil moisture threshold.",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    placeholder = "e.g., 700",
                    supportingText =
                        if (state.errors.thresholdEmpty != null) state.errors.thresholdEmpty
                        else state.errors.thresholdInvalid ?: "",
                    modifier = Modifier.width(300.dp)
                )

                Spacer(modifier = Modifier.padding(4.dp))

                AppButton(
                    onClick = {
                        viewModel.onSave(state.threshold.toInt()) { message ->
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        }
                        onSave(state)
                    },
                    modifier = Modifier.width(300.dp),
                    text = "save",
                    enabled = viewModel.setIsEnabled()
                )
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun SetPreferencesScreenPreview() {
    AppTheme {

//        SetPreferencesScreen(viewModel = vm)
    }
}
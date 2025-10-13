package com.example.smartirrigation.presentation.setup.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.smartirrigation.presentation.setup.components.SetupInstructions
import com.example.smartirrigation.presentation.setup.viewmodel.SetupViewModel

@Composable
fun SetupScreen(
    viewModel: SetupViewModel = hiltViewModel(),
    setUpComplete: () -> Unit = {}
) {

    val state = viewModel.state.collectAsState()

    LaunchedEffect(state.value.isSuccess) {
        if (state.value.isSuccess) {
            setUpComplete()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.primaryContainer
                    )
                )
            )
    ) {
        Box (
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ){
            SetupInstructions(modifier = Modifier.align(Alignment.Center)) {
                viewModel.refresh()
            }

            Text(
                if (state.value.isSuccess)
                "Connected successfully!"
                else if (state.value.isLoading)
                    "Connecting..."
                else if (state.value.error != null)
                    "Error: ${state.value.error}"
                else
                    "Please follow the instructions to connect.",
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 32.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Preview
@Composable
fun SetupScreenPreview() {
//    AppTheme {
////        SetupScreen()
//    }
}
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.smartirrigation.presentation.setup.components.SetupInstructions
import com.example.smartirrigation.presentation.setup.viewmodel.SetupViewModel
import com.example.smartirrigation.presentation.ui.theme.AppTheme

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
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Box (
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ){
            SetupInstructions(
                modifier = Modifier.align(Alignment.Center),
                isLoading = state.value.isLoading
            ) {
                viewModel.refresh()
            }

            val statusText = when {
                state.value.isSuccess -> "Connected successfully!"
                state.value.isLoading -> "Connecting..."
                state.value.error != null -> "Error: ${state.value.error}"
                else -> "Please follow the instructions to connect."
            }

            Text(
                text = statusText,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 32.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SetupScreenPreview() {
    AppTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Box (
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ){
                SetupInstructions(
                    modifier = Modifier.align(Alignment.Center),
                    isLoading = false
                )

                Text(
                    text = "Please follow the instructions to connect.",
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 32.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}
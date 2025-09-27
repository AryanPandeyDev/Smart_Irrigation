package com.example.smartirrigation.presentation.setup

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.smartirrigation.presentation.setup.components.SetupInstructions
import com.example.smartirrigation.presentation.ui.theme.AppTheme

@Composable
fun SetupScreen(
    viewModel: SetupViewModel = hiltViewModel(),
    context: Context = LocalContext.current
) {

    val state = viewModel.state.collectAsState()

    LaunchedEffect (state.value.isSuccess){
        if (state.value.isSuccess) {
            // Navigate to the next screen or perform any action on success
            Toast.makeText(context, "Setup Successful!", Toast.LENGTH_LONG).show()
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
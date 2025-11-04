package com.example.smartirrigation.presentation.chatbot.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun ChatbotScreen() {
    Box (
        Modifier.fillMaxSize()
    ){
        Text(
            modifier = Modifier.align(Alignment.Center),
            text = "Chatbot Screen"
        )
    }
}
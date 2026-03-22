package com.example.smartirrigation.data.network

/**
 * API configuration for the chatbot backend.
 * Change CHATBOT_BASE_URL when deploying to Google Cloud.
 */
object ApiConfig {
    // ESP8266 local IP (for irrigation device)
    const val ESP8266_BASE_URL = "http://192.168.1.150"
    
    // Chatbot API base URL
    // For PHYSICAL DEVICE: Use your PC's IP address (run 'ipconfig' in cmd to find it)
    // For EMULATOR: Use "http://10.0.2.2:8000"
    // For GOOGLE CLOUD: Use your cloud URL e.g. "https://your-project.run.app"
    const val CHATBOT_BASE_URL = "http://192.168.1.10:8000" // Change to your PC's IP
    
    // ESP8266 auth token
    const val ESP8266_AUTH_TOKEN = "myStrongAdminKey123"
}


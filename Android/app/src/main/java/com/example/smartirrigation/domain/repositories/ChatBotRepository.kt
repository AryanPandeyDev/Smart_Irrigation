package com.example.smartirrigation.domain.repositories

import com.example.smartirrigation.data.network.dto.chatbot.ChatRequest
import com.example.smartirrigation.data.network.dto.chatbot.ChatResponse
import com.example.smartirrigation.data.network.dto.chatbot.StreamToken
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for AI chatbot communication.
 */
interface ChatBotRepository {
    
    /**
     * Send a message to the chatbot with SSE streaming response.
     * Emits StreamToken objects as they arrive.
     * 
     * @param request The chat request containing message and device state
     * @return Flow of StreamToken objects, completing when [DONE] is received
     */
    fun sendMessage(request: ChatRequest): Flow<StreamToken>
    
    /**
     * Send a message to the chatbot and get full response (non-streaming).
     * 
     * @param request The chat request containing message and device state
     * @return Complete ChatResponse with full text and optional action
     */
    suspend fun sendMessageSync(request: ChatRequest): ChatResponse?
}

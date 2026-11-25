package com.example.criptotreck.ui.gemini

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.criptotreck.data.remote.GeminiClient
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.Dispatchers

data class GeminiMessage(val text: String, val isUser: Boolean)

data class GeminiUiState(
    val messages: List<GeminiMessage> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class GeminiViewModel : ViewModel() {
    private val geminiClient = GeminiClient()
    private val _uiState = MutableStateFlow(GeminiUiState())
    val uiState: StateFlow<GeminiUiState> = _uiState.asStateFlow()

    fun addWelcomeMessages() {
        val welcome = listOf(
            GeminiMessage(
                "Привет! Я твой ИИ-помощник по криптовалютам. Задавай вопрос — например, «Что такое биткоин?» или «Как анализировать график ETH?»",
                false
            )
        )
        _uiState.update { it.copy(messages = it.messages + welcome) }
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        viewModelScope.launch {
            val userMessage = GeminiMessage(text, true)
            _uiState.update { it.copy(messages = it.messages + userMessage, isLoading = true, error = null) }

            try {
                val response = withContext(Dispatchers.IO) {
                    geminiClient.generateText(text)
                }
                _uiState.update {
                    it.copy(
                        messages = it.messages + GeminiMessage(response, false),
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        messages = it.messages + GeminiMessage("Ошибка: ${e.message}", false),
                        isLoading = false
                    )
                }
            }
        }
    }
}
package com.example.criptotreck.ui.theme.crypto

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class)
class CryptoViewModel(
    private val viewModelScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    private val _uiState = MutableStateFlow<List<CryptoAsset>>(emptyList())
    val uiState: StateFlow<List<CryptoAsset>> = _uiState.asStateFlow()

   val searchBarState = TextFieldState()

    init {
        // Инициализируем стартовый список
        _uiState.value = listOf(
            CryptoAsset("1", "Bitcoin", "BTC", 92000.0, 1.5),
            CryptoAsset("2", "Ethereum", "ETH", 3100.0, -0.8),
            CryptoAsset("3", "Solana", "SOL", 210.0, 5.4)
        )
        val uiState = combine(
            uiState,
            snapshotFlow{searchBarState.text.toString()}.debounce(300.milliseconds)
        ){

        }

    }

    private fun startLiveUpdates() {
        viewModelScope.launch {
            while (true) {
                delay(1000) // Асинхронная пауза на 1 секунду
                _uiState.update { currentList ->
                    currentList.map { asset ->
                        // Случайное колебание цены в реальном времени
                        val change = Random.nextDouble(-2.0, 2.0)
                        val newPrice = asset.currentPrice * (1 + change / 100)
                        asset.copy(currentPrice = newPrice, priceChange24h = change)
                    }
                }
            }
        }
    }

    fun toggleFavorite(assetId: String) {
        _uiState.update { currentList ->
            currentList.map {
                if (it.id == assetId) it.copy(isFavorite = !it.isFavorite) else it
            }
        }
    }
}

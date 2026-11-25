package com.example.criptotreck.ui.screens.market

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.criptotreck.data.model.CryptoCoinUi
import com.example.criptotreck.data.repository.BinanceRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

data class MarketUiState(
    val listCoins: List<CryptoCoinUi> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val isLiveUpdateActive: Boolean = true
)

@OptIn(FlowPreview::class)
class CryptoViewModel(
    private val binanceRepository: BinanceRepository = BinanceRepository()
) : ViewModel() {
    val searchQuery = MutableStateFlow("")
    private val _marketState = MutableStateFlow(MarketUiState())

    val uiState: StateFlow<MarketUiState> = combine(
        _marketState,
        searchQuery.debounce(300.milliseconds)
    ) { state, query ->
        val filtered = if (query.isBlank()) {
            state.listCoins
        } else {
            state.listCoins.filter { coin ->
                coin.symbol.contains(query, ignoreCase = true) ||
                        coin.baseAsset.contains(query, ignoreCase = true)
            }
        }
        state.copy(listCoins = filtered)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MarketUiState()
    )

    fun onSearchQueryChange(query: String) {
        searchQuery.value = query
    }

    init {
        loadMarketData()
    }

    fun loadMarketData() {
        viewModelScope.launch {
            _marketState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = binanceRepository.getMarketCoins()
            result.fold(
                onSuccess = { coins ->
                    _marketState.update { it.copy(listCoins = coins, isLoading = false) }
                    startLiveStreaming()
                },
                onFailure = { error ->
                    _marketState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.localizedMessage ?: "Ошибка загрузки данных"
                        )
                    }
                }
            )
        }
    }

    private fun startLiveStreaming() {
        viewModelScope.launch {
            binanceRepository.streamMarketCoins(6000L).collect { result ->
                if (_marketState.value.isLiveUpdateActive) {
                    result.onSuccess { coins ->
                        _marketState.update { it.copy(listCoins = coins) }
                    }
                }
            }
        }
    }

    fun toggleFavorite(symbol: String) {
        _marketState.update { state ->
            val updated = state.listCoins.map { coin ->
                if (coin.symbol == symbol) coin.copy(isFavorite = !coin.isFavorite) else coin
            }
            state.copy(listCoins = updated)
        }
    }

    fun toggleLiveUpdate() {
        _marketState.update { it.copy(isLiveUpdateActive = !it.isLiveUpdateActive) }
    }
}

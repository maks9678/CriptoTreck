package com.example.criptotreck.ui.screens.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.criptotreck.data.model.CryptoAsset
import com.example.criptotreck.data.model.ListCrypto
import com.example.criptotreck.data.repository.BinanceRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

data class CryptoDetailsState(
    val cryptoAsset: CryptoAsset? = null,
    val currentBalance: Double = 0.0,
    val isMine: Boolean = false,
    val isLoading: Boolean = true,
)

class CryptoDetailsViewModel(
    private val binanceRepository: BinanceRepository = BinanceRepository(),
) : ViewModel() {
    private val _uiState = MutableStateFlow(CryptoDetailsState())
    val uiState: StateFlow<CryptoDetailsState> = _uiState.asStateFlow()
    private var liveUpdateJob: Job? = null

    fun mineCrypto() {
        viewModelScope.launch {
            _uiState.update { it.copy(isMine = true) }
            delay(3000.milliseconds)
            _uiState.update {
                it.copy(
                    currentBalance = it.currentBalance + 100.0,
                    isMine = false,
                )
            }
        }
    }

    fun loadCryptoById(idCrypto: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // 1. Проверяем локальный список
            val localAsset = ListCrypto.getCryptoById(idCrypto)
            if (localAsset != null) {
                _uiState.update { it.copy(cryptoAsset = localAsset, isLoading = false) }
                startLiveUpdates(localAsset.symbol)
                return@launch
            }

            // 2. Если нет в локальном списке, запрашиваем актуальные данные с Binance API
            val result = binanceRepository.getMarketCoins()
            result.onSuccess { coins ->
                val foundCoin = coins.find { (symbol, baseAsset) ->
                    symbol.equals(idCrypto, ignoreCase = true) || baseAsset.equals(idCrypto, ignoreCase = true)
                }
                if (foundCoin != null) {
                    val asset = CryptoAsset(
                        id = foundCoin.symbol,
                        name = foundCoin.baseAsset,
                        symbol = foundCoin.baseAsset,
                        currentPrice = foundCoin.currentPrice,
                        priceChange24h = foundCoin.changePercent24h,
                    )
                    _uiState.update { it.copy(cryptoAsset = asset, isLoading = false) }
                    startLiveUpdates(asset.symbol)
                } else {
                    val defaultAsset = CryptoAsset(idCrypto, idCrypto, idCrypto, 100.0, 0.0)
                    _uiState.update { it.copy(cryptoAsset = defaultAsset, isLoading = false) }
                    startLiveUpdates(idCrypto)
                }
            }.onFailure {
                val defaultAsset = CryptoAsset(idCrypto, idCrypto, idCrypto, 100.0, 0.0)
                _uiState.update { it.copy(cryptoAsset = defaultAsset, isLoading = false) }
                startLiveUpdates(idCrypto)
            }
        }
    }

    private fun startLiveUpdates(targetSymbol: String) {
        liveUpdateJob?.cancel()
        liveUpdateJob = viewModelScope.launch {
            binanceRepository.streamMarketCoins(5000L).collect { result ->
                result.onSuccess { coins ->
                    val updatedCoin = coins.find { (symbol, baseAsset) ->
                        symbol.equals(targetSymbol, ignoreCase = true) || baseAsset.equals(targetSymbol, ignoreCase = true)
                    }
                    if (updatedCoin != null) {
                        _uiState.update { state ->
                            state.cryptoAsset?.let { currentAsset ->
                                state.copy(
                                    cryptoAsset = currentAsset.copy(
                                        currentPrice = updatedCoin.currentPrice,
                                        priceChange24h = updatedCoin.changePercent24h,
                                    ),
                                )
                            } ?: state
                        }
                    }
                }
            }
        }
    }

    override fun onCleared() {
        liveUpdateJob?.cancel()
    }
}

package com.example.criptotreck.ui.screens.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.criptotreck.data.model.CryptoAsset
import com.example.criptotreck.data.model.ListCrypto
import com.example.criptotreck.data.repository.BinanceRepository
import com.example.criptotreck.ui.screens.demo.ChartPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlin.time.Duration.Companion.milliseconds

data class CryptoDetailsState(
    val cryptoAsset: CryptoAsset? = null,
    val chartPoints: List<ChartPoint> = emptyList(),
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

            supervisorScope {
                // Параллельно запрашиваем детали монеты и исторический график с Binance API
                val historicalChartDeferred = async { binanceRepository.getHistoricalChart(idCrypto) }
                val marketCoinsResult = binanceRepository.getMarketCoins()

                val chartPoints = historicalChartDeferred.await().getOrDefault(emptyList())

                val localAsset = ListCrypto.getCryptoById(idCrypto)
                if (localAsset != null) {
                    _uiState.update {
                        it.copy(
                            cryptoAsset = localAsset,
                            chartPoints = chartPoints.takeIf { pts -> pts.isNotEmpty() } ?: it.chartPoints,
                            isLoading = false,
                        )
                    }
                    startLiveUpdates(localAsset.symbol)
                    return@supervisorScope
                }

                marketCoinsResult.onSuccess { coins ->
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
                        _uiState.update {
                            it.copy(
                                cryptoAsset = asset,
                                chartPoints = chartPoints.takeIf { pts -> pts.isNotEmpty() } ?: it.chartPoints,
                                isLoading = false,
                            )
                        }
                        startLiveUpdates(asset.symbol)
                    } else {
                        val defaultAsset = CryptoAsset(idCrypto, idCrypto, idCrypto, 100.0, 0.0)
                        _uiState.update {
                            it.copy(
                                cryptoAsset = defaultAsset,
                                chartPoints = chartPoints.takeIf { pts -> pts.isNotEmpty() } ?: it.chartPoints,
                                isLoading = false,
                            )
                        }
                        startLiveUpdates(idCrypto)
                    }
                }.onFailure {
                    val defaultAsset = CryptoAsset(idCrypto, idCrypto, idCrypto, 100.0, 0.0)
                    _uiState.update {
                        it.copy(
                            cryptoAsset = defaultAsset,
                            chartPoints = chartPoints.takeIf { pts -> pts.isNotEmpty() } ?: it.chartPoints,
                            isLoading = false,
                        )
                    }
                    startLiveUpdates(idCrypto)
                }
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

package com.example.criptotreck.ui.theme.crypto

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

data class UiState(
    val listCrypto: List<CryptoAsset> = emptyList(),
    val isLoading: Boolean = true,
)

@OptIn(FlowPreview::class)
class CryptoViewModel(
) : ViewModel() {
    val searchBarState = TextFieldState()
    private val _allCryptoState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = combine(
        _allCryptoState,
        snapshotFlow { searchBarState.text.toString() }.debounce(300.milliseconds)
    ) { state, inputText ->
        val filterList = if (inputText.isBlank()) {
            state.listCrypto
        } else {
            state.listCrypto.filter { crupto ->
                crupto.name.contains(inputText, ignoreCase = true) ||
                        crupto.symbol.contains(inputText, ignoreCase = true)
            }
        }
        state.copy(listCrypto = filterList)

    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = UiState()
    )

    init {
        // Инициализируем стартовый список в _allCryptoState
        _allCryptoState.update {
            it.copy(
                listCrypto = listOf(
                    CryptoAsset("1", "Bitcoin", "BTC", 92000.0, 1.5),
                    CryptoAsset("2", "Ethereum", "ETH", 3100.0, -0.8),
                    CryptoAsset("3", "Solana", "SOL", 210.0, 5.4)
                )
            )
        }

        viewModelScope.launch {
            val coinsDeferred = async { loadMarketCoins() }
            val favoritesDeferred = async { loadUserFavorites() }
            val coins = coinsDeferred.await()
            val favorites = favoritesDeferred.await()
            val mergedList = coins.map { coin ->
                if (coin.id in favorites) coin.copy(isFavorite = true)
                else coin.copy(isFavorite = false)
            }
            _allCryptoState.update { it.copy(listCrypto = mergedList, isLoading = false) }
            startLiveUpdates()
        }

    }

    fun toggleFavorite(assetId: String) {
        _allCryptoState.update { currentState ->
            // 1. Создаём новый список, меняя флаг isFavorite у нужной монеты
            val updatedList = currentState.listCrypto.map { coin ->
                if (coin.id == assetId) {
                    coin.copy(isFavorite = !coin.isFavorite)
                } else {
                    coin
                }
            }
            // 2. Возвращаем копию всего UiState с обновлённым списком
            currentState.copy(listCrypto = updatedList)
        }
    }

    suspend fun loadMarketCoins(): List<CryptoAsset> {
        delay(2000.milliseconds)
        val listCrypto = listOf(
            CryptoAsset("1", "Bitcoin", "BTC", 92000.0, 2.5),
            CryptoAsset("2", "Ethereum", "ETH", 3100.0, -3.8),
            CryptoAsset("3", "Solana", "SOL", 210.0, 5.4)
        )
        return listCrypto

    }

    suspend fun loadUserFavorites(): Set<String> {
        delay(2000.milliseconds)
        return setOf("1")
    }

    private fun startLiveUpdates() {
        // Запускаем корутину в viewModelScope.
        // Она автоматически остановится, когда пользователь закроет экран.
        viewModelScope.launch {

            // Проверяем isActive вместо простого while(true),
            // чтобы цикл гарантированно прервался при отмене корутины.
            while (isActive) {
                // Ждём 3 секунды перед следующим обновлением.
                // delay не блокирует главный поток, экран продолжает плавно работать.
                delay(2000)

                // Обновляем исходный список монет через потокобезопасный .update
                _allCryptoState.update { currentState ->
                    val updatedList = currentState.listCrypto.map { coin ->
                        // Генерируем случайное изменение цены от -2% до +2%
                        val changePercent = Random.nextDouble(-0.02, 0.02)
                        val newPrice = coin.currentPrice * (1.0 + changePercent)

                        // Создаём копию монеты с новой ценой (округляем до 2 знаков)
                        coin.copy(
                            currentPrice = (newPrice * 100).toInt() / 100.0
                        )
                    }

                    // Возвращаем обновлённый UiState с новым списком
                    currentState.copy(listCrypto = updatedList)
                }
            }
        }
    }
}
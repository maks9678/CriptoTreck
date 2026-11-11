package com.example.criptotreck.ui.theme.cryptoDetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.criptotreck.ui.theme.crypto.CryptoAsset
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

val asset = CryptoAsset("1", "Bitcoin", "BTC", 92000.0, 1.5)
data class CryptoDetailsState(
    val cryptoAsset: CryptoAsset = asset,
    val currentBalance: Double = 0.0,
    val isMine: Boolean = false,

    )

class CryptoDetailsViewModel(
) : ViewModel() {
    private val _uiState = MutableStateFlow(CryptoDetailsState())
    val uiState: StateFlow<CryptoDetailsState> = _uiState.asStateFlow()
    fun mineCrypto() {
        viewModelScope.launch {
            _uiState.update{it.copy(isMine = true)}
            delay(3000.milliseconds)
            _uiState.update {
                it.copy(currentBalance = it.currentBalance + 100.0,
                    isMine = false)
            }
        }
    }
}
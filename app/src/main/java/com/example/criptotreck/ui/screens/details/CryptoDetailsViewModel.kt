package com.example.criptotreck.ui.screens.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.criptotreck.data.model.CryptoAsset
import com.example.criptotreck.ui.screens.market.ListCrypto
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
)

class CryptoDetailsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(CryptoDetailsState())
    val uiState: StateFlow<CryptoDetailsState> = _uiState.asStateFlow()

    fun mineCrypto() {
        viewModelScope.launch {
            _uiState.update { it.copy(isMine = true) }
            delay(3000.milliseconds)
            _uiState.update {
                it.copy(
                    currentBalance = it.currentBalance + 100.0,
                    isMine = false
                )
            }
        }
    }

    fun loadCryptoById(idCrypto: String) {
        ListCrypto.getCryptoById(idCrypto)?.let { foundCrypto ->
            _uiState.update { it.copy(cryptoAsset = foundCrypto) }
        }
    }
}

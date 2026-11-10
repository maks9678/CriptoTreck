package com.example.criptotreck.ui.theme.cryptoDetails

import com.example.criptotreck.ui.theme.crypto.CryptoAsset
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CryptoDetailsViewModel (

    private val viewModelScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
){
    private val _uiState = MutableStateFlow<CryptoAsset>(Any )
    val uiState: StateFlow<CryptoAsset> = _uiState.asStateFlow()

}
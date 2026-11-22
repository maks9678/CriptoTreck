package com.example.criptotreck.data.repository

import com.example.criptotreck.data.model.CryptoAsset
import com.example.criptotreck.ui.screens.market.ListCrypto
import kotlinx.coroutines.delay

class CryptoRepository {
    suspend fun getAssetDetails(assetId: String): CryptoAsset? {
        delay(300)
        return ListCrypto.getCryptoById(assetId) ?: ListCrypto.initialCryptoList.firstOrNull()
    }
}

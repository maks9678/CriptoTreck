package com.example.criptotreck.ui.theme.crypto

data class CryptoAsset(
    val id: String,
    val name: String,
    val symbol: String,
    val currentPrice: Double,
    val priceChange24h: Double,
    val isFavorite: Boolean = false
)

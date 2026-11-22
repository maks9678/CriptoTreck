package com.example.criptotreck.data.model

data class CryptoAsset(
    val id: String,
    val name: String,
    val symbol: String,
    val currentPrice: Double,
    val priceChange24h: Double,
    val isFavorite: Boolean = false,
)

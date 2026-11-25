package com.example.criptotreck.data.model

import kotlinx.serialization.Serializable

/**
 * UI-модель криптовалюты для отображения на экранах и графиках
 */
@Serializable
data class CryptoCoinUi(
    val symbol: String,
    val baseAsset: String,
    val currentPrice: Double,
    val changePercent24h: Double,
    val volume24h: Double,
    val isFavorite: Boolean = false
)

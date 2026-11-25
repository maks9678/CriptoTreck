package com.example.criptotreck.data.remote

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import retrofit2.http.GET
import retrofit2.http.Query

// --- DTO Модели для Binance REST API ---

@Serializable
data class ExchangeInfoResponseDto(
    val symbols: List<SymbolInfoDto> = emptyList(),
)

@Serializable
data class SymbolInfoDto(
    val symbol: String,
    val baseAsset: String,
    val quoteAsset: String,
    val status: String,
)

@Serializable
data class Ticker24hrDto(
    val symbol: String,
    val lastPrice: String,
    val priceChangePercent: String,
    val quoteVolume: String,
)

// --- Retrofit API интерфейс ---

interface BinanceApiService {
    @GET("api/v3/exchangeInfo")
    suspend fun getExchangeInfo(
        @Query("permissions") permissions: String = "SPOT",
        @Query("symbolStatus") symbolStatus: String = "TRADING",
        @Query("showPermissionSets") showPermissionSets: Boolean = false,
    ): ExchangeInfoResponseDto

    @GET("api/v3/ticker/24hr")
    suspend fun getTicker24hr(): List<Ticker24hrDto>

    @GET("api/v3/klines")
    suspend fun getKlines(
        @Query("symbol") symbol: String,
        @Query("interval") interval: String = "1h",
        @Query("limit") limit: Int = 100,
    ): List<List<JsonElement>>

    companion object {
        const val BASE_URL = "https://api.binance.com/"
    }
}

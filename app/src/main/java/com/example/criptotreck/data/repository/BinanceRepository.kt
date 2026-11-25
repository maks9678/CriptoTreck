package com.example.criptotreck.data.repository

import android.util.Log
import com.example.criptotreck.data.model.CryptoCoinUi
import com.example.criptotreck.data.remote.BinanceApiService
import com.example.criptotreck.ui.screens.demo.ChartPoint
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.supervisorScope
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.floatOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.max
import kotlin.time.Duration.Companion.milliseconds

/**
 * Репозиторий для получения данных с публичного REST API Binance.
 * Реализует параллельные запросы exchangeInfo и ticker/24hr, фильтрацию, сортировку,
 * загрузку исторических свечей (klines) и потоковую отправку обновлений в реальном времени.
 */
class BinanceRepository(
    private val apiService: BinanceApiService = createDefaultApiService(),
) {
    companion object {
        private val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
        }

        fun createDefaultApiService(): BinanceApiService {
            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build()

            val contentType = "application/json".toMediaType()

            val retrofit = Retrofit.Builder()
                .baseUrl(BinanceApiService.BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(json.asConverterFactory(contentType))
                .build()

            return retrofit.create(BinanceApiService::class.java)
        }
    }

    /**
     * Запрашивает оба эндпоинта параллельно (через async/await), сопоставляет по символу,
     * фильтрует по quoteAsset == "USDT", сортирует по объему и возвращает Result.
     */
    suspend fun getMarketCoins(): Result<List<CryptoCoinUi>> = runCatching {
        supervisorScope {
            val exchangeInfoDeferred = async { apiService.getExchangeInfo() }
            val tickerDeferred = async { apiService.getTicker24hr() }

            val exchangeInfo = exchangeInfoDeferred.await()
            val tickers = tickerDeferred.await()

            val tickerMap = tickers.associateBy { it.symbol }

            Log.i("BinanceRepository","${tickerMap.values}")
            Log.i("BinanceRepository","${exchangeInfo.symbols}")
            exchangeInfo.symbols
                .filter { it.quoteAsset == "USDT" && it.status == "TRADING" }
                .mapNotNull { symbolInfo ->
                    val ticker = tickerMap[symbolInfo.symbol] ?: return@mapNotNull null

                    CryptoCoinUi(
                        symbol = symbolInfo.symbol,
                        baseAsset = symbolInfo.baseAsset,
                        currentPrice = ticker.lastPrice.toDoubleOrNull() ?: 0.0,
                        changePercent24h = ticker.priceChangePercent.toDoubleOrNull() ?: 0.0,
                        volume24h = ticker.quoteVolume.toDoubleOrNull() ?: 0.0,
                    )
                }
                .sortedByDescending { it.volume24h }
        }
    }

    /**
     * Загружает исторические свечи (klines) для конкретной монеты с публичного API Binance
     * и возвращает готовый список ChartPoint для отрисовки графиков.
     */
    suspend fun getHistoricalChart(symbol: String, interval: String = "1h"): Result<List<ChartPoint>> = runCatching {
        val formattedSymbol = if (symbol.endsWith("USDT", ignoreCase = true)) symbol else "${symbol.uppercase()}USDT"
        val rawKlines = apiService.getKlines(symbol = formattedSymbol, interval = interval, limit = 100)
        val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())

        val points = rawKlines.mapIndexed { index, kline ->
            val openTime = kline[0].jsonPrimitive.longOrNull ?: System.currentTimeMillis()
            val open = kline[1].jsonPrimitive.floatOrNull ?: 0f
            val high = kline[2].jsonPrimitive.floatOrNull ?: 0f
            val low = kline[3].jsonPrimitive.floatOrNull ?: 0f
            val close = kline[4].jsonPrimitive.floatOrNull ?: 0f
            val volume = kline[5].jsonPrimitive.floatOrNull ?: 0f

            val dateStr = dateFormat.format(Date(openTime))

            ChartPoint(
                timestampIndex = index,
                open = open,
                high = high,
                low = low,
                close = close,
                volume = volume,
                dateLabel = dateStr,
            )
        }

        points.mapIndexed { index, pt ->
            val startIndex = max(0, index - 19)
            val ma = points.subList(startIndex, index + 1).map { it.close }.average().toFloat()
            pt.copy(ma20 = ma)
        }
    }

    /**
     * Поток для получения данных в реальном времени (периодический опрос REST API каждые 6 секунд).
     * Автоматически обрабатывает ошибки сети, не допуская падений приложения.
     */
    fun streamMarketCoins(intervalMillis: Long = 6000L): Flow<Result<List<CryptoCoinUi>>> = flow {
        while (true) {
            val result = getMarketCoins()
            emit(result)
            delay(intervalMillis.milliseconds)
        }
    }.flowOn(Dispatchers.IO)
}

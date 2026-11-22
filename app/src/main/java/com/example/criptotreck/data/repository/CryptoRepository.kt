package com.example.criptotreck.data.repository

import com.example.criptotreck.data.local.LocalDataSource
import com.example.criptotreck.data.model.CryptoAsset
import com.example.criptotreck.data.model.ListCrypto
import com.example.criptotreck.data.remote.RemoteDataSource
import com.example.criptotreck.ui.screens.demo.ChartPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow

/**
 * Репозиторий для работы с криптовалютными данными, живыми потоками цен через WebSocket
 * и локальной историей.
 */
class CryptoRepository(
    private val remoteDataSource: RemoteDataSource = RemoteDataSource(),
    private val localDataSource: LocalDataSource = LocalDataSource()
) {
    suspend fun getAssetDetails(assetId: String): CryptoAsset? {
        delay(300)
        return ListCrypto.getCryptoById(assetId) ?: ListCrypto.initialCryptoList.firstOrNull()
    }

    /**
     * Поток живых данных графика с Binance WebSocket с сохранением в локальную историю
     */
    fun streamLiveChart(): Flow<ChartPoint> {
        return remoteDataSource.streamMarketData()
    }

    /**
     * Сохранение точки в локальную историю
     */
    fun saveChartPointToHistory(point: ChartPoint) {
        localDataSource.savePoint(point)
    }

    /**
     * Получение сохраненной локальной истории
     */
    fun getLocalHistory(): List<ChartPoint> {
        return localDataSource.getHistory()
    }
}

package com.example.criptotreck.data.local

import com.example.criptotreck.ui.screens.demo.ChartPoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Локальный источник данных (LocalDataSource) для сохранения истории графиков и кэширования.
 * Хранит историю точек цен локально для просмотра в оффлайн-режиме.
 */
class LocalDataSource {
    private val _historyFlow = MutableStateFlow<List<ChartPoint>>(emptyList())
    val historyFlow: Flow<List<ChartPoint>> = _historyFlow.asStateFlow()

    /**
     * Сохраняет новую точку в локальную историю
     */
    fun savePoint(point: ChartPoint) {
        _historyFlow.update { currentList ->
            val updated = currentList + point
            // Ограничиваем историю последними 500 точками
            if (updated.size > 500) updated.takeLast(500) else updated
        }
    }

    /**
     * Сохраняет целый список исторических точек
     */
    fun saveAll(points: List<ChartPoint>) {
        _historyFlow.update { points }
    }

    /**
     * Возвращает текущую историю
     */
    fun getHistory(): List<ChartPoint> {
        return _historyFlow.value
    }

    /**
     * Очищает историю
     */
    fun clearHistory() {
        _historyFlow.update { emptyList() }
    }
}

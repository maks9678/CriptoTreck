package com.example.criptotreck.data.model

import androidx.compose.ui.graphics.Color

enum class CategoryNews(val title: String) {
    BURSE("Рынок"),
    DEFI("DeFi"),
    SAFETY("Безопасность");

    fun getContainerColor(): Color {
        return when (this) {
            BURSE -> Color(0xFF0878D5)  // Синий
            DEFI -> Color(0xFF53E11C)   // Зеленый
            SAFETY -> Color(0xFFFF0000) // Красный
        }
    }
}

data class Portfolio(
    val cash: Double = 0.0,
    val percent: Double = 0.0,
)

data class News(
    val idNews: String = "",
    val titleText: String = "",
    val descriptionText: String = "",
    val categoryNews: CategoryNews? = null,
    val timePublication: String = "",
    val minutesAgo: Int = 0,
)

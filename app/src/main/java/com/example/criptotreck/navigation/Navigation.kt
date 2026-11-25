package com.example.criptotreck.navigation

import kotlinx.serialization.Serializable

@Serializable
object NewsListRoute

@Serializable
object CryptoRoute

@Serializable
data class CryptoDetailRoute(
    val id: String
)

@Serializable
object CryptoDetailActivityRoute

@Serializable
object DemoRoute

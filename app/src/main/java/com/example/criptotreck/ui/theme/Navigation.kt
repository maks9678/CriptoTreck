package com.example.criptotreck.ui.theme

import kotlinx.serialization.Serializable


@Serializable
object NewsListRoute
@Serializable
object CryptoRoute
@Serializable
data class CryptoDetailRoute(
    val id:String
)
package com.example.criptotreck.data.model

object ListCrypto {
    val initialCryptoList = listOf(
        CryptoAsset("1", "Bitcoin", "BTC", 92000.0, 2.5),
        CryptoAsset("2", "Ethereum", "ETH", 3100.0, -3.8),
        CryptoAsset("3", "Solana", "SOL", 210.0, 5.4)
    )

    fun getCryptoById(id: String): CryptoAsset? {
        return initialCryptoList.find { id.equals(it.id, ignoreCase = true) || id.equals(it.symbol, ignoreCase = true) }
    }
}

package com.example.criptotreck.ui.screens.details

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.criptotreck.data.repository.CryptoRepository
import com.example.criptotreck.ui.theme.CriptoTreckTheme

class CryptoDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = CryptoRepository()
        val assetId = intent.getStringExtra("EXTRA_ASSET_ID") ?: "1"

        setContent {
            CriptoTreckTheme {
                CryptoDetailScreen(assetId = assetId, repository = repository)
            }
        }
    }
}

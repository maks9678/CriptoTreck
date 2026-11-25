package com.example.criptotreck.ui.screens.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.criptotreck.ui.components.MiningControlButton
import com.example.criptotreck.ui.screens.demo.InteractiveCryptoChartScreen

@Composable
fun CryptoDetailsScreen(
    idCrypto: String,
    onBackClick: () -> Unit,
) {
    val viewModel: CryptoDetailsViewModel = viewModel()
    LaunchedEffect(idCrypto) {
        viewModel.loadCryptoById(idCrypto)
    }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    CryptoDetailsIternal(
        state,
        viewModel::mineCrypto,
        onBackClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CryptoDetailsIternal(
    assets: CryptoDetailsState,
    mineCurrency: () -> Unit,
    onBackClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(assets.cryptoAsset?.name ?: "Загрузка...") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад",
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            val isNotLoaded = assets.isLoading || (assets.cryptoAsset == null)
            when {
                isNotLoaded -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Загрузка информации о монете...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                else -> {
                    val asset = assets.cryptoAsset
                    val symbol = asset.symbol
                    val currentPrice = asset.currentPrice
                    val priceChange24h = asset.priceChange24h

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column {
                                Text(
                                    text = "Баланс: ${assets.currentBalance} $symbol",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                val isPositive = priceChange24h >= 0
                                Text(
                                    text = "Цена: $$currentPrice (${if (isPositive) "+" else ""}$priceChange24h%)",
                                    color = if (isPositive) Color(0xFF00C853) else Color(0xFFFF3D00),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                            }

                            Button(
                                onClick = mineCurrency,
                                enabled = !assets.isMine,
                            ) {
                                Text(if (assets.isMine) "Майнинг..." else "Майнить 100")
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        MiningControlButton(coinTicker = symbol)
                        Spacer(modifier = Modifier.height(8.dp))

                        // Интерактивный график с реальными историческими данными с Binance API
                        InteractiveCryptoChartScreen(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            coinSymbol = symbol,
                            basePrice = currentPrice.toFloat(),
                            initialPriceChange = priceChange24h.toFloat(),
                            initialChartPoints = assets.chartPoints,
                        )
                    }
                }
            }
        }
    }
}

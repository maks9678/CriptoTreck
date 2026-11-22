package com.example.criptotreck.ui.screens.details

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.criptotreck.data.model.CryptoAsset
import com.example.criptotreck.data.repository.CryptoRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CryptoDetailScreen(
    assetId: String,
    repository: CryptoRepository
) {
    var asset by remember { mutableStateOf<CryptoAsset?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(assetId) {
        isLoading = true
        error = null
        try {
            val fetchedAsset = repository.getAssetDetails(assetId)
            asset = fetchedAsset
        } catch (e: Exception) {
            error = "Ошибка при загрузке: ${e.localizedMessage}"
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Детали актива (CryptoDetailActivity)") })
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoading -> CircularProgressIndicator()
                error != null -> Text(error!!)
                asset != null -> CryptoDetailContent(asset = asset!!)
                else -> Text("Актив с ID $assetId не найден.")
            }
        }
    }
}

@Composable
fun CryptoDetailContent(asset: CryptoAsset) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(asset.name, style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Text(asset.symbol, style = MaterialTheme.typography.headlineMedium)
        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

        Text("Текущая цена: $${String.format("%.2f", asset.currentPrice)}", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Изменение 24ч: ${String.format("%.2f", asset.priceChange24h)}%", style = MaterialTheme.typography.titleMedium)
    }
}

package com.example.criptotreck.ui.theme.crypto

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.criptotreck.ui.theme.CriptoTreckTheme

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CriptoTreckTheme {
        CryptoScreen()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CryptoScreen(viewModel: CryptoViewModel = remember { CryptoViewModel() }) {
    // Подписываемся на асинхронный StateFlow из ViewModel
    val assets by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("CryptoLearn Dashboard") }) }
    ) { paddingValues ->
        Column( modifier = Modifier
            .padding(paddingValues)) {
            OutlinedTextField(
                viewModel.searchBarState,
                modifier = Modifier
                    .padding(16.dp)
            )
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(assets, key = { it.id }) { asset ->
                    CryptoCard(
                        asset = asset,
                        onFavoriteClick = { viewModel.toggleFavorite(asset.id) })
                }
            }
        }
    }
}

@Composable
fun CryptoCard(asset: CryptoAsset, onFavoriteClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Левая часть: Имя и Тикер
            Column {
                Text(asset.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(asset.symbol, color = Color.Gray, fontSize = 14.sp)
            }

            // Правая часть: Цена и Лайк
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.padding(end = 16.dp)
                ) {
                    // Форматируем цену до 2 знаков
                    Text("$${"%.2f".format(asset.currentPrice)}", fontWeight = FontWeight.SemiBold)

                    // Цвет меняется в зависимости от роста/падения
                    val percentColor =
                        if (asset.priceChange24h >= 0) Color(0xFF2E7D32) else Color.Red
                    Text(
                        text = "${if (asset.priceChange24h >= 0) "+" else ""}${"%.2f".format(asset.priceChange24h)}%",
                        color = percentColor,
                        fontSize = 12.sp
                    )
                }

                // Иконка Избранного
                Icon(
                    imageVector = if (asset.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (asset.isFavorite) Color.Red else Color.Gray,
                    modifier = Modifier
                        .clickable { onFavoriteClick() }
                        .size(28.dp)
                )
            }
        }
    }
}

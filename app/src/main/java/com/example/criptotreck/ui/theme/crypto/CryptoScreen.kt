package com.example.criptotreck.ui.theme.crypto

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.criptotreck.ui.theme.CriptoTreckTheme

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CriptoTreckTheme {
        CryptoScreen{}
    }
}

@Composable
fun CryptoScreen(
    onCryptoClick: (String) -> Unit,
) {
    val viewModel: CryptoViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    CryptoIternal(
        uiState,
        viewModel.searchBarState,
        viewModel::toggleFavorite,
        viewModel::toggleLiveUpdate,
        onCryptoClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CryptoIternal(
    state: UiState,
    searchBarState: TextFieldState,
    onFavoriteClick: (String) -> Unit,
    onToggleLive: () -> Unit,
    onClickItemCrypto:(String)-> Unit,
) {

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("CryptoLearn Dashboard")
                },
                actions = {
                    IconButton(onClick = onToggleLive) {
                        Icon(
                            imageVector =
                                if (state.isLiveUpdateActive) {
                                    Icons.Default.Pause
                                } else {
                                    Icons.Default.PlayArrow
                                },
                            contentDescription = if (state.isLiveUpdateActive) {
                                "запустить обновление"
                            } else {
                                "остановить обновление"
                            }
                        )
                    }
                },
            )
        }
    ) { paddingValues ->
        if (state.isLoading) {
            CircularProgressIndicator(
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        } else {
            Column(
                modifier = Modifier
                    .padding(paddingValues)
            ) {
                OutlinedTextField(
                    searchBarState,
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
                    items(state.listCrypto, key = { it.id }) { asset ->
                        CryptoCard(
                            asset = asset,
                            onFavoriteClick = { onFavoriteClick(asset.id) },
                            onItemClick = { onClickItemCrypto(asset.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CryptoCard(
    asset: CryptoAsset,
    onFavoriteClick: () -> Unit,
    onItemClick: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth(),
        onClick = onItemClick
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

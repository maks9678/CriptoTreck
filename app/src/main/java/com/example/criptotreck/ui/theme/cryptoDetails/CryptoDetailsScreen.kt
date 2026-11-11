package com.example.criptotreck.ui.theme.cryptoDetails

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun CryptoDetailsScreen() {
    val viewModel: CryptoDetailsViewModel = viewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    CryptoDetailsIternal(
        state,
        viewModel::mineCrypto,
        {},

    )

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CryptoDetailsIternal(
    assets:CryptoDetailsState,
    mineCurrency : () -> Unit,
    onBackClick: () -> Unit,
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("${assets.cryptoAsset.name}") },
            navigationIcon = { IconButton(onBackClick){ Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Назад"
            )} }
        ) }

    ) { paddingValues ->
            Column(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("${assets.currentBalance}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.padding(horizontal = 2.dp))
                    Text(assets.cryptoAsset.symbol, color = Color.Gray, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.padding(vertical = 8.dp))
                Text("${assets.cryptoAsset.currentPrice}", color = Color.Gray, fontSize = 14.sp)
                Text("${assets.cryptoAsset.priceChange24h}", color = Color.Gray, fontSize = 14.sp)
                Spacer(modifier = Modifier.padding(vertical = 8.dp))
                Button(
                    onClick = mineCurrency ,
                    enabled = !assets.isMine,
                    modifier = Modifier.padding(2.dp)
                ) {
                    Text("майнить 100 ${assets.cryptoAsset.symbol}")
                }
            }
        }
    }
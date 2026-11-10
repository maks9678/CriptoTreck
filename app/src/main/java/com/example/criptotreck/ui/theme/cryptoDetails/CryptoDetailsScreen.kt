package com.example.criptotreck.ui.theme.cryptoDetails

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.criptotreck.ui.theme.crypto.CryptoAsset
import java.nio.file.WatchEvent

val crypto1 = CryptoAsset("1", "Bitcoin", "BTC", 92000.0, 1.5)

@Composable
@Preview(showBackground = true)
fun PreviewCryptoDetails() {
    CryptoDetailsScreen(crypto1)
}

@Composable
fun CryptoDetailsScreen(
    viewModel: CryptoDetailsViewModel = remember { CryptoDetailsViewModel() }
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        Column() {
            Text(viewModel..name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(cryptoAsset.symbol, color = Color.Gray, fontSize = 14.sp)
            Text(cryptoAsset.currentPrice, color = Color.Gray, fontSize = 14.sp)
            Text(cryptoAsset.priceChange24h, color = Color.Gray, fontSize = 14.sp)
        }

    }
}
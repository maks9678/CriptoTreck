package com.example.criptotreck

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.criptotreck.ui.theme.CriptoTreckTheme
import com.example.criptotreck.ui.theme.crypto.CryptoScreen
import com.example.criptotreck.ui.theme.cryptoDetails.CryptoDetailsScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CryptoScreen()
        }
    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CriptoTreckTheme {
        CryptoScreen()
    }
}
package com.example.criptotreck.ui.screens.demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import com.example.criptotreck.R
import com.example.criptotreck.ui.theme.CriptoTreckTheme

/**
 * Approach 4: XML with Compose View (Hybrid)
 */
class HybridActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hybrid)

        val composeView = findViewById<ComposeView>(R.id.composeView)
        composeView?.setContent {
            CriptoTreckTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    HybridComposeContent()
                }
            }
        }
    }
}

@Composable
fun HybridComposeContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Approach 4: Hybrid (XML + ComposeView)")
    }
}

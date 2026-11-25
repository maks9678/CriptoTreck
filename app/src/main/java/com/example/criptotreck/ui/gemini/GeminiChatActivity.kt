package com.example.criptotreck.ui.gemini

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.criptotreck.ui.theme.CriptoTreckTheme

class GeminiChatActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CriptoTreckTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    GeminiScreen(showWelcome = true)
                }
            }
        }
    }

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, GeminiChatActivity::class.java))
        }
    }
}
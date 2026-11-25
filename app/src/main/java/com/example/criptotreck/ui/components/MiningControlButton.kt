package com.example.criptotreck.ui.components

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.criptotreck.data.service.MiningService

@Composable
fun MiningControlButton(coinTicker: String) {
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startMiningService(context, coinTicker)
        }
    }

    Button(
        onClick = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                startMiningService(context, coinTicker)
            }
        }
    ) {
        Text("Запустить фоновый майнинг")
    }
}

private fun startMiningService(context: Context, coinName: String) {
    val intent = Intent(context, MiningService::class.java).apply {
        action = MiningService.ACTION_START
        putExtra(MiningService.EXTRA_COIN_NAME, coinName)
    }
    ContextCompat.startForegroundService(context, intent)
}

package com.example.criptotreck.data.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow

class PriceTrackerService : Service() {
    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var priceJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null
}

object PriceTrackerState {
    val isTracking = MutableStateFlow(false)
    val isPaused = MutableStateFlow(false)
    val livePrice = MutableStateFlow<Double?>(null)
}

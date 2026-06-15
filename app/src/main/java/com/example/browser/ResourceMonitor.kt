package com.example.browser

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

data class ResourceState(
    val ramUsedPercent: Float,
    val temperatureCelsius: Float,
    val isLowPowerModeRecommended: Boolean
)

class ResourceMonitor(private val context: Context) {
    private val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

    fun getResourceFlow(): Flow<ResourceState> = flow {
        while (true) {
            val memoryInfo = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(memoryInfo)
            val totalRam = memoryInfo.totalMem.toFloat()
            val availRam = memoryInfo.availMem.toFloat()
            val usedRam = totalRam - availRam
            val ramUsedPercent = if (totalRam > 0) (usedRam / totalRam) * 100f else 0f

            val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            val tempTenths = intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
            val tempCelsius = tempTenths / 10f

            val isLowPower = ramUsedPercent >= 90f || tempCelsius >= 45f

            emit(ResourceState(ramUsedPercent, tempCelsius, isLowPower))
            delay(5000)
        }
    }
}

package com.example.browser

import android.app.ActivityManager
import android.content.Context

data class HardwareProfile(
    val totalRamGb: Double,
    val availableRamGb: Double,
    val recommendedModel: AIModelConfig,
    val supportedModels: List<AIModelConfig>
)

class AIModelScanner(private val context: Context) {
    fun scanDevice(): HardwareProfile {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)

        val totalRamGb = memoryInfo.totalMem / (1024.0 * 1024.0 * 1024.0)
        val availableRamGb = memoryInfo.availMem / (1024.0 * 1024.0 * 1024.0)

        val tinyLlama = AIModelConfig(
            "TinyLlama-1.1B", 
            "Lightweight model for limited RAM.", 
            "600 MB", 
            "tinyllama_1b.bin", 
            "https://huggingface.co/dummy/tinyllama/resolve/main/tinyllama.bin"
        )
        val gemma2b = AIModelConfig(
            "Gemma-2B", 
            "Balanced model for mid-range devices.", 
            "1.4 GB", 
            "gemma_2b.bin", 
            "https://huggingface.co/dummy/gemma/resolve/main/gemma_2b.bin"
        )
        val llama3 = AIModelConfig(
            "Llama-3-8B-Quantized", 
            "High performance model for powerful devices.", 
            "4.2 GB", 
            "llama3_8b.bin",
            "https://huggingface.co/dummy/llama3/resolve/main/llama3_8b.bin"
        )
        val int4Model = AIModelConfig(
            "Gemma-2B (INT4 Quantized)",
            "4-bit quantized model for fast NPU inference.",
            "800 MB",
            "gemma_2b_int4.bin",
            "https://huggingface.co/dummy/gemma/resolve/main/gemma_2b_int4.bin"
        )

        val supportedModels = listOf(tinyLlama, int4Model, gemma2b, llama3)
        val recommendedModel = if (totalRamGb > 8.0) {
            llama3
        } else if (totalRamGb > 4.0) {
            int4Model
        } else {
            tinyLlama
        }

        return HardwareProfile(totalRamGb, availableRamGb, recommendedModel, supportedModels)
    }
}

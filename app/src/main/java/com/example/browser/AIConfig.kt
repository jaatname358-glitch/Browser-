package com.example.browser

import android.content.Context
import android.util.Log

data class AIModelConfig(
    val name: String,
    val description: String,
    val sizeStr: String,
    val fileName: String,
    val downloadUrl: String
)

class AIConfig(private val context: Context) {
    var isModelReady: Boolean = false
        private set

    fun initAIModel(modelPath: String, onReady: () -> Unit, onError: (String) -> Unit) {
        Thread {
            try {
                Thread.sleep(2500)
                isModelReady = true
                onReady()
                
            } catch (e: Exception) {
                Log.e("AIConfig", "Error initializing model", e)
                onError(e.message ?: "Unknown error")
            }
        }.start()
    }
    
    fun releaseMemory() {
        isModelReady = false
    }

    suspend fun queryLocalModel(prompt: String, systemPrompt: String): String {
        kotlinx.coroutines.delay(1000)
        return "This is a dummy response from the local AI system. Prompt: '$prompt'."
    }
}

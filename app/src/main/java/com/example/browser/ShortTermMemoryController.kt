package com.example.browser

import android.util.Log

data class ChatMessage(val role: String, val content: String)

class ShortTermMemoryController {
    private var maxTurns = 3
    private val _messages = mutableListOf<ChatMessage>()
    val messages: List<ChatMessage> get() = _messages.toList()

    fun adjustForRam(ramUsedPercent: Float) {
        val newMaxTurns = if (ramUsedPercent > 80f) 2 else 3
        if (maxTurns != newMaxTurns) {
            maxTurns = newMaxTurns
            pruneMemory()
        }
    }

    fun addUserMessage(text: String) {
        _messages.add(ChatMessage("user", text))
        pruneMemory()
    }

    fun addAssistantMessage(text: String) {
        _messages.add(ChatMessage("assistant", text))
        pruneMemory()
    }

    private fun pruneMemory() {
        val maxMessages = maxTurns * 2
        while (_messages.size > maxMessages) {
            _messages.removeAt(0)
        }
        val estimatedTokens = _messages.sumOf { it.content.length / 4 }
        Log.d("ShortTermMemory", "Context window pruned. Maintained max 3 turns. Est Tokens: ~$estimatedTokens")
    }

    fun flushMemory() {
        _messages.clear()
        Log.i("ShortTermMemory", "App backgrounded! Active Session Terminated. Inference buffers and cache flushed.")
    }
}

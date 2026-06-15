package com.example.browser

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gemini.GeminiHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class BrowserViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(BrowserState())
    val uiState: StateFlow<BrowserState> = _uiState.asStateFlow()

    fun updateUrl(id: String, url: String) {
        val tabs = _uiState.value.tabs.map { if (it.id == id) it.copy(currentUrl = url) else it }
        _uiState.value = _uiState.value.copy(tabs = tabs)
    }

    fun setUrlInput(id: String, input: String) {
        val tabs = _uiState.value.tabs.map { if (it.id == id) it.copy(urlInput = input) else it }
        _uiState.value = _uiState.value.copy(tabs = tabs)
    }

    fun setLoading(id: String, isLoading: Boolean, progress: Int = 0) {
        val tabs = _uiState.value.tabs.map { if (it.id == id) it.copy(isLoading = isLoading, progress = progress) else it }
        _uiState.value = _uiState.value.copy(tabs = tabs)
    }

    fun updateNavState(id: String, canGoBack: Boolean, canGoForward: Boolean) {
        val tabs = _uiState.value.tabs.map { if (it.id == id) it.copy(canGoBack = canGoBack, canGoForward = canGoForward) else it }
        _uiState.value = _uiState.value.copy(tabs = tabs)
    }

    fun focusTab(id: String?) {
        _uiState.value = _uiState.value.copy(focusedTabId = id)
    }

    fun addNewTab() {
        if (_uiState.value.tabs.size < 4) {
            val newTab = TabState()
            val newTabs = _uiState.value.tabs + newTab
            _uiState.value = _uiState.value.copy(tabs = newTabs)
        }
    }
    
    fun closeTab(id: String) {
        val newTabs = _uiState.value.tabs.filter { it.id != id }
        if (newTabs.isEmpty()) {
            val newTab = TabState()
            _uiState.value = _uiState.value.copy(tabs = listOf(newTab), focusedTabId = null)
        } else {
            val nextFocusId = if (_uiState.value.focusedTabId == id) null else _uiState.value.focusedTabId
            _uiState.value = _uiState.value.copy(tabs = newTabs, focusedTabId = nextFocusId)
        }
    }

    fun toggleGemini() {
        _uiState.value = _uiState.value.copy(isGeminiOpen = !_uiState.value.isGeminiOpen)
    }

    fun resetGemini() {
         _uiState.value = _uiState.value.copy(geminiResponse = "")
    }

    fun askGemini(prompt: String, currentContextUrl: String) {
        _uiState.value = _uiState.value.copy(geminiResponse = "Thinking...", isGeminiLoading = true)
        viewModelScope.launch {
            val contextText = if (currentContextUrl.isNotEmpty() && currentContextUrl != "about:blank" && !currentContextUrl.startsWith("file://")) {
                "I am currently browsing looking at: $currentContextUrl\n"
            } else ""
            val fullPrompt = contextText + prompt
            _uiState.value = _uiState.value.copy(isGeminiLoading = false, geminiResponse = "")
            GeminiHelper.askGeminiStream(fullPrompt).collect { chunk ->
                _uiState.value = _uiState.value.copy(geminiResponse = chunk)
            }
        }
    }
}

data class TabState(
    val id: String = UUID.randomUUID().toString(),
    val currentUrl: String = "",
    val urlInput: String = "",
    val isLoading: Boolean = false,
    val progress: Int = 0,
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false
)

data class BrowserState(
    val tabs: List<TabState> = listOf(TabState()),
    val focusedTabId: String? = null,
    val isGeminiOpen: Boolean = false,
    val isGeminiLoading: Boolean = false,
    val geminiResponse: String = ""
)

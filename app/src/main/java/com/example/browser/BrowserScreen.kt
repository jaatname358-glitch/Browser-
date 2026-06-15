package com.example.browser

import android.graphics.Bitmap
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import android.annotation.SuppressLint

fun formatUrl(input: String): String {
    if (input.isBlank()) return "https://www.google.com"
    var url = input.trim()
    if (!url.startsWith("http://") && !url.startsWith("https://")) {
        url = if (url.contains('.') && !url.contains(' ')) {
            "https://$url"
        } else {
            "https://www.google.com/search?q=${url.replace(" ", "+")}"
        }
    }
    return url
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun BrowserScreen(
    viewModel: BrowserViewModel = viewModel(),
    onNavigateToSettings: () -> Unit,
    onNavigateToDownloads: () -> Unit,
    onNavigateToAIAssistant: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusedTabId = uiState.focusedTabId ?: uiState.tabs.firstOrNull()?.id
    val currentTab = uiState.tabs.find { it.id == focusedTabId }

    var showMenu by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F1115))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Custom Browser Top Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp, start = 8.dp, end = 8.dp)
                    .background(Color(0xFF151921), RoundedCornerShape(24.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        currentTab?.let { tab ->
                            var inputUrl by remember { mutableStateOf(tab.currentUrl) }
                            var isFocused by remember { mutableStateOf(false) }

                            LaunchedEffect(tab.currentUrl) {
                                if (!isFocused) inputUrl = tab.currentUrl
                            }

                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .background(Color(0xFF000000).copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                                    .padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Outlined.Lock, 
                                    contentDescription = "Secure", 
                                    tint = if (tab.currentUrl.startsWith("https")) Color(0xFF10A37F) else Color.White.copy(alpha = 0.5f), 
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                BasicTextField(
                                    value = inputUrl,
                                    onValueChange = { inputUrl = it },
                                    modifier = Modifier
                                        .weight(1f)
                                        .onFocusChanged { isFocused = it.isFocused },
                                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 14.sp),
                                    singleLine = true,
                                    cursorBrush = SolidColor(Color.White),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Go),
                                    keyboardActions = KeyboardActions(onGo = {
                                        viewModel.setUrlInput(tab.id, formatUrl(inputUrl))
                                    }),
                                    decorationBox = { innerTextField ->
                                        if (inputUrl.isEmpty() && !isFocused) {
                                            Text("Search or type URL", color = Color.Gray, fontSize = 14.sp)
                                        }
                                        innerTextField()
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                "${uiState.tabs.size}",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .border(2.dp, Color.White, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                    .clickable { /* Tab switcher */ }
                            )
                        }
                        IconButton(onClick = { showMenu = !showMenu }) {
                            Icon(Icons.Outlined.MoreVert, contentDescription = "Menu", tint = Color.White)
                        }
                    }

                    // Tab Row
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.tabs) { tab ->
                            val isSelected = tab.id == focusedTabId
                            Box(
                                modifier = Modifier
                                    .width(140.dp)
                                    .height(36.dp)
                                    .background(
                                        color = if (isSelected) Color(0xFF2A2D35) else Color.Transparent,
                                        shape = RoundedCornerShape(18.dp)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) Color.White.copy(alpha = 0.1f) else Color.Transparent,
                                        shape = RoundedCornerShape(18.dp)
                                    )
                                    .clickable { viewModel.focusTab(tab.id) }
                                    .padding(horizontal = 12.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(16.dp).background(Color.White, CircleShape))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (tab.currentUrl.isEmpty()) "New Tab" else tab.currentUrl,
                                        color = if (isSelected) Color.White else Color.White.copy(alpha = 0.5f),
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (uiState.tabs.size > 1) {
                                        Icon(
                                            Icons.Outlined.Close,
                                            contentDescription = "Close",
                                            tint = Color.White.copy(alpha = 0.5f),
                                            modifier = Modifier.size(14.dp).clickable { viewModel.closeTab(tab.id) }
                                        )
                                    }
                                }
                            }
                        }
                        item {
                            IconButton(
                                onClick = { viewModel.addNewTab() },
                                modifier = Modifier.size(36.dp).background(Color(0xFF2A2D35), CircleShape)
                            ) {
                                Icon(Icons.Outlined.Add, contentDescription = "Add Tab", tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(Color(0xFF151921))
                ) {
                    DropdownMenuItem(
                        text = { Text("Local AI Model", color = Color.White) },
                        leadingIcon = { Icon(Icons.Outlined.Memory, contentDescription = null, tint = Color(0xFF4EEBFF)) },
                        onClick = { showMenu = false; onNavigateToAIAssistant() }
                    )
                    DropdownMenuItem(
                        text = { Text("Settings", color = Color.White) },
                        leadingIcon = { Icon(Icons.Outlined.Settings, contentDescription = null, tint = Color.White) },
                        onClick = { showMenu = false; onNavigateToSettings() }
                    )
                    DropdownMenuItem(
                        text = { Text("Downloads", color = Color.White) },
                        leadingIcon = { Icon(Icons.Outlined.Download, contentDescription = null, tint = Color.White) },
                        onClick = { showMenu = false; onNavigateToDownloads() }
                    )
                }
            }

            // WebView Content Area
            currentTab?.let { tab ->
                if (tab.isLoading) {
                    LinearProgressIndicator(
                        progress = { tab.progress / 100f },
                        modifier = Modifier.fillMaxWidth().height(2.dp),
                        color = Color(0xFF4EEBFF),
                        trackColor = Color.Transparent
                    )
                } else {
                    Spacer(modifier = Modifier.height(2.dp))
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    if (tab.currentUrl.isEmpty() && tab.urlInput.isEmpty()) {
                        NewTabPage(onSearch = { viewModel.setUrlInput(tab.id, formatUrl(it)) })
                    } else {
                        var webView by remember { mutableStateOf<WebView?>(null) }
                        
                        AndroidView(
                            factory = { context ->
                                WebView(context).apply {
                                    settings.javaScriptEnabled = true
                                    settings.domStorageEnabled = true
                                    settings.useWideViewPort = true
                                    settings.loadWithOverviewMode = true
                                    
                                    webViewClient = object : WebViewClient() {
                                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                            super.onPageStarted(view, url, favicon)
                                            if (url != null) viewModel.updateUrl(tab.id, url)
                                            viewModel.setLoading(tab.id, true, 0)
                                        }

                                        override fun onPageFinished(view: WebView?, url: String?) {
                                            super.onPageFinished(view, url)
                                            if (url != null) viewModel.updateUrl(tab.id, url)
                                            viewModel.setLoading(tab.id, false, 100)
                                            viewModel.updateNavState(tab.id, view?.canGoBack() ?: false, view?.canGoForward() ?: false)
                                        }
                                    }
                                    
                                    webChromeClient = object : WebChromeClient() {
                                        override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                            super.onProgressChanged(view, newProgress)
                                            viewModel.setLoading(tab.id, true, newProgress)
                                        }
                                    }
                                    webView = this
                                }
                            },
                            update = { view ->
                                if (tab.urlInput.isNotEmpty() && tab.urlInput != view.url) {
                                    view.loadUrl(tab.urlInput)
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
    
    // Gemini Bottom Sheet Area
    if (uiState.isGeminiOpen) {
        val currentContextUrl = currentTab?.currentUrl ?: ""
        GeminiBottomSheet(
            onDismiss = { viewModel.toggleGemini() },
            isGeminiLoading = uiState.isGeminiLoading,
            geminiResponse = uiState.geminiResponse,
            onAsk = { prompt -> viewModel.askGemini(prompt, currentContextUrl) },
            currentUrl = currentContextUrl
        )
    }
}

@Composable
fun NewTabPage(onSearch: (String) -> Unit) {
    var searchInput by remember { mutableStateOf("") }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Animated3DBackground()
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                "NEXY",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(32.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(28.dp))
                    .padding(horizontal = 24.dp)
                    .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(28.dp))
            ) {
                Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Search, contentDescription = "Search", tint = Color.Gray)
                    Spacer(modifier = Modifier.width(16.dp))
                    BasicTextField(
                        value = searchInput,
                        onValueChange = { searchInput = it },
                        modifier = Modifier.weight(1f),
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 16.sp),
                        singleLine = true,
                        cursorBrush = SolidColor(Color.White),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { onSearch(searchInput) }),
                        decorationBox = { innerTextField ->
                            if (searchInput.isEmpty()) {
                                Text("Search or enter address", color = Color.Gray, fontSize = 16.sp)
                            }
                            innerTextField()
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(48.dp))
            
            // Grid System
            val shortcuts = listOf(
                Pair("Google", Icons.Outlined.Language),
                Pair("Wikipedia", Icons.Outlined.Article),
                Pair("X (Twitter)", Icons.Outlined.AlternateEmail),
                Pair("YouTube", Icons.Outlined.SmartDisplay)
            )
            val utilities = listOf(
                Pair("Doc Scanner", Icons.Outlined.DocumentScanner),
                Pair("QR Scan", Icons.Outlined.QrCodeScanner),
                Pair("Translate", Icons.Outlined.Translate),
                Pair("Privacy Mode", Icons.Outlined.Security)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                shortcuts.forEach { (name, icon) ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable {
                        onSearch(name)
                    }) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(icon, contentDescription = name, tint = Color.White, modifier = Modifier.size(28.dp))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(name, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                utilities.forEach { (name, icon) ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable {
                        // TODO: trigger specific utility
                    }) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(Color(0xFF4EEBFF).copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                                .border(1.dp, Color(0xFF4EEBFF).copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(icon, contentDescription = name, tint = Color(0xFF4EEBFF), modifier = Modifier.size(28.dp))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(name, color = Color(0xFF4EEBFF).copy(alpha = 0.8f), fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeminiBottomSheet(
    onDismiss: () -> Unit,
    isGeminiLoading: Boolean,
    geminiResponse: String,
    onAsk: (String) -> Unit,
    currentUrl: String
) {
    var prompt by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E1E24),
        scrimColor = Color.Black.copy(alpha = 0.6f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.AutoAwesome, contentDescription = "Gemini", tint = Color(0xFFA67CFF))
                Spacer(modifier = Modifier.width(12.dp))
                Text("Gemini Assistant", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            if (currentUrl.isNotEmpty()) {
                Text(
                    text = "Reading context from: ${if(currentUrl.length > 40) currentUrl.take(40) + "..." else currentUrl}",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            if (geminiResponse.isNotEmpty() || isGeminiLoading) {
                Box(modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp, max = 300.dp).background(Color(0xFF2A2D35), RoundedCornerShape(12.dp)).padding(16.dp)) {
                    if (isGeminiLoading) {
                        CircularProgressIndicator(color = Color(0xFFA67CFF), modifier = Modifier.align(Alignment.Center))
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            item {
                                Text(geminiResponse, color = Color.White, fontSize = 14.sp)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = prompt,
                    onValueChange = { prompt = it },
                    placeholder = { Text("Ask anything about this page...", color = Color.White.copy(alpha = 0.5f)) },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFA67CFF),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(24.dp),
                    maxLines = 3
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = { 
                        if (prompt.isNotBlank()) {
                            onAsk(prompt)
                            prompt = ""
                        }
                    },
                    modifier = Modifier.size(48.dp).background(Color(0xFFA67CFF), CircleShape)
                ) {
                    Icon(Icons.Outlined.Send, contentDescription = "Send", tint = Color.White)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

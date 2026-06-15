package com.example.browser

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CloseFullscreen
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import androidx.work.WorkManager
import androidx.work.WorkInfo
import java.util.UUID
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material.icons.outlined.Thermostat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIAssistantScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scanner = remember { AIModelScanner(context) }
    val config = remember { AIConfig(context) }
    
    var hardwareProfile by remember { mutableStateOf<HardwareProfile?>(null) }
    var isInstalling by remember { mutableStateOf(false) }
    var isInitializing by remember { mutableStateOf(false) }
    var isInstalled by remember { mutableStateOf(false) }
    
    var downloadWorkId by remember { mutableStateOf<UUID?>(null) }
    var installationState by remember { mutableStateOf("") }
    var installationProgress by remember { mutableFloatStateOf(0f) }
    
    var chatMessage by remember { mutableStateOf("") }
    
    val resourceMonitor = remember { ResourceMonitor(context) }
    val resourceState by resourceMonitor.getResourceFlow().collectAsStateWithLifecycle(initialValue = ResourceState(0f, 0f, false))
    val memoryController = remember { ShortTermMemoryController() }
    
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(resourceState.ramUsedPercent) {
        memoryController.adjustForRam(resourceState.ramUsedPercent)
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP) {
                memoryController.flushMemory()
                config.releaseMemory() 
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            config.releaseMemory()
        }
    }

    LaunchedEffect(Unit) {
        delay(1500) 
        hardwareProfile = scanner.scanDevice()
    }

    if (downloadWorkId != null) {
        val workInfo by WorkManager.getInstance(context)
            .getWorkInfoByIdLiveData(downloadWorkId!!)
            .run {
                var state by mutableStateOf<WorkInfo?>(null)
                observeForever { state = it }
                derivedStateOf { state }
            }
            
        LaunchedEffect(workInfo) {
            workInfo?.let { info ->
                val progressData = info.progress
                val progress = progressData.getFloat("PROGRESS", 0f)
                val state = progressData.getString("STATE") ?: "STARTING"
                
                installationProgress = progress
                installationState = state
                
                if (info.state == WorkInfo.State.SUCCEEDED) {
                    val filePath = info.outputData.getString("FILE_PATH") ?: "dummy_path"
                    isInstalling = false
                    isInitializing = true
                    
                    config.initAIModel(filePath, onReady = {
                        isInitializing = false
                        isInstalled = true
                    }, onError = {
                        isInitializing = false
                        installationState = "Error: $it"
                    })
                    
                    downloadWorkId = null
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF03081A))
    ) {
        Animated3DBackground()

        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp, bottom = 8.dp, start = 16.dp, end = 16.dp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(Color.White.copy(alpha = 0.15f), Color.White.copy(alpha = 0.05f))
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Local AI Assistant",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val tempColor = if (resourceState.temperatureCelsius > 45f) Color.Red else Color.Green
                            val ramColor = if (resourceState.ramUsedPercent > 90f) Color.Red else Color.Green
                            Icon(Icons.Outlined.Thermostat, contentDescription = "Temp", tint = tempColor, modifier = Modifier.size(12.dp))
                            Text(" ${resourceState.temperatureCelsius}ºC", color = tempColor, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Outlined.Memory, contentDescription = "RAM", tint = ramColor, modifier = Modifier.size(12.dp))
                            Text(" ${String.format("%.1f", resourceState.ramUsedPercent)}% used", color = ramColor, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                    IconButton(onClick = { 
                        FloatingAIState.isVisible = true
                        onBack()
                    }, modifier = Modifier.size(32.dp)) {
                        Icon(androidx.compose.material.icons.Icons.Outlined.CloseFullscreen, contentDescription = "Floating Overlay", tint = Color.White)
                    }
                }
            }
            
            if (resourceState.isLowPowerModeRecommended) {
                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).background(Color(0xFFFF3B30).copy(alpha = 0.2f), RoundedCornerShape(12.dp)).padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Warning, contentDescription = "Warning", tint = Color(0xFFFF453A))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Low-Power Mode trigger: High thermal or memory threshold reached. Limiting inference speed.", color = Color(0xFFFF453A), fontSize = 12.sp, lineHeight = 16.sp)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                if (!isInstalled) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(8.dp, RoundedCornerShape(20.dp))
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(Color.White.copy(alpha = 0.1f), Color.White.copy(alpha = 0.02f))
                                ),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .padding(16.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.Memory, contentDescription = "Hardware", tint = Color(0xFF4EEBFF))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Device Hardware Profile", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            if (hardwareProfile == null) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(color = Color(0xFF4EEBFF), modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Scanning device capabilities...", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                                }
                            } else {
                                val prof = hardwareProfile!!
                                Text("Total RAM: ${String.format("%.1f", prof.totalRamGb)} GB", color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp)
                                Text("Available RAM: ${String.format("%.1f", prof.availableRamGb)} GB", color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp)
                                Text("NPU: Supported", color = Color(0xFF10A37F), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }

                if (hardwareProfile != null && !isInstalling && !isInitializing && !isInstalled) {
                    Text("Recommended Models", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(bottom = 12.dp))
                    
                    hardwareProfile!!.supportedModels.forEach { model ->
                        val isRecommended = model == hardwareProfile!!.recommendedModel
                        ModelCard(
                            model.name + (if(isRecommended) " (Recommended)" else ""), 
                            model.description, 
                            model.sizeStr, 
                            enabled = true
                        ) {
                            isInstalling = true
                            downloadWorkId = LocalAIDownloader.startDownload(context, model)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                if (isInstalling || isInitializing) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(8.dp, RoundedCornerShape(20.dp))
                            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                            .padding(16.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 12.dp)) {
                                Text(if(isInitializing) "Initializing Model" else "Downloading Engine", color = Color(0xFF10A37F), fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.weight(1f))
                                Text(if(isInitializing) "..." else "${(installationProgress * 100).toInt()}%", color = Color.White, fontFamily = FontFamily.Monospace)
                            }
                            if (!isInitializing) {
                                LinearProgressIndicator(
                                    progress = { installationProgress.coerceIn(0f, 1f) },
                                    modifier = Modifier.fillMaxWidth().height(4.dp),
                                    color = Color(0xFF10A37F),
                                    trackColor = Color.White.copy(alpha = 0.1f)
                                )
                            } else {
                                LinearProgressIndicator(
                                    modifier = Modifier.fillMaxWidth().height(4.dp),
                                    color = Color(0xFF10A37F),
                                    trackColor = Color.White.copy(alpha = 0.1f)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "user@nexy:~$ " + (if(isInitializing) "allocating tensors and moving to memory..." else installationState),
                                color = Color.White.copy(alpha = 0.8f),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                if (isInstalled) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        LazyColumn(modifier = Modifier.weight(1f)) {
                            item {
                                Spacer(modifier = Modifier.height(20.dp))
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                                    Icon(Icons.Outlined.CheckCircle, contentDescription = "Success", tint = Color(0xFF10A37F), modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Model loaded in memory", color = Color(0xFF10A37F), fontSize = 14.sp)
                                }
                                Spacer(modifier = Modifier.height(20.dp))
                            }
                        }
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(8.dp, RoundedCornerShape(24.dp))
                                .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                OutlinedTextField(
                                    value = chatMessage,
                                    onValueChange = { chatMessage = it },
                                    placeholder = { Text("Ask local AI...", color = Color.White.copy(alpha = 0.5f)) },
                                    modifier = Modifier.weight(1f),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color.Transparent,
                                        unfocusedBorderColor = Color.Transparent,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    )
                                )
                                IconButton(onClick = { /* Handle chat */ }) {
                                    Icon(Icons.Outlined.Send, contentDescription = "Send", tint = Color(0xFF4EEBFF))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ModelCard(title: String, desc: String, size: String, enabled: Boolean = true, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(Color.White.copy(alpha = if (enabled) 0.1f else 0.05f), Color.White.copy(alpha = 0.02f))
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(enabled = enabled) { onClick() }
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = if (enabled) Color.White else Color.White.copy(alpha = 0.5f), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(desc, color = if (enabled) Color.White.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.4f), fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Outlined.Download, contentDescription = "Download", tint = if (enabled) Color(0xFF4EEBFF) else Color.Gray)
                Text(size, color = if (enabled) Color.White else Color.Gray, fontSize = 10.sp)
            }
        }
    }
}

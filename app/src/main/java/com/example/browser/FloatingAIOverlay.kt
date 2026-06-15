package com.example.browser

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectDragGestures
import kotlin.math.roundToInt

object FloatingAIState {
    var isVisible by mutableStateOf(false)
}

@Composable
fun FloatingAIOverlay() {
    if (!FloatingAIState.isVisible) return

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val config = remember { AIConfig(context) }
    val memoryController = remember { ShortTermMemoryController() }
    
    var chatMessage by remember { mutableStateOf("") }
    var offsetX by remember { mutableFloatStateOf(50f) }
    var offsetY by remember { mutableFloatStateOf(100f) }

    Box(
        modifier = Modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .width(320.dp)
            .height(450.dp)
            .shadow(elevation = 24.dp, shape = RoundedCornerShape(16.dp))
            .background(Color(0xFF151921), RoundedCornerShape(16.dp))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF2A2D35))
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            offsetX += dragAmount.x
                            offsetY += dragAmount.y
                        }
                    }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Local AI Assistant",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { FloatingAIState.isVisible = false }, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Outlined.Close, contentDescription = "Minimize", tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
            
            Box(modifier = Modifier.weight(1f).padding(8.dp)) {
                LazyColumn(reverseLayout = true, modifier = Modifier.fillMaxSize()) {
                    items(memoryController.messages.reversed()) { msg ->
                        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), contentAlignment = if (msg.role == "user") Alignment.CenterEnd else Alignment.CenterStart) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = if (msg.role == "user") Color(0xFF4285F4) else Color(0xFF2A2D35),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .padding(12.dp)
                                    .widthIn(max = 240.dp)
                            ) {
                                Text(text = msg.content, color = Color.White, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .background(Color(0xFF0F1115))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    value = chatMessage,
                    onValueChange = { chatMessage = it },
                    modifier = Modifier
                        .weight(1f)
                        .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 14.sp),
                    cursorBrush = SolidColor(Color.White),
                    decorationBox = { innerTextField ->
                        Box(contentAlignment = Alignment.CenterStart) {
                            if (chatMessage.isEmpty()) {
                                Text("Ask...", color = Color.Gray, fontSize = 14.sp)
                            }
                            innerTextField()
                        }
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFF4285F4), CircleShape)
                        .clickable {
                            if (chatMessage.isNotBlank()) {
                                val query = chatMessage
                                chatMessage = ""
                                memoryController.addUserMessage(query)
                                coroutineScope.launch {
                                    val response = config.queryLocalModel(query, "system_prompt_here")
                                    memoryController.addAssistantMessage(response)
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.AutoMirrored.Outlined.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

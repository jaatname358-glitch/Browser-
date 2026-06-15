package com.example.browser

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.InsertDriveFile
import androidx.compose.material.icons.outlined.VideoFile
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.browser.Animated3DBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadsScreen(onBack: () -> Unit) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Current Page Media", "Download History")

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
                    Text(
                        text = "Downloads",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.Transparent,
                contentColor = Color.White,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = Color(0xFF4EEBFF),
                        height = 3.dp
                    )
                },
                divider = {
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                fontSize = 14.sp,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedTabIndex == index) Color(0xFF4EEBFF) else Color.White.copy(alpha = 0.6f)
                            )
                        }
                    )
                }
            }

            if (selectedTabIndex == 0) {
                CurrentPageMediaTab()
            } else {
                DownloadHistoryTab()
            }
        }
    }
}

@Composable
fun CurrentPageMediaTab() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Detected Media", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Button(
                onClick = { /* Download All */ },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C6FF), contentColor = Color.Black)
            ) {
                Text("Download All", fontWeight = FontWeight.Bold)
            }
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(3) { index ->
                MediaItem(
                    icon = Icons.Outlined.VideoFile,
                    name = "video_clip_0${index + 1}.mp4",
                    details = "1080p • 12MB"
                )
            }
            items(2) { index ->
                MediaItem(
                    icon = Icons.Outlined.Image,
                    name = "image_hires_0${index + 1}.png",
                    details = "4K • 3MB"
                )
            }
        }
    }
}

@Composable
fun DownloadHistoryTab() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(5) { index ->
                MediaItem(
                    icon = Icons.Outlined.InsertDriveFile,
                    name = "downloaded_document_0${index + 1}.pdf",
                    details = "Completed • 2.4MB • Today"
                )
            }
        }
    }
}

@Composable
fun MediaItem(icon: ImageVector, name: String, details: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(Color.White.copy(alpha = 0.1f), Color.White.copy(alpha = 0.05f))
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = name, tint = Color(0xFF4EEBFF), modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = name, color = Color.White, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                Text(text = details, color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
            }
            Button(
                onClick = { },
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f))
            ) {
                Text("Action", color = Color.White, fontSize = 12.sp)
            }
        }
    }
}

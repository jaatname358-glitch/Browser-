package com.example.browser

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

@Composable
fun Animated3DBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val floatOffset1 by infiniteTransition.animateFloat(
        initialValue = -80f, targetValue = 80f,
        animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing), RepeatMode.Reverse),
        label = ""
    )
    val floatOffset2 by infiniteTransition.animateFloat(
        initialValue = 80f, targetValue = -80f,
        animationSpec = infiniteRepeatable(tween(5000, easing = LinearEasing), RepeatMode.Reverse),
        label = ""
    )
    val floatRotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(15000, easing = LinearEasing)),
        label = ""
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF03081A)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .offset(x = floatOffset1.dp, y = floatOffset2.dp)
                .size(400.dp)
                .graphicsLayer {
                    translationX = -300f
                    translationY = -400f
                    alpha = 0.5f
                    rotationZ = floatRotation
                    rotationX = 20f
                    rotationY = 15f
                }
                .background(Color(0xFF00C6FF), CircleShape)
                .blur(120.dp)
        )
        Box(
            modifier = Modifier
                .offset(x = floatOffset2.dp, y = floatOffset1.dp)
                .size(500.dp)
                .graphicsLayer {
                    translationX = 300f
                    translationY = 500f
                    alpha = 0.4f
                    rotationZ = -floatRotation
                    rotationX = -10f
                    rotationY = -20f
                }
                .background(Color(0xFF8922E8), CircleShape)
                .blur(140.dp)
        )
        Box(
            modifier = Modifier
                .offset(x = floatOffset1.dp * 0.5f, y = floatOffset1.dp * 0.5f)
                .size(350.dp)
                .graphicsLayer {
                    translationX = 100f
                    translationY = -100f
                    alpha = 0.25f
                    rotationZ = floatRotation * 1.5f
                }
                .background(Color(0xFFE1306C), CircleShape)
                .blur(100.dp)
        )
    }
}

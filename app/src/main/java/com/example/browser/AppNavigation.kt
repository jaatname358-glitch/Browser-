package com.example.browser

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = "splash",
            modifier = Modifier.fillMaxSize().background(Color(0xFF0F1115)),
            enterTransition = { androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(500)) + androidx.compose.animation.scaleIn(initialScale = 0.9f, animationSpec = androidx.compose.animation.core.tween(500)) },
            exitTransition = { androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(500)) + androidx.compose.animation.scaleOut(targetScale = 1.1f, animationSpec = androidx.compose.animation.core.tween(500)) }
        ) {
            composable("splash") {
                SplashScreen(
                    onNavigateToLogin = {
                        navController.navigate("login") {
                            popUpTo("splash") { inclusive = true }
                        }
                    },
                    onNavigateToHome = {
                        navController.navigate("home") {
                            popUpTo("splash") { inclusive = true }
                        }
                    }
                )
            }
            composable("login") {
                LoginScreen(
                    onNavigateToHome = {
                        navController.navigate("home") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                )
            }
            composable("home") {
                BrowserScreen(
                    onNavigateToSettings = { navController.navigate("settings") },
                    onNavigateToDownloads = { navController.navigate("downloads") },
                    onNavigateToAIAssistant = { navController.navigate("ai_assistant") }
                )
            }
            composable("settings") {
                SettingsScreen(onBack = { navController.popBackStack() })
            }
            composable("downloads") {
                DownloadsScreen(onBack = { navController.popBackStack() })
            }
            composable("ai_assistant") {
                AIAssistantScreen(onBack = { navController.popBackStack() })
            }
        }
        
        // Render Floating Overlay on top of everything
        FloatingAIOverlay()
    }
}

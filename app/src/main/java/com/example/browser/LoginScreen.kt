package com.example.browser

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

fun Modifier.glassmorphism(radius: Float = 20f, alpha: Float = 0.1f): Modifier = this
    .clip(RoundedCornerShape(radius.dp))
    .background(Color.White.copy(alpha = alpha))
    .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(radius.dp))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(onNavigateToHome: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val auth = remember { FirebaseAuth.getInstance() }
    var showTOSBottomSheet by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF03081A)),
        contentAlignment = Alignment.Center
    ) {
        Animated3DBackground()

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .glassmorphism(alpha = 0.05f)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(text = "NEXY", fontSize = 42.sp, color = Color.White, letterSpacing = 4.sp, fontWeight = FontWeight.Bold)
            Text(text = "BROWSER", fontSize = 28.sp, color = Color(0xFF4EEBFF), letterSpacing = 8.sp, fontWeight = FontWeight.Light)
            Spacer(modifier = Modifier.height(64.dp))
            Text(text = "Sign in to continue", fontSize = 18.sp, color = Color.White.copy(alpha = 0.8f), fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(32.dp))

            Surface(
                onClick = {
                    coroutineScope.launch {
                        isLoading = true
                        try {
                            val masterKey = MasterKey.Builder(context)
                                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                                .build()
                            val sharedPreferences = EncryptedSharedPreferences.create(
                                context,
                                "auth_prefs",
                                masterKey,
                                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                            )
                            with(sharedPreferences.edit()) {
                                putBoolean("is_authenticated", true)
                                apply()
                            }
                            onNavigateToHome()
                        } catch (e: Exception) {
                            auth.signInAnonymously().addOnCompleteListener {
                                isLoading = false
                                onNavigateToHome()
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp).glassmorphism(alpha = 0.1f),
                color = Color.Transparent
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("G", color = Color(0xFF4EEBFF), fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(16.dp))
                            Text("Continue with Google", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "By continuing you agree to our",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Terms of Service and Privacy Policy",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 12.sp,
                textDecoration = TextDecoration.Underline,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(bottom = 32.dp, top = 4.dp)
                    .clickable { showTOSBottomSheet = true }
            )
        }
    }

    if (showTOSBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showTOSBottomSheet = false },
            sheetState = sheetState,
            containerColor = Color(0xFF151921),
            scrimColor = Color.Black.copy(alpha = 0.6f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Terms of Service & Privacy Policy",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Welcome to NEXY Browser. By using our app, you agree to these terms.",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = { showTOSBottomSheet = false },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C6FF))
                ) {
                    Text("I Understand", color = Color.Black, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

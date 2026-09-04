package com.example.ui

import com.example.ui.theme.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    playerViewModel: PlayerViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToAuth: () -> Unit
) {
    val isLoggedIn by playerViewModel.isLoggedIn.collectAsState()
    val currentUser by playerViewModel.currentUser.collectAsState()
    val scope = rememberCoroutineScope()
    var isUploading by remember { mutableStateOf(false) }
    
    val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            isUploading = true
            scope.launch {
                playerViewModel.authManager.uploadAvatar(uri)
                playerViewModel.refreshUser()
                isUploading = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWavve)
            .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = PrimaryText)
            }
            Text("Settings", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = PrimaryText)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

        // Profile Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(DividerColor)
                    .clickable(enabled = isLoggedIn && !isUploading) {
                        try {
                            launcher.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (isLoggedIn) {
                    val avatarUrl = currentUser?.photoUrl?.toString() ?: "https://api.dicebear.com/7.x/avataaars/png?seed=${currentUser?.uid ?: "default"}"
                    AsyncImage(
                        model = avatarUrl,
                        contentDescription = "Profile",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                    if (isUploading) {
                        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = AccentColor, modifier = Modifier.size(24.dp))
                        }
                    }
                } else {
                    Icon(Icons.Filled.Person, contentDescription = null, modifier = Modifier.fillMaxSize().padding(24.dp), tint = SecondaryText)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            if (isLoggedIn) {
                val fallbackName = if (!currentUser?.displayName.isNullOrBlank()) currentUser?.displayName else if (!currentUser?.email.isNullOrBlank()) currentUser?.email else "User"
                Text(fallbackName ?: "User", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = PrimaryText)
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { playerViewModel.logout() },
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceWavve, contentColor = AccentColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Log Out")
                }
            } else {
                Text("Not Logged In", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = PrimaryText)
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onNavigateToAuth,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentColor, contentColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Log In / Sign Up")
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        
        // Settings Options
        Text("APP SETTINGS", modifier = Modifier.padding(horizontal = 24.dp), color = SecondaryText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        
        val currentAudioQuality by playerViewModel.currentAudioQuality.collectAsState()
        var showAudioDialog by remember { mutableStateOf(false) }
        SettingsRow("Audio Quality", currentAudioQuality) { showAudioDialog = true }
        if (showAudioDialog) {
            SettingsDialog("Audio Quality", listOf("Low (96kbps)", "Medium (160kbps)", "High (320kbps)"), currentAudioQuality, { showAudioDialog = false }) { 
                playerViewModel.settingsRepository.setAudioQuality(it) 
            }
        }

        val currentTheme by playerViewModel.currentTheme.collectAsState()
        var showThemeDialog by remember { mutableStateOf(false) }
        SettingsRow("Theme", currentTheme) { showThemeDialog = true }
        if (showThemeDialog) {
            SettingsDialog("Theme", listOf("System", "Light", "Dark"), currentTheme, { showThemeDialog = false }) {
                playerViewModel.settingsRepository.setTheme(it)
            }
        }

        val currentDownloads by playerViewModel.currentDownloads.collectAsState()
        var showDownloadsDialog by remember { mutableStateOf(false) }
        SettingsRow("Downloads", currentDownloads) { showDownloadsDialog = true }
        if (showDownloadsDialog) {
            SettingsDialog("Downloads", listOf("Wi-Fi Only", "Over Cellular"), currentDownloads, { showDownloadsDialog = false }) {
                playerViewModel.settingsRepository.setDownloads(it)
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Text("ABOUT", modifier = Modifier.padding(horizontal = 24.dp), color = SecondaryText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        SettingsRow("Version", "1.0.0") {}
        SettingsRow("Developer", "Utsav Vasava") {}
        
        var showPrivacyDialog by remember { mutableStateOf(false) }
        SettingsRow("Privacy Policy", "") { showPrivacyDialog = true }
        
        if (showPrivacyDialog) {
            AlertDialog(
                onDismissRequest = { showPrivacyDialog = false },
                title = { Text("Privacy Policy", style = MaterialTheme.typography.titleLarge) },
                text = {
                    Text(
                        "Wavve respects your privacy. We collect minimal data required to provide our music streaming services.\n\n" +
                        "• Account Data: Your email and profile picture are securely managed via Google Firebase.\n" +
                        "• Library Data: Your playlists, saved albums, and followed artists are synced to Firebase Firestore so you can access them across devices.\n" +
                        "• Analytics: We do not track your listening habits for third-party advertising.\n\n" +
                        "By using Wavve, you agree to the collection and use of information in accordance with this policy.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SecondaryText
                    )
                },
                confirmButton = {
                    androidx.compose.material3.TextButton(onClick = { showPrivacyDialog = false }) {
                        Text("Close", color = AccentColor)
                    }
                }
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SettingsDialog(title: String, options: List<String>, selectedOption: String, onDismiss: () -> Unit, onSelect: (String) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, style = MaterialTheme.typography.titleLarge) },
        text = {
            Column {
                options.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { 
                                onSelect(option)
                                onDismiss()
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = option == selectedOption,
                            onClick = null,
                            colors = RadioButtonDefaults.colors(selectedColor = AccentColor)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(option, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = AccentColor) }
        },
        containerColor = SurfaceWavve,
        titleContentColor = PrimaryText,
        textContentColor = PrimaryText
    )
}

@Composable
fun SettingsRow(title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.bodyLarge, color = PrimaryText)
        if (subtitle.isNotEmpty()) {
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = SecondaryText)
        }
    }
}

@Composable
fun AuthScreen(
    playerViewModel: PlayerViewModel,
    onNavigateBack: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf("") }
    var isSignUpMode by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    

    
    val activity = androidx.compose.ui.platform.LocalContext.current as? android.app.Activity

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWavve)
            .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = PrimaryText)
            }
        }
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {

                Text("Welcome to Wavve.", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold, color = PrimaryText)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Log in or create an account to save playlists and follow your favorite artists.", color = SecondaryText)
            
            Spacer(modifier = Modifier.height(32.dp))

            // Social Logins
            val scope = rememberCoroutineScope()
            OutlinedButton(
                onClick = {
                    scope.launch {
                        activity?.let {
                            val success = playerViewModel.authManager.signInWithGoogle(it)
                            if (success) {
                                onNavigateBack()
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryText)
            ) {
                Icon(
                    painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_google),
                    contentDescription = "Google",
                    modifier = Modifier.size(20.dp),
                    tint = Color.Unspecified
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Continue with Google", fontWeight = FontWeight.SemiBold)
            }
            Spacer(modifier = Modifier.height(32.dp))
            
            // Divider
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.weight(1f).height(1.dp).background(DividerColor))
                Text(" OR ", color = SecondaryText, modifier = Modifier.padding(horizontal = 8.dp))
                Box(modifier = Modifier.weight(1f).height(1.dp).background(DividerColor))
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Email Login
            if (emailError.isNotEmpty()) {
                Text(emailError, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))
            }

            OutlinedTextField(
                value = email,
                onValueChange = { email = it; emailError = "" },
                label = { Text("Email", color = SecondaryText) },
                leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
                
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it; emailError = "" },
                label = { Text("Password", color = SecondaryText) },
                leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
            
            if (!isSignUpMode) {
                Text(
                    text = "Forgot password?",
                    color = AccentColor,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 8.dp)
                        .clickable { showResetDialog = true }
                )
            }
            
            if (showResetDialog) {
                var resetEmail by remember { mutableStateOf(email) }
                AlertDialog(
                    onDismissRequest = { showResetDialog = false },
                    title = { Text("Reset Password") },
                    text = {
                        OutlinedTextField(
                            value = resetEmail,
                            onValueChange = { resetEmail = it },
                            label = { Text("Email") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            if (resetEmail.isNotBlank()) {
                                scope.launch {
                                    val success = playerViewModel.authManager.sendPasswordResetEmail(resetEmail)
                                    if (success) {
                                        activity?.let {
                                            android.widget.Toast.makeText(it, "Password reset link sent to $resetEmail", android.widget.Toast.LENGTH_LONG).show()
                                        }
                                    } else {
                                        emailError = "Failed to send reset link."
                                    }
                                    showResetDialog = false
                                }
                            }
                        }) {
                            Text("Send Link", color = AccentColor)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showResetDialog = false }) {
                            Text("Cancel", color = SecondaryText)
                        }
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        emailError = "Please enter email and password"
                    } else if (password.length < 8) {
                        emailError = "Password must be at least 8 characters"
                    } else {
                        scope.launch {
                            try {
                                if (isSignUpMode) {
                                    val success = playerViewModel.authManager.signUpWithEmail(email, password)
                                    if (success) onNavigateBack()
                                } else {
                                    val success = playerViewModel.authManager.signInWithEmail(email, password)
                                    if (success) onNavigateBack()
                                }
                            } catch (e: Exception) {
                                emailError = e.message ?: "Authentication failed."
                            }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentColor, contentColor = Color.White),
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (isSignUpMode) "Sign Up" else "Log In", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            androidx.compose.material3.TextButton(
                onClick = {
                    isSignUpMode = !isSignUpMode
                    emailError = ""
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (isSignUpMode) "Already have an account? Log In" else "Don't have an account? Sign Up",
                    color = AccentColor,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
}

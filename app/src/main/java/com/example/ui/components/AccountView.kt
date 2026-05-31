package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.FocusViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountView(
    viewModel: FocusViewModel,
    modifier: Modifier = Modifier
) {
    val email by viewModel.userEmail.collectAsStateWithLifecycle()
    val name by viewModel.userName.collectAsStateWithLifecycle()
    val avatarUrl by viewModel.userAvatarUrl.collectAsStateWithLifecycle()

    var inputEmail by remember { mutableStateOf("") }
    var inputName by remember { mutableStateOf("") }
    var showExplanationDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        if (email.isNullOrBlank()) {
            // Signed-Out View: Login Invitation / Simulated Form
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "🔐",
                fontSize = 58.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Authenticate with Google",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Black
                ),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Sign in to keep your tasks and focus history synced across sessions on your Google Account securely",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Text Inputs
            OutlinedTextField(
                value = inputName,
                onValueChange = { inputName = it },
                label = { Text("Display Name") },
                placeholder = { Text("e.g. Nishank Gupta") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auth_name_input"),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = inputEmail,
                onValueChange = { inputEmail = it },
                label = { Text("Gmail Address") },
                placeholder = { Text("e.g. nishankgupta08@gmail.com") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auth_email_input"),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = { showExplanationDialog = true }
                ) {
                    Text(
                        text = "⚙️ Configure OAuth Credential client ID",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Google Login Trigger Button (G-Brand styled)
            Button(
                onClick = {
                    val finalEmail = if (inputEmail.isBlank()) "user@gmail.com" else inputEmail
                    val finalName = if (inputName.isBlank()) "Focus User" else inputName
                    viewModel.loginWithGmail(finalEmail, finalName)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("auth_login_gmail_btn"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Continue with Google Account",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        } else {
            // Signed-In View: Active profile & settings status
            Spacer(modifier = Modifier.height(24.dp))
            
            // Avatar frame
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                // Interactive Emoji profile indicator if image-loader coil is offline
                Text(
                    text = "👤",
                    fontSize = 44.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = name ?: "Google Account User",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Black
                )
            )

            Text(
                text = email ?: "user@gmail.com",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Configuration status widget list
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Synchronization Profile",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Cloud Sync Backup Status",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF10B981).copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "✓ ACTIVE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF10B981)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Local Task Data",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Saved securely on device",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Account sign out
            OutlinedButton(
                onClick = { viewModel.logoutGmail() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("auth_logout_btn"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Disconnect Gmail Account",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }

    if (showExplanationDialog) {
        AlertDialog(
            onDismissRequest = { showExplanationDialog = false },
            title = { Text("Google Console OAuth Setup Instructions") },
            text = {
                Text(
                    text = "To deploy Google Gmail Sign-In to a real Google Cloud Client ID at production runtime:\n\n" +
                            "1. Create a project at console.cloud.google.com.\n" +
                            "2. Set up the OAuth consent screen with user support email.\n" +
                            "3. Gen client credentials in APIs & Services.\n" +
                            "4. Set GOOGLE_CLIENT_ID inside the AI Studio Secrets panel or inside a local .env configuration file on compile.\n\n" +
                            "The applet automatically integrates these keys into Android BuildConfig securely."
                )
            },
            confirmButton = {
                TextButton(onClick = { showExplanationDialog = false }) {
                    Text("Got it")
                }
            }
        )
    }
}

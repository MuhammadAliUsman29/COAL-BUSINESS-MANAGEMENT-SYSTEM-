package com.example.myapplication55.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.alpha
import androidx.compose.material.icons.filled.Security
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication55.ui.theme.CoalPrimary
import com.example.myapplication55.ui.theme.MyApplication55Theme

@Composable
fun LoginScreen(viewModel: AuthViewModel, onSignUp: () -> Unit, onAdminOversight: () -> Unit = {}) {
    val error by viewModel.loginError
    var showForgotDialog by remember { mutableStateOf(false) }
    var showAdminAuthDialog by remember { mutableStateOf(false) }
    var adminPassword by remember { mutableStateOf("") }
    var adminAuthError by remember { mutableStateOf<String?>(null) }

    LoginContent(
        error = error,
        onLogin = { cnic, password -> viewModel.login(cnic, password) },
        onSignUp = onSignUp,
        onForgotPassword = { showForgotDialog = true },
        onAdminTrigger = { showAdminAuthDialog = true }
    )

    if (showForgotDialog) {
        ForgotPasswordDialog(
            onDismiss = { showForgotDialog = false },
            onConfirm = { cnic, mobile, newPass ->
                viewModel.resetPassword(cnic, mobile, newPass)
                showForgotDialog = false
            }
        )
    }

    if (showAdminAuthDialog) {
        AlertDialog(
            onDismissRequest = { showAdminAuthDialog = false },
            title = { Text("ADMIN OVERRIDE", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Enter Secondary Master Password to access User Management.", fontSize = 12.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = adminPassword,
                        onValueChange = { adminPassword = it },
                        label = { Text("Master Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (adminAuthError != null) {
                        Text(adminAuthError!!, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (adminPassword == "CMS-ADMIN-2026") { // Secondary Master Password
                        showAdminAuthDialog = false
                        onAdminOversight()
                    } else {
                        adminAuthError = "Incorrect Master Password"
                    }
                }) {
                    Text("ACCESS")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAdminAuthDialog = false }) { Text("CANCEL") }
            }
        )
    }
}

@Composable
fun LoginContent(
    error: String?,
    onLogin: (String, String) -> Unit,
    onSignUp: () -> Unit,
    onForgotPassword: () -> Unit,
    onAdminTrigger: () -> Unit = {}
) {
    var cnic by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "WELCOME BACK",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "SIGN IN TO MANAGE YOUR COAL BUSINESS",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            modifier = Modifier.padding(bottom = 32.dp)
        )

        OutlinedTextField(
            value = cnic,
            onValueChange = { cnic = it },
            label = { Text("CNIC Number") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedLabelColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                cursorColor = MaterialTheme.colorScheme.primary
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = null, tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedLabelColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                cursorColor = MaterialTheme.colorScheme.primary
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onForgotPassword) {
                Text(text = "FORGOT PASSWORD?", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f), fontSize = 12.sp)
            }
        }

        if (error != null) {
            Text(text = error, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, modifier = Modifier.padding(bottom = 16.dp))
        }

        Button(
            onClick = { onLogin(cnic, password) },
            modifier = Modifier.fillMaxWidth().height(56.dp).padding(top = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(text = "LOGIN", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
        }

        TextButton(onClick = onSignUp, modifier = Modifier.padding(top = 16.dp)) {
            Text(text = "DON'T HAVE AN ACCOUNT? SIGN UP", color = MaterialTheme.colorScheme.primary)
        }

        // High-visibility Admin Trigger (Yellow Shield Icon)
        Spacer(modifier = Modifier.height(32.dp))
        IconButton(
            onClick = onAdminTrigger,
            modifier = Modifier
                .size(48.dp)
                .background(Color(0xFFFFD700).copy(alpha = 0.1f), CircleShape) // Subtle gold glow
                .border(2.dp, Color(0xFFFFD700), CircleShape) // High-visibility bright yellow border
        ) {
            Icon(
                Icons.Default.Security, 
                contentDescription = "Admin Override", 
                tint = Color(0xFFFFD700), // Bright Yellow/Gold
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
fun ForgotPasswordDialog(onDismiss: () -> Unit, onConfirm: (String, String, String) -> Unit) {
    var cnic by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Account Recovery", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Verify your identity to reset password.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 12.sp)
                OutlinedTextField(
                    value = cnic, 
                    onValueChange = { cnic = it }, 
                    label = { Text("Registered CNIC") }, 
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )
                OutlinedTextField(
                    value = mobile, 
                    onValueChange = { mobile = it }, 
                    label = { Text("Registered Mobile") }, 
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )
                OutlinedTextField(
                    value = newPassword, 
                    onValueChange = { newPassword = it }, 
                    label = { Text("New Password") }, 
                    visualTransformation = PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(cnic, mobile, newPassword) },
                enabled = cnic.isNotBlank() && mobile.isNotBlank() && newPassword.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("RESET PASSWORD", color = MaterialTheme.colorScheme.onPrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = MaterialTheme.colorScheme.primary)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurface
    )
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    MyApplication55Theme {
        LoginContent(
            error = null,
            onLogin = { _, _ -> },
            onSignUp = {},
            onForgotPassword = {}
        )
    }
}

package com.example.myapplication55.ui.screens.profile

import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import com.example.myapplication55.ui.theme.BorderBlueGrey
import com.example.myapplication55.ui.theme.CoalPrimary
import com.example.myapplication55.ui.theme.CoalSurface
import com.example.myapplication55.ui.theme.ErrorRed
import com.example.myapplication55.util.FileUtils
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(viewModel: ProfileViewModel) {
    val context = LocalContext.current
    val user by viewModel.currentUser.collectAsState()
    var fullName by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var profilePicUri by remember { mutableStateOf<android.net.Uri?>(null) }
    
    val launcher = com.example.myapplication55.ui.screens.auth.rememberImagePickerLauncher { uri ->
        profilePicUri = uri
    }

    var showResetDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var resetActionType by remember { mutableStateOf("MASTER") } // "MASTER" or "CUSTOMER"
    var targetCustomerId by remember { mutableStateOf("") }
    var resetPassword by remember { mutableStateOf("") }
    val resetSuccess by viewModel.resetSuccess
    val resetError by viewModel.resetError

    LaunchedEffect(user) {
        user?.let {
            fullName = it.fullName
            mobile = it.mobileNumber
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PROFILE MANAGEMENT", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Header
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(CoalSurface)
                    .clickable { launcher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (profilePicUri != null || user?.profilePicPath != null) {
                    val painter = coil.compose.rememberAsyncImagePainter(
                        model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                            .data(profilePicUri ?: user?.profilePicPath)
                            .crossfade(true)
                            .build()
                    )
                    androidx.compose.foundation.Image(
                        painter = painter,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.Person, contentDescription = null, tint = CoalPrimary, modifier = Modifier.size(64.dp))
                }
            }
            Text(text = "TAP TO CHANGE PHOTO", fontSize = 10.sp, color = Color.Gray, modifier = Modifier.padding(top = 8.dp))
            Text(
                text = user?.fullName ?: "Loading...",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 16.dp)
            )
            Text(text = "CNIC: ${user?.cnic ?: ""}", color = Color.Gray, fontSize = 14.sp)

            Spacer(modifier = Modifier.height(32.dp))

            // Info Fields
            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Update Full Name") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CoalPrimary,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                )
            )

            OutlinedTextField(
                value = mobile,
                onValueChange = { mobile = it },
                label = { Text("Update Mobile Number") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CoalPrimary,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )

            Button(
                onClick = { 
                    val internalPath = profilePicUri?.let { FileUtils.saveImageToInternalStorage(context, it, "profile") }
                    viewModel.updateUser(fullName, mobile, internalPath) 
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CoalPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = "SAVE CHANGES", color = Color.White, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.weight(1f))

            // Administrative Section
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.Red.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                color = Color.Red.copy(alpha = 0.1f),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = Color.Red, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "ADMINISTRATIVE TOOLS", fontWeight = FontWeight.Bold, color = Color.Red, fontSize = 14.sp)
                    }
                    Text(text = "Critical actions require password confirmation.", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(vertical = 8.dp))
                    
                    Button(
                        onClick = { showChangePasswordDialog = true },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CoalSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CoalPrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("CHANGE PASSWORD", color = CoalPrimary)
                    }
                }
            }
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text(if (resetActionType == "MASTER") "CONFIRM MASTER RESET" else "RESET CUSTOMER DATA") },
            text = {
                Column {
                    Text(
                        if (resetActionType == "MASTER") "This action will wipe all customers, transactions, and inventory data. This cannot be undone."
                        else "This will wipe all transactions for the specified customer and reset their balance.",
                        color = Color.Gray
                    )
                    
                    if (resetActionType == "CUSTOMER") {
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = targetCustomerId,
                            onValueChange = { targetCustomerId = it },
                            label = { Text("Customer ID") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = resetPassword,
                        onValueChange = { resetPassword = it },
                        label = { Text("Enter Password to Confirm") },
                        visualTransformation = PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (resetError != null) {
                        Text(text = resetError!!, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { 
                        if (resetActionType == "MASTER") {
                            viewModel.masterReset(resetPassword) 
                        } else {
                            viewModel.resetCustomerData(targetCustomerId.toLongOrNull() ?: -1L, resetPassword)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text(if (resetActionType == "MASTER") "ERASE EVERYTHING" else "RESET CUSTOMER")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showResetDialog = false 
                    viewModel.clearStatus()
                }) {
                    Text("CANCEL")
                }
            },
            containerColor = CoalSurface
        )
    }

    if (resetSuccess) {
        AlertDialog(
            onDismissRequest = { 
                viewModel.clearStatus()
                showResetDialog = false
            },
            title = { Text("SUCCESS") },
            text = { Text("Action completed successfully.") },
            confirmButton = {
                Button(onClick = { 
                    viewModel.clearStatus()
                    showResetDialog = false
                }) {
                    Text("OK")
                }
            },
            containerColor = CoalSurface
        )
    }

    if (showChangePasswordDialog) {
        var currentPw by remember { mutableStateOf("") }
        var newPw by remember { mutableStateOf("") }
        var confirmPw by remember { mutableStateOf("") }
        var pwError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showChangePasswordDialog = false },
            title = { Text("CHANGE PASSWORD") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = currentPw,
                        onValueChange = { currentPw = it },
                        label = { Text("Current Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.Black, unfocusedTextColor = Color.Black)
                    )
                    OutlinedTextField(
                        value = newPw,
                        onValueChange = { newPw = it },
                        label = { Text("New Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.Black, unfocusedTextColor = Color.Black)
                    )
                    OutlinedTextField(
                        value = confirmPw,
                        onValueChange = { confirmPw = it },
                        label = { Text("Confirm New Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.Black, unfocusedTextColor = Color.Black)
                    )
                    if (pwError != null) {
                        Text(pwError!!, color = Color.Red, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPw != confirmPw) {
                            pwError = "Passwords do not match"
                        } else {
                            viewModel.changePassword(currentPw, newPw)
                            showChangePasswordDialog = false
                        }
                    }
                ) {
                    Text("UPDATE")
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangePasswordDialog = false }) {
                    Text("CANCEL")
                }
            },
            containerColor = CoalSurface
        )
    }
}

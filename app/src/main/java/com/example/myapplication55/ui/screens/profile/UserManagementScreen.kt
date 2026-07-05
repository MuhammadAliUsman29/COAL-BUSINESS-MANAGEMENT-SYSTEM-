package com.example.myapplication55.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication55.ui.theme.CoalPrimary
import com.example.myapplication55.ui.theme.CoalSurface

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManagementScreen(viewModel: ProfileViewModel, onBack: () -> Unit) {
    val users by viewModel.allUsers.collectAsState()
    val logs by viewModel.activityLogs.collectAsState()
    val stocks by viewModel.allStocks.collectAsState()
    
    var selectedUserForDetails by remember { mutableStateOf<com.example.myapplication55.data.local.entities.User?>(null) }

    // Check for alerts
    val highValueThreshold = 100000.0
    val highValueTransactions = logs.filter { it.action.contains("Sale", true) || it.action.contains("OUT", true) }
        .filter { log -> 
            val amountStr = log.details.substringAfter("Rs. ").substringBefore(" ").replace(",", "")
            (amountStr.toDoubleOrNull() ?: 0.0) > highValueThreshold
        }
    
    val lowStockAlerts = stocks.filter { it.quantity < (it.totalCapacity * 0.1) }

    val resetSuccess by viewModel.resetSuccess
    val resetError by viewModel.resetError
    var showResetDialog by remember { mutableStateOf(false) }
    var resetActionType by remember { mutableStateOf("MASTER") } // "MASTER" or "CUSTOMER"
    var targetCustomerId by remember { mutableStateOf("") }
    var resetPassword by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("USER OVERSIGHT", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Stats Row
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                KpiMiniCard(title = "TOTAL ACCOUNTS", value = users.size.toString(), modifier = Modifier.weight(1f))
                KpiMiniCard(title = "ACTIVE LOGS", value = logs.size.toString(), modifier = Modifier.weight(1f))
            }

            if (highValueTransactions.isNotEmpty() || lowStockAlerts.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                AdminSectionHeader(title = "SYSTEM ALERTS")
                
                lowStockAlerts.forEach { stock ->
                    AlertCard(message = "LOW STOCK ALERT: ${stock.origin} is at ${(stock.quantity/stock.totalCapacity*100).toInt()}% capacity")
                    Spacer(modifier = Modifier.height(8.dp))
                }

                highValueTransactions.take(3).forEach { log ->
                    AlertCard(message = "HIGH VALUE TRANSACTION: ${log.details}")
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // User List Section
            AdminSectionHeader(title = "REGISTERED USERS (CLICK TO VIEW ACTIVITY)")
            users.forEach { user ->
                UserItem(user, onClick = { selectedUserForDetails = user })
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CoalSurface.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("SYSTEM RESET TOOLS", fontWeight = FontWeight.Bold, color = CoalPrimary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = { 
                            resetActionType = "CUSTOMER"
                            showResetDialog = true 
                        },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CoalSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                    ) {
                        Text("RESET INDIVIDUAL CUSTOMER", color = Color.Red)
                    }

                    Button(
                        onClick = { 
                            resetActionType = "MASTER"
                            showResetDialog = true 
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("MASTER SYSTEM RESET")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(80.dp))
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
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = resetPassword,
                        onValueChange = { resetPassword = it },
                        label = { Text("Enter Password to Confirm") },
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
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
            containerColor = MaterialTheme.colorScheme.surface
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
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    selectedUserForDetails?.let { user ->
        UserActivityDetailDialog(
            user = user,
            viewModel = viewModel,
            onDismiss = { selectedUserForDetails = null }
        )
    }
}

@Composable
fun KpiMiniCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = CoalPrimary.copy(alpha = 0.1f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, CoalPrimary.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = title, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = CoalPrimary)
            Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserActivityDetailDialog(
    user: com.example.myapplication55.data.local.entities.User,
    viewModel: ProfileViewModel,
    onDismiss: () -> Unit
) {
    val userLogs by viewModel.getLogsByUser(user.id).collectAsState(initial = emptyList())

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.fillMaxSize()) {
                TopAppBar(
                    title = { Column {
                        Text(user.fullName, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("System Activity", fontSize = 12.sp, color = Color.Gray)
                    }},
                    navigationIcon = {
                        IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "Close") }
                    }
                )

                if (userLogs.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No activity recorded for this user.", color = Color.Gray)
                    }
                } else {
                    androidx.compose.foundation.lazy.LazyColumn(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(userLogs.size) { index ->
                            val log = userLogs[index]
                            LogItem(log)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminSectionHeader(title: String) {
    Text(
        text = title,
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Black,
        color = CoalPrimary,
        letterSpacing = 1.5.sp
    )
}

@Composable
fun UserItem(user: com.example.myapplication55.data.local.entities.User, onClick: () -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = CoalSurface)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = CircleShape, modifier = Modifier.size(40.dp), color = CoalPrimary.copy(alpha = 0.1f)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Group, contentDescription = null, tint = CoalPrimary, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = user.fullName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(text = "CNIC: ${user.cnic}", color = Color.Gray, fontSize = 11.sp)
            }
            Spacer(modifier = Modifier.weight(1f))
            if (user.isAdmin) {
                Surface(color = com.example.myapplication55.ui.theme.ProfessionalBlue.copy(alpha = 0.1f), shape = RoundedCornerShape(4.dp)) {
                    Text("ADMIN", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), color = com.example.myapplication55.ui.theme.ProfessionalBlue, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun LogItem(log: com.example.myapplication55.data.local.entities.ActivityLog) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = log.action, fontWeight = FontWeight.ExtraBold, color = CoalPrimary, fontSize = 11.sp)
                Text(text = java.text.SimpleDateFormat("dd MMM, hh:mm a", java.util.Locale.getDefault()).format(java.util.Date(log.timestamp)), fontSize = 10.sp, color = Color.Gray)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = log.details, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun AlertCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = com.example.myapplication55.ui.theme.ErrorRed.copy(alpha = 0.1f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, com.example.myapplication55.ui.theme.ErrorRed)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Warning, contentDescription = null, tint = com.example.myapplication55.ui.theme.ErrorRed)
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = message, color = com.example.myapplication55.ui.theme.ErrorRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

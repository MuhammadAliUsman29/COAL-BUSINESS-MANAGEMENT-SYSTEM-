package com.example.myapplication55.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.myapplication55.ui.theme.CoalPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PRIVACY POLICY", fontWeight = FontWeight.Bold) },
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
            Icon(
                Icons.Rounded.Security,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = CoalPrimary
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Data Security & Privacy",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            PrivacySection(
                title = "Offline Application",
                content = "The Coal Management System (CMS) is strictly an offline application. It does not require an internet connection for its core operations, ensuring your data remains under your control."
            )
            
            PrivacySection(
                title = "Local Data Storage",
                content = "All database records, transaction history, and personal information are stored locally on your device's internal storage. We do not transmit or share any of your data with external servers or third parties."
            )
            
            PrivacySection(
                title = "Scope of Use",
                content = "This application is designed exclusively for Coal Management operations. It only accesses permissions necessary for its function, such as storage for report generation and camera/gallery for profile/transaction images."
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "Your privacy is our priority.",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = CoalPrimary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun PrivacySection(title: String, content: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
            textAlign = TextAlign.Justify
        )
    }
}

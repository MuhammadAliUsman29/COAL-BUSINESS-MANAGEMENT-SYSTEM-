package com.example.myapplication55.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.zIndex
import com.example.myapplication55.data.local.entities.Customer
import androidx.compose.foundation.BorderStroke
import com.example.myapplication55.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

data class Quadruple<out A, out B, out C, out D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onAddCustomer: () -> Unit = {},
    onAddStock: () -> Unit = {},
    onViewAllCustomers: () -> Unit = {},
    onViewReports: () -> Unit = {},
    onViewInventory: () -> Unit = {}
) {
    val totalBalance by viewModel.totalBalance.collectAsState()
    val pendingRecovery by viewModel.pendingRecovery.collectAsState()
    val totalStock by viewModel.totalStock.collectAsState()
    val totalCapacity by viewModel.totalCapacity.collectAsState()
    val recentCustomers by viewModel.recentCustomers.collectAsState()
    var showFabMenu by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // KPI Cards Row
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    KpiCard(
                        title = "TOTAL BALANCE",
                        value = "Rs. ${String.format("%,.0f", totalBalance)}",
                        subtitle = "Confirmed Received",
                        backgroundColor = Color(0xFF1A237E), // Deep Blue
                        contentColor = Color.White,
                        modifier = Modifier.weight(1f),
                        onClick = onViewAllCustomers
                    )
                    KpiCard(
                        title = "PENDING STATUS",
                        value = "Rs. ${String.format("%,.0f", pendingRecovery)}",
                        subtitle = "Account Recovery",
                        backgroundColor = Color(0xFFC62828), // Deep Red
                        contentColor = Color.White,
                        modifier = Modifier.weight(1f),
                        onClick = onViewAllCustomers
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    KpiCard(
                        title = "STOCK SUMMARY",
                        value = String.format("%,.1f", totalStock),
                        unit = "KG",
                        subtitle = "Total Inventory",
                        backgroundColor = Color(0xFF1B5E20), // Deep Green
                        contentColor = Color.White,
                        modifier = Modifier.weight(1f),
                        onClick = onViewInventory
                    )
                    val capacityPercent = if (totalCapacity > 0) ((totalStock / totalCapacity) * 100).toInt().coerceIn(0, 100) else 0
                    KpiCard(
                        title = "STOCK CAPACITY",
                        value = "$capacityPercent%",
                        subtitle = "Warehouse Usage",
                        backgroundColor = Color(0xFFE65100), // Deep Orange
                        contentColor = Color.White,
                        modifier = Modifier.weight(1f),
                        onClick = onViewInventory
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    KpiCard(
                        title = "FINANCIAL HISTORY",
                        value = if (totalBalance > 100000) "HIGH" else "STABLE",
                        subtitle = "Risk Assessment",
                        backgroundColor = Color(0xFF4A148C), // Deep Purple
                        contentColor = Color.White,
                        modifier = Modifier.weight(1f),
                        onClick = onViewReports
                    )
                }
            }

            // Recent Customers Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RECENT CUSTOMERS",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 0.5.sp
                )
                Row {
                    TextButton(onClick = onViewAllCustomers) {
                        Text("VIEW ALL", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Recent Customers List
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f, fill = false)
            ) {
                recentCustomers.forEach { customer ->
                    CustomerItem(customer)
                }
                
                if (recentCustomers.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.PeopleOutline, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.Gray)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("No recent customers", color = Color.Gray, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(80.dp))
        }

        // FAB Menu Overlay
        if (showFabMenu) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .clickable { showFabMenu = false }
                    .zIndex(1f)
            )
        }

        // FAB Actions
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .zIndex(2f),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (showFabMenu) {
                FabMenuItem(label = "ADD CUSTOMER", icon = Icons.Default.PersonAdd, onClick = {
                    showFabMenu = false
                    onAddCustomer()
                })
                FabMenuItem(label = "ADD STOCK", icon = Icons.Default.AddBox, onClick = {
                    showFabMenu = false
                    onAddStock()
                })
            }
            
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(CoalPrimary, Color(0xFFBF6E1E))
                        )
                    )
                    .clickable { showFabMenu = !showFabMenu },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (showFabMenu) Icons.Default.Close else Icons.Default.Add,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
fun FabMenuItem(label: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable { onClick() }
    ) {
        Surface(
            color = Color(0xFF1A237E), // Professional Blue
            shape = RoundedCornerShape(4.dp)
        ) {
            Text(
                text = label,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Surface(
            color = Color(0xFF1A237E), // Professional Blue
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun TelemetrySection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "LOAD TELEMETRY AND ANALYSIS",
            fontSize = 10.sp,
            color = Color.Gray,
            letterSpacing = 2.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(24.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            val heights = listOf(0.3f, 0.5f, 0.4f, 0.8f, 0.6f, 1f, 0.7f)
            heights.forEach { h ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(h)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFFE69138).copy(alpha = 0.4f), Color(0xFFE69138).copy(alpha = 0.1f))
                            ),
                            shape = RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp)
                        )
                )
            }
        }
    }
}

@Composable
fun KpiCard(
    title: String,
    value: String,
    subtitle: String? = null,
    unit: String? = null,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier
            .height(130.dp)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title, 
                fontSize = 10.sp, 
                color = contentColor.copy(alpha = 0.8f), 
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    fontWeight = FontWeight.ExtraBold,
                    color = contentColor,
                    fontSize = 20.sp
                )
                if (unit != null) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = unit, fontSize = 12.sp, color = contentColor, fontWeight = FontWeight.Bold)
                }
            }
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle, 
                    fontSize = 10.sp, 
                    color = contentColor.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun CustomerItem(customer: Customer) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderBlueGrey, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = customer.name,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp
                )
                Text(
                    text = customer.phoneNumber,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(customer.lastTransactionDate)).uppercase(),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (customer.totalBalance >= 0) SuccessGreen.copy(alpha = 0.1f) else ErrorRed.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = if (customer.totalBalance >= 0) "CASH IN" else "CASH OUT",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        color = if (customer.totalBalance >= 0) SuccessGreen else ErrorRed,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

@Composable
fun CustomerPlaceholder(name: String, type: String, phone: String, date: String) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderBlueGrey, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp
                )
                Text(
                    text = phone,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = date,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (type == "CASH IN") SuccessGreen.copy(alpha = 0.1f) else ErrorRed.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = type,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        color = if (type == "CASH IN") SuccessGreen else ErrorRed,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

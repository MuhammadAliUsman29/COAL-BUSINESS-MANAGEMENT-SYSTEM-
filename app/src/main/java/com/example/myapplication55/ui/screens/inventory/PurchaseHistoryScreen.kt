package com.example.myapplication55.ui.screens.inventory

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication55.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchaseHistoryScreen(viewModel: InventoryViewModel, onBack: () -> Unit) {
    val purchases by viewModel.purchaseHistory.collectAsState()
    var purchaseToDelete by remember { mutableStateOf<com.example.myapplication55.data.local.entities.PurchaseHistory?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PURCHASE HISTORY", fontWeight = FontWeight.Bold) },
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
                .padding(16.dp)
        ) {
            if (purchases.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No purchase records found.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(purchases) { purchase ->
                        PurchaseItem(purchase, onDelete = { purchaseToDelete = purchase })
                    }
                }
            }
        }
    }

    purchaseToDelete?.let { purchase ->
        AlertDialog(
            onDismissRequest = { purchaseToDelete = null },
            title = { Text("Revert Purchase?") },
            text = { Text("Deleting this record will add Rs. ${purchase.total_cost} back to your balance and deduct ${purchase.quantity} KG from stock. Continue?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deletePurchase(purchase)
                    purchaseToDelete = null
                }) { Text("YES, REVERT", color = ErrorRed) }
            },
            dismissButton = {
                TextButton(onClick = { purchaseToDelete = null }) { Text("CANCEL") }
            }
        )
    }
}

@Composable
fun PurchaseItem(purchase: com.example.myapplication55.data.local.entities.PurchaseHistory, onDelete: () -> Unit) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderBlueGrey, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = SuccessGreen.copy(alpha = 0.1f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = SuccessGreen)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = purchase.item_name.uppercase(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "${purchase.quantity} KG @ Rs. ${purchase.unit_price}/KG",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
                Text(
                    text = SimpleDateFormat("MMM dd, yyyy | hh:mm a", Locale.getDefault()).format(Date(purchase.date_timestamp)),
                    color = Color.Gray.copy(alpha = 0.6f),
                    fontSize = 10.sp
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Rs. ${String.format(Locale.getDefault(), "%,.0f", purchase.total_cost)}",
                    fontWeight = FontWeight.ExtraBold,
                    color = ErrorRed, // Money going out
                    fontSize = 16.sp
                )
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

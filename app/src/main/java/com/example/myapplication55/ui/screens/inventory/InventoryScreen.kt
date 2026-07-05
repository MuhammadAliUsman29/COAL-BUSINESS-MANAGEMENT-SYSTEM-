package com.example.myapplication55.ui.screens.inventory

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.myapplication55.data.local.entities.PaymentStatus
import com.example.myapplication55.data.local.entities.Transaction
import com.example.myapplication55.data.local.entities.TransactionType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.myapplication55.data.local.entities.CoalStock
import com.example.myapplication55.data.local.entities.Customer
import com.example.myapplication55.ui.theme.*
import java.io.File
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(viewModel: InventoryViewModel, onViewPurchaseHistory: () -> Unit = {}) {
    val stocks by viewModel.allStocks.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val customers by viewModel.allCustomers.collectAsState()
    val errorMessage by viewModel.errorMessage
    var showAddDialog by remember { mutableStateOf(false) }
    var stockToUpdate by remember { mutableStateOf<CoalStock?>(null) }
    var stockToDelete by remember { mutableStateOf<CoalStock?>(null) }
    var updateType by remember { mutableStateOf(TransactionType.STOCK_IN) }
    var showHistoryForStock by remember { mutableStateOf<CoalStock?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            Text(
                text = "PRECISION INVENTORY",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Button(
                onClick = onViewPurchaseHistory,
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.History, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("VIEW PURCHASE HISTORY", fontWeight = FontWeight.Bold)
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                placeholder = { Text("Search by origin or grade...", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = null, tint = Color.Gray)
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ProfessionalBlue,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(stocks) { stock ->
                    StockItem(
                        stock = stock,
                        onStockIn = {
                            stockToUpdate = stock
                            updateType = TransactionType.STOCK_IN
                        },
                        onStockOut = {
                            stockToUpdate = stock
                            updateType = TransactionType.STOCK_OUT
                        },
                        onLongClick = {
                            stockToDelete = stock
                        },
                        onClick = {
                            showHistoryForStock = stock
                        }
                    )
                }
                
                if (stocks.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                            Text(text = "No stock entries found.", color = Color.Gray)
                        }
                    }
                }
            }
        }

        ExtendedFloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = CoalPrimary,
            contentColor = Color.Black,
            icon = { Icon(Icons.Default.Add, contentDescription = null) },
            text = { Text("NEW STOCKPILE") }
        )
    }

    showHistoryForStock?.let { stock ->
        StockHistoryDialog(
            stock = stock,
            viewModel = viewModel,
            onDismiss = { showHistoryForStock = null }
        )
    }

    if (stockToDelete != null) {
        AlertDialog(
            onDismissRequest = { stockToDelete = null },
            title = { Text("Delete Stockpile?", color = MaterialTheme.colorScheme.onSurface) },
            text = { Text("Are you sure you want to delete the stockpile '${stockToDelete?.origin}'? This action cannot be undone.", color = MaterialTheme.colorScheme.onSurface) },
            confirmButton = {
                TextButton(
                    onClick = {
                        stockToDelete?.let { viewModel.deleteStock(it) }
                        stockToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = ErrorRed)
                ) {
                    Text("DELETE")
                }
            },
            dismissButton = {
                TextButton(onClick = { stockToDelete = null }) {
                    Text("CANCEL")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    if (showAddDialog) {
        AddStockDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { origin, moisture, quantity, grade, capacity ->
                viewModel.addStock(origin, moisture, quantity, grade, capacity)
                showAddDialog = false
            }
        )
    }

    stockToUpdate?.let { stock ->
        StockMovementDialog(
            stock = stock,
            type = updateType,
            customers = customers,
            onDismiss = { stockToUpdate = null },
            onConfirm = { quantity, price, customerId, imagePath, pStatus ->
                viewModel.logStockMovement(stock, quantity, updateType, price, customerId, imagePath, pStatus)
                stockToUpdate = null
            }
        )
    }

    errorMessage?.let { msg ->
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { Text("Validation Error", color = ErrorRed) },
            text = { Text(msg, color = MaterialTheme.colorScheme.onSurface) },
            confirmButton = {
                Button(onClick = { viewModel.clearError() }) {
                    Text("OK")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

@Composable
fun StockItem(
    stock: CoalStock,
    onStockIn: () -> Unit,
    onStockOut: () -> Unit,
    onLongClick: () -> Unit,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderBlueGrey, RoundedCornerShape(16.dp))
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = { onLongClick() }
                )
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Inventory, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stock.origin.uppercase(Locale.getDefault()),
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 18.sp
                    )
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = stock.qualityGrade,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(text = "QUANTITY", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Text(text = "${String.format(Locale.getDefault(), "%.1f", stock.quantity)} KG", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "MOISTURE", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Text(text = stock.moistureLevel, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "CAPACITY", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    val capacityPercent = if (stock.totalCapacity > 0) (stock.quantity / stock.totalCapacity * 100).toInt().coerceIn(0, 100) else 0
                    val remaining = (stock.totalCapacity - stock.quantity).coerceAtLeast(0.0)
                    Text(text = "$capacityPercent%", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                    Text(text = String.format(Locale.getDefault(), "%.0f KG LEFT", remaining), fontSize = 8.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { (stock.quantity / stock.totalCapacity).toFloat().coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onStockOut,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ErrorRed.copy(alpha = 0.5f))
                ) {
                    Icon(Icons.Default.ArrowUpward, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("SALES (kg)", fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = onStockIn,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.White),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.ArrowDownward, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("PURCHASE", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AddStockDialog(onDismiss: () -> Unit, onConfirm: (String, String, Double, String, Double) -> Unit) {
    var origin by remember { mutableStateOf("") }
    var moisture by remember { mutableStateOf("++") }
    var quantity by remember { mutableStateOf("") }
    var grade by remember { mutableStateOf("") }
    var capacity by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val moistureOptions = listOf("++", "--", "+-")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Coal Stock", color = MaterialTheme.colorScheme.onSurface) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = origin, 
                    onValueChange = { origin = it }, 
                    label = { Text("Origin (e.g. Indonesia)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )
                
                Box {
                    OutlinedTextField(
                        value = moisture,
                        onValueChange = {},
                        label = { Text("Moisture Level") },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { expanded = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        moistureOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    moisture = option
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = quantity, 
                    onValueChange = { quantity = it }, 
                    label = { Text("Initial Quantity (KG)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )
                OutlinedTextField(
                    value = grade, 
                    onValueChange = { grade = it }, 
                    label = { Text("Quality Grade (e.g. GAR 4200)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )
                OutlinedTextField(
                    value = capacity, 
                    onValueChange = { capacity = it }, 
                    label = { Text("Max Capacity (KG)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        origin,
                        moisture,
                        quantity.toDoubleOrNull() ?: 0.0,
                        grade,
                        capacity.toDoubleOrNull() ?: 1000.0
                    )
                },
                enabled = origin.isNotBlank() && quantity.isNotBlank()
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
fun StockMovementDialog(
    stock: CoalStock, 
    type: TransactionType, 
    customers: List<Customer>,
    onDismiss: () -> Unit, 
    onConfirm: (Double, Double, Long?, String?, PaymentStatus) -> Unit
) {
    var quantity by remember { mutableStateOf("") }
    var pricePerUnit by remember { mutableStateOf("") }
    var selectedCustomer by remember { mutableStateOf<Customer?>(null) }
    var customerSearchQuery by remember { mutableStateOf("") }
    var customerExpanded by remember { mutableStateOf(false) }
    var paymentStatus by remember { mutableStateOf(PaymentStatus.PAID) }
    var imageUri by rememberSaveable { mutableStateOf<android.net.Uri?>(null) }
    var imagePath by rememberSaveable { mutableStateOf<String?>(null) }
    
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            imagePath = imageUri?.toString()
        }
    }

    val filteredCustomers = customers.filter { it.name.contains(customerSearchQuery, ignoreCase = true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (type == TransactionType.STOCK_IN) "Stock In (Purchase)" else "Stock Out (Sales)", color = MaterialTheme.colorScheme.onSurface) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Stockpile: ${stock.origin} (${stock.qualityGrade})", 
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantity (KG)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )

                OutlinedTextField(
                    value = pricePerUnit,
                    onValueChange = { pricePerUnit = it },
                    label = { Text(if (type == TransactionType.STOCK_IN) "Price In (Purchase Rate)" else "Sale Price") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )

                if (type == TransactionType.STOCK_OUT) {
                    Box {
                        OutlinedTextField(
                            value = selectedCustomer?.name ?: customerSearchQuery,
                            onValueChange = { 
                                customerSearchQuery = it
                                selectedCustomer = null
                                customerExpanded = true
                            },
                            label = { Text("Select Customer") },
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                IconButton(onClick = { customerExpanded = !customerExpanded }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        DropdownMenu(
                            expanded = customerExpanded && filteredCustomers.isNotEmpty(),
                            onDismissRequest = { customerExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.8f).heightIn(max = 200.dp)
                        ) {
                            filteredCustomers.forEach { customer ->
                                DropdownMenuItem(
                                    text = { Text(customer.name) },
                                    onClick = {
                                        selectedCustomer = customer
                                        customerSearchQuery = customer.name
                                        customerExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                if (type == TransactionType.STOCK_OUT) {
                    var statusExpanded by remember { mutableStateOf(false) }
                    Box {
                        OutlinedTextField(
                            value = if (paymentStatus == PaymentStatus.PAID) "Paid (Cash In)" else "Pending",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Payment Status") },
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                IconButton(onClick = { statusExpanded = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        DropdownMenu(expanded = statusExpanded, onDismissRequest = { statusExpanded = false }) {
                            DropdownMenuItem(
                                text = { Text("Paid (Cash In)") },
                                onClick = { paymentStatus = PaymentStatus.PAID; statusExpanded = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Pending") },
                                onClick = { paymentStatus = PaymentStatus.PENDING; statusExpanded = false }
                            )
                        }
                    }
                }

                Button(
                    onClick = {
                        try {
                            val imagesDir = File(context.filesDir, "images")
                            if (!imagesDir.exists()) imagesDir.mkdirs()
                            val file = File(imagesDir, "stock_trans_${System.currentTimeMillis()}.jpg")
                            val uri = FileProvider.getUriForFile(context, "com.example.myapplication55.fileprovider", file)
                            imageUri = uri
                            launcher.launch(uri)
                        } catch (e: Exception) {
                            android.widget.Toast.makeText(context, "Camera Error: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (imagePath == null) "Capture Receipt/Photo" else "Image Attached")
                }
                
                if (imagePath != null) {
                    AsyncImage(
                        model = imagePath,
                        contentDescription = "Selected Image",
                        modifier = Modifier.fillMaxWidth().height(100.dp).clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    val q = quantity.toDoubleOrNull() ?: 0.0
                    onConfirm(
                        q, 
                        pricePerUnit.toDoubleOrNull() ?: 0.0,
                        selectedCustomer?.id,
                        imagePath,
                        paymentStatus
                    ) 
                },
                enabled = quantity.isNotBlank() && pricePerUnit.isNotBlank() && (if (type == TransactionType.STOCK_OUT) selectedCustomer != null else true),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
fun StockHistoryDialog(
    stock: CoalStock,
    viewModel: InventoryViewModel,
    onDismiss: () -> Unit
) {
    val history by viewModel.getStockHistory(stock.id).collectAsState(initial = emptyList())
    var editingTransaction by remember { mutableStateOf<Transaction?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Movement History: ${stock.origin}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)) {
                if (history.isEmpty()) {
                    Text("No history available.", modifier = Modifier.padding(16.dp), color = Color.Gray)
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(history) { transaction ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                onClick = { editingTransaction = transaction }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = transaction.type.name,
                                            fontWeight = FontWeight.Bold,
                                            color = if (transaction.type == TransactionType.STOCK_IN) SuccessGreen else ErrorRed
                                        )
                                        Text(
                                            text = "${String.format(Locale.getDefault(), "%.2f", transaction.quantity)} KG @ ${transaction.pricePerUnit}/KG",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        Text(
                                            text = java.text.SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault()).format(java.util.Date(transaction.timestamp)),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.Gray
                                        )
                                    }
                                    Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp), tint = ProfessionalBlue)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("CLOSE") }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )

    editingTransaction?.let { transaction ->
        EditTransactionDialog(
            transaction = transaction,
            onDismiss = { editingTransaction = null },
            onConfirm = { updated ->
                viewModel.updateTransaction(updated)
                editingTransaction = null
            }
        )
    }
}

@Composable
fun EditTransactionDialog(
    transaction: Transaction,
    onDismiss: () -> Unit,
    onConfirm: (Transaction) -> Unit
) {
    var quantity by remember { mutableStateOf(transaction.quantity.toString()) }
    var price by remember { mutableStateOf(transaction.pricePerUnit.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Transaction") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantity (KG)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Price per Unit") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val q = quantity.toDoubleOrNull() ?: transaction.quantity
                val p = price.toDoubleOrNull() ?: transaction.pricePerUnit
                onConfirm(transaction.copy(quantity = q, pricePerUnit = p))
            }) {
                Text("SAVE")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CANCEL") }
        }
    )
}

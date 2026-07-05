package com.example.myapplication55.ui.screens.customers

import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.Sort
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.runtime.saveable.rememberSaveable
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.myapplication55.data.local.entities.Transaction
import com.example.myapplication55.data.local.entities.TransactionType
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.rounded.Help
import com.example.myapplication55.ui.theme.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDetailScreen(viewModel: CustomerDetailViewModel, onBack: () -> Unit) {
    val errorMessage by viewModel.errorMessage
    val customer by viewModel.customer.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    var showTransactionDialog by rememberSaveable { mutableStateOf<TransactionType?>(null) }
    var showFilterMenu by remember { mutableStateOf(false) }
    var detailedTransactionId by remember { mutableStateOf<Long?>(null) }
    var zoomedImageUrl by rememberSaveable { mutableStateOf<String?>(null) }
    var transactionToDelete by remember { mutableStateOf<Transaction?>(null) }
    var editingTransaction by remember { mutableStateOf<Transaction?>(null) }

    val detailedTransaction = transactions.find { it.id == detailedTransactionId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(customer?.name ?: "Customer Detail") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleAmountSort() }) {
                        Icon(Icons.Rounded.Sort, contentDescription = "Sort by Amount", tint = MaterialTheme.colorScheme.primary)
                    }
                    Box {
                        IconButton(onClick = { showFilterMenu = true }) {
                            Icon(Icons.Rounded.FilterList, contentDescription = "Filter", tint = MaterialTheme.colorScheme.primary)
                        }
                        DropdownMenu(
                            expanded = showFilterMenu,
                            onDismissRequest = { showFilterMenu = false },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                        ) {
                            DropdownMenuItem(
                                text = { Text("All Transactions", color = MaterialTheme.colorScheme.onSurface) },
                                onClick = { 
                                    viewModel.setTypeFilter(null)
                                    showFilterMenu = false 
                                }
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
                            DropdownMenuItem(
                                text = { Text("Cash In Only", color = MaterialTheme.colorScheme.onSurface) },
                                onClick = { 
                                    viewModel.setTypeFilter(TransactionType.CASH_IN)
                                    showFilterMenu = false 
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Cash Out Only", color = MaterialTheme.colorScheme.onSurface) },
                                onClick = { 
                                    viewModel.setTypeFilter(TransactionType.CASH_OUT)
                                    showFilterMenu = false 
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.primary
                )
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
            // Balance Card
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ElevatedCard(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, BorderBlueGrey, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "TOTAL BALANCE", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontWeight = FontWeight.Bold)
                        Text(text = "Rs. ${String.format("%,.0f", customer?.totalBalance ?: 0.0)}", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = if ((customer?.totalBalance ?: 0.0) >= 0) SuccessGreen else ErrorRed)
                    }
                }
                ElevatedCard(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, BorderBlueGrey, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "PENDING BALANCE", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontWeight = FontWeight.Bold)
                        Text(text = "Rs. ${String.format("%,.0f", customer?.pendingBalance ?: 0.0)}", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = ErrorRed)
                    }
                }
            }

            Text(
                text = "TRANSACTION HISTORY",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(transactions, key = { it.id }) { transaction ->
                    TransactionItem(
                        transaction = transaction, 
                        onBodyClick = { detailedTransactionId = transaction.id },
                        onEditClick = { editingTransaction = transaction },
                        onLongClick = { transactionToDelete = transaction },
                        onImageClick = { zoomedImageUrl = it }
                    )
                }
                
                if (transactions.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                            Text(text = "No transactions found.", color = Color.Gray)
                        }
                    }
                }
            }

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

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = { showTransactionDialog = TransactionType.CASH_OUT },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Remove, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("CASH OUT")
                }
                Button(
                    onClick = { showTransactionDialog = TransactionType.CASH_IN },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("CASH IN")
                }
            }
        }
    }

    showTransactionDialog?.let { type ->
        AddCashTransactionDialog(
            type = type,
            onDismiss = { showTransactionDialog = null },
            onConfirm = { amount, note, pending, imagePath ->
                viewModel.addTransaction(amount, type, note, pending, imagePath)
                showTransactionDialog = null
            }
        )
    }

    zoomedImageUrl?.let { url ->
        Dialog(
            onDismissRequest = { zoomedImageUrl = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .clickable { zoomedImageUrl = null },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = url,
                    contentDescription = "Zoomed Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
                IconButton(
                    onClick = { zoomedImageUrl = null },
                    modifier = Modifier.align(Alignment.TopEnd).padding(16.dp).background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }
        }
    }

    transactionToDelete?.let { transaction ->
        AlertDialog(
            onDismissRequest = { transactionToDelete = null },
            title = { Text("Delete Transaction") },
            text = { Text("Are you sure you want to delete this transaction? This will also revert the balance change.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteTransaction(transaction)
                        transactionToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { transactionToDelete = null }) {
                    Text("Cancel")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurface
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

    detailedTransaction?.let { transaction ->
        TransactionHistoryDialog(
            transaction = transaction,
            onDismiss = { detailedTransactionId = null },
            onImageClick = { zoomedImageUrl = it },
            onCashIn = { id, path -> 
                viewModel.markAsPaid(id, path)
                detailedTransactionId = null
            }
        )
    }
}

@Composable
fun TransactionItem(
    transaction: Transaction, 
    onBodyClick: () -> Unit,
    onEditClick: () -> Unit,
    onLongClick: () -> Unit,
    onImageClick: (String) -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderBlueGrey, RoundedCornerShape(12.dp))
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onBodyClick() },
                    onLongPress = { onLongClick() }
                )
            },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (transaction.imagePath != null) {
                        AsyncImage(
                            model = transaction.imagePath,
                            contentDescription = "Transaction Image",
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onImageClick(transaction.imagePath!!) },
                            contentScale = ContentScale.Crop
                        )
                    }
                    Column {
                        val isPending = transaction.paymentStatus == com.example.myapplication55.data.local.entities.PaymentStatus.PENDING
                        
                        Text(
                            text = SimpleDateFormat("MMM dd, yyyy | hh:mm a", Locale.getDefault()).format(Date(transaction.timestamp)),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (transaction.type == TransactionType.CASH_IN) {
                                Icon(
                                    Icons.Default.MonetizationOn, 
                                    contentDescription = null, 
                                    tint = SuccessGreen, 
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                text = when(transaction.type) {
                                    TransactionType.CASH_IN -> "BANK"
                                    TransactionType.CASH_OUT -> "Credit Given"
                                    TransactionType.STOCK_OUT -> if (isPending) "Pending Sale" else "Stock Sale"
                                    TransactionType.STOCK_IN -> "Stock Purchase"
                                },
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isPending && transaction.type != TransactionType.CASH_OUT) ErrorRed else if (transaction.type == TransactionType.CASH_OUT) ProfessionalBlue else MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    val isPending = transaction.paymentStatus == com.example.myapplication55.data.local.entities.PaymentStatus.PENDING
                    val isCashInflow = transaction.type == TransactionType.CASH_IN || 
                                       (transaction.type == TransactionType.STOCK_OUT && transaction.paymentStatus == com.example.myapplication55.data.local.entities.PaymentStatus.PAID)
                    
                    val isCashOutflow = transaction.type == TransactionType.CASH_OUT || 
                                        (transaction.type == TransactionType.STOCK_IN)

                    val sign = if (isCashInflow) "+" else if (isCashOutflow) "-" else ""
                    
                    Text(
                        text = "$sign Rs. ${String.format("%,.2f", transaction.amount)}",
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isPending) ErrorRed else if (isCashInflow) SuccessGreen else if (isCashOutflow) ErrorRed else MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp
                    )
                    if (isPending) {
                        Surface(
                            color = ErrorRed.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "PENDING",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                color = ErrorRed,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.width(32.dp))
            }
            
            // Blue Edit Button in Top-Right Corner
            IconButton(
                onClick = onEditClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(32.dp)
            ) {
                Icon(
                    Icons.Default.Edit, 
                    contentDescription = "Edit", 
                    tint = ProfessionalBlue,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun TransactionDetailDialog(
    transaction: Transaction, 
    onDismiss: () -> Unit,
    onImageClick: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Transaction Details", color = MaterialTheme.colorScheme.primary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (transaction.imagePath != null) {
                    AsyncImage(
                        model = transaction.imagePath,
                        contentDescription = "Transaction Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onImageClick(transaction.imagePath!!) },
                        contentScale = ContentScale.Crop
                    )
                    Text("Tap image to zoom", fontSize = 10.sp, color = Color.Gray, modifier = Modifier.align(Alignment.CenterHorizontally))
                }
                DetailRow("Type", if (transaction.type == TransactionType.CASH_IN) "CASH IN" else "CASH OUT")
                DetailRow("Amount", "Rs. ${String.format("%,.2f", transaction.amount)}")
                if (transaction.pendingAmount > 0) {
                    DetailRow("Pending Amount", "Rs. ${String.format("%,.2f", transaction.pendingAmount)}")
                }
                DetailRow("Date", SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(transaction.timestamp)))
                DetailRow("Time", SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).format(Date(transaction.timestamp)))
                if (transaction.note.isNotBlank()) {
                    Column {
                        Text("Note:", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 12.sp)
                        Text(transaction.note, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Close")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
fun AddCashTransactionDialog(
    type: TransactionType, 
    onDismiss: () -> Unit, 
    onConfirm: (Double, String, Double, String?) -> Unit
) {
    var amount by rememberSaveable { mutableStateOf("") }
    var pendingAmount by rememberSaveable { mutableStateOf("") }
    var note by rememberSaveable { mutableStateOf("") }
    var imageUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var imagePath by rememberSaveable { mutableStateOf<String?>(null) }
    
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            imagePath = imageUri?.toString()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (type == TransactionType.CASH_IN) "BANK (Cash In)" else "Cash Out") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = amount, 
                    onValueChange = { amount = it }, 
                    label = { Text("Amount (Rs.)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )
                if (type == TransactionType.CASH_IN) {
                    OutlinedTextField(
                        value = pendingAmount, 
                        onValueChange = { pendingAmount = it }, 
                        label = { Text("Pending Amount (Optional)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
                OutlinedTextField(
                    value = note, 
                    onValueChange = { note = it }, 
                    label = { Text("Note (Optional)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Button(
                    onClick = {
                        try {
                            val imagesDir = File(context.filesDir, "images")
                            if (!imagesDir.exists()) imagesDir.mkdirs()
                            val file = File(imagesDir, "transaction_${System.currentTimeMillis()}.jpg")
                            val uri = FileProvider.getUriForFile(context, "com.example.myapplication55.fileprovider", file)
                            imageUri = uri
                            launcher.launch(uri)
                        } catch (e: Exception) {
                            android.widget.Toast.makeText(context, "Camera Error: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CTA_Yellow,
                        contentColor = DarkGrey
                    )
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (imagePath == null) "ADD IMAGES" else "IMAGE ATTACHED")
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
                    onConfirm(
                        amount.toDoubleOrNull() ?: 0.0, 
                        note, 
                        pendingAmount.toDoubleOrNull() ?: 0.0,
                        imagePath
                    ) 
                },
                enabled = amount.isNotBlank()
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
fun EditTransactionDialog(
    transaction: Transaction,
    onDismiss: () -> Unit,
    onConfirm: (Transaction) -> Unit
) {
    var amount by remember { mutableStateOf(transaction.amount.toString()) }
    var note by remember { mutableStateOf(transaction.note) }
    var imagePath by remember { mutableStateOf(transaction.imagePath) }
    var receiptPath by remember { mutableStateOf(transaction.receiptImagePath) }
    var paymentStatus by remember { mutableStateOf(transaction.paymentStatus) }

    val context = LocalContext.current
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val stockLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { if (it) imagePath = imageUri?.toString() }
    val receiptLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { if (it) receiptPath = imageUri?.toString() }

    fun launchCamera(isReceipt: Boolean) {
        val file = File(context.filesDir, "images/edit_${if (isReceipt) "receipt" else "stock"}_${System.currentTimeMillis()}.jpg")
        file.parentFile?.mkdirs()
        val uri = FileProvider.getUriForFile(context, "com.example.myapplication55.fileprovider", file)
        imageUri = uri
        if (isReceipt) receiptLauncher.launch(uri) else stockLauncher.launch(uri)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Transaction") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Amount (Rs.)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("Note") }, modifier = Modifier.fillMaxWidth())
                
                if (transaction.type != TransactionType.CASH_OUT) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Payment Status:", fontSize = 14.sp)
                        Switch(checked = paymentStatus == com.example.myapplication55.data.local.entities.PaymentStatus.PAID, onCheckedChange = { paymentStatus = if (it) com.example.myapplication55.data.local.entities.PaymentStatus.PAID else com.example.myapplication55.data.local.entities.PaymentStatus.PENDING })
                        Text(if (paymentStatus == com.example.myapplication55.data.local.entities.PaymentStatus.PAID) "PAID" else "PENDING", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Text("STOCK PHOTO", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Box(modifier = Modifier.fillMaxWidth().height(100.dp).clip(RoundedCornerShape(8.dp)).background(Color.LightGray).clickable { launchCamera(false) }, contentAlignment = Alignment.Center) {
                    if (imagePath != null) AsyncImage(model = imagePath, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    else Icon(Icons.Default.AddAPhoto, contentDescription = null)
                }

                Text("RECEIPT PHOTO", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Box(modifier = Modifier.fillMaxWidth().height(100.dp).clip(RoundedCornerShape(8.dp)).background(Color.LightGray).clickable { launchCamera(true) }, contentAlignment = Alignment.Center) {
                    if (receiptPath != null) AsyncImage(model = receiptPath, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    else Icon(Icons.Default.AddAPhoto, contentDescription = null)
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val a = amount.toDoubleOrNull() ?: transaction.amount
                onConfirm(transaction.copy(amount = a, note = note, imagePath = imagePath, receiptImagePath = receiptPath, paymentStatus = paymentStatus))
            }) { Text("SAVE") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("CANCEL") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionHistoryDialog(
    transaction: Transaction,
    onDismiss: () -> Unit,
    onImageClick: (String) -> Unit,
    onCashIn: (Long, String?) -> Unit
) {
    var receiptUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var receiptPath by rememberSaveable { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) { receiptPath = receiptUri?.toString() }
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                TopAppBar(
                    title = { Text("Transaction Details", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header Status
                    val isPending = transaction.paymentStatus == com.example.myapplication55.data.local.entities.PaymentStatus.PENDING
                    val isCredit = transaction.type == TransactionType.CASH_OUT
                    Surface(
                        color = if (isCredit) ProfessionalBlue.copy(alpha = 0.1f) else if (isPending) ErrorRed.copy(alpha = 0.1f) else SuccessGreen.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (isCredit) "CREDIT GIVEN" else if (isPending) "PENDING PAYMENT" else "PAID",
                                fontWeight = FontWeight.Black,
                                color = if (isCredit) ProfessionalBlue else if (isPending) ErrorRed else SuccessGreen,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Rs. ${String.format("%,.2f", transaction.amount)}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp,
                                color = if (isCredit) ProfessionalBlue else if (isPending) ErrorRed else SuccessGreen
                            )
                        }
                    }

                    // Hierarchy Data
                    DetailSection("TRANSACTION INFO") {
                        DetailRow("Date", SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(transaction.timestamp)))
                        DetailRow("Time", SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).format(Date(transaction.timestamp)))
                        DetailRow("Type", if (transaction.type == TransactionType.CASH_IN) "BANK (CASH IN)" else transaction.type.name.replace("_", " "))
                        if (transaction.pendingAmount > 0) {
                            DetailRow("Pending Khata", "Rs. ${String.format("%,.2f", transaction.pendingAmount)}")
                        }
                    }

                    if (transaction.type == TransactionType.STOCK_OUT || transaction.type == TransactionType.STOCK_IN) {
                        DetailSection("STOCK DETAILS") {
                            DetailRow("Quantity", "${transaction.quantity} KG")
                            DetailRow("Price/Unit", "Rs. ${transaction.pricePerUnit}")
                            transaction.note.takeIf { it.isNotBlank() }?.let {
                                Text("Notes:", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                                Text(it, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }

                    DetailSection("ATTACHED MEDIA") {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            // Stock Image
                            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("STOCK PHOTO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                Spacer(modifier = Modifier.height(8.dp))
                                if (transaction.imagePath != null) {
                                    AsyncImage(
                                        model = transaction.imagePath,
                                        contentDescription = "Stock",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(120.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable { onImageClick(transaction.imagePath!!) },
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Box(Modifier.fillMaxWidth().height(120.dp).background(Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.ImageNotSupported, contentDescription = null, tint = Color.Gray)
                                    }
                                }
                            }
                            
                            // Receipt Image
                            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("RECEIPT PHOTO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                Spacer(modifier = Modifier.height(8.dp))
                                val displayPath = receiptPath ?: transaction.receiptImagePath
                                if (displayPath != null) {
                                    AsyncImage(
                                        model = displayPath,
                                        contentDescription = "Receipt",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(120.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable { onImageClick(displayPath) },
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Box(Modifier.fillMaxWidth().height(120.dp).background(Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                                        if (isPending) {
                                            IconButton(onClick = {
                                                val imagesDir = File(context.filesDir, "images")
                                                if (!imagesDir.exists()) imagesDir.mkdirs()
                                                val file = File(imagesDir, "receipt_${System.currentTimeMillis()}.jpg")
                                                receiptUri = FileProvider.getUriForFile(context, "com.example.myapplication55.fileprovider", file)
                                                launcher.launch(receiptUri!!)
                                            }) {
                                                Icon(Icons.Default.AddAPhoto, contentDescription = "Capture Receipt", tint = ProfessionalBlue, modifier = Modifier.size(32.dp))
                                            }
                                        } else {
                                            Icon(Icons.Default.ImageNotSupported, contentDescription = null, tint = Color.Gray)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                }

                // Bottom Action Button for Pending
                if (transaction.paymentStatus == com.example.myapplication55.data.local.entities.PaymentStatus.PENDING && transaction.type != TransactionType.CASH_OUT) {
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
                        shadowElevation = 8.dp,
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Button(
                            onClick = { onCashIn(transaction.id, receiptPath) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.AccountBalanceWallet, contentDescription = null)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("PROCESS CASH IN (MARK AS PAID)", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = title, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = ProfessionalBlue, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BorderBlueGrey, RoundedCornerShape(12.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            content()
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 14.sp)
        Text(value, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

package com.example.myapplication55.ui.screens.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.example.myapplication55.ui.theme.BorderBlueGrey
import java.io.File
import com.example.myapplication55.ui.theme.ProfessionalBlue
import com.example.myapplication55.ui.theme.PureWhite
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(viewModel: ReportsViewModel, onOpenGallery: () -> Unit = {}) {
    val context = LocalContext.current
    val startDate by viewModel.startDate.collectAsState()
    val endDate by viewModel.endDate.collectAsState()
    val existingReports by viewModel.existingReports.collectAsState()
    val customers by viewModel.allCustomers.collectAsState(initial = emptyList())
    val stocks by viewModel.allStocks.collectAsState(initial = emptyList())
    val selectedCustomerId by viewModel.selectedCustomerId.collectAsState()
    val selectedStockId by viewModel.selectedStockId.collectAsState()

    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    var showDatePicker by remember { mutableStateOf(false) }
    var customerExpanded by remember { mutableStateOf(false) }
    var stockExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadExistingReports(context)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "REPORT ENGINE",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = ProfessionalBlue,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Filters Section
        Text("FILTERS", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // Date Range
            Box(modifier = Modifier.weight(1f).border(1.dp, BorderBlueGrey, RoundedCornerShape(8.dp)).clickable { showDatePicker = true }.padding(12.dp)) {
                Column {
                    Text("DATE RANGE", fontSize = 8.sp, color = Color.Gray)
                    Text("${dateFormat.format(Date(startDate))} - ${dateFormat.format(Date(endDate))}", fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                }
            }
            
            // Customer Filter
            Box(modifier = Modifier.weight(1f)) {
                Box(modifier = Modifier.fillMaxWidth().border(1.dp, BorderBlueGrey, RoundedCornerShape(8.dp)).clickable { customerExpanded = true }.padding(12.dp)) {
                    Column {
                        Text("CUSTOMER", fontSize = 8.sp, color = Color.Gray)
                        Text(customers.find { it.id == selectedCustomerId }?.name ?: "All Customers", fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                }
                DropdownMenu(expanded = customerExpanded, onDismissRequest = { customerExpanded = false }) {
                    DropdownMenuItem(text = { Text("All Customers") }, onClick = { viewModel.setSelectedCustomer(null); customerExpanded = false })
                    customers.forEach { customer ->
                        DropdownMenuItem(text = { Text(customer.name) }, onClick = { viewModel.setSelectedCustomer(customer.id); customerExpanded = false })
                    }
                }
            }

            // Stockholder Filter
            Box(modifier = Modifier.weight(1f)) {
                Box(modifier = Modifier.fillMaxWidth().border(1.dp, BorderBlueGrey, RoundedCornerShape(8.dp)).clickable { stockExpanded = true }.padding(12.dp)) {
                    Column {
                        Text("STOCKHOLDER", fontSize = 8.sp, color = Color.Gray)
                        Text(stocks.find { it.id == selectedStockId }?.origin ?: "All Origins", fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                }
                DropdownMenu(expanded = stockExpanded, onDismissRequest = { stockExpanded = false }) {
                    DropdownMenuItem(text = { Text("All Origins") }, onClick = { viewModel.setSelectedStock(null); stockExpanded = false })
                    stocks.forEach { stock ->
                        DropdownMenuItem(text = { Text(stock.origin) }, onClick = { viewModel.setSelectedStock(stock.id); stockExpanded = false })
                    }
                }
            }
        }

        ReportCategory(
            title = "FINANCIAL REPORTS",
            options = listOf(
                ReportOptionItem("Customer Ledger (PDF)", Icons.Default.Description) { viewModel.exportCustomerLedgerPdf(context) },
                ReportOptionItem("Daily Cash Flow (PDF)", Icons.Default.Description) { viewModel.exportDailyCashFlowPdf(context) },
                ReportOptionItem("Grand Business Summary (PDF)", Icons.Default.Description) { viewModel.exportGrandSummaryPdf(context) }
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        ReportCategory(
            title = "INVENTORY REPORTS",
            options = listOf(
                ReportOptionItem("Current Stock Summary (PDF)", Icons.Default.Description) { viewModel.exportStockSummaryPdf(context) },
                ReportOptionItem("Stock Movement Log (Excel)", Icons.Default.TableChart) { viewModel.exportStockMovementExcel(context) }
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { onOpenGallery() },
            modifier = Modifier.fillMaxWidth().height(50.dp).border(1.dp, ProfessionalBlue, RoundedCornerShape(12.dp)),
            colors = ButtonDefaults.buttonColors(containerColor = PureWhite, contentColor = ProfessionalBlue),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.TableChart, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("OPEN REPORT GALLERY", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "PDF & EXCEL ARCHIVE",
            fontSize = 12.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            existingReports.forEach { file ->
                HistoryItem(
                    file = file,
                    onShare = { viewModel.sharePdf(context, file) },
                    onDelete = { viewModel.deletePdf(context, file) }
                )
            }
            if (existingReports.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text("No saved reports found.", color = Color.Gray, fontSize = 12.sp)
                }
            }
        }
    }

    if (showDatePicker) {
        val dateRangePickerState = rememberDateRangePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val start = dateRangePickerState.selectedStartDateMillis
                    val end = dateRangePickerState.selectedEndDateMillis
                    if (start != null && end != null) {
                        viewModel.updateDateRange(start, end)
                    }
                    showDatePicker = false
                }) {
                    Text("OK", color = ProfessionalBlue)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("CANCEL", color = Color.Gray)
                }
            },
            colors = DatePickerDefaults.colors(
                containerColor = PureWhite
            )
        ) {
            DateRangePicker(
                state = dateRangePickerState,
                title = { Text("SELECT RANGE", modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Bold) },
                headline = { Text("Filter Reports", modifier = Modifier.padding(16.dp)) },
                showModeToggle = false,
                colors = DatePickerDefaults.colors(
                    containerColor = PureWhite,
                    titleContentColor = ProfessionalBlue,
                    headlineContentColor = Color.Black,
                    weekdayContentColor = Color.Gray,
                    dayContentColor = Color.Black,
                    selectedDayContainerColor = ProfessionalBlue,
                    selectedDayContentColor = Color.White,
                    todayContentColor = ProfessionalBlue,
                    todayDateBorderColor = ProfessionalBlue
                )
            )
        }
    }
}

@Composable
fun DateDisplay(label: String, date: String) {
    Column {
        Text(label, color = Color.Gray, fontSize = 10.sp)
        Text(date, color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

@Composable
fun ReportCategory(title: String, options: List<ReportOptionItem>) {
    Column {
        Text(
            text = title,
            fontSize = 12.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        options.forEach { option ->
            ReportItem(option)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

data class ReportOptionItem(val name: String, val icon: ImageVector, val onClick: () -> Unit)

@Composable
fun HistoryItem(file: File, onShare: () -> Unit, onDelete: () -> Unit) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val context = LocalContext.current

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Confirmation") },
            text = { Text("Are you sure you want to delete this file?") },
            confirmButton = { Button(onClick = { onDelete(); showDeleteConfirm = false }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("YES") } },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("CANCEL") } }
        )
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderBlueGrey, RoundedCornerShape(12.dp))
            .clickable {
                try {
                    val uri = FileProvider.getUriForFile(context, "com.example.myapplication55.fileprovider", file)
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, if (file.extension == "pdf") "application/pdf" else "application/vnd.ms-excel")
                        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                } catch (e: Exception) {
                    android.widget.Toast.makeText(context, "Cannot open file: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                }
            },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = file.name,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Text(
                    text = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(file.lastModified())),
                    color = Color.Gray,
                    fontSize = 11.sp
                )
            }
            Row {
                IconButton(onClick = onShare) {
                    Icon(Icons.Default.Share, contentDescription = "Share", tint = ProfessionalBlue)
                }
                IconButton(onClick = { showDeleteConfirm = true }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun ReportItem(option: ReportOptionItem) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderBlueGrey, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(option.icon, contentDescription = null, tint = ProfessionalBlue)
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = option.name, color = Color.Black, fontWeight = FontWeight.Medium)
            }
            IconButton(onClick = option.onClick) {
                Icon(Icons.Default.FileDownload, contentDescription = "Download", tint = ProfessionalBlue)
            }
        }
    }
}

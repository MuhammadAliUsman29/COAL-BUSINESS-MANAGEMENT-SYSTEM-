package com.example.myapplication55.ui.screens.reports

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication55.data.repository.CmsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class ReportsViewModel(private val repository: CmsRepository) : ViewModel() {

    private val _startDate = MutableStateFlow(System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)) // 7 days ago
    val startDate: StateFlow<Long> = _startDate.asStateFlow()

    private val _endDate = MutableStateFlow(System.currentTimeMillis())
    val endDate: StateFlow<Long> = _endDate.asStateFlow()

    private val _existingReports = MutableStateFlow<List<File>>(emptyList())
    val existingReports: StateFlow<List<File>> = _existingReports.asStateFlow()

    val allCustomers = repository.allCustomers
    val allStocks = repository.allStocks

    private val _selectedCustomerId = MutableStateFlow<Long?>(null)
    val selectedCustomerId = _selectedCustomerId.asStateFlow()

    private val _selectedStockId = MutableStateFlow<Long?>(null)
    val selectedStockId = _selectedStockId.asStateFlow()

    fun setSelectedCustomer(id: Long?) { _selectedCustomerId.value = id }
    fun setSelectedStock(id: Long?) { _selectedStockId.value = id }

    fun loadExistingReports(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val directory = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            val files = directory?.listFiles { file -> file.extension == "pdf" }?.toList()?.sortedByDescending { it.lastModified() } ?: emptyList()
            _existingReports.value = files
        }
    }

    fun updateDateRange(start: Long, end: Long) {
        _startDate.value = start
        _endDate.value = end
    }

    fun exportCustomerLedgerPdf(context: Context) {
        viewModelScope.launch {
            var transactions = repository.getTransactionsInRange(_startDate.value, _endDate.value)
            
            // Apply Filters
            _selectedCustomerId.value?.let { cid ->
                transactions = transactions.filter { it.customerId == cid }
            }
            _selectedStockId.value?.let { sid ->
                transactions = transactions.filter { it.stockId == sid }
            }

            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 Size
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas
            val paint = Paint()
            val titlePaint = Paint().apply {
                textSize = 18f
                isFakeBoldText = true
            }

            val filterText = when {
                _selectedCustomerId.value != null -> "Customer: ${repository.getCustomerById(_selectedCustomerId.value!!)?.name}"
                _selectedStockId.value != null -> "Stock: ${repository.getStockById(_selectedStockId.value!!)?.origin}"
                else -> "All Entities"
            }

            canvas.drawText("Comprehensive Ledger Report", 20f, 40f, titlePaint)
            canvas.drawText("Filter: $filterText", 20f, 65f, paint)
            canvas.drawText("Range: ${formatDate(_startDate.value)} - ${formatDate(_endDate.value)}", 20f, 85f, paint)

            var yPos = 120f
            paint.textSize = 10f
            canvas.drawText("Date", 20f, yPos, paint)
            canvas.drawText("Entity", 120f, yPos, paint)
            canvas.drawText("Type", 250f, yPos, paint)
            canvas.drawText("Amount (Rs.)", 350f, yPos, paint)
            canvas.drawText("Balance", 450f, yPos, paint)

            yPos += 20f
            canvas.drawLine(20f, yPos, 575f, yPos, paint)
            yPos += 20f

            transactions.forEach { trans ->
                if (yPos > 800) {
                    // return@forEach // Simple limit for now
                }
                val customerName = trans.customerId?.let { repository.getCustomerById(it)?.name } ?: ""
                val stockOrigin = trans.stockId?.let { repository.getStockById(it)?.origin } ?: ""
                val entityName = if (customerName.isNotEmpty()) customerName else if (stockOrigin.isNotEmpty()) stockOrigin else "General"
                
                val dateTime = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(Date(trans.timestamp))
                
                canvas.drawText(dateTime, 20f, yPos, paint)
                canvas.drawText(entityName.take(20), 120f, yPos, paint)
                canvas.drawText(trans.type.name, 250f, yPos, paint)
                canvas.drawText(String.format(Locale.US, "%,.0f", trans.amount), 350f, yPos, paint)
                canvas.drawText(String.format(Locale.US, "%,.0f", trans.balanceAfter), 450f, yPos, paint)
                yPos += 20f
            }

            pdfDocument.finishPage(page)
            savePdf(context, pdfDocument, "FilteredLedger")
        }
    }

    private fun formatDate(timestamp: Long): String {
        return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(timestamp))
    }

    private suspend fun savePdf(context: Context, pdfDocument: PdfDocument, fileName: String) {
        withContext(Dispatchers.IO) {
            val directory = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            val file = File(directory, "${fileName}_${System.currentTimeMillis()}.pdf")
            try {
                pdfDocument.writeTo(FileOutputStream(file))
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "PDF Exported: ${file.name}", Toast.LENGTH_LONG).show()
                    loadExistingReports(context)
                    sharePdf(context, file)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Export Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } finally {
                pdfDocument.close()
            }
        }
    }

    fun sharePdf(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "com.example.myapplication55.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Report via"))
    }

    fun deletePdf(context: Context, file: File) {
        viewModelScope.launch(Dispatchers.IO) {
            if (file.exists()) {
                file.delete()
                loadExistingReports(context)
            }
        }
    }

    fun exportDailyCashFlowPdf(context: Context) {
        viewModelScope.launch {
            val startOfDay = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }.timeInMillis
            val endOfDay = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
            }.timeInMillis
            
            val transactions = repository.getTransactionsInRange(startOfDay, endOfDay)
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas
            val paint = Paint()
            val titlePaint = Paint().apply { textSize = 18f; isFakeBoldText = true }

            canvas.drawText("Daily Cash Flow Report", 20f, 40f, titlePaint)
            canvas.drawText("Date: ${formatDate(System.currentTimeMillis())}", 20f, 70f, paint)

            var yPos = 100f
            canvas.drawText("Time", 20f, yPos, paint)
            canvas.drawText("Customer", 100f, yPos, paint)
            canvas.drawText("Type", 250f, yPos, paint)
            canvas.drawText("Amount", 350f, yPos, paint)
            canvas.drawText("Note", 450f, yPos, paint)

            yPos += 20f
            canvas.drawLine(20f, yPos, 575f, yPos, paint)
            yPos += 20f

            transactions.forEach { trans ->
                if (yPos > 800) return@forEach
                val customerName = trans.customerId?.let { repository.getCustomerById(it)?.name } ?: "General"
                val time = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(trans.timestamp))
                canvas.drawText(time, 20f, yPos, paint)
                canvas.drawText(customerName, 100f, yPos, paint)
                canvas.drawText(trans.type.name, 250f, yPos, paint)
                canvas.drawText(String.format(Locale.US, "%.2f", trans.amount), 350f, yPos, paint)
                canvas.drawText(trans.note.take(15), 450f, yPos, paint)
                yPos += 20f
            }

            pdfDocument.finishPage(page)
            savePdf(context, pdfDocument, "DailyCashFlow")
        }
    }

    fun exportStockSummaryPdf(context: Context) {
        viewModelScope.launch {
            val stocks = repository.allStocks.first()
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas
            val titlePaint = Paint().apply { textSize = 18f; isFakeBoldText = true }
            val paint = Paint().apply { textSize = 12f }

            canvas.drawText("Stock Summary Report", 20f, 40f, titlePaint)
            canvas.drawText("Date: ${formatDate(System.currentTimeMillis())}", 20f, 70f, paint)

            var yPos = 100f
            canvas.drawText("Stockpile (Origin)", 20f, yPos, paint)
            canvas.drawText("Grade", 200f, yPos, paint)
            canvas.drawText("Quantity (KG)", 350f, yPos, paint)
            canvas.drawText("Moisture", 480f, yPos, paint)

            yPos += 20f
            canvas.drawLine(20f, yPos, 575f, yPos, paint)
            yPos += 20f

            stocks.forEach { stock ->
                if (yPos > 800) return@forEach
                canvas.drawText(stock.origin, 20f, yPos, paint)
                canvas.drawText(stock.qualityGrade, 200f, yPos, paint)
                canvas.drawText(String.format(Locale.US, "%.1f", stock.quantity), 350f, yPos, paint)
                canvas.drawText(stock.moistureLevel, 480f, yPos, paint)
                yPos += 20f
            }
            
            pdfDocument.finishPage(page)
            savePdf(context, pdfDocument, "StockSummary")
        }
    }

    fun exportGrandSummaryPdf(context: Context) {
        viewModelScope.launch {
            val transactions = repository.getTransactionsInRange(_startDate.value, _endDate.value)
            val purchases = repository.purchaseHistory.first().filter { 
                it.date_timestamp in _startDate.value.._endDate.value 
            }

            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas
            val paint = Paint()
            val titlePaint = Paint().apply { textSize = 18f; isFakeBoldText = true }

            canvas.drawText("Grand Business Summary", 20f, 40f, titlePaint)
            canvas.drawText("Range: ${formatDate(_startDate.value)} - ${formatDate(_endDate.value)}", 20f, 65f, paint)

            var yPos = 100f
            paint.isFakeBoldText = true
            canvas.drawText("SECTION 1: STOCK PROCUREMENT (PURCHASES)", 20f, yPos, paint)
            paint.isFakeBoldText = false
            yPos += 20f
            
            canvas.drawText("Date", 20f, yPos, paint)
            canvas.drawText("Item", 100f, yPos, paint)
            canvas.drawText("Qty", 250f, yPos, paint)
            canvas.drawText("Rate", 350f, yPos, paint)
            canvas.drawText("Total", 450f, yPos, paint)
            yPos += 15f
            canvas.drawLine(20f, yPos, 575f, yPos, paint)
            yPos += 20f

            purchases.forEach { p ->
                if (yPos > 400) return@forEach // Basic limit
                canvas.drawText(SimpleDateFormat("dd/MM", Locale.US).format(Date(p.date_timestamp)), 20f, yPos, paint)
                canvas.drawText(p.item_name.take(15), 100f, yPos, paint)
                canvas.drawText("${p.quantity} KG", 250f, yPos, paint)
                canvas.drawText(p.unit_price.toString(), 350f, yPos, paint)
                canvas.drawText(p.total_cost.toString(), 450f, yPos, paint)
                yPos += 15f
            }

            yPos += 30f
            paint.isFakeBoldText = true
            canvas.drawText("SECTION 2: SALES & CASH FLOW", 20f, yPos, paint)
            paint.isFakeBoldText = false
            yPos += 20f
            
            val totalSales = transactions.filter { it.type == com.example.myapplication55.data.local.entities.TransactionType.STOCK_OUT }.sumOf { it.amount }
            val totalPurchases = purchases.sumOf { it.total_cost }
            
            canvas.drawText("Total Procurement Cost: Rs. ${String.format(Locale.US, "%,.0f", totalPurchases)}", 20f, yPos, paint)
            yPos += 20f
            canvas.drawText("Total Sales Revenue: Rs. ${String.format(Locale.US, "%,.0f", totalSales)}", 20f, yPos, paint)
            yPos += 40f
            
            paint.textSize = 14f
            paint.isFakeBoldText = true
            val net = totalSales - totalPurchases
            canvas.drawText("Net Flow: Rs. ${String.format(Locale.US, "%,.0f", net)}", 20f, yPos, paint)

            pdfDocument.finishPage(page)
            savePdf(context, pdfDocument, "GrandSummary")
        }
    }

    fun exportStockMovementExcel(context: Context) {
        viewModelScope.launch {
            // Placeholder for Excel generation logic
            Toast.makeText(context, "Excel export feature coming soon", Toast.LENGTH_SHORT).show()
        }
    }

    class Factory(private val repository: CmsRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ReportsViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ReportsViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

package com.example.myapplication55.ui.screens.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.example.myapplication55.data.repository.CmsRepository
import com.example.myapplication55.data.local.entities.CoalStock
import com.example.myapplication55.data.local.entities.Transaction
import com.example.myapplication55.data.local.entities.PaymentStatus
import com.example.myapplication55.data.local.entities.TransactionType
import com.example.myapplication55.data.local.entities.Customer
import com.example.myapplication55.data.local.entities.PurchaseHistory
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class InventoryViewModel(private val repository: CmsRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val allCustomers = repository.allCustomers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allStocks: StateFlow<List<CoalStock>> = combine(
        repository.allStocks,
        _searchQuery
    ) { stocks, query ->
        if (query.isBlank()) stocks
        else stocks.filter { it.origin.contains(query, ignoreCase = true) || it.qualityGrade.contains(query, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val purchaseHistory = repository.purchaseHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun addStock(origin: String, moisture: String, quantity: Double, grade: String, capacity: Double) {
        viewModelScope.launch {
            val stock = CoalStock(
                origin = origin,
                moistureLevel = moisture,
                quantity = quantity,
                qualityGrade = grade,
                totalCapacity = capacity
            )
            repository.insertStock(stock)
        }
    }

    val errorMessage = mutableStateOf<String?>(null)

    fun logStockMovement(
        stock: CoalStock, 
        deltaQuantity: Double, 
        type: TransactionType, 
        pricePerUnit: Double = 0.0,
        customerId: Long? = null,
        imagePath: String? = null,
        paymentStatus: PaymentStatus = PaymentStatus.PAID
    ) {
        viewModelScope.launch {
            val totalAmount = deltaQuantity * pricePerUnit
            val transaction = Transaction(
                stockId = stock.id,
                customerId = customerId,
                quantity = deltaQuantity,
                amount = totalAmount,
                pricePerUnit = pricePerUnit,
                type = type,
                imagePath = imagePath,
                note = "${type.name} for ${stock.origin} coal",
                paymentStatus = paymentStatus
            )
            try {
                repository.addTransaction(transaction)
                
                // Requirement: If customer pays during sale, add to Cash History as "Credit Received"
                // REMOVED: This causes double charging if not handled carefully in addTransaction.
                // The addTransaction logic in CmsRepository already updates user balance and customer ledger.
                // Adding a second transaction here is redundant and causes the mismatch reported.
            } catch (e: Exception) {
                errorMessage.value = e.message
            }
        }
    }

    fun getStockHistory(stockId: Long): Flow<List<Transaction>> = repository.allTransactions.map { list ->
        list.filter { it.stockId == stockId }
    }

    fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.updateTransaction(transaction)
        }
    }

    fun clearError() {
        errorMessage.value = null
    }

    fun deleteStock(stock: CoalStock) {
        viewModelScope.launch {
            repository.deleteStock(stock)
        }
    }

    fun deletePurchase(purchase: PurchaseHistory) {
        viewModelScope.launch {
            repository.deletePurchase(purchase)
        }
    }

    class Factory(private val repository: CmsRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(InventoryViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return InventoryViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

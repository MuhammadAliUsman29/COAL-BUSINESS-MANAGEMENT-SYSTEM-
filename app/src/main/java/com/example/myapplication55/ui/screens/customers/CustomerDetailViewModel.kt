package com.example.myapplication55.ui.screens.customers

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication55.data.local.entities.Customer
import com.example.myapplication55.data.local.entities.Transaction
import com.example.myapplication55.data.local.entities.TransactionType
import com.example.myapplication55.data.repository.CmsRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CustomerDetailViewModel(
    private val repository: CmsRepository,
    private val customerId: Long
) : ViewModel() {

    private val _customer = MutableStateFlow<Customer?>(null)
    val customer: StateFlow<Customer?> = _customer.asStateFlow()

    private val _transactions = repository.getTransactionsByCustomer(customerId)
    private val _typeFilter = MutableStateFlow<TransactionType?>(null)
    private val _amountSort = MutableStateFlow(false) // false = date desc, true = amount desc
    private val _dateFilter = MutableStateFlow("All") // "All", "Last 7 Days", "This Month"

    val transactions: StateFlow<List<Transaction>> = combine(_transactions, _typeFilter, _amountSort, _dateFilter) { list, type, sort, dateFilter ->
        val now = System.currentTimeMillis()
        var filtered = if (type != null) list.filter { it.type == type } else list
        
        filtered = when (dateFilter) {
            "Last 7 Days" -> filtered.filter { it.timestamp >= now - (7 * 24 * 60 * 60 * 1000L) }
            "This Month" -> filtered.filter { 
                val cal = java.util.Calendar.getInstance()
                val currentMonth = cal.get(java.util.Calendar.MONTH)
                val currentYear = cal.get(java.util.Calendar.YEAR)
                cal.timeInMillis = it.timestamp
                cal.get(java.util.Calendar.MONTH) == currentMonth && cal.get(java.util.Calendar.YEAR) == currentYear
            }
            else -> filtered
        }

        if (sort) filtered.sortedByDescending { it.amount } else filtered.sortedByDescending { it.timestamp }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setDateFilter(filter: String) {
        _dateFilter.value = filter
    }

    fun setTypeFilter(type: TransactionType?) {
        _typeFilter.value = type
    }

    fun toggleAmountSort() {
        _amountSort.value = !_amountSort.value
    }

    init {
        loadCustomer()
    }

    private fun loadCustomer() {
        viewModelScope.launch {
            repository.allCustomers.collect { customers ->
                _customer.value = customers.find { it.id == customerId }
            }
        }
    }

    val errorMessage = mutableStateOf<String?>(null)

    fun clearError() {
        errorMessage.value = null
    }

    fun addTransaction(amount: Double, type: TransactionType, note: String, pendingAmount: Double = 0.0, imagePath: String? = null) {
        viewModelScope.launch {
            val transaction = Transaction(
                customerId = customerId,
                amount = amount,
                pendingAmount = pendingAmount,
                type = type,
                note = note,
                imagePath = imagePath
            )
            try {
                repository.addTransaction(transaction)
                loadCustomer() // Refresh customer balance
            } catch (e: Exception) {
                errorMessage.value = e.message
            }
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            try {
                repository.deleteTransaction(transaction)
                loadCustomer()
            } catch (e: Exception) {
                errorMessage.value = e.message
            }
        }
    }

    fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch {
            try {
                repository.updateTransaction(transaction)
            } catch (e: Exception) {
                errorMessage.value = e.message
            }
        }
    }

    fun markAsPaid(transactionId: Long, receiptPath: String?) {
        viewModelScope.launch {
            try {
                repository.markTransactionAsPaid(transactionId, receiptPath)
                loadCustomer()
            } catch (e: Exception) {
                errorMessage.value = e.message
            }
        }
    }

    class Factory(private val repository: CmsRepository, private val customerId: Long) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CustomerDetailViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return CustomerDetailViewModel(repository, customerId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

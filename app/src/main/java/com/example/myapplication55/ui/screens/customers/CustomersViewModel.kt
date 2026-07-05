package com.example.myapplication55.ui.screens.customers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication55.data.local.entities.Customer
import com.example.myapplication55.data.repository.CmsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CustomersViewModel(private val repository: CmsRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val filteredCustomers: StateFlow<List<Customer>> = combine(
        repository.allCustomers,
        _searchQuery
    ) { customers, query ->
        if (query.isEmpty()) {
            customers
        } else {
            customers.filter {
                it.name.contains(query, ignoreCase = true) ||
                        it.phoneNumber.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun addCustomer(name: String, phone: String) {
        viewModelScope.launch {
            repository.insertCustomer(Customer(name = name, phoneNumber = phone))
        }
    }

    fun deleteCustomer(customerId: Long) {
        viewModelScope.launch {
            repository.getCustomerById(customerId)?.let {
                repository.deleteCustomer(it)
            }
        }
    }

    class Factory(private val repository: CmsRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CustomersViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return CustomersViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

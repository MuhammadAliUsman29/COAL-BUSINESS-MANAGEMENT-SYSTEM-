package com.example.myapplication55.ui.screens.profile

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication55.data.local.entities.User
import com.example.myapplication55.data.local.entities.ActivityLog
import com.example.myapplication55.data.repository.CmsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileViewModel(private val repository: CmsRepository) : ViewModel() {

    val currentUser: StateFlow<User?> = repository.currentUser
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allUsers: StateFlow<List<User>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activityLogs: StateFlow<List<ActivityLog>> = repository.allActivityLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getLogsByUser(userId: Long) = repository.getLogsByUser(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allStocks = repository.allStocks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val resetSuccess = mutableStateOf(false)
    val resetError = mutableStateOf<String?>(null)

    fun updateUser(fullName: String, mobile: String, profilePicPath: String?) {
        viewModelScope.launch {
            currentUser.value?.let { user ->
                repository.updateUser(user.copy(
                    fullName = fullName,
                    mobileNumber = mobile,
                    profilePicPath = profilePicPath ?: user.profilePicPath
                ))
                // Profile images should persist correctly now
            }
        }
    }

    fun masterReset(password: String) {
        viewModelScope.launch {
            val user = repository.getUser()
            if (user != null && user.passwordHash == password) {
                repository.masterReset()
                resetSuccess.value = true
                resetError.value = null
            } else {
                resetError.value = "Incorrect Password"
            }
        }
    }

    fun resetCustomerData(customerId: Long, password: String) {
        viewModelScope.launch {
            val user = repository.getUser()
            if (user != null && user.passwordHash == password) {
                repository.resetCustomerData(customerId)
                resetSuccess.value = true
                resetError.value = null
            } else {
                resetError.value = "Incorrect Password"
            }
        }
    }

    fun changePassword(currentPw: String, newPw: String) {
        viewModelScope.launch {
            val user = repository.getUser()
            if (user != null && user.passwordHash == currentPw) {
                repository.updateUser(user.copy(passwordHash = newPw))
                resetSuccess.value = true
                resetError.value = null
            } else {
                resetError.value = "Incorrect Current Password"
            }
        }
    }

    fun clearStatus() {
        resetSuccess.value = false
        resetError.value = null
    }

    class Factory(private val repository: CmsRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ProfileViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

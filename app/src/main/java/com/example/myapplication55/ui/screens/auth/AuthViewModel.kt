package com.example.myapplication55.ui.screens.auth

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication55.data.local.entities.User
import com.example.myapplication55.data.repository.CmsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: CmsRepository) : ViewModel() {

    val currentUser: StateFlow<User?> = repository.currentUser
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val isAuthenticated = mutableStateOf(false)
    val loginError = mutableStateOf<String?>(null)

    fun signUp(fullName: String, cnic: String, mobile: String, password: String, profilePicPath: String?) {
        viewModelScope.launch {
            // Check if user already exists
            val existingUser = repository.getUserByCnic(cnic)
            if (existingUser != null) {
                loginError.value = "Account already exists with this CNIC. Please use a different one."
                return@launch
            }

            val user = User(
                fullName = fullName,
                cnic = cnic,
                mobileNumber = mobile,
                passwordHash = password, // In a real app, hash this
                profilePicPath = profilePicPath
            )
            val id = repository.saveUser(user)
            repository.setCurrentUserId(id)
            isAuthenticated.value = true
            loginError.value = null
        }
    }

    fun login(cnic: String, password: String) {
        viewModelScope.launch {
            val user = repository.getUserByCnic(cnic)
            if (user != null && user.passwordHash == password) {
                repository.setCurrentUserId(user.id)
                isAuthenticated.value = true
                loginError.value = null
            } else {
                loginError.value = "Invalid CNIC or Password"
            }
        }
    }

    fun logout() {
        repository.setCurrentUserId(null)
        isAuthenticated.value = false
    }

    fun resetPassword(cnic: String, mobile: String, newPassword: String) {
        viewModelScope.launch {
            val user = repository.getUserByCnic(cnic)
            if (user != null && user.mobileNumber == mobile) {
                repository.updateUser(user.copy(passwordHash = newPassword))
                loginError.value = "Password reset successful. Please login."
            } else {
                loginError.value = "Account recovery failed. Invalid details."
            }
        }
    }

    fun checkAuth() {
        viewModelScope.launch {
            val user = repository.getUser()
            if (user == null) {
                // Stay on onboarding/signup
            }
        }
    }

    class Factory(private val repository: CmsRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return AuthViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

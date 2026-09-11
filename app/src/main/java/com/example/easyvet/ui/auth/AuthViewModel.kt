package com.example.easyvet.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.easyvet.EasyVetApplication
import com.example.easyvet.data.local.UserSessionManager
import com.example.easyvet.data.local.entity.UserEntity
import com.example.easyvet.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoginMode: Boolean = true,
    val email: String = "",
    val password: String = "",
    val fullName: String = "",
    val phone: String = "",
    val selectedRole: String = "Veterinary Officer",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val currentUser: UserEntity? = null
)

class AuthViewModel(
    private val userRepository: UserRepository,
    private val sessionManager: UserSessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        // Restore session on app launch so user stays logged in
        viewModelScope.launch {
            val savedEmail = sessionManager.getSavedUserEmail()
            if (!savedEmail.isNullOrBlank()) {
                val savedUser = userRepository.getUserByEmail(savedEmail)
                if (savedUser != null) {
                    _uiState.value = _uiState.value.copy(currentUser = savedUser)
                }
            }
        }
    }

    fun toggleAuthMode() {
        _uiState.value = _uiState.value.copy(
            isLoginMode = !_uiState.value.isLoginMode,
            errorMessage = null,
            successMessage = null
        )
    }

    fun onEmailChange(email: String) {
        _uiState.value = _uiState.value.copy(email = email, errorMessage = null)
    }

    fun onPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(password = password, errorMessage = null)
    }

    fun onFullNameChange(name: String) {
        _uiState.value = _uiState.value.copy(fullName = name, errorMessage = null)
    }

    fun onPhoneChange(phone: String) {
        _uiState.value = _uiState.value.copy(phone = phone, errorMessage = null)
    }

    fun onRoleChange(role: String) {
        _uiState.value = _uiState.value.copy(selectedRole = role)
    }

    fun fillDemoCredentials(isVet: Boolean) {
        if (isVet) {
            _uiState.value = _uiState.value.copy(
                isLoginMode = true,
                email = "vet@easyvet.com",
                password = "password123",
                errorMessage = null
            )
        } else {
            _uiState.value = _uiState.value.copy(
                isLoginMode = true,
                email = "farmer@easyvet.com",
                password = "password123",
                errorMessage = null
            )
        }
    }

    fun login() {
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password.trim()

        if (email.isBlank() || password.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please enter both email and password")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val user = userRepository.login(email, password)
            if (user != null) {
                sessionManager.saveUserEmail(user.email)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    currentUser = user,
                    successMessage = "Welcome back, ${user.fullName}!"
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Invalid email or password. Click 'Demo Login' below or Register."
                )
            }
        }
    }

    fun register() {
        val fullName = _uiState.value.fullName.trim()
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password.trim()
        val phone = _uiState.value.phone.trim()
        val role = _uiState.value.selectedRole

        if (fullName.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Full name is required")
            return
        }
        if (email.isBlank() || !email.contains("@")) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please enter a valid email address")
            return
        }
        if (password.length < 6) {
            _uiState.value = _uiState.value.copy(errorMessage = "Password must be at least 6 characters long")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val newUser = UserEntity(
                fullName = fullName,
                email = email,
                passwordHash = password,
                phone = phone,
                role = role
            )
            val success = userRepository.register(newUser)
            if (success) {
                sessionManager.saveUserEmail(newUser.email)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    currentUser = newUser,
                    successMessage = "Registration successful! Welcome to EasyVet."
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Account with this email already exists. Please login instead."
                )
            }
        }
    }

    fun logout() {
        sessionManager.clearSession()
        _uiState.value = AuthUiState(isLoginMode = true)
    }

    class Factory(private val app: EasyVetApplication) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AuthViewModel(app.userRepository, app.userSessionManager) as T
        }
    }
}

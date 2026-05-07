package com.memorial.app.ui.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.memorial.app.data.local.TokenManager
import com.memorial.app.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AuthRepository(TokenManager(application))

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState

    fun signInWithGoogle(idToken: String) {
        if (idToken.isBlank()) {
            _uiState.value = LoginUiState.Error("Google Sign-In did not return a valid token.")
            return
        }

        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            val result = repository.loginWithGoogle(idToken)
            if (result.isSuccess) {
                _uiState.value = LoginUiState.Success
            } else {
                _uiState.value = LoginUiState.Error(
                    result.exceptionOrNull()?.message ?: "Login failed"
                )
            }
        }
    }

    fun showGoogleSignInError(message: String) {
        _uiState.value = LoginUiState.Error(message)
    }

    fun devSkipLogin() {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            repository.devSetMockToken(getApplication())
            _uiState.value = LoginUiState.Success
        }
    }

    sealed class LoginUiState {
        data object Idle : LoginUiState()
        data object Loading : LoginUiState()
        data object Success : LoginUiState()
        data class Error(val message: String) : LoginUiState()
    }
}

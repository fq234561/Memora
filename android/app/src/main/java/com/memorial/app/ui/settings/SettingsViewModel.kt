package com.memorial.app.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.memorial.app.data.local.TokenManager
import com.memorial.app.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AuthRepository(TokenManager(application))
    private val tokenManager = TokenManager(application)

    private val _userEmail = MutableStateFlow(tokenManager.userEmail ?: "")
    val userEmail: StateFlow<String> = _userEmail

    private val _signOutEvent = MutableStateFlow(false)
    val signOutEvent: StateFlow<Boolean> = _signOutEvent

    fun deleteAllData() {
        // TODO: Implement real data deletion via API
    }

    fun signOut() {
        viewModelScope.launch {
            repository.logout(getApplication())
            _signOutEvent.value = true
        }
    }

    fun onSignOutEventConsumed() {
        _signOutEvent.value = false
    }
}

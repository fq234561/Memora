package com.memorial.app.ui.settings

import androidx.lifecycle.ViewModel
import com.memorial.app.data.mock.MockData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SettingsViewModel : ViewModel() {

    private val _userEmail = MutableStateFlow(MockData.mockUser.email)
    val userEmail: StateFlow<String> = _userEmail

    fun deleteAllData() {
        // Mock data deletion
    }

    fun signOut() {
        // Mock sign out
    }
}

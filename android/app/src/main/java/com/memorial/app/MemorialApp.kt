package com.memorial.app

import androidx.compose.runtime.Composable
import com.memorial.app.data.local.TokenManager
import com.memorial.app.data.remote.RetrofitClient
import com.memorial.app.navigation.AppNavigation
import com.memorial.app.navigation.Screen
import androidx.compose.ui.platform.LocalContext

@Composable
fun MemorialApp() {
    val context = LocalContext.current
    val tokenManager = TokenManager(context)
    
    // Dev auto-login: if not logged in, set mock token automatically
    if (!tokenManager.isLoggedIn) {
        tokenManager.accessToken = "mock_token_dev_auto"
        tokenManager.userId = "dev_user_001"
        tokenManager.userName = "Dev User"
        tokenManager.userEmail = "dev@example.com"
        RetrofitClient.initialize(context)
        RetrofitClient.setMockToken("mock_token_dev_auto")
    }
    
    AppNavigation(startDestination = Screen.Home.route)
}

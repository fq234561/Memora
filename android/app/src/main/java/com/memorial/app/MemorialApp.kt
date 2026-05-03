package com.memorial.app

import android.app.Application
import androidx.compose.runtime.Composable
import coil.ImageLoader
import coil.disk.DiskCache
import com.memorial.app.data.local.TokenManager
import com.memorial.app.data.remote.RetrofitClient
import com.memorial.app.navigation.AppNavigation
import com.memorial.app.navigation.Screen
import androidx.compose.ui.platform.LocalContext
import coil.compose.LocalImageLoader

@Composable
fun MemorialApp() {
    val context = LocalContext.current
    val tokenManager = TokenManager(context)

    // Dev auto-login: only in debug builds
    if (BuildConfig.ENABLE_MOCK_AUTH && !tokenManager.isLoggedIn) {
        tokenManager.accessToken = "mock_token_dev_auto"
        tokenManager.userId = "dev_user_001"
        tokenManager.userName = "Dev User"
        tokenManager.userEmail = "dev@example.com"
        RetrofitClient.initialize(context)
        RetrofitClient.setMockToken("mock_token_dev_auto")
    }

    // Configure Coil to use authenticated OkHttp client for private image access
    val imageLoader = ImageLoader.Builder(context)
        .okHttpClient {
            RetrofitClient.getAuthenticatedClient()
        }
        .diskCache {
            DiskCache.Builder()
                .directory(context.cacheDir.resolve("image_cache"))
                .build()
        }
        .build()

    val startDestination = if (tokenManager.isLoggedIn) {
        Screen.Home.route
    } else {
        Screen.Login.route
    }

    @Suppress("DEPRECATION")
    androidx.compose.runtime.CompositionLocalProvider(
        LocalImageLoader provides imageLoader
    ) {
        AppNavigation(startDestination = startDestination)
    }
}

package com.memorial.app.data.repository

import com.memorial.app.data.local.TokenManager
import com.memorial.app.data.remote.RetrofitClient
import com.memorial.app.data.remote.dto.AuthResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository(private val tokenManager: TokenManager) {

    suspend fun loginWithGoogle(idToken: String): Result<AuthResponse> = withContext(Dispatchers.IO) {
        try {
            val response = RetrofitClient.apiService.loginWithGoogle(
                mapOf("idToken" to idToken)
            )
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.data != null) {
                    tokenManager.accessToken = body.data.accessToken
                    tokenManager.userId = body.data.user.id
                    tokenManager.userName = body.data.user.name
                    tokenManager.userEmail = body.data.user.email
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body?.error ?: "Login failed"))
                }
            } else {
                Result.failure(Exception("Login failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun isLoggedIn(): Boolean = tokenManager.isLoggedIn

    suspend fun logout(context: android.content.Context) {
        tokenManager.clear()
        try {
            val credentialManager = androidx.credentials.CredentialManager.create(context)
            credentialManager.clearCredentialState(
                androidx.credentials.ClearCredentialStateRequest()
            )
        } catch (_: Exception) {
            // Ignore errors from credential clearing
        }
    }

    fun getUserName(): String = tokenManager.userName ?: "User"

    fun devSetMockToken(context: android.content.Context) {
        tokenManager.accessToken = "mock_token_dev_skip"
        tokenManager.userId = "dev_user_001"
        tokenManager.userName = "Dev User"
        tokenManager.userEmail = "dev@example.com"
        // Also initialize RetrofitClient with the same token manager so API calls are authenticated
        com.memorial.app.data.remote.RetrofitClient.initialize(context)
        com.memorial.app.data.remote.RetrofitClient.setMockToken("mock_token_dev_skip")
    }
}

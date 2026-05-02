package com.memorial.app.data.repository

import android.content.Context
import android.net.Uri
import com.memorial.app.data.model.PhotoStyle
import com.memorial.app.data.remote.RetrofitClient
import com.memorial.app.data.remote.dto.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

class ProjectRepository {

    suspend fun getProjects(): Result<List<ProjectDto>> = withContext(Dispatchers.IO) {
        try {
            val response = RetrofitClient.apiService.getProjects()
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true) {
                    Result.success(body.data ?: emptyList())
                } else {
                    Result.failure(Exception(body?.error ?: "Failed to load projects"))
                }
            } else {
                Result.failure(Exception("Failed to load projects: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProject(projectId: String): Result<ProjectDto> = withContext(Dispatchers.IO) {
        try {
            val response = RetrofitClient.apiService.getProject(projectId)
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body?.error ?: "Project not found"))
                }
            } else {
                Result.failure(Exception("Failed to load project: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createProject(title: String, style: PhotoStyle): Result<ProjectDto> = withContext(Dispatchers.IO) {
        try {
            val response = RetrofitClient.apiService.createProject(
                CreateProjectRequest(title, style.name)
            )
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body?.error ?: "Failed to create project"))
                }
            } else {
                Result.failure(Exception("Failed to create project: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadPhotoFile(
        projectId: String,
        type: String,
        uri: Uri,
        context: Context
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Copy URI content to temporary file
            val tempFile = File(context.cacheDir, "upload_${type}_${System.currentTimeMillis()}.jpg")
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            }

            val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
            val requestFile = tempFile.asRequestBody(mimeType.toMediaTypeOrNull())
            val photoPart = MultipartBody.Part.createFormData("photo", tempFile.name, requestFile)
            val typeBody = type.toRequestBody("text/plain".toMediaTypeOrNull())

            val response = RetrofitClient.apiService.uploadPhoto(projectId, photoPart, typeBody)

            // Clean up temp file
            tempFile.delete()

            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.data != null) {
                    Result.success(body.data.url)
                } else {
                    Result.failure(Exception(body?.error ?: "Failed to upload photo"))
                }
            } else {
                Result.failure(Exception("Failed to upload photo: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun generatePhoto(projectId: String, customPrompt: String? = null): Result<ProjectDto> = withContext(Dispatchers.IO) {
        try {
            val body = if (customPrompt != null) mapOf("customPrompt" to customPrompt) else null
            val response = RetrofitClient.apiService.generatePhoto(projectId, body)
            if (response.isSuccessful) {
                val respBody = response.body()
                if (respBody?.success == true && respBody.data != null) {
                    Result.success(respBody.data)
                } else {
                    Result.failure(Exception(respBody?.error ?: "Failed to start generation"))
                }
            } else {
                Result.failure(Exception("Failed to start generation: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getStatus(projectId: String): Result<StatusResponse> = withContext(Dispatchers.IO) {
        try {
            val response = RetrofitClient.apiService.getStatus(projectId)
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body?.error ?: "Failed to get status"))
                }
            } else {
                Result.failure(Exception("Failed to get status: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun giveConsent(projectId: String): Result<ProjectDto> = withContext(Dispatchers.IO) {
        try {
            val response = RetrofitClient.apiService.giveConsent(projectId)
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body?.error ?: "Failed to record consent"))
                }
            } else {
                Result.failure(Exception("Failed to record consent: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteProject(projectId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = RetrofitClient.apiService.deleteProject(projectId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to delete project: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createPurchase(projectId: String, productId: String, purchaseToken: String): Result<com.memorial.app.data.remote.dto.PurchaseDto> = withContext(Dispatchers.IO) {
        try {
            val response = RetrofitClient.apiService.createPurchase(
                com.memorial.app.data.remote.dto.PurchaseRequest(projectId, productId, purchaseToken)
            )
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body?.error ?: "Failed to create purchase"))
                }
            } else {
                Result.failure(Exception("Failed to create purchase: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun verifyPurchase(purchaseId: String): Result<com.memorial.app.data.remote.dto.PurchaseDto> = withContext(Dispatchers.IO) {
        try {
            val response = RetrofitClient.apiService.verifyPurchase(
                mapOf("purchaseId" to purchaseId)
            )
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body?.error ?: "Failed to verify purchase"))
                }
            } else {
                Result.failure(Exception("Failed to verify purchase: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun optimizePrompt(request: com.memorial.app.data.remote.dto.PromptOptimizeRequest): Result<com.memorial.app.data.remote.dto.OptimizedPromptResult> = withContext(Dispatchers.IO) {
        try {
            val response = RetrofitClient.apiService.optimizePrompt(request)
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body?.error ?: "Failed to optimize prompt"))
                }
            } else {
                Result.failure(Exception("Failed to optimize prompt: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

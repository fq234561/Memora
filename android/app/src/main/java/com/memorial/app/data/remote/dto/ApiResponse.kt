package com.memorial.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ApiResponse<T>(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: T? = null,
    @SerializedName("error") val error: String? = null,
    @SerializedName("message") val message: String? = null
)

data class AuthResponse(
    @SerializedName("user") val user: UserDto,
    @SerializedName("accessToken") val accessToken: String
)

data class UserDto(
    @SerializedName("id") val id: String,
    @SerializedName("email") val email: String,
    @SerializedName("name") val name: String,
    @SerializedName("avatarUrl") val avatarUrl: String? = null,
    @SerializedName("createdAt") val createdAt: String
)

data class ProjectDto(
    @SerializedName("id") val id: String,
    @SerializedName("userId") val userId: String,
    @SerializedName("title") val title: String,
    @SerializedName("style") val style: String,
    @SerializedName("deceasedPhotoUrl") val deceasedPhotoUrl: String? = null,
    @SerializedName("livingPhotoUrl") val livingPhotoUrl: String? = null,
    @SerializedName("generatedPhotoUrl") val generatedPhotoUrl: String? = null,
    @SerializedName("hdPhotoUrl") val hdPhotoUrl: String? = null,
    @SerializedName("status") val status: String,
    @SerializedName("consentGiven") val consentGiven: Boolean = false,
    @SerializedName("regenerationCount") val regenerationCount: Int = 0,
    @SerializedName("regenerationLimit") val regenerationLimit: Int = 0,
    @SerializedName("candidateUrls") val candidateUrls: List<String>? = null,
    @SerializedName("selectedCandidateIndex") val selectedCandidateIndex: Int? = null,
    @SerializedName("purchasedProductId") val purchasedProductId: String? = null,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String
)

data class CreateProjectRequest(
    @SerializedName("title") val title: String,
    @SerializedName("style") val style: String
)

data class UploadUrlRequest(
    @SerializedName("type") val type: String
)

data class UploadUrlResponse(
    @SerializedName("uploadUrl") val uploadUrl: String,
    @SerializedName("fileKey") val fileKey: String
)

data class ConfirmUploadRequest(
    @SerializedName("type") val type: String,
    @SerializedName("fileKey") val fileKey: String
)

data class StatusResponse(
    @SerializedName("status") val status: String,
    @SerializedName("progress") val progress: Int? = null,
    @SerializedName("resultUrl") val resultUrl: String? = null,
    @SerializedName("candidateUrls") val candidateUrls: List<String>? = null,
    @SerializedName("regenerationRemaining") val regenerationRemaining: Int? = null
)

data class PurchaseRequest(
    @SerializedName("projectId") val projectId: String,
    @SerializedName("productId") val productId: String,
    @SerializedName("purchaseToken") val purchaseToken: String
)

data class UploadResponse(
    @SerializedName("url") val url: String,
    @SerializedName("fileName") val fileName: String
)

data class PurchaseDto(
    @SerializedName("id") val id: String,
    @SerializedName("projectId") val projectId: String,
    @SerializedName("userId") val userId: String,
    @SerializedName("productId") val productId: String,
    @SerializedName("purchaseToken") val purchaseToken: String,
    @SerializedName("status") val status: String,
    @SerializedName("verifiedAt") val verifiedAt: String? = null,
    @SerializedName("createdAt") val createdAt: String
)

data class PromptOptimizeRequest(
    @SerializedName("userDescription") val userDescription: String? = null,
    @SerializedName("relationship") val relationship: String,
    @SerializedName("photoType") val photoType: String,
    @SerializedName("style") val style: String,
    @SerializedName("mood") val mood: String? = null,
    @SerializedName("compositionPrefs") val compositionPrefs: String? = null
)

data class OptimizedPromptResult(
    @SerializedName("optimizedPrompt") val optimizedPrompt: String,
    @SerializedName("negativePrompt") val negativePrompt: String,
    @SerializedName("stylePrompt") val stylePrompt: String,
    @SerializedName("safetyNotes") val safetyNotes: List<String>,
    @SerializedName("modelParams") val modelParams: ModelParams
)

data class ModelParams(
    @SerializedName("size") val size: String? = null,
    @SerializedName("quality") val quality: String? = null,
    @SerializedName("style") val style: String? = null
)

data class ContactRequest(
    @SerializedName("type") val type: String,
    @SerializedName("email") val email: String,
    @SerializedName("message") val message: String,
    @SerializedName("projectId") val projectId: String? = null
)

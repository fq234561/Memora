package com.memorial.app.data.remote

import com.memorial.app.data.remote.dto.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // Auth
    @POST("api/auth/google")
    suspend fun loginWithGoogle(@Body request: Map<String, String>): Response<ApiResponse<AuthResponse>>

    // Projects
    @GET("api/projects")
    suspend fun getProjects(@Query("year") year: Int? = null, @Query("month") month: Int? = null, @Query("activityType") activityType: String? = null, @Query("personType") personType: String? = null): Response<ApiResponse<List<ProjectDto>>>

    @POST("api/projects")
    suspend fun createProject(@Body request: CreateProjectRequest): Response<ApiResponse<ProjectDto>>

    @GET("api/projects/{id}")
    suspend fun getProject(@Path("id") id: String): Response<ApiResponse<ProjectDto>>

    @Multipart
    @POST("api/projects/{id}/upload")
    suspend fun uploadPhoto(
        @Path("id") id: String,
        @Part photo: MultipartBody.Part,
        @Part("type") type: RequestBody
    ): Response<ApiResponse<UploadProjectResponse>>

    @POST("api/projects/{id}/generate")
    suspend fun generatePhoto(
        @Path("id") id: String,
        @Body request: Map<String, String?>? = null
    ): Response<ApiResponse<ProjectDto>>

    @GET("api/projects/{id}/status")
    suspend fun getStatus(@Path("id") id: String): Response<ApiResponse<StatusResponse>>

    @POST("api/projects/{id}/consent")
    suspend fun giveConsent(@Path("id") id: String): Response<ApiResponse<ProjectDto>>

    @DELETE("api/projects/{id}")
    suspend fun deleteProject(@Path("id") id: String): Response<ApiResponse<Unit>>

    // Purchases
    @POST("api/purchases")
    suspend fun createPurchase(@Body request: PurchaseRequest): Response<ApiResponse<PurchaseDto>>

    @POST("api/purchases/verify")
    suspend fun verifyPurchase(@Body request: Map<String, String>): Response<ApiResponse<PurchaseDto>>

    @POST("api/projects/{id}/select-candidate")
    suspend fun selectCandidate(
        @Path("id") id: String,
        @Body request: Map<String, Int>
    ): Response<ApiResponse<ProjectDto>>

    // Prompts
    @POST("api/prompts/optimize")
    suspend fun optimizePrompt(@Body request: PromptOptimizeRequest): Response<ApiResponse<OptimizedPromptResult>>

    // Albums
    @GET("api/albums")
    suspend fun getAlbums(): Response<ApiResponse<List<AlbumDto>>>

    @POST("api/albums")
    suspend fun createAlbum(@Body request: CreateAlbumRequest): Response<ApiResponse<AlbumDto>>

    @GET("api/albums/{id}")
    suspend fun getAlbum(@Path("id") id: String): Response<ApiResponse<AlbumDto>>

    @POST("api/albums/{id}/render")
    suspend fun renderAlbum(@Path("id") id: String): Response<ApiResponse<AlbumDto>>

    @DELETE("api/albums/{id}")
    suspend fun deleteAlbum(@Path("id") id: String): Response<ApiResponse<Unit>>

    // Contact
    @POST("api/contact")
    suspend fun sendContact(@Body request: ContactRequest): Response<ApiResponse<Unit>>

    // Health
    @GET("api/health")
    suspend fun healthCheck(): Response<ApiResponse<Map<String, Any>>>
}

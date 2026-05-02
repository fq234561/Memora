package com.memorial.app.data.model

data class Project(
    val id: String,
    val title: String,
    val style: PhotoStyle,
    val status: ProjectStatus,
    val createdAt: String,
    val updatedAt: String,
    val generatedPhotoUrl: String? = null,
    val hdPhotoUrl: String? = null,
    val candidateUrls: List<String>? = null,
    val regenerationCount: Int = 0,
    val regenerationLimit: Int = 0,
    val purchasedProductId: String? = null
)

enum class PhotoStyle {
    NATURAL_FAMILY,
    VINTAGE_RESTORE,
    BIRTHDAY,
    GRADUATION_WEDDING_HOLIDAY
}

enum class ProjectStatus {
    DRAFT,
    UPLOADED,
    GENERATING,
    PREVIEW_QUEUED,
    PREVIEW_GENERATING,
    PREVIEW_READY,
    PAYMENT_PENDING,
    PAID,
    PURCHASED,
    FINAL_GENERATING,
    FINAL_READY,
    COMPLETED,
    FAILED,
    DELETED
}

data class Asset(
    val id: String,
    val projectId: String,
    val type: AssetType,
    val url: String?,
    val width: Int?,
    val height: Int?,
    val mimeType: String?,
    val sizeBytes: Long?,
    val isPaidAsset: Boolean
)

enum class AssetType {
    DECEASED_REFERENCE,
    LIVING_REFERENCE,
    PREVIEW_IMAGE,
    FINAL_IMAGE,
    FINAL_VIDEO,
    WATERMARK_PREVIEW
}

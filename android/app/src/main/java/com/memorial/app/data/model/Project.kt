package com.memorial.app.data.model

data class Project(
    val id: String,
    val title: String,
    val style: PhotoStyle,
    val status: ProjectStatus,
    val createdAt: String,
    val updatedAt: String
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
    PREVIEW_QUEUED,
    PREVIEW_GENERATING,
    PREVIEW_READY,
    PAYMENT_PENDING,
    PAID,
    FINAL_GENERATING,
    FINAL_READY,
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

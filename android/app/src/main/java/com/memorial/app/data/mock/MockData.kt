package com.memorial.app.data.mock

import com.memorial.app.data.model.Asset
import com.memorial.app.data.model.AssetType
import com.memorial.app.data.model.PhotoStyle
import com.memorial.app.data.model.Project
import com.memorial.app.data.model.ProjectStatus
import com.memorial.app.data.model.User

object MockData {

    val mockUser = User(
        id = "user_001",
        email = "demo@example.com",
        displayName = "Demo User",
        country = "US"
    )

    val mockProjects = listOf(
        Project(
            id = "project_001",
            title = "For Mom",
            style = PhotoStyle.NATURAL_FAMILY,
            status = ProjectStatus.FINAL_READY,
            createdAt = "2026-04-28T10:00:00Z",
            updatedAt = "2026-04-28T12:00:00Z"
        ),
        Project(
            id = "project_002",
            title = "Dad's Birthday",
            style = PhotoStyle.PARTY_GATHERING,
            status = ProjectStatus.PREVIEW_READY,
            createdAt = "2026-04-29T14:00:00Z",
            updatedAt = "2026-04-29T14:30:00Z"
        )
    )

    val mockAssets = listOf(
        Asset(
            id = "asset_001",
            projectId = "project_001",
            type = AssetType.BASE_REFERENCE,
            url = null,
            width = 1024,
            height = 1024,
            mimeType = "image/jpeg",
            sizeBytes = 2048000,
            isPaidAsset = false
        ),
        Asset(
            id = "asset_002",
            projectId = "project_001",
            type = AssetType.FINAL_IMAGE,
            url = "https://example.com/final_001.jpg",
            width = 2048,
            height = 2048,
            mimeType = "image/jpeg",
            sizeBytes = 4096000,
            isPaidAsset = true
        )
    )

    fun getProjectById(id: String): Project? = mockProjects.find { it.id == id }

    fun getAssetsForProject(projectId: String): List<Asset> =
        mockAssets.filter { it.projectId == projectId }
}

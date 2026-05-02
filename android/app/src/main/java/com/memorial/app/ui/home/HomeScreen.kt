package com.memorial.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.memorial.app.data.model.Project
import com.memorial.app.data.model.ProjectStatus

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun HomeScreen(
    onCreateProject: () -> Unit,
    onOpenProject: (String, ProjectStatus) -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val projects by viewModel.projects.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = { viewModel.refresh() }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Memorials") },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateProject) {
                Icon(Icons.Default.Add, contentDescription = "Create Project")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .pullRefresh(pullRefreshState)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Welcome, $userName",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )

                if (projects.isEmpty() && !isLoading) {
                    EmptyState(onCreateProject)
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(projects, key = { it.id }) { project ->
                            ProjectCard(
                                project = project,
                                onClick = { id, status -> onOpenProject(id, status) }
                            )
                        }
                    }
                }
            }

            PullRefreshIndicator(
                refreshing = isRefreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

@Composable
private fun EmptyState(onCreateProject: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "No memorial projects yet",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Tap + to create your first memorial photo",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ProjectCard(
    project: Project,
    onClick: (String, ProjectStatus) -> Unit
) {
    val statusInfo = statusDisplayInfo(project.status)
    val isClickable = project.status != ProjectStatus.GENERATING

    Card(
        onClick = { onClick(project.id, project.status) },
        enabled = isClickable,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail
            val thumbnailUrl = project.hdPhotoUrl
                ?: project.generatedPhotoUrl
                ?: project.candidateUrls?.firstOrNull()
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (thumbnailUrl != null) {
                    AsyncImage(
                        model = thumbnailUrl,
                        contentDescription = "Project thumbnail",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    androidx.compose.material3.Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "📷",
                                style = MaterialTheme.typography.titleLarge,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = project.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )

                StatusBadge(status = project.status, info = statusInfo)

                Text(
                    text = "Created: ${project.createdAt.take(10)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                val actionText = when (project.status) {
                    ProjectStatus.UPLOADED ->
                        if (project.purchasedProductId != null) "Generate Candidates →" else "Purchase Preview →"
                    ProjectStatus.PURCHASED -> "Unlock HD →"
                    else -> statusInfo.actionText
                }
                Text(
                    text = actionText,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isClickable) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }

            if (project.status == ProjectStatus.GENERATING) {
                androidx.compose.material3.CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
            }
        }
    }
}

@Composable
private fun StatusBadge(status: ProjectStatus, info: StatusInfo) {
    androidx.compose.material3.Surface(
        color = info.containerColor,
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = info.label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = info.contentColor,
            fontWeight = FontWeight.Medium
        )
    }
}

private data class StatusInfo(
    val label: String,
    val actionText: String,
    val containerColor: androidx.compose.ui.graphics.Color,
    val contentColor: androidx.compose.ui.graphics.Color
)

@Composable
private fun statusDisplayInfo(status: ProjectStatus): StatusInfo {
    val colors = MaterialTheme.colorScheme
    return when (status) {
        ProjectStatus.DRAFT -> StatusInfo(
            label = "Draft",
            actionText = "Continue Upload →",
            containerColor = colors.surfaceVariant,
            contentColor = colors.onSurfaceVariant
        )
        ProjectStatus.UPLOADED -> StatusInfo(
            label = "Uploaded",
            actionText = "Generate Preview →",
            containerColor = colors.primaryContainer,
            contentColor = colors.onPrimaryContainer
        )
        ProjectStatus.GENERATING -> StatusInfo(
            label = "Generating...",
            actionText = "Please wait",
            containerColor = colors.secondaryContainer,
            contentColor = colors.onSecondaryContainer
        )
        ProjectStatus.PREVIEW_READY -> StatusInfo(
            label = "Preview Ready",
            actionText = "View Preview →",
            containerColor = colors.tertiaryContainer,
            contentColor = colors.onTertiaryContainer
        )
        ProjectStatus.PURCHASED -> StatusInfo(
            label = "Purchase Pending",
            actionText = "Complete Purchase →",
            containerColor = colors.errorContainer,
            contentColor = colors.onErrorContainer
        )
        ProjectStatus.COMPLETED -> StatusInfo(
            label = "Completed",
            actionText = "Download →",
            containerColor = colors.primaryContainer,
            contentColor = colors.onPrimaryContainer
        )
        ProjectStatus.FAILED -> StatusInfo(
            label = "Failed",
            actionText = "Retry →",
            containerColor = colors.errorContainer,
            contentColor = colors.onErrorContainer
        )
        else -> StatusInfo(
            label = status.name.replace("_", " "),
            actionText = "Open →",
            containerColor = colors.surfaceVariant,
            contentColor = colors.onSurfaceVariant
        )
    }
}

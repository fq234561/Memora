package com.memorial.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.memorial.app.data.model.Project
import com.memorial.app.data.model.ProjectStatus
import com.memorial.app.ui.theme.BackgroundWarm
import com.memorial.app.ui.theme.CardSurface
import com.memorial.app.ui.theme.DividerLight
import com.memorial.app.ui.theme.PrimaryGreen
import com.memorial.app.ui.theme.PrimaryGreenDark
import com.memorial.app.ui.theme.PrimaryGreenLight
import com.memorial.app.ui.theme.StatusCompleted
import com.memorial.app.ui.theme.StatusCompletedBg
import com.memorial.app.ui.theme.StatusDraft
import com.memorial.app.ui.theme.StatusDraftBg
import com.memorial.app.ui.theme.StatusFailed
import com.memorial.app.ui.theme.StatusFailedBg
import com.memorial.app.ui.theme.StatusGenerating
import com.memorial.app.ui.theme.StatusGeneratingBg
import com.memorial.app.ui.theme.StatusPreview
import com.memorial.app.ui.theme.StatusPreviewBg
import com.memorial.app.ui.theme.StatusPurchased
import com.memorial.app.ui.theme.StatusPurchasedBg
import com.memorial.app.ui.theme.StatusUpload
import com.memorial.app.ui.theme.StatusUploadBg
import com.memorial.app.ui.theme.TextMuted
import com.memorial.app.ui.theme.TextPrimary
import com.memorial.app.ui.theme.TextSecondary

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HomeScreen(
    onCreateProject: () -> Unit,
    onOpenProject: (String, ProjectStatus) -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val projects by viewModel.projects.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val selectedYear by viewModel.selectedYear.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val selectedActivityFilter by viewModel.selectedActivityType.collectAsState()
    val selectedPersonFilter by viewModel.selectedPersonType.collectAsState()

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = { viewModel.refresh() }
    )

    Scaffold(
        containerColor = BackgroundWarm
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .pullRefresh(pullRefreshState)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // Brand Header
                item {
                    BrandHeader(onOpenSettings = onOpenSettings)
                }

                // Filter Bar
                item {
                    FilterBar(
                        selectedFilter = selectedFilter,
                        onFilterSelected = viewModel::onFilterSelected,
                        selectedYear = selectedYear,
                        selectedMonth = selectedMonth,
                        selectedActivityType = selectedActivityFilter,
                        selectedPersonType = selectedPersonFilter,
                        onYearSelected = viewModel::onYearSelected,
                        onMonthSelected = viewModel::onMonthSelected,
                        onActivityTypeSelected = viewModel::onActivityTypeFilter,
                        onPersonTypeSelected = viewModel::onPersonTypeFilter,
                        onApplyFilter = { viewModel.loadProjects() }
                    )
                }

                // Main CTA Card
                item {
                    CreateProjectCard(onClick = onCreateProject)
                }

                // Section Title
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .padding(top = 8.dp, bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "My Projects",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                        Text(
                            text = "${projects.size}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TextMuted
                            )
                        )
                    }
                }

                // Content
                val filterActive = selectedFilter != HomeViewModel.FilterType.ALL
                if (projects.isEmpty() && !isLoading) {
                    item {
                        EmptyState(onCreateProject = onCreateProject, filterActive = filterActive)
                    }
                } else {
                    items(projects, key = { it.id }) { project ->
                        ProjectCard(
                            project = project,
                            onClick = { id, status -> onOpenProject(id, status) }
                        )
                    }
                }

                // Loading indicator at bottom
                if (isLoading && projects.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                            .padding(48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = PrimaryGreen,
                                strokeWidth = 3.dp
                            )
                        }
                    }
                }
            }

            PullRefreshIndicator(
                refreshing = isRefreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                backgroundColor = CardSurface,
                contentColor = PrimaryGreen
            )

            // Floating Action Button (bottom right)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(PrimaryGreen, PrimaryGreenDark),
                                start = Offset(0f, 0f),
                                end = Offset(56f, 56f)
                            )
                        )
                        .clickable { onCreateProject() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Create Family Memory Photo",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun BrandHeader(
    onOpenSettings: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 16.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Memora",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = PrimaryGreen,
                    letterSpacing = (-0.5).sp
                )
            )
            Text(
                text = "Complete meaningful family photos from precious moments",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextMuted,
                    fontSize = 12.sp
                )
            )
        }

        IconButton(
            onClick = onOpenSettings,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFFF0EFF6))
        ) {
            Icon(
                imageVector = Icons.Outlined.Settings,
                contentDescription = "Settings",
                tint = TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun CreateProjectCard(
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 8.dp, bottom = 16.dp)
            .height(100.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(PrimaryGreen, PrimaryGreenDark),
                        start = Offset(0f, 0f),
                        end = Offset(300f, 100f)
                    )
                )
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Create a Family Memory Photo",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Text(
                        text = "Upload event photos and person references, AI helps you complete naturally",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ProjectCard(
    project: Project,
    onClick: (String, ProjectStatus) -> Unit
) {
    val isClickable = project.status != ProjectStatus.GENERATING
    val statusStyle = statusStyleFor(project.status)

    Card(
        onClick = { onClick(project.id, project.status) },
        enabled = isClickable,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 12.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                    .size(88.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF0EFF6)),
                contentAlignment = Alignment.Center
            ) {
                if (thumbnailUrl != null) {
                    AsyncImage(
                        model = thumbnailUrl,
                        contentDescription = project.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    PlaceholderImage()
                }

                // Generating overlay
                if (project.status == ProjectStatus.GENERATING) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.4f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Title + Status row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = project.title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    StatusChip(label = statusStyle.label, color = statusStyle.color, bgColor = statusStyle.bgColor)
                }

                // Date + Tags row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Created ${project.createdAt.take(10).replace("-", ".")}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    )
                    if (project.eventDate != null) {
                        Text(
                            text = "|",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = DividerLight,
                                fontSize = 11.sp
                            )
                        )
                        Text(
                            text = project.eventDate,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = PrimaryGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }

                // Tags chips
                if (project.activityType != null || !project.personTypes.isNullOrEmpty()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        project.activityType?.let {
                            TagChip(label = formatTagLabel(it))
                        }
                        project.personTypes?.forEach { personType ->
                            TagChip(label = formatTagLabel(personType))
                        }
                    }
                }

                // Action hint
                val actionText = when (project.status) {
                    ProjectStatus.DRAFT -> "Continue Uploading Photos"
                    ProjectStatus.UPLOADED ->
                        if (project.purchasedProductId != null) "Generate AI Photo Preview" else "Buy Preview Pack"
                    ProjectStatus.GENERATING -> "Generating..."
                    ProjectStatus.PREVIEW_READY -> "Choose Your Favorite Version"
                    ProjectStatus.PURCHASED -> "Unlock HD Photo"
                    ProjectStatus.COMPLETED -> "Download HD Photo"
                    ProjectStatus.FAILED -> "Regenerate"
                    else -> "View Details"
                }

                Text(
                    text = actionText,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = if (isClickable) PrimaryGreen else TextMuted,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun StatusChip(
    label: String,
    color: Color,
    bgColor: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = color,
                fontWeight = FontWeight.Medium,
                fontSize = 10.sp
            )
        )
    }
}

@Composable
private fun PlaceholderImage() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.size(36.dp)) {
            // Simple flower-like memorial icon
            val centerX = size.width / 2
            val centerY = size.height / 2
            val petalRadius = size.width * 0.18f
            val centerRadius = size.width * 0.12f

            // Petals
            for (i in 0..4) {
                val angle = (i * 72f - 90f) * (kotlin.math.PI / 180f).toFloat()
                val px = centerX + kotlin.math.cos(angle) * size.width * 0.22f
                val py = centerY + kotlin.math.sin(angle) * size.height * 0.22f
                drawCircle(
                    color = PrimaryGreenLight.copy(alpha = 0.3f),
                    radius = petalRadius,
                    center = Offset(px, py)
                )
            }
            // Center
            drawCircle(
                color = PrimaryGreen.copy(alpha = 0.5f),
                radius = centerRadius,
                center = Offset(centerX, centerY)
            )
        }
    }
}

@Composable
private fun EmptyState(
    onCreateProject: () -> Unit,
    filterActive: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 40.dp)
            .padding(top = 40.dp, bottom = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Memorial illustration
        Box(
            modifier = Modifier
                .size(160.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFFEDE9FE)),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val centerX = w / 2
                val centerY = h / 2

                // Soft frame
                drawRoundRect(
                    color = PrimaryGreenLight.copy(alpha = 0.2f),
                    size = androidx.compose.ui.geometry.Size(w * 0.6f, h * 0.55f),
                    topLeft = Offset(centerX - w * 0.3f, centerY - h * 0.25f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f)
                )

                // Photo placeholder inside frame
                drawRoundRect(
                    color = CardSurface.copy(alpha = 0.9f),
                    size = androidx.compose.ui.geometry.Size(w * 0.48f, h * 0.38f),
                    topLeft = Offset(centerX - w * 0.24f, centerY - h * 0.18f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
                )

                // Two person silhouettes
                drawCircle(
                    color = PrimaryGreen.copy(alpha = 0.15f),
                    radius = w * 0.1f,
                    center = Offset(centerX - w * 0.08f, centerY - h * 0.02f)
                )
                drawCircle(
                    color = PrimaryGreenDark.copy(alpha = 0.15f),
                    radius = w * 0.08f,
                    center = Offset(centerX + w * 0.08f, centerY + h * 0.02f)
                )

                // Hearts / sparkles
                val sparkleColor = PrimaryGreen.copy(alpha = 0.25f)
                drawCircle(
                    color = sparkleColor,
                    radius = w * 0.03f,
                    center = Offset(centerX + w * 0.22f, centerY - h * 0.18f)
                )
                drawCircle(
                    color = sparkleColor,
                    radius = w * 0.02f,
                    center = Offset(centerX + w * 0.28f, centerY - h * 0.12f)
                )
            }
        }

        Text(
            text = if (filterActive) "No family photos match your filters" else "No family photos yet",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
        )

        Text(
            text = "Upload event photos and person references,\nlet AI complete a family memory photo",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = TextSecondary,
                lineHeight = 22.sp
            ),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(PrimaryGreen, PrimaryGreenDark)
                    )
                )
                .clickable { onCreateProject() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Create Your First Family Photo",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            )
        }
    }
}

@Composable
private fun TagChip(label: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(PrimaryGreen.copy(alpha = 0.10f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = PrimaryGreen,
                fontWeight = FontWeight.Medium,
                fontSize = 9.sp
            )
        )
    }
}

private data class StatusStyle(
    val label: String,
    val color: Color,
    val bgColor: Color
)

private fun formatTagLabel(value: String): String {
    return value
        .lowercase()
        .split('_')
        .joinToString(" ") { segment ->
            segment.replaceFirstChar { char -> char.uppercaseChar() }
        }
}

private fun statusStyleFor(status: ProjectStatus): StatusStyle {
    return when (status) {
        ProjectStatus.DRAFT -> StatusStyle("Draft", StatusDraft, StatusDraftBg)
        ProjectStatus.UPLOADED -> StatusStyle("Pending", StatusUpload, StatusUploadBg)
        ProjectStatus.GENERATING -> StatusStyle("Generating", StatusGenerating, StatusGeneratingBg)
        ProjectStatus.PREVIEW_READY -> StatusStyle("Preview Ready", StatusPreview, StatusPreviewBg)
        ProjectStatus.PURCHASED -> StatusStyle("Unlocked", StatusPurchased, StatusPurchasedBg)
        ProjectStatus.COMPLETED -> StatusStyle("Completed", StatusCompleted, StatusCompletedBg)
        ProjectStatus.FAILED -> StatusStyle("Failed", StatusFailed, StatusFailedBg)
        else -> StatusStyle("Unknown", StatusDraft, StatusDraftBg)
    }
}

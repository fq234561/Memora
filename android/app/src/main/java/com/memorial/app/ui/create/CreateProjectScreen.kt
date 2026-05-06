package com.memorial.app.ui.create

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.memorial.app.data.model.ActivityType
import com.memorial.app.data.model.PersonType
import com.memorial.app.data.model.PhotoStyle
import com.memorial.app.ui.theme.BackgroundWarm
import com.memorial.app.ui.theme.CardSurface
import com.memorial.app.ui.theme.DividerLight
import com.memorial.app.ui.theme.PrimaryGreen
import com.memorial.app.ui.theme.PrimaryGreenLight
import com.memorial.app.ui.theme.TextMuted
import com.memorial.app.ui.theme.TextPrimary
import com.memorial.app.ui.theme.TextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateProjectScreen(
    onProjectCreated: (String) -> Unit,
    onCancel: () -> Unit,
    viewModel: CreateProjectViewModel = viewModel()
) {
    val title by viewModel.title.collectAsState()
    val selectedStyle by viewModel.selectedStyle.collectAsState()
    val eventDate by viewModel.eventDate.collectAsState()
    val selectedActivityType by viewModel.selectedActivityType.collectAsState()
    val selectedPersonTypes by viewModel.selectedPersonTypes.collectAsState()
    val isCreating by viewModel.isCreating.collectAsState()
    val error by viewModel.error.collectAsState()

    Scaffold(
        containerColor = BackgroundWarm
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Error message
            if (error != null) {
                Text(
                    text = error!!,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.error.copy(alpha = 0.08f),
                            RoundedCornerShape(10.dp)
                        )
                        .padding(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Header
            Text(
                text = "Create Family Memory",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )
            Text(
                text = "Complete family, travel, party, and milestone photos with AI",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextSecondary
                )
            )

            // Title input card
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Project Name",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = viewModel::onTitleChange,
                    placeholder = { Text("e.g. Family trip, class reunion") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryGreen,
                        unfocusedBorderColor = DividerLight
                    )
                )

                // Quick fill for dev
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { viewModel.onTitleChange("Test Family Memory") }
                        .padding(vertical = 4.dp)
                ) {
                    Text(
                        text = "Use Test Title (Dev)",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = PrimaryGreen,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }

            // Style selection
            Text(
                text = "Select Photo Style",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            )

            PhotoStyle.values().forEach { style ->
                StyleOptionCard(
                    style = style,
                    selected = style == selectedStyle,
                    onSelect = { viewModel.onStyleSelected(style) }
                )
            }

            // Event Date input
            Text(
                text = "Event Date",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            )
            OutlinedTextField(
                value = eventDate,
                onValueChange = viewModel::onEventDateChange,
                placeholder = { Text("YYYY-MM-DD (optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryGreen,
                    unfocusedBorderColor = DividerLight
                )
            )

            // Activity Type chips
            Text(
                text = "Activity Type",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            )
            FlowRow(horizontalArrangement = spacedBy(8.dp), verticalArrangement = spacedBy(8.dp)) {
                ActivityType.values().forEach { type ->
                    val selected = type == selectedActivityType
                    FilterChip(
                        selected = selected,
                        onClick = { viewModel.onActivitySelected(if (selected) null else type) },
                        label = { Text(type.label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryGreen.copy(alpha = 0.12f),
                            selectedLabelColor = PrimaryGreen
                        )
                    )
                }
            }

            // Person Type chips
            Text(
                text = "Person Types",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            )
            FlowRow(horizontalArrangement = spacedBy(8.dp), verticalArrangement = spacedBy(8.dp)) {
                PersonType.values().forEach { type ->
                    val selected = type in selectedPersonTypes
                    FilterChip(
                        selected = selected,
                        onClick = { viewModel.togglePersonType(type) },
                        label = { Text(type.label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryGreen.copy(alpha = 0.12f),
                            selectedLabelColor = PrimaryGreen
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (isCreating) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    color = PrimaryGreen
                )
            } else {
                Button(
                    onClick = { viewModel.createProject(onProjectCreated) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    enabled = title.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryGreen,
                        disabledContainerColor = DividerLight
                    )
                ) {
                    Text(
                        "Create Project",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }

                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = TextSecondary
                    )
                ) {
                    Text("Cancel")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun StyleOptionCard(
    style: PhotoStyle,
    selected: Boolean,
    onSelect: () -> Unit
) {
    val (title, description) = when (style) {
        PhotoStyle.NATURAL_FAMILY -> "Natural Family" to "Warm, natural family portrait style"
        PhotoStyle.TRAVEL_MEMORY -> "Travel Memory" to "Naturally blend loved ones into travel scenery"
        PhotoStyle.PARTY_GATHERING -> "Party Gathering" to "Birthday, parties and other relaxed, joyful scenes"
        PhotoStyle.HOLIDAY_CELEBRATION -> "Holiday Celebration" to "Holiday reunion, warm celebration atmosphere"
        PhotoStyle.MILESTONE_EVENT -> "Milestone Event" to "Weddings, graduations and other life milestones"
    }

    val bgColor = if (selected) PrimaryGreen.copy(alpha = 0.08f) else CardSurface

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .clickable { onSelect() }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = if (selected) PrimaryGreen else TextPrimary
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondary
                    )
                )
            }

            if (selected) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(PrimaryGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(DividerLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

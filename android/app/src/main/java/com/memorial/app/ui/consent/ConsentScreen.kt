package com.memorial.app.ui.consent

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.memorial.app.data.repository.ProjectRepository
import com.memorial.app.ui.theme.BackgroundWarm
import com.memorial.app.ui.theme.DividerLight
import com.memorial.app.ui.theme.PrimaryGreen
import com.memorial.app.ui.theme.TextMuted
import com.memorial.app.ui.theme.TextPrimary
import com.memorial.app.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@Composable
fun ConsentScreen(
    projectId: String,
    onConsentGiven: () -> Unit,
    onBack: () -> Unit,
    viewModel: ConsentViewModel = viewModel()
) {
    val hasRight by viewModel.hasRightToPhotos.collectAsState()
    val privateUse by viewModel.privateUseOnly.collectAsState()
    val understandAi by viewModel.understandAiGenerated.collectAsState()
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = BackgroundWarm
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Confirm Authorization",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    ),
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onBack() }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Back",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = TextSecondary
                        )
                    )
                }
            }

            Text(
                text = "Before generating a family memory photo, please confirm the following",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextSecondary
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            ConsentItemCard(
                checked = hasRight,
                onCheckedChange = { viewModel.toggleHasRightToPhotos() },
                title = "I have the right to use all uploaded photos",
                description = "I confirm the uploaded photos are taken by me or legally authorized for use."
            )

            ConsentItemCard(
                checked = privateUse,
                onCheckedChange = { viewModel.togglePrivateUseOnly() },
                title = "For Private Family Memory Only",
                description = "This photo is for personal/family memory use only, not for commercial purposes, and will not be presented as authentic historical photos."
            )

            ConsentItemCard(
                checked = understandAi,
                onCheckedChange = { viewModel.toggleUnderstandAiGenerated() },
                title = "I understand this is an AI-generated image",
                description = "I understand the final image is AI-generated and may not be fully realistic. I will use it in a respectful and appropriate manner."
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    scope.launch {
                        val repo = ProjectRepository()
                        repo.giveConsent(projectId)
                        onConsentGiven()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                enabled = hasRight && privateUse && understandAi,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryGreen,
                    disabledContainerColor = DividerLight
                )
            ) {
                Text(
                    "Confirm & Continue",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ConsentItemCard(
    checked: Boolean,
    onCheckedChange: () -> Unit,
    title: String,
    description: String
) {
    val bgColor = if (checked) PrimaryGreen.copy(alpha = 0.08f) else Color.White

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .clickable { onCheckedChange() }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            // Custom checkbox
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (checked) PrimaryGreen else DividerLight),
                contentAlignment = Alignment.Center
            ) {
                if (checked) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.size(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = if (checked) PrimaryGreen else TextPrimary
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondary
                    )
                )
            }
        }
    }
}

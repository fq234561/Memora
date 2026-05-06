package com.memorial.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.memorial.app.data.model.ActivityType
import com.memorial.app.data.model.PersonType
import com.memorial.app.ui.theme.CardSurface
import com.memorial.app.ui.theme.DividerLight
import com.memorial.app.ui.theme.PrimaryGreen
import com.memorial.app.ui.theme.TextMuted
import com.memorial.app.ui.theme.TextPrimary
import com.memorial.app.ui.theme.TextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FilterBar(
    selectedFilter: HomeViewModel.FilterType,
    onFilterSelected: (HomeViewModel.FilterType) -> Unit,
    selectedYear: Int?,
    selectedMonth: Int?,
    selectedActivityType: String?,
    selectedPersonType: String?,
    onYearSelected: (Int?) -> Unit,
    onMonthSelected: (Int?) -> Unit,
    onActivityTypeSelected: (String?) -> Unit,
    onPersonTypeSelected: (String?) -> Unit,
    onApplyFilter: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 8.dp, bottom = 8.dp)
    ) {
        // Filter type pills
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterPill(
                label = "ALL",
                selected = selectedFilter == HomeViewModel.FilterType.ALL,
                onClick = { onFilterSelected(HomeViewModel.FilterType.ALL) }
            )
            FilterPill(
                label = "Year/Month",
                selected = selectedFilter == HomeViewModel.FilterType.YEAR_MONTH,
                onClick = { onFilterSelected(HomeViewModel.FilterType.YEAR_MONTH) }
            )
            FilterPill(
                label = "Activity",
                selected = selectedFilter == HomeViewModel.FilterType.ACTIVITY,
                onClick = { onFilterSelected(HomeViewModel.FilterType.ACTIVITY) }
            )
            FilterPill(
                label = "Person",
                selected = selectedFilter == HomeViewModel.FilterType.PERSON,
                onClick = { onFilterSelected(HomeViewModel.FilterType.PERSON) }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Filter-specific controls
        when (selectedFilter) {
            HomeViewModel.FilterType.YEAR_MONTH -> {
                YearMonthFilters(
                    selectedYear = selectedYear,
                    selectedMonth = selectedMonth,
                    onYearSelected = onYearSelected,
                    onMonthSelected = onMonthSelected
                )
            }
            HomeViewModel.FilterType.ACTIVITY -> {
                ActivityTypeFilters(
                    selectedActivityType = selectedActivityType,
                    onActivityTypeSelected = onActivityTypeSelected
                )
            }
            HomeViewModel.FilterType.PERSON -> {
                PersonTypeFilters(
                    selectedPersonType = selectedPersonType,
                    onPersonTypeSelected = onPersonTypeSelected
                )
            }
            else -> { /* No extra filters for ALL */ }
        }

        // Apply button
        if (selectedFilter != HomeViewModel.FilterType.ALL) {
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onApplyFilter,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryGreen
                )
            ) {
                Text(
                    "Apply Filter",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = androidx.compose.ui.graphics.Color.White
                    )
                )
            }
        }
    }
}

@Composable
private fun FilterPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (selected) PrimaryGreen.copy(alpha = 0.12f) else CardSurface
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                color = if (selected) PrimaryGreen else TextSecondary
            )
        )
    }
}

@Composable
private fun YearMonthFilters(
    selectedYear: Int?,
    selectedMonth: Int?,
    onYearSelected: (Int?) -> Unit,
    onMonthSelected: (Int?) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Year dropdown
        var yearExpanded by remember { mutableStateOf(false) }
        Box {
            OutlinedButton(
                onClick = { yearExpanded = true },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(selectedYear?.toString() ?: "Select Year")
            }
            DropdownMenu(
                expanded = yearExpanded,
                onDismissRequest = { yearExpanded = false }
            ) {
                listOf(2024, 2025, 2026).forEach { year ->
                    DropdownMenuItem(
                        text = { Text(year.toString()) },
                        onClick = {
                            onYearSelected(year)
                            yearExpanded = false
                        },
                        trailingIcon = if (selectedYear == year) {
                            { Icon(Icons.Default.Check, contentDescription = null, tint = PrimaryGreen) }
                        } else null
                    )
                }
            }
        }

        // Month dropdown
        var monthExpanded by remember { mutableStateOf(false) }
        Box {
            OutlinedButton(
                onClick = { monthExpanded = true },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(selectedMonth?.let { String.format("%02d", it) } ?: "Select Month")
            }
            DropdownMenu(
                expanded = monthExpanded,
                onDismissRequest = { monthExpanded = false }
            ) {
                (1..12).forEach { month ->
                    DropdownMenuItem(
                        text = { Text(String.format("%02d", month)) },
                        onClick = {
                            onMonthSelected(month)
                            monthExpanded = false
                        },
                        trailingIcon = if (selectedMonth == month) {
                            { Icon(Icons.Default.Check, contentDescription = null, tint = PrimaryGreen) }
                        } else null
                    )
                }
            }
        }

        // Clear button
        if (selectedYear != null || selectedMonth != null) {
            Text(
                text = "Clear",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = PrimaryGreen,
                    fontWeight = FontWeight.Medium
                ),
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickable {
                        onYearSelected(null)
                        onMonthSelected(null)
                    }
                    .padding(4.dp)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ActivityTypeFilters(
    selectedActivityType: String?,
    onActivityTypeSelected: (String?) -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        ActivityType.values().forEach { type ->
            val selected = type.name == selectedActivityType
            FilterChip(
                selected = selected,
                onClick = { onActivityTypeSelected(if (selected) null else type.name) },
                label = { Text(type.label) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = PrimaryGreen.copy(alpha = 0.12f),
                    selectedLabelColor = PrimaryGreen
                )
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PersonTypeFilters(
    selectedPersonType: String?,
    onPersonTypeSelected: (String?) -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        PersonType.values().forEach { type ->
            val selected = type.name == selectedPersonType
            FilterChip(
                selected = selected,
                onClick = { onPersonTypeSelected(if (selected) null else type.name) },
                label = { Text(type.label) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = PrimaryGreen.copy(alpha = 0.12f),
                    selectedLabelColor = PrimaryGreen
                )
            )
        }
    }
}

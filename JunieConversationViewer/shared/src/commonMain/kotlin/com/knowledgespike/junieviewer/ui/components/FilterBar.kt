package com.knowledgespike.junieviewer.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.knowledgespike.junieviewer.ui.FilterKind
import com.knowledgespike.junieviewer.ui.FilterState
import com.knowledgespike.junieviewer.ui.theme.JunieViewerTheme

/** Pill shape for filter chips per Sprint 3 section 12.3. */
private val PillShape = RoundedCornerShape(50)

/**
 * Horizontal row of Message Kind filter chips.
 * Uses pill-shaped chips with themed selected/unselected colours.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBar(
    filter: FilterState,
    onToggleFilter: (FilterKind) -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = JunieViewerTheme.spacing

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.xl),
        horizontalArrangement = Arrangement.spacedBy(spacing.md)
    ) {
        FilterChip(
            selected = filter.showHuman,
            onClick = { onToggleFilter(FilterKind.Human) },
            label = { Text("Human", style = MaterialTheme.typography.labelMedium) },
            shape = PillShape,
            colors = themedFilterChipColors(),
            modifier = Modifier.testTag("filter_human")
        )
        FilterChip(
            selected = filter.showJunie,
            onClick = { onToggleFilter(FilterKind.Junie) },
            label = { Text("Junie", style = MaterialTheme.typography.labelMedium) },
            shape = PillShape,
            colors = themedFilterChipColors(),
            modifier = Modifier.testTag("filter_junie")
        )
        FilterChip(
            selected = filter.showThoughts,
            onClick = { onToggleFilter(FilterKind.Thought) },
            label = { Text("Thoughts", style = MaterialTheme.typography.labelMedium) },
            shape = PillShape,
            colors = themedFilterChipColors(),
            modifier = Modifier.testTag("filter_thought")
        )
        FilterChip(
            selected = filter.showTools,
            onClick = { onToggleFilter(FilterKind.Tool) },
            label = { Text("Tools", style = MaterialTheme.typography.labelMedium) },
            shape = PillShape,
            colors = themedFilterChipColors(),
            modifier = Modifier.testTag("filter_tool")
        )
        FilterChip(
            selected = filter.showPatches,
            onClick = { onToggleFilter(FilterKind.Patch) },
            label = { Text("Patches", style = MaterialTheme.typography.labelMedium) },
            shape = PillShape,
            colors = themedFilterChipColors(),
            modifier = Modifier.testTag("filter_patch")
        )
        FilterChip(
            selected = filter.showTerminal,
            onClick = { onToggleFilter(FilterKind.Terminal) },
            label = { Text("Terminal", style = MaterialTheme.typography.labelMedium) },
            shape = PillShape,
            colors = themedFilterChipColors(),
            modifier = Modifier.testTag("filter_terminal")
        )
    }
}

/** Themed colours for filter chips — selected uses primary container, unselected uses surface. */
@Composable
private fun themedFilterChipColors() = FilterChipDefaults.filterChipColors(
    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
    containerColor = MaterialTheme.colorScheme.surfaceVariant,
    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
)

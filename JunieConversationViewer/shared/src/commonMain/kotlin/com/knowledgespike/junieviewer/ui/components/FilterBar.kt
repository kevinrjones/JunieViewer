package com.knowledgespike.junieviewer.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.knowledgespike.junieviewer.ui.FilterKind
import com.knowledgespike.junieviewer.ui.FilterState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBar(
    filter: FilterState,
    onToggleFilter: (FilterKind) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = filter.showHuman,
            onClick = { onToggleFilter(FilterKind.Human) },
            label = { Text("Human") }
        )
        FilterChip(
            selected = filter.showJunie,
            onClick = { onToggleFilter(FilterKind.Junie) },
            label = { Text("Junie") }
        )
        FilterChip(
            selected = filter.showThoughts,
            onClick = { onToggleFilter(FilterKind.Thought) },
            label = { Text("Thoughts") }
        )
        FilterChip(
            selected = filter.showTools,
            onClick = { onToggleFilter(FilterKind.Tool) },
            label = { Text("Tools") }
        )
        FilterChip(
            selected = filter.showPatches,
            onClick = { onToggleFilter(FilterKind.Patch) },
            label = { Text("Patches") }
        )
        FilterChip(
            selected = filter.showTerminal,
            onClick = { onToggleFilter(FilterKind.Terminal) },
            label = { Text("Terminal") }
        )
    }
}

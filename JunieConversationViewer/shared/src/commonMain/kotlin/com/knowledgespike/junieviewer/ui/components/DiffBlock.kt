package com.knowledgespike.junieviewer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Renders unified diff content with added/removed line styling.
 * Added lines use a green-tinted background with "+" prefix.
 * Removed lines use a red-tinted background with "-" prefix.
 * Includes a copy affordance for clean diff text.
 */
@Composable
fun DiffBlock(
    diff: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "📝 Diff",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.weight(1f))
            CopyButton(text = diff)
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(8.dp)
                .horizontalScroll(rememberScrollState())
        ) {
            diff.lines().forEach { line ->
                val (bgColor, prefix) = when {
                    line.startsWith("+") && !line.startsWith("+++") ->
                        Color(0x3300AA00) to "+"
                    line.startsWith("-") && !line.startsWith("---") ->
                        Color(0x33CC0000) to "-"
                    line.startsWith("@@") ->
                        Color(0x220000CC) to "@"
                    else -> Color.Transparent to " "
                }
                Text(
                    text = line,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(bgColor)
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                )
            }
        }
    }
}

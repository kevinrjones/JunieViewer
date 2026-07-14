package com.knowledgespike.junieviewer.ui.theme

import androidx.compose.ui.unit.dp
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

/** Verifies the spacing scale matches Sprint 3 section 12.4 values. */
class JunieViewerSpacingTest {

    private val spacing = JunieViewerSpacing()

    @Test
    fun `xs is 2dp`() {
        expectThat(spacing.xs).isEqualTo(2.dp)
    }

    @Test
    fun `sm is 4dp`() {
        expectThat(spacing.sm).isEqualTo(4.dp)
    }

    @Test
    fun `md is 8dp`() {
        expectThat(spacing.md).isEqualTo(8.dp)
    }

    @Test
    fun `lg is 12dp`() {
        expectThat(spacing.lg).isEqualTo(12.dp)
    }

    @Test
    fun `xl is 16dp`() {
        expectThat(spacing.xl).isEqualTo(16.dp)
    }

    @Test
    fun `xxl is 24dp`() {
        expectThat(spacing.xxl).isEqualTo(24.dp)
    }
}

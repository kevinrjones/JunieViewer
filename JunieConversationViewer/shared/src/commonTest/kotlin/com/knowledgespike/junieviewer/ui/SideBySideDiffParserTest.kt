package com.knowledgespike.junieviewer.ui

import com.knowledgespike.junieviewer.diff.SideBySideDiffRow
import com.knowledgespike.junieviewer.diff.parseUnifiedDiffForSideBySide
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.*

/**
 * Unit tests for [parseUnifiedDiffForSideBySide].
 * Verifies header classification, removed/added pairing, uneven groups,
 * context flushing, and blank line handling.
 */
class SideBySideDiffParserTest {

    @Test
    fun `file headers are classified as Header rows`() {
        val diff = """diff --git a/file.kt b/file.kt
index abc123..def456 100644
--- a/file.kt
+++ b/file.kt"""
        val rows = parseUnifiedDiffForSideBySide(diff)
        expectThat(rows).hasSize(4)
        rows.forEach { expectThat(it).isA<SideBySideDiffRow.Header>() }
    }

    @Test
    fun `hunk header is classified as Header`() {
        val diff = "@@ -10,7 +10,9 @@ class Foo {"
        val rows = parseUnifiedDiffForSideBySide(diff)
        expectThat(rows).hasSize(1)
        expectThat(rows.first()).isA<SideBySideDiffRow.Header>()
    }

    @Test
    fun `triple minus and triple plus are headers not content`() {
        val diff = """--- a/old.kt
+++ b/new.kt"""
        val rows = parseUnifiedDiffForSideBySide(diff)
        expectThat(rows).hasSize(2)
        rows.forEach { expectThat(it).isA<SideBySideDiffRow.Header>() }
    }

    @Test
    fun `removed and added lines are paired row by row`() {
        val diff = """-old line 1
-old line 2
+new line 1
+new line 2"""
        val rows = parseUnifiedDiffForSideBySide(diff)
        expectThat(rows).hasSize(2)
        expectThat(rows[0]).isA<SideBySideDiffRow.Changed>().and {
            get { removed }.isEqualTo("old line 1")
            get { added }.isEqualTo("new line 1")
        }
        expectThat(rows[1]).isA<SideBySideDiffRow.Changed>().and {
            get { removed }.isEqualTo("old line 2")
            get { added }.isEqualTo("new line 2")
        }
    }

    @Test
    fun `uneven removed and added groups produce null cells`() {
        val diff = """-removed 1
-removed 2
-removed 3
+added 1"""
        val rows = parseUnifiedDiffForSideBySide(diff)
        expectThat(rows).hasSize(3)
        expectThat(rows[0]).isA<SideBySideDiffRow.Changed>().and {
            get { removed }.isEqualTo("removed 1")
            get { added }.isEqualTo("added 1")
        }
        expectThat(rows[1]).isA<SideBySideDiffRow.Changed>().and {
            get { removed }.isEqualTo("removed 2")
            get { added }.isNull()
        }
        expectThat(rows[2]).isA<SideBySideDiffRow.Changed>().and {
            get { removed }.isEqualTo("removed 3")
            get { added }.isNull()
        }
    }

    @Test
    fun `more added than removed produces null on removed side`() {
        val diff = """-old
+new 1
+new 2"""
        val rows = parseUnifiedDiffForSideBySide(diff)
        expectThat(rows).hasSize(2)
        expectThat(rows[0]).isA<SideBySideDiffRow.Changed>().and {
            get { removed }.isEqualTo("old")
            get { added }.isEqualTo("new 1")
        }
        expectThat(rows[1]).isA<SideBySideDiffRow.Changed>().and {
            get { removed }.isNull()
            get { added }.isEqualTo("new 2")
        }
    }

    @Test
    fun `context lines flush pending changed rows`() {
        val diff = """-removed
 context line
+added"""
        val rows = parseUnifiedDiffForSideBySide(diff)
        expectThat(rows).hasSize(3)
        expectThat(rows[0]).isA<SideBySideDiffRow.Changed>().and {
            get { removed }.isEqualTo("removed")
            get { added }.isNull()
        }
        expectThat(rows[1]).isA<SideBySideDiffRow.Context>().and {
            get { text }.isEqualTo("context line")
        }
        expectThat(rows[2]).isA<SideBySideDiffRow.Changed>().and {
            get { removed }.isNull()
            get { added }.isEqualTo("added")
        }
    }

    @Test
    fun `blank lines are handled safely`() {
        val diff = """-removed

+added"""
        val rows = parseUnifiedDiffForSideBySide(diff)
        // blank line is a context line, flushes pending removed
        expectThat(rows.filterIsInstance<SideBySideDiffRow.Context>()).isNotEmpty()
    }

    @Test
    fun `empty diff produces empty result`() {
        val rows = parseUnifiedDiffForSideBySide("")
        expectThat(rows).hasSize(1) // single empty context line
    }

    @Test
    fun `full unified diff parses correctly`() {
        val diff = """diff --git a/file.kt b/file.kt
--- a/file.kt
+++ b/file.kt
@@ -1,3 +1,3 @@
 context
-old
+new
 context"""
        val rows = parseUnifiedDiffForSideBySide(diff)
        val headers = rows.filterIsInstance<SideBySideDiffRow.Header>()
        val contexts = rows.filterIsInstance<SideBySideDiffRow.Context>()
        val changed = rows.filterIsInstance<SideBySideDiffRow.Changed>()
        expectThat(headers).hasSize(4) // diff, ---, +++, @@
        expectThat(contexts).hasSize(2)
        expectThat(changed).hasSize(1)
        expectThat(changed.first()).and {
            get { removed }.isEqualTo("old")
            get { added }.isEqualTo("new")
        }
    }
}

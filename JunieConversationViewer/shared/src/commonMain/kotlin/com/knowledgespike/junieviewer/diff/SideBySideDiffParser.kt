package com.knowledgespike.junieviewer.diff

/**
 * Represents a single row in a side-by-side diff view.
 * Each row is either a header (file/hunk metadata), a context line
 * (unchanged, shown on both sides), or a changed pair (removed on left, added on right).
 */
sealed interface SideBySideDiffRow {
    /** File header, index line, or hunk header — displayed spanning both columns. */
    data class Header(val text: String) : SideBySideDiffRow

    /** Unchanged context line — displayed identically on both sides. */
    data class Context(val text: String) : SideBySideDiffRow

    /**
     * A paired change row. Either side may be null when the change is
     * an unmatched addition or removal.
     */
    data class Changed(val removed: String?, val added: String?) : SideBySideDiffRow
}

/**
 * Parses a unified diff string into a list of [SideBySideDiffRow] for rendering
 * in a side-by-side diff view.
 *
 * Handles standard unified diff syntax:
 * - `diff --git ...`, `index ...`, `--- ...`, `+++ ...` → [SideBySideDiffRow.Header]
 * - `@@ ... @@` → [SideBySideDiffRow.Header]
 * - ` context line` → [SideBySideDiffRow.Context]
 * - `-removed line` → paired into [SideBySideDiffRow.Changed]
 * - `+added line` → paired into [SideBySideDiffRow.Changed]
 *
 * Consecutive removed lines followed by consecutive added lines are paired row-by-row.
 * If counts differ, the shorter side gets null cells.
 */
fun parseUnifiedDiffForSideBySide(diff: String): List<SideBySideDiffRow> {
    val result = mutableListOf<SideBySideDiffRow>()
    val pendingRemoved = mutableListOf<String>()
    val pendingAdded = mutableListOf<String>()

    fun flushPending() {
        val maxLen = maxOf(pendingRemoved.size, pendingAdded.size)
        for (i in 0 until maxLen) {
            result.add(
                SideBySideDiffRow.Changed(
                    removed = pendingRemoved.getOrNull(i),
                    added = pendingAdded.getOrNull(i)
                )
            )
        }
        pendingRemoved.clear()
        pendingAdded.clear()
    }

    diff.lines().forEach { line ->
        when {
            // File headers — must check before +/- single-char prefixes
            line.startsWith("diff ") ||
            line.startsWith("index ") ||
            line.startsWith("--- ") ||
            line.startsWith("+++ ") ||
            line.startsWith("@@ ") -> {
                flushPending()
                result.add(SideBySideDiffRow.Header(line))
            }
            // Removed line (but not --- file header, already handled above)
            line.startsWith("-") -> {
                // If we had pending added lines without removed, flush first
                if (pendingAdded.isNotEmpty() && pendingRemoved.isEmpty()) {
                    flushPending()
                }
                pendingRemoved.add(line.removePrefix("-"))
            }
            // Added line (but not +++ file header, already handled above)
            line.startsWith("+") -> {
                pendingAdded.add(line.removePrefix("+"))
            }
            // Context line or anything else
            else -> {
                flushPending()
                // Context lines typically start with a space; strip the leading space for display
                val contextText = if (line.startsWith(" ")) line.removePrefix(" ") else line
                result.add(SideBySideDiffRow.Context(contextText))
            }
        }
    }
    flushPending()
    return result
}

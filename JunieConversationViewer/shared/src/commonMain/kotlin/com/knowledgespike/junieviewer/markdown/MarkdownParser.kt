package com.knowledgespike.junieviewer.markdown

/** Parsed Markdown block types. */
sealed interface MarkdownBlock {
    data class Heading(val level: Int, val text: String) : MarkdownBlock
    data class Paragraph(val text: String) : MarkdownBlock
    data class ListItem(val bullet: String, val text: String) : MarkdownBlock
    data class CodeFence(val code: String, val language: String) : MarkdownBlock
}

/** Parses Markdown text into a list of blocks using line-based dispatch. */
fun parseMarkdownBlocks(markdown: String): List<MarkdownBlock> {
    val lines = markdown.lines()
    return MarkdownBlockParser(lines).parse()
}

/**
 * Line-based Markdown block parser. Each block type is handled by a dedicated
 * method, keeping the main parse loop small and extensible.
 */
private class MarkdownBlockParser(private val lines: List<String>) {
    private var cursor = 0
    private val blocks = mutableListOf<MarkdownBlock>()

    fun parse(): List<MarkdownBlock> {
        while (cursor < lines.size) {
            val line = lines[cursor]
            when {
                line.trimStart().startsWith("```") -> parseCodeFence(line)
                line.startsWith("#") -> parseHeading(line)
                line.trimStart().let { it.startsWith("- ") || it.startsWith("* ") } -> parseUnorderedList(line)
                line.trimStart().matches(ORDERED_LIST_REGEX) -> parseOrderedList(line)
                line.isBlank() -> cursor++
                else -> parseParagraph(line)
            }
        }
        return blocks
    }

    private fun parseCodeFence(openingLine: String) {
        val lang = openingLine.trimStart().removePrefix("```").trim()
        val codeLines = mutableListOf<String>()
        cursor++
        while (cursor < lines.size && !lines[cursor].trimStart().startsWith("```")) {
            codeLines.add(lines[cursor])
            cursor++
        }
        blocks.add(MarkdownBlock.CodeFence(codeLines.joinToString("\n"), lang))
        cursor++ // skip closing ```
    }

    private fun parseHeading(line: String) {
        val level = line.takeWhile { it == '#' }.length
        val text = line.drop(level).trimStart()
        blocks.add(MarkdownBlock.Heading(level.coerceIn(1, 6), text))
        cursor++
    }

    private fun parseUnorderedList(line: String) {
        val text = line.trimStart().drop(2)
        blocks.add(MarkdownBlock.ListItem("•", text))
        cursor++
    }

    private fun parseOrderedList(line: String) {
        val trimmed = line.trimStart()
        val num = trimmed.takeWhile { it.isDigit() || it == '.' }
        val text = trimmed.drop(num.length).trimStart()
        blocks.add(MarkdownBlock.ListItem(num, text))
        cursor++
    }

    private fun parseParagraph(firstLine: String) {
        val paraLines = mutableListOf(firstLine)
        cursor++
        while (cursor < lines.size && isContinuationLine(lines[cursor])) {
            paraLines.add(lines[cursor])
            cursor++
        }
        blocks.add(MarkdownBlock.Paragraph(paraLines.joinToString(" ")))
    }

    /** Returns true when the line should be folded into the current paragraph. */
    private fun isContinuationLine(line: String): Boolean =
        line.isNotBlank() &&
            !line.startsWith("#") &&
            !line.trimStart().startsWith("```") &&
            !line.trimStart().let { it.startsWith("- ") || it.startsWith("* ") } &&
            !line.trimStart().matches(ORDERED_LIST_REGEX)

    companion object {
        private val ORDERED_LIST_REGEX = Regex("^\\d+\\.\\s.*")
    }
}

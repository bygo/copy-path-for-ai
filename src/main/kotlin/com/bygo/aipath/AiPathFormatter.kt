package com.bygo.aipath

/**
 * Builds AI coding-agent file references:
 * `@path#L42` / `@path#L14-L24` (1-based lines), with a trailing space.
 *
 * Compatible with Claude Code, Codex, Grok Build, and similar `@`-path TUIs.
 */
object AiPathFormatter {
    fun format(path: String, startLine: Int, endLine: Int = startLine): String {
        require(path.isNotBlank()) { "path must not be blank" }
        require(startLine >= 1) { "startLine must be >= 1" }
        require(endLine >= startLine) { "endLine must be >= startLine" }
        val normalized = path.replace('\\', '/')
        return if (startLine == endLine) {
            "@$normalized#L$startLine "
        } else {
            "@$normalized#L$startLine-L$endLine "
        }
    }

    /**
     * Maps IntelliJ selection offsets to inclusive 1-based line numbers.
     * Empty selection → caret line only.
     */
    fun linesFromOffsets(
        startOffset: Int,
        endOffset: Int,
        lineNumber: (offset: Int) -> Int,
        lineStartOffset: (line: Int) -> Int,
    ): Pair<Int, Int> {
        val startLine0 = lineNumber(startOffset)
        if (startOffset == endOffset) {
            val line = startLine0 + 1
            return line to line
        }
        var endLine0 = lineNumber(endOffset)
        // Selection ending exactly at the next line start is exclusive of that line.
        if (endOffset > startOffset && lineStartOffset(endLine0) == endOffset) {
            endLine0 -= 1
        }
        if (endLine0 < startLine0) {
            endLine0 = startLine0
        }
        return (startLine0 + 1) to (endLine0 + 1)
    }
}

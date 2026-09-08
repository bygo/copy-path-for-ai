package com.bygo.aipath

import org.junit.Assert.assertEquals
import org.junit.Test

class AiPathFormatterTest {
    @Test
    fun formatSingleLine() {
        assertEquals(
            "@src/main.rs#L42 ",
            AiPathFormatter.format("src/main.rs", 42),
        )
    }

    @Test
    fun formatRange() {
        assertEquals(
            "@src/capture/macos.rs#L96-L110 ",
            AiPathFormatter.format("src/capture/macos.rs", 96, 110),
        )
    }

    @Test
    fun formatNormalizesBackslashes() {
        assertEquals(
            "@src/a/b.rs#L1 ",
            AiPathFormatter.format("src\\a\\b.rs", 1),
        )
    }

    @Test
    fun linesFromOffsetsEmptySelectionIsCaretLine() {
        val (start, end) = AiPathFormatter.linesFromOffsets(
            startOffset = 2,
            endOffset = 2,
            lineNumber = { offset ->
                when {
                    offset <= 1 -> 0
                    offset <= 3 -> 1
                    else -> 2
                }
            },
            lineStartOffset = { line ->
                when (line) {
                    0 -> 0
                    1 -> 2
                    else -> 4
                }
            },
        )
        assertEquals(2 to 2, start to end)
    }

    @Test
    fun linesFromOffsetsExclusiveEndAtLineStart() {
        val (start, end) = AiPathFormatter.linesFromOffsets(
            startOffset = 2,
            endOffset = 4, // exactly start of line 2 (0-based)
            lineNumber = { offset ->
                when {
                    offset < 2 -> 0
                    offset < 4 -> 1
                    else -> 2
                }
            },
            lineStartOffset = { line ->
                when (line) {
                    0 -> 0
                    1 -> 2
                    else -> 4
                }
            },
        )
        assertEquals(2 to 2, start to end)
    }

    @Test
    fun linesFromOffsetsMultiLineSelection() {
        val (start, end) = AiPathFormatter.linesFromOffsets(
            startOffset = 2,
            endOffset = 5,
            lineNumber = { offset ->
                when {
                    offset < 2 -> 0
                    offset < 4 -> 1
                    else -> 2
                }
            },
            lineStartOffset = { line ->
                when (line) {
                    0 -> 0
                    1 -> 2
                    else -> 4
                }
            },
        )
        assertEquals(2 to 3, start to end)
    }
}

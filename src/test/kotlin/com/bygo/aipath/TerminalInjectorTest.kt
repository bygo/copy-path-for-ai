package com.bygo.aipath

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.jetbrains.plugins.terminal.TerminalToolWindowManager

/**
 * Light-fixture coverage for inject gates.
 * Full PTY write / multi-tab selection is verified manually in the IDE.
 */
class TerminalInjectorTest : BasePlatformTestCase() {
    fun testInjectReturnsFalseWhenNoTerminalWidgets() {
        val widgets = TerminalToolWindowManager.getInstance(project).terminalWidgets
        assertTrue("Light fixture should start with no terminal widgets", widgets.isEmpty())
        assertFalse(
            "inject() must fail closed when no selected Terminal exists",
            TerminalInjector.inject(project, "@TerminalInjectorTest.kt#L1 "),
        )
    }

    fun testHasSelectedTerminalFalseWhenEmpty() {
        assertFalse(
            "hasSelectedTerminal requires a selected Terminal tab",
            TerminalInjector.hasSelectedTerminal(project),
        )
    }
}

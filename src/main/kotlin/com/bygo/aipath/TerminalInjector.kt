package com.bygo.aipath

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.terminal.ui.TerminalWidget
import com.intellij.ui.content.Content
import org.jetbrains.plugins.terminal.TerminalToolWindowFactory
import org.jetbrains.plugins.terminal.TerminalToolWindowManager
import java.awt.Component
import java.lang.reflect.Method
import javax.swing.JComponent

/**
 * Injects text into the **currently selected** IDE Terminal tab
 * (Claude Code, Codex, Grok, OpenCode, Gemini CLI, or any other TUI/shell).
 */
object TerminalInjector {
    private val log = Logger.getInstance(TerminalInjector::class.java)

    private const val REWORKED_TABS_MANAGER =
        "com.intellij.terminal.frontend.toolwindow.TerminalToolWindowTabsManager"
    private const val TERMINAL_VIEW_CLASS =
        "com.intellij.terminal.frontend.view.TerminalView"

    /** Result of an inject attempt, including which strategy succeeded (for smoke/tests). */
    data class InjectResult(val ok: Boolean, val strategy: String)

    fun inject(project: Project, text: String): Boolean = injectDetailed(project, text).ok

    fun injectDetailed(project: Project, text: String): InjectResult {
        val app = ApplicationManager.getApplication()
        var result = InjectResult(false, "unevaluated")
        val task = Runnable {
            result = injectOnEdt(project, text)
        }
        if (app.isDispatchThread) {
            task.run()
        } else {
            app.invokeAndWait(task)
        }
        return result
    }

    /** True when any Terminal tab is currently selected. */
    fun hasSelectedTerminal(project: Project): Boolean {
        val app = ApplicationManager.getApplication()
        var found = false
        val task = Runnable {
            found = selectedClassicWidget(project) != null ||
                selectedReworkedView(project) != null
        }
        if (app.isDispatchThread) {
            task.run()
        } else {
            app.invokeAndWait(task)
        }
        return found
    }

    private fun selectedReworkedView(project: Project): Any? {
        val selectedContent = terminalToolWindow(project)?.contentManager?.selectedContent
            ?: return null
        val tabs = loadReworkedTabs(project)
        for (tab in tabs) {
            val content = tabContent(tab) ?: continue
            if (content != selectedContent) {
                continue
            }
            tabView(tab)?.let { return it }
        }
        val component = selectedContent.component ?: return null
        return terminalViewFromDataContext(component) ?: findTerminalViewInHierarchy(component)
    }

    private fun selectedClassicWidget(project: Project): TerminalWidget? {
        val manager = TerminalToolWindowManager.getInstance(project)
        val toolWindow = terminalToolWindow(project, manager)
        val selected = toolWindow?.contentManager?.selectedContent
        if (selected != null) {
            TerminalToolWindowManager.findWidgetByContent(selected)?.let { return it }
        }
        return manager.terminalWidgets.firstOrNull { it.hasFocus() }
    }

    private fun injectOnEdt(project: Project, text: String): InjectResult {
        selectedReworkedView(project)?.let { reworked ->
            showTerminalToolWindow(project)
            requestFocus(reworked)
            if (writeViaTerminalViewObject(reworked, text)) {
                log.info("AI Path inject: selected Reworked TerminalView OK")
                return InjectResult(true, "reworked-terminal-view")
            }
            log.warn("AI Path inject: selected Reworked Terminal found but send failed")
            return InjectResult(false, "reworked-send-failed")
        }

        selectedClassicWidget(project)?.let { widget ->
            showTerminalToolWindow(project)
            runCatching { widget.requestFocus() }
            return injectIntoClassicWidget(widget, text)
        }

        log.info("AI Path inject: no selected Terminal tab")
        return InjectResult(false, "no-selected-terminal")
    }

    private fun injectIntoClassicWidget(widget: TerminalWidget, text: String): InjectResult {
        if (writeViaTerminalView(widget, text)) {
            log.info("AI Path inject: TerminalView sendText OK")
            return InjectResult(true, "terminal-view")
        }
        if (writeViaTty(widget, text)) {
            log.info("AI Path inject: ttyConnector write OK")
            return InjectResult(true, "tty-plain")
        }
        log.warn("AI Path inject: direct write failed")
        return InjectResult(false, "all-failed")
    }

    private fun writeViaTty(widget: TerminalWidget, text: String): Boolean {
        val connector = runCatching { widget.ttyConnector }.getOrNull() ?: return false
        if (!connector.isConnected) {
            return false
        }
        return try {
            connector.write(text)
            true
        } catch (t: Throwable) {
            log.warn("ttyConnector.write failed", t)
            false
        }
    }

    private fun writeViaTerminalView(widget: TerminalWidget, text: String): Boolean {
        val view = findTerminalView(widget) ?: return false
        return writeViaTerminalViewObject(view, text)
    }

    private fun writeViaTerminalViewObject(view: Any, text: String): Boolean {
        return try {
            val sendText: Method? = view.javaClass.methods.firstOrNull {
                it.name == "sendText" && it.parameterCount == 1 &&
                    it.parameterTypes[0] == String::class.java
            }
            if (sendText != null) {
                sendText.invoke(view, text)
                return true
            }
            val builderMethod = view.javaClass.methods.firstOrNull {
                it.name == "createSendTextBuilder" && it.parameterCount == 0
            } ?: return false
            val builder = builderMethod.invoke(view) ?: return false
            val send = builder.javaClass.methods.firstOrNull {
                it.name == "send" && it.parameterCount == 1 &&
                    it.parameterTypes[0] == String::class.java
            } ?: return false
            send.invoke(builder, text)
            true
        } catch (t: Throwable) {
            log.warn("TerminalView send failed", t)
            false
        }
    }

    private fun findTerminalView(widget: TerminalWidget): Any? {
        val focusable = widget.preferredFocusableComponent
        val root = widget.component
        sequenceOf(focusable, root).filterNotNull().forEach { component ->
            terminalViewFromDataContext(component)?.let { return it }
        }
        sequenceOf(focusable, root).filterNotNull().forEach { component ->
            findTerminalViewInHierarchy(component)?.let { return it }
        }
        return null
    }

    private fun terminalViewFromDataContext(component: Component): Any? {
        return try {
            val dataManagerClass = Class.forName("com.intellij.ide.DataManager")
            val getInstance = dataManagerClass.getMethod("getInstance")
            val dataManager = getInstance.invoke(null)
            val getDataContext = dataManager.javaClass.methods.first {
                it.name == "getDataContext" && it.parameterCount == 1 &&
                    Component::class.java.isAssignableFrom(it.parameterTypes[0])
            }
            val dataContext = getDataContext.invoke(dataManager, component) ?: return null

            val viewClass = Class.forName(TERMINAL_VIEW_CLASS)
            val companionField = viewClass.getField("Companion")
            val companion = companionField.get(null)
            val getDataKey = companion.javaClass.methods.first {
                it.name == "getDATA_KEY" && it.parameterCount == 0
            }
            val dataKey = getDataKey.invoke(companion)
            val getData = dataContext.javaClass.methods.first {
                it.name == "getData" && it.parameterCount == 1
            }
            getData.invoke(dataContext, dataKey)
        } catch (_: Throwable) {
            null
        }
    }

    private fun findTerminalViewInHierarchy(root: Component): Any? {
        val queue = ArrayDeque<Component>()
        queue.add(root)
        var steps = 0
        while (queue.isNotEmpty() && steps < 200) {
            steps++
            val c = queue.removeFirst()
            if (c.javaClass.name.contains("TerminalView") &&
                c.javaClass.methods.any { it.name == "sendText" || it.name == "createSendTextBuilder" }
            ) {
                return c
            }
            for (field in c.javaClass.declaredFields) {
                try {
                    field.isAccessible = true
                    val value = field.get(c) ?: continue
                    val name = value.javaClass.name
                    if (name.contains("TerminalView") &&
                        value.javaClass.methods.any {
                            it.name == "sendText" || it.name == "createSendTextBuilder"
                        }
                    ) {
                        return value
                    }
                } catch (_: Throwable) {
                    // ignore
                }
            }
            if (c is java.awt.Container) {
                c.components.forEach { queue.add(it) }
            }
        }
        return null
    }

    private fun loadReworkedTabs(project: Project): List<Any> {
        return try {
            val clazz = Class.forName(REWORKED_TABS_MANAGER)
            val manager = clazz.methods.firstOrNull {
                it.name == "getInstance" && it.parameterCount == 1 &&
                    Project::class.java.isAssignableFrom(it.parameterTypes[0])
            }?.invoke(null, project) ?: return emptyList()
            val tabs = manager.javaClass.methods.firstOrNull {
                it.name == "getTabs" && it.parameterCount == 0
            }?.invoke(manager) as? Collection<*> ?: return emptyList()
            tabs.filterNotNull()
        } catch (_: ClassNotFoundException) {
            emptyList()
        } catch (t: Throwable) {
            log.warn("Reworked Terminal tabs lookup failed", t)
            emptyList()
        }
    }

    private fun tabView(tab: Any): Any? =
        runCatching {
            tab.javaClass.methods.firstOrNull { it.name == "getView" && it.parameterCount == 0 }
                ?.invoke(tab)
        }.getOrNull()

    private fun tabContent(tab: Any): Content? =
        runCatching {
            tab.javaClass.methods.firstOrNull { it.name == "getContent" && it.parameterCount == 0 }
                ?.invoke(tab) as? Content
        }.getOrNull()

    private fun preferredFocusable(view: Any): JComponent? =
        runCatching {
            view.javaClass.methods.firstOrNull {
                it.name == "getPreferredFocusableComponent" && it.parameterCount == 0
            }?.invoke(view) as? JComponent
        }.getOrNull()

    private fun componentOf(view: Any): JComponent? =
        runCatching {
            view.javaClass.methods.firstOrNull {
                it.name == "getComponent" && it.parameterCount == 0
            }?.invoke(view) as? JComponent
        }.getOrNull()

    private fun requestFocus(view: Any) {
        preferredFocusable(view)?.requestFocusInWindow()
            ?: componentOf(view)?.requestFocusInWindow()
    }

    private fun terminalToolWindow(
        project: Project,
        manager: TerminalToolWindowManager = TerminalToolWindowManager.getInstance(project),
    ): ToolWindow? =
        manager.toolWindow
            ?: ToolWindowManager.getInstance(project)
                .getToolWindow(TerminalToolWindowFactory.TOOL_WINDOW_ID)

    private fun showTerminalToolWindow(project: Project) {
        val tw = terminalToolWindow(project) ?: return
        if (!tw.isVisible) {
            tw.show()
        }
    }
}

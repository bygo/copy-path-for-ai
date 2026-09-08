package com.bygo.aipath

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.terminal.ui.TerminalWidget
import org.jetbrains.plugins.terminal.TerminalToolWindowManager
import java.awt.datatransfer.StringSelection
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicReference

/**
 * Local-only smoke helper (not registered in plugin.xml).
 * Wire via a temporary runIde descriptor if needed:
 * `-Daipath.smokeInject=/tmp/aipath-smoke.txt`.
 */
class AiPathInjectSmoke : ProjectActivity {
    override suspend fun execute(project: Project) {
        val out = System.getProperty("aipath.smokeInject")?.trim().orEmpty()
        if (out.isEmpty()) {
            return
        }
        val log = Logger.getInstance(AiPathInjectSmoke::class.java)
        val resultPath = Path.of(out)
        try {
            val widgetRef = AtomicReference<TerminalWidget?>()
            ApplicationManager.getApplication().invokeAndWait {
                val manager = TerminalToolWindowManager.getInstance(project)
                widgetRef.set(manager.createShellWidget(project.basePath, "aipath-smoke", true, true))
            }
            val widget = widgetRef.get() ?: error("createShellWidget returned null")

            val deadline = System.currentTimeMillis() + 20_000
            while (System.currentTimeMillis() < deadline) {
                val connector = widget.ttyConnector
                if (connector != null && connector.isConnected) {
                    break
                }
                Thread.sleep(100)
            }

            val marker = "@AiPathInjectSmoke.kt#L1 "
            CopyPasteManager.getInstance().setContents(StringSelection(marker))
            val detailed = TerminalInjector.injectDetailed(project, marker)
            val line = if (detailed.ok) {
                "PASS:${detailed.strategy}:$marker"
            } else {
                "FAIL:${detailed.strategy}"
            }
            Files.writeString(resultPath, line + "\n")
            log.info("Copy Path for AI smoke: $line")
        } catch (t: Throwable) {
            runCatching {
                Files.writeString(resultPath, "FAIL:${t.javaClass.simpleName}:${t.message}\n")
            }
            log.warn("Copy Path for AI smoke failed", t)
        } finally {
            Thread.sleep(300)
            ApplicationManager.getApplication().invokeLater {
                ApplicationManager.getApplication().exit(true, true, false)
            }
        }
    }
}

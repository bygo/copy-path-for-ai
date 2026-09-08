package com.bygo.aipath

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import java.awt.datatransfer.StringSelection

/**
 * Copies an AI file reference (`@path#L42` / `@path#L14-L24`) and injects it
 * into the **selected** IDE Terminal tab (Claude, Codex, Grok, etc.).
 * Falls back to clipboard if inject fails.
 */
class CopyAiPathAction : AnAction(), DumbAware {
    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR)
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE)
        e.presentation.isEnabledAndVisible =
            e.project != null && editor != null && file != null && !file.isDirectory
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return
        if (file.isDirectory) {
            return
        }

        // Prefer project-relative path; fall back to absolute (never bare filename).
        val path = relativePath(project, file) ?: file.path.replace('\\', '/')
        val document = editor.document
        val selection = editor.selectionModel
        val (startLine, endLine) = AiPathFormatter.linesFromOffsets(
            startOffset = selection.selectionStart,
            endOffset = selection.selectionEnd,
            lineNumber = document::getLineNumber,
            lineStartOffset = document::getLineStartOffset,
        )
        val ref = AiPathFormatter.format(path, startLine, endLine)

        CopyPasteManager.getInstance().setContents(StringSelection(ref))

        if (TerminalInjector.inject(project, ref)) {
            notify(project, "notification.injected", ref)
        } else {
            notify(project, "notification.copied", ref)
        }
    }

    private fun notify(project: Project, messageKey: String, ref: String) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("Copy Path for AI")
            .createNotification(
                AiPathBundle.message("notification.title"),
                AiPathBundle.message(messageKey, ref),
                NotificationType.INFORMATION,
            )
            .notify(project)
    }

    private fun relativePath(project: Project, file: VirtualFile): String? {
        val base = project.guessProjectDir() ?: return null
        return VfsUtilCore.getRelativePath(file, base)?.replace('\\', '/')
    }
}

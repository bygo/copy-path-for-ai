# Copy Path for AI

JetBrains IDE plugin that copies a file reference from the current editor caret or selection and injects it into the IDE Terminal — for **Claude Code**, **Codex**, **Grok**, OpenCode, Gemini CLI, and similar AI coding agents.

<!-- Plugin description -->
**Copy Path for AI** injects a paste-ready reference like **`@main.rs#L42`** into the selected IDE Terminal tab (and also copies it to the clipboard).

Works with **Claude Code**, **OpenAI Codex**, **Grok Build**, OpenCode, Gemini CLI, and other terminal AI agents that accept `@path` file references.

[Github](https://github.com/bygo/copy-path-for-ai)

### Features

- One shortcut: **⌘3** (Windows/Linux: **Ctrl+3**)
- Editor context menu: **Copy Path for AI**
- Caret → `@path#Lline`; selection → `@path#Lstart-Lend` (trailing space included)
- Path is relative to the project root (absolute if outside the project)
- **Auto-inject** into the **currently selected** IDE Terminal tab
- Clipboard fallback if no Terminal is selected or inject fails
- Balloon confirms inject / copy

### Requirements for auto-inject

Uses the IDE **Terminal** tool window. Keep the tab with your AI agent selected. External apps (iTerm, Terminal.app) are not targeted — use clipboard paste there.
<!-- Plugin description end -->

## Install (local)

1. Clone and build the distribution:

```bash
git clone https://github.com/bygo/copy-path-for-ai.git
cd copy-path-for-ai
export JAVA_HOME=/opt/homebrew/opt/openjdk@21
./gradlew buildPlugin
```

2. In the IDE: **Settings → Plugins → ⚙️ → Install Plugin from Disk…**
3. Pick `build/distributions/copy-path-for-ai-1.0.0.zip`

## Usage

1. Start Claude / Codex / Grok (or another agent) in an IDE **Terminal** tab and keep that tab selected
2. In the editor, put the caret on a line, or select a range
3. Press **⌘3** / **Ctrl+3** (or use the editor context menu)
4. The `@` path is injected at the prompt (and also copied to the clipboard)

If inject fails (no Terminal selected), the reference is still on the clipboard — paste with `⌘V` / `Ctrl+V`.

Example payload:

```text
@src-tauri/src/capture/macos.rs#L96-L110 
```

## License

Apache-2.0

<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# Copy Path for AI Changelog

## [Unreleased]

## [1.0.1] - 2026-09-09

### Changed

- Plugin icon: clean typographic `@` (removed the awkward geometric stub)

## [1.0.0]

### Added

- Copy AI file references `@path#Lline` / `@path#Lstart-Lend` from caret or selection
- Editor context menu and **⌘3** / **Ctrl+3** shortcut
- Auto-inject into the selected IDE Terminal tab (Claude Code, Codex, Grok, OpenCode, Gemini CLI, …)
- Clipboard fallback and balloon notification

### Changed

- Renamed from **Grok Ref** (`com.bygo.grokref`) to **Copy Path for AI** (`com.bygo.aipath`)
- Reference format is now `@path#L14` / `@path#L14-L24` with a trailing space (was `@path@L14`)
- Inject into any selected Terminal tab (no Grok-only gate)
- Removed auto-start `grok` behavior

### Fixed

- Direct `sendText` / tty write for inject (no bracketed-paste / Terminal.Paste hacks)

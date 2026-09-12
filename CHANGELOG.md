<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# Copy Path for AI Changelog

## [Unreleased]

## [1.0.3] - 2026-09-12

### Changed

- Trigger Marketplace publish from version tag push (`1.0.3` / `v1.0.3`) via GitHub Actions

## [1.0.2] - 2026-09-12

### Changed

- Drop `until-build` cap so the plugin stays compatible with WebStorm / IntelliJ **2026.2+** (was limited to `261.*` / 2026.1)
- Add GitHub Actions for CI build and Marketplace publish on GitHub Release

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

[Unreleased]: https://github.com/bygo/copy-path-for-ai/compare/v1.0.2...HEAD
[1.0.2]: https://github.com/bygo/copy-path-for-ai/compare/v1.0.1...v1.0.2
[1.0.1]: https://github.com/bygo/copy-path-for-ai/compare/v1.0.0...v1.0.1
[1.0.0]: https://github.com/bygo/copy-path-for-ai/commits/v1.0.0

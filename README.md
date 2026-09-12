# Copy Path for AI

[![Version](https://img.shields.io/jetbrains/plugin/v/34168.svg)](https://plugins.jetbrains.com/plugin/34168)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/34168.svg)](https://plugins.jetbrains.com/plugin/34168)
[![Rating](https://img.shields.io/jetbrains/plugin/r/rating/34168.svg)](https://plugins.jetbrains.com/plugin/34168)

JetBrains IDE plugin that copies a file reference from the current editor caret or selection and injects it into the IDE Terminal — for **Claude Code**, **Codex**, **Grok**, OpenCode, Gemini CLI, and similar AI coding agents.

<!-- Plugin description -->
**Copy Path for AI** injects a paste-ready reference like **`@main.rs#L42`** into the selected IDE Terminal tab (and also copies it to the clipboard).

Works with **Claude Code**, **OpenAI Codex**, **Grok Build**, OpenCode, Gemini CLI, and other terminal AI agents that accept `@path` file references.

[Github](https://github.com/bygo/copy-path-for-ai) · [JetBrains Marketplace](https://plugins.jetbrains.com/plugin/34168)

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

## Install

### From JetBrains Marketplace

1. In the IDE: **Settings → Plugins → Marketplace**
2. Search for **Copy Path for AI**
3. Click **Install**, then restart if prompted

Or open the [plugin page](https://plugins.jetbrains.com/plugin/34168) and click **Install to IDE**.

### From disk (local build)

1. Clone and build the distribution:

```bash
git clone https://github.com/bygo/copy-path-for-ai.git
cd copy-path-for-ai
export JAVA_HOME=/opt/homebrew/opt/openjdk@21
./gradlew buildPlugin
```

2. In the IDE: **Settings → Plugins → ⚙️ → Install Plugin from Disk…**
3. Pick `build/distributions/copy-path-for-ai-1.0.1.zip`

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

## Release / CI

GitHub Actions:

- **Build** (`.github/workflows/build.yml`) — on `main` / PRs: build + test, upload the plugin zip artifact
- **Release** (`.github/workflows/release.yml`) — when you **push a version tag** (e.g. `1.0.3`) or publish a GitHub Release: sign and upload to [JetBrains Marketplace](https://plugins.jetbrains.com/plugin/34168)

### One-time secrets

In the GitHub repo: **Settings → Secrets and variables → Actions**, add:

| Secret | What it is |
|--------|------------|
| `PUBLISH_TOKEN` | Marketplace token from [plugins.jetbrains.com](https://plugins.jetbrains.com) → profile → **My Tokens** |
| `CERTIFICATE_CHAIN` | PEM certificate chain for [plugin signing](https://plugins.jetbrains.com/docs/intellij/plugin-signing.html) |
| `PRIVATE_KEY` | PEM private key |
| `PRIVATE_KEY_PASSWORD` | Key password (use an empty secret if the key is unencrypted) |

Generate a self-signed signing key (example):

```bash
openssl genpkey -aes-256-cbc -algorithm RSA -out private_encrypted.pem -pkeyopt rsa_keygen_bits:4096
openssl rsa -in private_encrypted.pem -out private.pem
openssl req -key private.pem -new -x509 -days 3650 -out chain.crt \
  -subj "/CN=Copy Path for AI"
```

Put the contents of `chain.crt` / `private.pem` into the secrets above.

### Publish a new version

1. Bump `pluginVersion` in `gradle.properties` (and add notes under `CHANGELOG.md`)
2. Commit and push to `main`
3. Create and push a matching tag (leading `v` optional):

```bash
git tag 1.0.3
git push origin 1.0.3
```

4. The Release workflow runs `./gradlew publishPlugin`, then creates/updates the GitHub Release with the zip attached

Marketplace review can take a short while after upload before the new build appears.

## License

Apache-2.0

# Junie Conversation Viewer

A Compose Desktop application for browsing **Junie** conversation **Sessions** stored under your Junie home directory. It parses each Session's `events.jsonl` into a readable **Conversation** of Human and Junie **Messages** — rendering Markdown, code, patches/diffs, terminal output, tool output, and structured messages.

[![Tag Build and Release](https://github.com/kevinrjones/JunieViewer/actions/workflows/tag-build.yml/badge.svg)](https://github.com/kevinrjones/JunieViewer/actions/workflows/tag-build.yml)
![Kotlin](https://img.shields.io/badge/Kotlin-2.4.0-7F52FF?logo=kotlin&logoColor=white)
![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk&logoColor=white)
![Compose Desktop](https://img.shields.io/badge/Compose%20Desktop-1.11.1-4285F4?logo=jetpackcompose&logoColor=white)

> **Screenshot coming soon.**

## Features

- **Browse Sessions** — automatically scans and lists Sessions from the Junie home directory (defaults to `~/.junie/sessions/`).
- **View Human and Junie Messages** — asymmetric Human/Junie layout with accent rails, Turn grouping, and Message Kind markers.
- **Rich content rendering** — Markdown, syntax-highlighted code, patches/diffs (added/removed/hunk colouring), terminal output, tool calls, structured output, and error/warning blocks.
- **Filter by message category** — pill-shaped chips to show/hide Human, Junie, Thoughts, Tools, Patches, and Terminal Messages.
- **Search and navigate matches** — case-insensitive full-text search with match count, ▲/▼ navigation, and in-content highlighting of the current match.
- **Toggle sort order** — switch between Oldest First and Newest First with auto-scroll adaptation.
- **Collapse/Show rich content** — global Collapse All / Show All controls for all collapsible blocks.
- **Live auto-refresh** — optionally poll the selected Session's `events.jsonl` for new Events, preserving scroll position.
- **Persistent preferences** — remembers theme, Junie home path, last viewed Session, sort order, and auto-refresh state.
- **Light/Dark/System themes** — Material 3 colour schemes with semantic conversation colour tokens.
- **Copy support** — copy selected text via toolbar/menu, plus per-block copy buttons for full rich content.
- **Sub-agent representation** — sub-agent delegations are shown as dedicated blocks with agent name and status.
- **Accessibility** — WCAG AA contrast, screen-reader labels, non-colour-only indicators, and keyboard-navigable focus order.

Terminology used throughout: **Session**, **Conversation**, **Message**, **Human**, **Junie**. See [`docs/UBIQUITOUS-LANGUAGE.md`](JunieConversationViewer/docs/UBIQUITOUS-LANGUAGE.md).

## Installation from GitHub Releases

Pushing a version tag (`vX.Y.Z`) publishes a **GitHub Release** with per-OS installers and zipped distributables. Assets follow the naming pattern `JunieConversationViewer-<tag>-<platform>`:

```
JunieConversationViewer-v1.2.0-macos.dmg
JunieConversationViewer-v1.2.0-windows-x64.msi
JunieConversationViewer-v1.2.0-windows-arm64.msi
JunieConversationViewer-v1.2.0-linux-x64.deb
JunieConversationViewer-v1.2.0-linux-arm64.deb
JunieConversationViewer-v1.2.0-<platform>-distributable.zip
```

Each asset has a matching `.sha256` checksum sidecar for verification, for example:

```bash
# macOS / Linux
shasum -a 256 -c JunieConversationViewer-v1.2.0-macos.dmg.sha256
```

Download the installer for your platform, or use the `-distributable.zip` no-install runnable app image. The tag only affects artifact/Release names; the internal application version remains `1.0.0`.

> The installers are **not** notarized, code-signed, or published to any package manager (no Homebrew, no registry, no auto-update). Your OS may warn about an unidentified developer on first launch.

## Run from source

**Prerequisites:** Java (JDK) 21, Git, and the bundled Gradle wrapper.

```bash
# macOS / Linux
./gradlew :desktopApp:run

# Windows
.\gradlew.bat :desktopApp:run
```

For development with hot reload:

```bash
./gradlew :desktopApp:hotRun --auto
```

## Build/package locally

Produce a native installer/distributable for your current OS:

```bash
# Native installer + app image for the current OS
./gradlew :desktopApp:packageDistributionForCurrentOS

# Per-format installers
./gradlew :desktopApp:packageDmg    # macOS  -> build/compose/binaries/main/dmg/
./gradlew :desktopApp:packageMsi    # Windows -> build/compose/binaries/main/msi/
./gradlew :desktopApp:packageDeb    # Linux  -> build/compose/binaries/main/deb/

# No-install runnable app image
./gradlew :desktopApp:createDistributable   # -> build/compose/binaries/main/app/
```

Outputs are written under `desktopApp/build/compose/binaries/main/`.

## Usage overview

On launch the viewer scans `~/.junie/sessions/` and lets you pick a **Session** from the toolbar or **File** menu; **Settings** lets you change the Junie home path. Messages are grouped into **Turns**, with Human Messages on the left and Junie Messages on the right, each tagged with a Message Kind marker. Use the **Filters** chips to control which Message Kinds are shown, **Search** to find and step through matches, **Sort Order** to flip chronological direction, and **Collapse All / Show All** to manage rich content. Enable **Auto-Refresh** to track a live Session as new Events are written.

See [`docs/HOW_TO_USE.md`](JunieConversationViewer/docs/HOW_TO_USE.md) for the full, detailed usage reference.

## Keyboard shortcuts

| Command | macOS | Windows / Linux |
| :--- | :--- | :--- |
| Open Session | Cmd+O | Ctrl+O |
| Refresh | Cmd+R | Ctrl+R |
| Auto-Refresh | Cmd+Shift+R | Ctrl+Shift+R |
| Copy | Cmd+C | Ctrl+C |
| Find | Cmd+F | Ctrl+F |
| Find Next | Cmd+G | F3 |
| Find Previous | Cmd+Shift+G | Shift+F3 |
| Collapse All | Cmd+Shift+− | Ctrl+Shift+− |
| Show All | Cmd+Shift++ | Ctrl+Shift++ |
| Quit | Cmd+Q | Alt+F4 |

## Sessions and logs paths

- **Sessions:** `~/.junie/sessions/` — each Session directory contains an `events.jsonl` file (configurable via **Settings**).
- **Logs:** `~/.junieviewer/logs/` — rolling file logs (date and size based). Provide `~/.junieviewer/logback.xml` to override logging config.

## Troubleshooting

- **No Sessions found** — verify `~/.junie/sessions/` exists and contains session directories with `events.jsonl` files; check the Junie home path in Settings.
- **Conversation does not update live** — ensure Auto-Refresh is enabled and the Session file is being written; check logs in `~/.junieviewer/logs/`.
- **Search does not find expected text** — search matches only visible content; confirm the relevant Filter is enabled (case-insensitive substring match).
- **Unsupported events** — unrecognised Events render safely as "Unsupported event" cards; no data is lost.
- **Very large blocks** — collapse large code/diff/terminal blocks to improve scroll performance.

## Development and test commands

```bash
# Shared JVM unit tests
./gradlew :shared:jvmTest

# Full aggregate test task (used by CI before packaging)
./gradlew test
```

See [`docs/TESTING.md`](JunieConversationViewer/docs/TESTING.md) for the testing strategy.

## Documentation

- [How to Use](JunieConversationViewer/docs/HOW_TO_USE.md) — full usage reference.
- [Testing](JunieConversationViewer/docs/TESTING.md) — testing strategy and commands.
- [Ubiquitous Language](JunieConversationViewer/docs/UBIQUITOUS-LANGUAGE.md) — domain terminology.
- [GitHub Setup Guide](JunieConversationViewer/docs/GITHUB_SETUP.md) — maintainers: wire up CI and publish releases.
- Sprint and task docs under [`docs/`](JunieConversationViewer/docs/).

## Status / limitations

- **Platform verification:** only macOS has been fully verified locally; Windows and Linux (incl. ARM64) builds are produced by CI but have not yet been HITL-verified.
- Code block syntax highlighting does not yet follow the Light/Dark theme selection.
- Search highlighting within rendered Markdown may not highlight all matches.
- Side-by-side diff view is not yet implemented; diffs render in unified format.
- Installers are unsigned/unnotarized and not published to any package manager.

## Contributing

Contributions are welcome. Please:

- Build and run from source (Java 21 + Gradle wrapper) before opening a PR.
- Run `./gradlew test` and keep the build green.
- Follow the existing Kotlin/Compose code style and the domain terminology in [`docs/UBIQUITOUS-LANGUAGE.md`](JunieConversationViewer/docs/UBIQUITOUS-LANGUAGE.md).

---

Built with [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html) and Compose Desktop.

# Sprint 7 Area 2 — GitHub Actions Workflow Design

> **Date:** 2026-07-22
> **Author:** Junie (automated design)
> **Status:** Awaiting HITL review (tasks 2.1–2.3 complete; 2.4 pending HITL sign-off)
> **Sprint:** [Sprint 7 — CI/GitHub Automation and GitHub README](sprints/junie-conversation-viewer-sprint-7-ci-github-automation-and-readme.md)
> **Task doc:** [Sprint 7 Task Breakdown](tasks/junie-conversation-viewer-tasks-sprint-7-ci-github-automation-and-readme.md)
> **Predecessor:** [Area 1 Discovery Findings](sprint-7-area-1-discovery-findings.md)

---

## 1. Summary

Area 2 proposes the complete design for the tag-triggered release workflow
`.github/workflows/tag-build.yml`, without writing any YAML. It specifies the
trigger, permissions, JDK/cache, test gate, cross-platform build matrix
(including ARM Linux and Windows ARM64), per-OS package tasks and output paths (from confirmed
Area 1 findings), tag-aware artifact naming, SHA256 checksums, and the GitHub
Release publishing step with prerelease detection.

**Implementation is intentionally deferred to Area 3.** This document exists so
the HITL can approve the workflow shape before any `tag-build.yml` is created.
Nothing in Area 2 changes `desktopApp/build.gradle.kts`, the README, or
`packageVersion` (stays `1.0.0`, per D3).

The design is closely modelled on the confirmed-available LogViewer workflow
(`../LogViewer/.github/workflows/build.yml`), adapted per the Area 1 HITL
decisions: module `:app` → `:desktopApp`; Detekt gate dropped; `:ui:desktopTest`
replaced by Junie's test commands; artifact base name `JunieConversationViewer`;
tag-only trigger.

---

## 2. Design Inputs

### 2.1 Area 1 confirmed facts (see [Area 1 findings](sprint-7-area-1-discovery-findings.md) §3)

- Package tasks all exist under `:desktopApp`: `packageDmg`, `packageMsi`,
  `packageDeb`, `createDistributable`, `packageDistributionForCurrentOS`.
- Output paths under `desktopApp/build/compose/binaries/main/`:
  - Distributable app image: `.../main/app/`
  - macOS installer: `.../main/dmg/*.dmg` (verified locally)
  - Windows installer: `.../main/msi/*.msi` (standard layout; verify on runner)
  - Linux installer: `.../main/deb/*.deb` (standard layout; verify on runner)
- Installer filename embeds `packageVersion` (`...-1.0.0.dmg`); the git tag is
  applied only in workflow artifact/Release **names**, not by renaming Gradle.
- Repo root for Gradle is `JunieConversationViewer/`; module paths are prefixed
  `desktopApp/` (not `app/` as in LogViewer).
- Test commands (from `docs/TESTING.md`): `./gradlew :shared:jvmTest`,
  `./gradlew test`, `./gradlew check`.

### 2.2 Area 1 HITL decisions (see [Area 1 findings](sprint-7-area-1-discovery-findings.md) §7)

| ID | Decision |
|---|---|
| Q1 | Workflow file named `tag-build.yml`. |
| Q2 | Tag-only this sprint; PR/`main` CI deferred. |
| Q3 | Tags containing `-` publish as prereleases. |
| Q-add | Publish **both** per-OS installer **and** zipped distributable. |
| Q3/D2 | SHA256 checksums produced and attached (tags only). |
| Q5 | **Include ARM Linux** runner `ubuntu-24.04-arm` now. |
| Q6 | A license file + License badge will be added (Area 5/6). |
| Q4 | `HOW_TO_USE.md` authoritative; README summarizes (out of Area 2 scope). |

### 2.3 Decisions D1–D4 (task doc Notes/Decisions Log)

- **D1** — Full matrix: macOS (`.dmg`), Windows (`.msi`), Linux (`.deb` under Xvfb).
- **D2** — Publish a GitHub Release on tags via `softprops/action-gh-release`, gated on `refs/tags/`.
- **D3** — Tag in artifact/Release names only; `packageVersion` stays `1.0.0`.
- **D4** — Confirm packaging task names/paths before hard-coding (done in Area 1).

---

## 3. Workflow Trigger / JDK / Cache Design

| Aspect | Design |
|---|---|
| File name | `.github/workflows/tag-build.yml` (Q1). |
| Workflow `name` | `Tag Build and Release`. |
| Trigger | `on: push: tags: [ "v*" ]` only. **No** `workflow_dispatch`, `branches`, or `pull_request` this sprint (Q2). |
| Permissions | Job-level `permissions: contents: write` (needed for Release publishing, D2). `packages: write` from LogViewer is **not** needed and is dropped. |
| Checkout | `actions/checkout@v4` (or latest v4). |
| Default shell | `defaults: run: shell: bash` so Linux/macOS steps share one script; Windows-specific packaging steps override with `shell: pwsh`. |
| Java setup | `actions/setup-java@v5`, `java-version: '21'`, `distribution` carried per-matrix (`matrix.java-distribution`). **Temurin** on macOS / Windows x64 / Linux (x64 + ARM). **Microsoft Build of OpenJDK** (`microsoft`) on the Windows ARM64 runner, because Eclipse **Temurin does not ship a Windows `aarch64` JDK 21** (Adoptium build stalled — see [temurin#271](https://github.com/adoptium/temurin/issues/271)); Microsoft ships `microsoft-jdk-21-windows-aarch64` and `setup-java` supports `distribution: microsoft`. |
| Gradle cache | Use `setup-java`'s built-in cache: `cache: 'gradle'`. Area 1 found no reason to prefer a separate `gradle/actions/setup-gradle`; this matches LogViewer and is simplest. |
| Gradle wrapper perms | `chmod +x ./gradlew` on non-Windows runners (guarded `if: runner.os != 'Windows'`, or run unconditionally under bash where harmless). |
| Working directory | All Gradle commands run from repo root `JunieConversationViewer/` (the checkout root). |

Planned step order (per matrix job):

1. Checkout.
2. Set up JDK 21 (`matrix.java-distribution` — Temurin, except `microsoft` on Windows ARM64; Gradle cache).
3. Grant `gradlew` execute permission (non-Windows).
4. Linux Xvfb setup (Linux runners only).
5. Test gate (see §4).
6. Package installer (per-OS task).
7. Create distributable (`createDistributable`).
8. Prepare/rename/zip artifacts (per-OS).
9. Upload build artifacts (`actions/upload-artifact`).
10. Generate SHA256 checksums (tags only).
11. Publish GitHub Release (tags only).

---

## 4. Test Gate Design

- **Command choice:** run `./gradlew test` (the aggregate task). Per Area 1,
  Junie's tests live in `:shared` (`:shared:jvmTest`) and the aggregate `test`
  runs them; `test` is the broadest safe gate and matches the LogViewer pattern
  without needing a `:ui` module (which this project lacks).
  - **Recommendation to HITL:** use `./gradlew test`. If a stricter gate is
    preferred, `./gradlew check` also runs tests but adds any verification
    tasks; since there is no Detekt/quality-gate plugin, `check ≈ test` here.
    `:shared:jvmTest` alone is narrower and is **not** recommended as the sole
    gate because it would miss any future non-shared test source sets.
- **Ordering:** tests run **before** packaging. If tests fail, the job fails and
  no packaging/upload/Release step runs (they are later steps in the same job).
- **Linux under Xvfb:** on `ubuntu*` runners the test command runs via
  `xvfb-run ./gradlew test`; on macOS/Windows it runs `./gradlew test` directly
  (branch on `matrix.os` / `runner.os`).

---

## 5. Build Matrix Design

`strategy: fail-fast: false` so one OS failure does not cancel the others
(makes debugging per-OS packaging easier). Matrix uses `include:` entries:

| `os` (runner) | `artifact-suffix` | Arch | `java-distribution` | `package-task` | Installer output | Distributable output |
|---|---|---|---|---|---|---|
| `macos-latest` | `macos` | x64/arm64 (runner default) | `temurin` | `packageDmg` | `desktopApp/build/compose/binaries/main/dmg/*.dmg` | `desktopApp/build/compose/binaries/main/app` |
| `windows-latest` | `windows-x64` | x64 | `temurin` | `packageMsi` | `desktopApp/build/compose/binaries/main/msi/*.msi` | `desktopApp/build/compose/binaries/main/app` |
| `windows-11-arm` | `windows-arm64` | arm64 | `microsoft` | `packageMsi` | `desktopApp/build/compose/binaries/main/msi/*.msi` | `desktopApp/build/compose/binaries/main/app` |
| `ubuntu-latest` | `linux-x64` | x64 | `temurin` | `packageDeb` | `desktopApp/build/compose/binaries/main/deb/*.deb` | `desktopApp/build/compose/binaries/main/app` |
| `ubuntu-24.04-arm` | `linux-arm64` | arm64 | `temurin` | `packageDeb` | `desktopApp/build/compose/binaries/main/deb/*.deb` | `desktopApp/build/compose/binaries/main/app` |

Notes/assumptions:

- Package task invoked as `./gradlew :desktopApp:${{ matrix.package-task }}`.
- All jobs also run `./gradlew :desktopApp:createDistributable` for the zipped
  app image (Q-add).
- The macOS runner architecture (Intel vs Apple Silicon) is whatever
  `macos-latest` currently maps to; the produced `.dmg` targets that host arch.
  If a specific macOS arch is required later, pin the runner label — deferred.
- **ARM Linux risk (R):** ARM Linux `packageDeb` support depends on the JDK
  jpackage/Compose Desktop toolchain being available on `ubuntu-24.04-arm`. Not
  verified in Area 1 (macOS-only host). Treated as a **risk** (§9) to confirm on
  the runner in Area 7; if ARM packaging fails there, the ARM row can be dropped
  without affecting x64/macOS/Windows.
- **Windows x64 + Windows ARM64 (both native):** Windows has **no "universal"
  fat binary** — `jpackage`/Compose Desktop produces an architecture-specific
  `.msi` for the JDK/runner it runs on. To support both Copilot+ ARM PCs and
  traditional x64 PCs we build **two** MSIs on **two** native runners:
  `windows-latest` (x64) and `windows-11-arm` (arm64, GA on GitHub-hosted
  runners since Apr 2025). No cross-compilation and no emulation is used — each
  runner produces a native installer for its own architecture.
- **Windows ARM64 JDK caveat:** the `windows-11-arm` job must use
  `distribution: microsoft` (see §3) since Temurin has no Windows `aarch64`
  JDK 21. All other jobs stay on Temurin.

---

## 6. Linux Xvfb Design

- **Packages:** install via `sudo apt-get update && sudo apt-get install -y
  xvfb libegl1 libgles2 libgl1` (mirrors LogViewer; Compose Desktop needs GL
  libs even for packaging).
- **Applies to:** both `ubuntu-latest` (x64) and `ubuntu-24.04-arm` — guard with
  `if: startsWith(matrix.os, 'ubuntu')`.
- **Which commands run under `xvfb-run`:** the **test** command (Compose UI
  tests need a display). Packaging (`packageDeb`, `createDistributable`) is also
  run under `xvfb-run` defensively, matching the safest LogViewer-style pattern,
  since jpackage/Compose tooling may touch AWT/graphics during image assembly.
- macOS/Windows never use Xvfb.

---

## 7. Artifact Naming Design

- **Base name:** `JunieConversationViewer`.
- **Tag extraction:** use `${{ github.ref_name }}` (e.g. `v1.2.0`) directly; no
  stripping of the `v` prefix (keeps names unambiguous and matches LogViewer).
- **On-disk installer filename stays `com.knowledgespike.junieviewer-1.0.0.*`**
  (D3); the workflow **copies/renames** it into a release name below.

Naming patterns (with example `github.ref_name = v1.2.0`):

| Artifact | Pattern | Example |
|---|---|---|
| macOS installer | `JunieConversationViewer-<tag>-macos.dmg` | `JunieConversationViewer-v1.2.0-macos.dmg` |
| macOS distributable | `JunieConversationViewer-<tag>-macos-distributable.zip` | `JunieConversationViewer-v1.2.0-macos-distributable.zip` |
| Windows x64 installer | `JunieConversationViewer-<tag>-windows-x64.msi` | `JunieConversationViewer-v1.2.0-windows-x64.msi` |
| Windows x64 distributable | `JunieConversationViewer-<tag>-windows-x64-distributable.zip` | `JunieConversationViewer-v1.2.0-windows-x64-distributable.zip` |
| Windows ARM64 installer | `JunieConversationViewer-<tag>-windows-arm64.msi` | `JunieConversationViewer-v1.2.0-windows-arm64.msi` |
| Windows ARM64 distributable | `JunieConversationViewer-<tag>-windows-arm64-distributable.zip` | `JunieConversationViewer-v1.2.0-windows-arm64-distributable.zip` |
| Linux x64 installer | `JunieConversationViewer-<tag>-linux-x64.deb` | `JunieConversationViewer-v1.2.0-linux-x64.deb` |
| Linux x64 distributable | `JunieConversationViewer-<tag>-linux-x64-distributable.zip` | `JunieConversationViewer-v1.2.0-linux-x64-distributable.zip` |
| Linux ARM installer | `JunieConversationViewer-<tag>-linux-arm64.deb` | `JunieConversationViewer-v1.2.0-linux-arm64.deb` |
| Linux ARM distributable | `JunieConversationViewer-<tag>-linux-arm64-distributable.zip` | `JunieConversationViewer-v1.2.0-linux-arm64-distributable.zip` |
| SHA256 checksum | `<artifact-filename>.sha256` (one per file) | `JunieConversationViewer-v1.2.0-macos.dmg.sha256` |

- The `<tag>-<suffix>` piece uses `matrix.artifact-suffix` (`macos`,
  `windows-x64`, `windows-arm64`, `linux-x64`, `linux-arm64`).
- **`upload-artifact` design:** each matrix job uploads its prepared
  `release-artifacts/*` under artifact name
  `JunieConversationViewer-${{ matrix.artifact-suffix }}` (`actions/upload-artifact@v4`).
  This makes per-OS outputs downloadable from the workflow run even for
  non-tag debugging runs (though the workflow only triggers on tags this sprint).
- **Zip preparation** is per-OS: Linux/macOS use `zip -r` (bash); Windows uses
  `Compress-Archive` (`pwsh`) — matching LogViewer's split steps.

### 7.1 Checksum naming decision (for HITL)

Two options; **recommended: per-file `.sha256` sidecars** attached as separate
Release assets, so each download has a matching verifiable checksum. (LogViewer
uses a single `checksums.txt`; per-file sidecars are clearer for end users and
satisfy FR7.) HITL may prefer a single `checksums.txt` bundle instead.

---

## 8. Release Publishing Design

- **Action:** `softprops/action-gh-release@v2` (or latest v2).
- **Gate:** the Release step (and checksum generation) run only when
  `startsWith(github.ref, 'refs/tags/')`. Since the workflow only triggers on
  `v*` tags this sprint, this is belt-and-braces but kept for correctness.
- **Release title / `name`:** `JunieConversationViewer ${{ github.ref_name }}`
  (e.g. `JunieConversationViewer v1.2.0`).
- **`tag_name`:** `${{ github.ref_name }}`.
- **`draft`:** `false`.
- **Prerelease detection:** `prerelease: ${{ contains(github.ref_name, '-') }}`
  → `v1.0.0` normal release; `v1.0.0-beta.1` / `v1.0.0-rc1` prerelease (Q3).
- **`files`:** `release-artifacts/*` (installers + distributable zips +
  `.sha256` sidecars).
- **Release body (draft):** short auto-generated notes, e.g.:
  > Automated release for `${{ github.ref_name }}`.
  > Cross-platform Compose Desktop builds: macOS (.dmg), Windows x64 & ARM64
  > (.msi), Linux x64 & ARM64 (.deb), each with a zipped runnable distributable
  > and a SHA256 checksum. Built from tag `${{ github.ref_name }}`.
  (Final wording can be refined in Area 3; HITL to confirm shape.)
- **Auth:** `env: GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}`.

### 8.1 Failure behaviour

- **Tests fail →** job fails before packaging; nothing uploaded/released for
  that OS.
- **Packaging fails on one matrix entry →** because each matrix job runs its own
  Release step, other OS jobs still publish their assets to the **same** tag's
  Release (each `action-gh-release` call appends assets to the existing Release
  for that tag). This means a partial release is *possible* if one OS fails.
  - **Design note / HITL decision point:** LogViewer accepts this (per-job
    publish). If the HITL wants "all-or-nothing" releases, we would need a
    separate design: matrix jobs upload artifacts only, then a **single**
    dependent `release` job (`needs: build`) downloads all artifacts and
    publishes once. **Recommendation:** keep the simpler per-job publish for
    this sprint (matches LogViewer, `fail-fast: false` gives visibility), and
    note all-or-nothing as a possible follow-up. HITL to confirm.

---

## 9. Risks and Mitigations

| # | Risk | Mitigation |
|---|---|---|
| R1 | **ARM Linux packaging support** unverified — `ubuntu-24.04-arm` jpackage/Compose Desktop `.deb` may fail. | Verify on the runner in Area 7; `fail-fast: false` isolates it; ARM row can be dropped without impacting other OSes if unsupported. |
| R2 | **Linux display requirement** — Compose Desktop needs GL/display for tests and possibly packaging. | Install `xvfb libegl1 libgles2 libgl1`; run test (and packaging) under `xvfb-run` on both Linux runners. |
| R3 | **Release permission issues** — publishing needs write access. | `permissions: contents: write`; `GITHUB_TOKEN` env; drop unused `packages: write`. |
| R4 | **Cross-platform path/glob differences** — `*.dmg`/`*.msi`/`*.deb` globbing and zip differ per OS. | Per-OS prepare steps: bash `cp`/`zip` on Linux/macOS, `pwsh` `Copy-Item`/`Compress-Archive` on Windows; installer/dist paths carried in matrix vars. |
| R5 | **`packageVersion` drift** — installer filename embeds `1.0.0`, not the tag. | Do **not** change `packageVersion` (D3); apply the tag only via workflow copy/rename into `JunieConversationViewer-<tag>-<suffix>.*`. |
| R6 | **Partial release** if one OS packaging fails (per-job publish). | Documented as a design decision (§8.1); all-or-nothing single-release job offered as an alternative for HITL. |
| R7 | **Config cache warnings** on first CI runs (config cache enabled locally). | Monitor first runs (Area 7); no action expected. |
| R8 | **Windows ARM64 JDK distribution** — Temurin has **no** Windows `aarch64` JDK 21 (Adoptium build stalled, [temurin#271](https://github.com/adoptium/temurin/issues/271)), so `distribution: temurin` fails on `windows-11-arm`. | Use `distribution: microsoft` (Microsoft Build of OpenJDK ships Windows `aarch64` 21) for the ARM64 job only, carried via `matrix.java-distribution`; other jobs stay on Temurin. |
| R9 | **Windows ARM64 packaging/runner** unverified — `windows-11-arm` `packageMsi` (jpackage on ARM64) not run in Area 1 (macOS-only host); WiX/jpackage ARM support to confirm. | Verify on the runner in Area 7; `fail-fast: false` isolates it; the `windows-arm64` row can be dropped without affecting `windows-x64`/macOS/Linux if unsupported. |
| R10 | **No Windows universal binary** — a single MSI cannot serve both x64 and ARM64. | Ship two native MSIs from two native runners (`windows-latest` + `windows-11-arm`); document both downloads in the README (Area 5). |

---

## 10. Area 3 Implementation Checklist

When Area 2 is HITL-approved, Area 3 should create `.github/workflows/tag-build.yml` with:

- [ ] `name: Tag Build and Release`; `on: push: tags: [ "v*" ]`.
- [ ] `permissions: contents: write`.
- [ ] `strategy: fail-fast: false` + 5-row `include:` matrix (§5), incl. `windows-11-arm` (windows-arm64) and a per-row `java-distribution`.
- [ ] `defaults: run: shell: bash`.
- [ ] Step: `actions/checkout@v4`.
- [ ] Step: `actions/setup-java@v5` (`distribution: ${{ matrix.java-distribution }}` — Temurin everywhere except `microsoft` on Windows ARM64; Java 21, `cache: gradle`).
- [ ] Step: `chmod +x ./gradlew` (non-Windows).
- [ ] Step: Xvfb install on `ubuntu*` (`xvfb libegl1 libgles2 libgl1`).
- [ ] Step: tests — `xvfb-run ./gradlew test` on Linux, `./gradlew test` else; before packaging.
- [ ] Step: `./gradlew :desktopApp:${{ matrix.package-task }}`.
- [ ] Step: `./gradlew :desktopApp:createDistributable`.
- [ ] Step: prepare artifacts — copy/rename installer to `JunieConversationViewer-<tag>-<suffix>.<ext>`, zip `.../main/app` to `...-distributable.zip` (bash on Linux/macOS, pwsh on Windows).
- [ ] Step: `actions/upload-artifact@v4` per OS.
- [ ] Step: generate `.sha256` sidecars (tags only).
- [ ] Step: `softprops/action-gh-release@v2` (tags only) with title, `tag_name`, `prerelease: contains(ref_name,'-')`, `files: release-artifacts/*`, `GITHUB_TOKEN`.
- [ ] Do **not** modify `desktopApp/build.gradle.kts` or `packageVersion`.

---

## 11. HITL Questions / Approval Checklist

Please approve (or amend) the following before Area 3 implementation:

1. **Workflow name/display name** — file `tag-build.yml`, display `Tag Build and Release`. ✅/✏️
2. **Matrix incl. ARM Linux + Windows ARM64** — `macos-latest`, `windows-latest` (windows-x64), **`windows-11-arm` (windows-arm64)**, `ubuntu-latest` (linux-x64), `ubuntu-24.04-arm` (linux-arm64). ✅/✏️
2a. **Windows ARM64 JDK distribution** — use `microsoft` on `windows-11-arm` (Temurin has no Windows aarch64 21); Temurin elsewhere. Confirm no Windows "universal" binary is expected — two native MSIs instead. ✅/✏️
3. **Test command choice** — `./gradlew test` (aggregate), under `xvfb-run` on Linux, before packaging. ✅/✏️
4. **Artifact naming** — `JunieConversationViewer-<tag>-<suffix>.<ext>` (+ `-distributable.zip`) per §7. ✅/✏️
5. **Installer + zipped distributable** — publish both per OS (Q-add). ✅/✏️
6. **SHA256 attachment** — per-file `.sha256` sidecars as separate Release assets (vs single `checksums.txt`). ✅/✏️
7. **Prerelease detection** — tags containing `-` → `prerelease: true`. ✅/✏️
8. **Release title/body** — title `JunieConversationViewer <tag>`; body draft per §8. ✅/✏️
9. **Release failure policy** — per-job publish (partial release possible, R6) vs single all-or-nothing `release` job. ✅/✏️

Task 2.4 stays unchecked until this approval is explicit.

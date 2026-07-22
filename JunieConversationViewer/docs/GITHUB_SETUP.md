# GitHub Setup Guide — CI and Publish/Release

This guide walks a maintainer through setting up a GitHub repository so the
Sprint 7 tag-triggered CI/release workflow can build and publish the **Junie
Conversation Viewer** across macOS, Windows (x64 + ARM64), and Linux (x64 +
ARM64).

> **Status note.** The workflow file `.github/workflows/tag-build.yml` is
> **not implemented yet** — it is designed in
> [`docs/sprint-7-area-2-workflow-design.md`](sprint-7-area-2-workflow-design.md)
> and will be created in Sprint 7 **Area 3**. Steps below that describe the
> workflow's runtime behaviour (release triggering, artifact production,
> monitoring, verification) therefore describe the **designed** behaviour and
> apply once Area 3 lands. They are marked **(depends on Area 3)**.

---

## Prerequisites

- A **GitHub account** and permission to create/administer a repository.
- **git** installed locally and configured (`git --version`).
- **JDK 21** installed locally if you want to verify a packaging build before
  pushing (the CI runners install JDK 21 themselves — see step 3).
- The project builds locally: from the repository root you can run
  `./gradlew :desktopApp:packageDistributionForCurrentOS` and produce an
  installer under `desktopApp/build/compose/binaries/main/`.

No custom repository **secrets** are required. The workflow authenticates
GitHub Release publishing with the **built-in `GITHUB_TOKEN`** that GitHub
provides to every Actions run — you do not need to create or store a personal
access token.

---

## 1. Create and push the repository

1. Create a new repository on GitHub (public is recommended so the ARM runners
   and Actions minutes are free — see step 3).
2. From your local project root, add the remote and push:

   ```bash
   git init                       # if not already a git repo
   git add .
   git commit -m "chore: initial import"
   git branch -M main
   git remote add origin https://github.com/<owner>/<repo>.git
   git push -u origin main
   ```

3. Confirm the code (including the `JunieConversationViewer/` project and its
   `gradlew` wrapper) is visible on GitHub.

---

## 2. Enable GitHub Actions and grant release permissions

1. In the repository, open **Settings → Actions → General**.
2. Under **Actions permissions**, ensure Actions are **allowed** to run
   (the default "Allow all actions and reusable workflows" is fine).
3. Under **Workflow permissions**, select **Read and write permissions**, then
   **Save**.

   **Why:** the release workflow publishes a GitHub Release, which requires the
   `GITHUB_TOKEN` to have write access to repository contents. The workflow
   declares `permissions: contents: write`, but that request is only granted if
   the repository's default workflow permissions allow write. A read-only
   default causes the Release step to fail with a `403` (see Troubleshooting).

---

## 3. Confirm hosted runner availability

The workflow builds on a matrix of GitHub-hosted runners. Confirm these labels
are available to your repository:

| Platform | Runner label | Notes |
| :--- | :--- | :--- |
| macOS | `macos-latest` | Builds the `.dmg`. |
| Windows x64 | `windows-latest` | Builds the x64 `.msi`. |
| Windows ARM64 | `windows-11-arm` | Builds the ARM64 `.msi`; GA on GitHub-hosted runners since Apr 2025. |
| Linux x64 | `ubuntu-latest` | Builds the x64 `.deb`. |
| Linux ARM64 | `ubuntu-24.04-arm` | Builds the ARM64 `.deb`. |

Notes:

- **Free for public repositories.** Standard and ARM GitHub-hosted runners run
  without minute charges on public repos. On private repos, ARM/Windows minutes
  consume your plan's allowance.
- **Windows has no universal binary.** `jpackage`/Compose Desktop emits an
  architecture-specific `.msi`, so both an x64 and an ARM64 MSI are built on
  their own native runners.
- **JDK distribution caveat (depends on Area 3).** Every job installs **JDK 21**
  via `actions/setup-java`. All jobs use **Temurin** except the
  `windows-11-arm` job, which uses the **Microsoft Build of OpenJDK**
  (`distribution: microsoft`) because Temurin does not ship a Windows `aarch64`
  JDK 21.
- **Fallback if an ARM runner is unavailable.** The matrix uses
  `fail-fast: false`, so a missing/unsupported ARM runner isolates to its own
  job. If `windows-11-arm` or `ubuntu-24.04-arm` is unavailable in your account,
  that row can be dropped without affecting the other platforms.

---

## 4. Publish a release (depends on Area 3)

Releases are triggered **only** by pushing a version tag matching `v*` — there
is no build on `main` pushes or pull requests this sprint.

1. Make sure `main` contains the commit you want to release.
2. Create and push an annotated tag:

   ```bash
   git tag v1.0.0
   git push origin v1.0.0
   ```

3. **Prerelease convention:** a tag containing a **hyphen** publishes as a
   GitHub **prerelease**; a tag with no hyphen publishes as a normal release.

   | Tag | Result |
   | :--- | :--- |
   | `v1.0.0` | Normal release |
   | `v1.1.0` | Normal release |
   | `v1.0.0-rc1` | Prerelease |
   | `v1.0.0-beta.1` | Prerelease |

4. **`packageVersion` stays `1.0.0`.** The Gradle `packageVersion` is not
   changed by tagging. The tag only affects the **artifact filenames** and the
   **Release name** (e.g. `JunieConversationViewer-v1.1.0-macos.dmg`); the
   embedded application/package version remains `1.0.0`.

---

## 5. Monitor the workflow run (depends on Area 3)

1. Open the **Actions** tab of the repository.
2. Select the **Tag Build and Release** run triggered by your tag.
3. Each matrix job (macOS, Windows x64, Windows ARM64, Linux x64, Linux ARM64)
   runs tests first, then packages. If tests fail, that job's packaging does not
   run.
4. When all jobs succeed, the run publishes a GitHub **Release** for the tag with
   the built artifacts attached (see step 6).

---

## 6. Verify the produced artifacts (depends on Area 3)

Open **Releases** (or the run's uploaded artifacts) and confirm the following
per-OS files are attached, each alongside a `.sha256` checksum file:

| Platform | Installer | Zipped distributable |
| :--- | :--- | :--- |
| macOS | `JunieConversationViewer-<tag>-macos.dmg` | `...-macos-distributable.zip` |
| Windows x64 | `JunieConversationViewer-<tag>-windows-x64.msi` | `...-windows-x64-distributable.zip` |
| Windows ARM64 | `JunieConversationViewer-<tag>-windows-arm64.msi` | `...-windows-arm64-distributable.zip` |
| Linux x64 | `JunieConversationViewer-<tag>-linux-x64.deb` | `...-linux-x64-distributable.zip` |
| Linux ARM64 | `JunieConversationViewer-<tag>-linux-arm64.deb` | `...-linux-arm64-distributable.zip` |

Each artifact has a matching `<artifact-filename>.sha256`. To verify a download:

```bash
# macOS / Linux
shasum -a 256 -c JunieConversationViewer-v1.0.0-macos.dmg.sha256
```

```powershell
# Windows (PowerShell)
$expected = (Get-Content JunieConversationViewer-v1.0.0-windows-x64.msi.sha256).Split(" ")[0]
$actual   = (Get-FileHash JunieConversationViewer-v1.0.0-windows-x64.msi -Algorithm SHA256).Hash
if ($expected -ieq $actual) { "OK" } else { "MISMATCH" }
```

The **installer** is the native, installable package for each OS. The **zipped
distributable** is a no-install runnable application image for users who prefer
to unzip and run.

---

## 7. Troubleshooting

- **Release step fails with `403`/permission error.** The repository's default
  workflow permissions are read-only. Fix per step 2: **Settings → Actions →
  General → Workflow permissions → Read and write**. No custom token is needed —
  the built-in `GITHUB_TOKEN` is sufficient once write is allowed.
- **ARM runner unavailable / job cannot start.** If `windows-11-arm` or
  `ubuntu-24.04-arm` is not available to your account, `fail-fast: false` keeps
  the other jobs running. You can temporarily remove the affected matrix row;
  the remaining platforms still build and publish.
- **Windows ARM64 job fails installing the JDK.** Temurin has no Windows
  `aarch64` JDK 21; the ARM64 job must use `distribution: microsoft`. If you
  adapt the workflow, keep that distinction (depends on Area 3).
- **Linux job fails with a display/`DISPLAY` or headless error.** Compose
  Desktop packaging on Linux needs a virtual display. The workflow installs Xvfb
  packages (`xvfb libegl1 libgles2 libgl1`) and runs Gradle under `xvfb-run` on
  Linux runners (depends on Area 3).
- **First-run Gradle configuration-cache warnings.** The first CI runs may log
  configuration-cache warnings because the cache is cold. These are expected on
  a fresh runner and do not fail the build.

---

## Related documentation

- [`docs/sprint-7-area-2-workflow-design.md`](sprint-7-area-2-workflow-design.md)
  — the authoritative workflow design (matrix, tasks, artifact naming, release
  publishing). This guide is a distilled operator view; if the two ever
  disagree, the design document is the source of truth.
- [`docs/HOW_TO_USE.md`](HOW_TO_USE.md) — how to use the application once it is
  installed.

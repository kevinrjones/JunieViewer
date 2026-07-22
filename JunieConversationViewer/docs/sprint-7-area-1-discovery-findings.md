# Sprint 7 Area 1 — Discovery and Scope Confirmation Findings

> **Date:** 2026-07-22
> **Author:** Junie (automated discovery)
> **Status:** HITL review complete — Area 1 closed (tasks 1.1–1.6 complete; open questions resolved, see §7)
> **Sprint:** [Sprint 7 — CI/GitHub Automation and GitHub README](sprints/junie-conversation-viewer-sprint-7-ci-github-automation-and-readme.md)
> **Task doc:** [Sprint 7 Task Breakdown](tasks/junie-conversation-viewer-tasks-sprint-7-ci-github-automation-and-readme.md)

---

## 1. Summary

Area 1 confirms every fact the Sprint 7 tag-build workflow and README rewrite depend on, **without writing any workflow YAML, rewriting the README, or changing `desktopApp/build.gradle.kts`**.

What Area 1 confirmed:

- All assumed Compose Desktop packaging/build Gradle tasks (`packageDmg`, `packageMsi`, `packageDeb`, `createDistributable`, `packageDistributionForCurrentOS`) **exist exactly as named** under `:desktopApp`, plus `Release` variants and `runDistributable`.
- The on-disk output paths under `desktopApp/build/compose/binaries/main/` are confirmed by a **real local macOS package run** (DMG + distributable produced).
- The current `README.md` is a minimal Kotlin-Multiplatform starter README with **no badges** and one factual drift (JDK 17 vs the sprint's Java 21).
- `docs/HOW_TO_USE.md` is a complete, well-structured usage reference suitable to remain the authoritative deep-dive that the README summarizes and links to.
- The LogViewer reference workflow **is available locally** at `../LogViewer/.github/workflows/build.yml` and maps cleanly onto this project with a small number of adaptations (module rename `:app` → `:desktopApp`; drop the Detekt gate; rename artifacts to `JunieConversationViewer`).

**Recommendation:** Workflow design (Area 2) can proceed once the HITL approves these findings and resolves open questions Q1–Q6 (Section 7). Nothing discovered blocks implementation; the packaging assumptions in the sprint doc are fully validated.

---

## 2. Documentation Reviewed

| Document | Key findings |
|---|---|
| `docs/tasks/...-sprint-7-...md` | 46 tasks across 8 areas. Decisions D1–D4 recorded in the Notes/Decisions Log. Area 1 = tasks 1.1–1.6; 1.6 is a `HITL Review` gate. Constraints: confirm task names before hard-coding (D4), `packageVersion` stays `1.0.0` (D3). |
| `docs/sprints/...-sprint-7-...md` | Sprint goal: tag-triggered cross-platform build + GitHub Release + GitHub-ready README subsuming `HOW_TO_USE.md`. Design Findings §5.1–5.3 describe LogViewer reuse. Open questions Q1–Q6 (§18). README blueprint (§13, 17 sections). Risks R1–R6. |
| `docs/sprints/...-sprint-6-...md` + task doc | Sprint 6 delivered a green-build code-quality remediation baseline (425 tests, 0 failures). No Detekt/quality-gate plugin is configured. This is the baseline Sprint 7 builds on. |
| `docs/UBIQUITOUS-LANGUAGE.md` | Canonical terms to keep consistent in README prose: Conversation, Session, Event, Message, Human, Junie, Search Query, Filter, Message Kind, HITL. |
| `docs/TESTING.md` | Test stack (JUnit4, Turbine, Compose Test Rule, Strikt, Okio); commands `./gradlew :shared:jvmTest`, `./gradlew test`, `./gradlew check`. These are the CI test commands. |
| `docs/project_memory.md` / `docs/RECAP.md` | Chronological history and decisions; to be updated at Sprint 7 completion (Area 6/8), not in Area 1. |
| `docs/HOW_TO_USE.md` | Full 167-line usage guide (see Section 5). |
| `README.md` | Minimal starter README (see Section 4). |
| `docs/sprint-6-area-1-discovery-findings.md` | Used as the structural template for this findings document. |

### Confirmed decisions D1–D4 (from the task doc Notes/Decisions Log)

- **D1** — Full cross-platform matrix: macOS (`.dmg`), Windows (`.msi`), Linux (`.deb`, with Xvfb).
- **D2** — Publish a GitHub Release on tags via `softprops/action-gh-release`, gated on `refs/tags/`.
- **D3** — Embed the git tag in artifact/Release names only; Gradle `packageVersion` stays `1.0.0` (true tag-driven versioning deferred).
- **D4** — Confirm exact packaging task names and output paths before hard-coding them into the workflow (this Area).

---

## 3. Gradle Packaging Findings

### 3.1 Commands run

| Command | Result |
|---|---|
| `./gradlew :desktopApp:tasks` | **Success.** Listed all packaging/distribution tasks (below). |
| `./gradlew :desktopApp:packageDistributionForCurrentOS` | **Success (2m 40s)** on macOS. Produced the DMG and the distributable `app`; `packageDeb`/`packageMsi` correctly `SKIPPED` on macOS. |

### 3.2 Confirmed task names (all exist under `:desktopApp`)

- `:desktopApp:packageDmg` ✅
- `:desktopApp:packageMsi` ✅
- `:desktopApp:packageDeb` ✅
- `:desktopApp:createDistributable` ✅
- `:desktopApp:packageDistributionForCurrentOS` ✅
- Additional (available, not required this sprint): `createReleaseDistributable`, `packageReleaseDmg/Msi/Deb`, `packageReleaseDistributionForCurrentOS`, `packageUberJarForCurrentOS`, `packageReleaseUberJarForCurrentOS`, `runDistributable`, `run`, `hotRun`.

The build config (`desktopApp/build.gradle.kts`) sets `targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)`, `packageName = "com.knowledgespike.junieviewer"`, `packageVersion = "1.0.0"`, `mainClass = "com.knowledgespike.junieviewer.MainKt"`.

### 3.3 Confirmed output paths (under `desktopApp/build/compose/binaries/main/`)

| Artifact | Path (relative to repo root `JunieConversationViewer/`) | Confirmed by |
|---|---|---|
| Distributable app image | `desktopApp/build/compose/binaries/main/app/` (e.g. `com.knowledgespike.junieviewer.app` on macOS) | Local run ✅ |
| macOS installer | `desktopApp/build/compose/binaries/main/dmg/com.knowledgespike.junieviewer-1.0.0.dmg` | Local run ✅ |
| Windows installer | `desktopApp/build/compose/binaries/main/msi/*.msi` | Inferred (standard Compose layout; not runnable on macOS) |
| Linux installer | `desktopApp/build/compose/binaries/main/deb/*.deb` | Inferred (standard Compose layout; not runnable on macOS) |

The installer filename embeds `packageVersion` (`...-1.0.0.dmg`). Because D3 keeps `packageVersion = 1.0.0`, **installer filenames stay `...-1.0.0.*`**; the git **tag** must therefore be applied in the workflow's artifact/Release *names* (upload-artifact `name:` and copied/renamed files), not derived from the on-disk installer filename.

### 3.4 Recommended task/path mapping per OS (for Area 2/3)

| Runner | Package task | Installer output | Distributable output |
|---|---|---|---|
| `macos-latest` | `:desktopApp:packageDmg` | `desktopApp/build/compose/binaries/main/dmg/*.dmg` | `.../main/app/` |
| `windows-latest` | `:desktopApp:packageMsi` | `desktopApp/build/compose/binaries/main/msi/*.msi` | `.../main/app/` |
| `ubuntu-latest` | `:desktopApp:packageDeb` (under `xvfb-run`) | `desktopApp/build/compose/binaries/main/deb/*.deb` | `.../main/app/` |

### 3.5 `createDistributable` vs installer tasks

- The installer tasks (`packageDmg/Msi/Deb`) already **depend on** `createRuntimeImage`/`createDistributable`, so running the installer task alone produces the installer without a separate `createDistributable` call.
- `createDistributable` is only needed **additionally** if the workflow also wants to publish the plain (non-installer) executable `app/` image as a zip — which is exactly what LogViewer does. **Recommendation:** follow LogViewer and run `createDistributable` too, so each OS uploads both an installer and a zipped runnable distributable. This is a Q for the HITL only insofar as they may want installer-only artifacts (see Q-add in Section 7).

### 3.6 Notes on `packageVersion`

- `packageVersion` remains `"1.0.0"` (D3). This Area did **not** modify `desktopApp/build.gradle.kts`. Installer filenames will contain `1.0.0`; the tag is carried in workflow artifact/Release names only.

### 3.7 Risks / unknowns

- **Windows/Linux packaging not run locally** (single macOS host). The `.msi`/`.deb` task existence is confirmed via the task list, and the output subfolder names (`msi/`, `deb/`) follow the standard Compose Desktop convention, but the actual installer production must be verified on GitHub `windows-latest`/`ubuntu-latest` runners in Area 7.
- **Linux needs a display** for Compose Desktop — packaging/tests must run under `xvfb-run` (Risk R2).
- **Configuration cache** is enabled (`org.gradle.configuration-cache=true`); the local run stored a config-cache entry without error, so CI should be unaffected, but watch for cache-related warnings on first CI runs.

---

## 4. README Findings

### 4.1 Current state

`README.md` (76 lines) is a lightly-customized Kotlin Multiplatform starter README:

- **Sections present:** Title + one-paragraph description, Features (large bullet list — accurate and current), Getting Started (Prerequisites, Running, Configuration), Project Structure, Testing, and a trailing KMP link.
- **Tone/target:** developer-oriented "run from source", not a GitHub front-door for end users downloading installers.

### 4.2 Badge gaps

- **No badges at all.** FR11 wants: tag-build workflow status, Kotlin, Java 21, Compose Desktop, and — per the HITL decision on Q6 — a **License** badge (a license file will be added in Area 5/6).

### 4.3 Content to preserve vs. replace

| Preserve (accurate, reusable) | Replace / fix |
|---|---|
| Feature list (bullets are current and detailed) | **JDK version drift:** README says "JDK 17 or higher"; sprint standard is **Java 21** — reconcile in Area 5. |
| Run-from-source commands (`:desktopApp:run`, `:desktopApp:hotRun --auto`) | Add **Installation from GitHub Releases** section (new, post-CI). |
| Configuration/logging paragraph (logback, `~/.junieviewer/logs`) | Add **badges**, **screenshot placeholder**, **build/package-locally** (confirmed tasks), **usage summary** (subsuming `HOW_TO_USE.md`), **keyboard shortcuts**, **Sessions path**, **troubleshooting**, **doc links**, **status/limitations**, **contributing**. |
| Doc links to `HOW_TO_USE.md` and `TESTING.md` | Expand doc links (add `UBIQUITOUS-LANGUAGE.md`, sprint docs). |
| Project Structure section | Keep, lightly trimmed. |

### 4.4 Recommended README skeleton for Area 5 (per sprint §13 blueprint)

Title → short description → badges → screenshot placeholder → Features → Installation (GitHub Releases) → Run from source → Build/package locally (confirmed tasks) → How to use (summary + link to `HOW_TO_USE.md`) → Keyboard shortcuts → Sessions path (`~/.junie/sessions/`) → Logs path (`~/.junieviewer/logs/`) → Troubleshooting → Development/testing commands → Documentation links → Status/limitations → Contributing.

---

## 5. HOW_TO_USE Subsumption Plan

`docs/HOW_TO_USE.md` (167 lines) is comprehensive and well-organized. Sections: Overview, Selecting a Session, Toolbar, Application Menu, Keyboard Shortcuts (full table), Reading the Conversation, Search, Filters, Copying/Text Selection, Rich Content Blocks, Sort Order, Collapse/Show All, Live Session Tracking, About/How to Use, Theme, Troubleshooting, Known Limitations.

### 5.1 What the README should summarize (subsume)

- A short **"How to use"** section covering: selecting a Session, the Toolbar's 7 commands + search, Filters, sort order, collapse/show all, and live auto-refresh — each in 1–2 lines.
- The **keyboard-shortcuts table** (small and high-value for a README) *or* a condensed subset with a link to the full table.
- The **Sessions path** (`~/.junie/sessions/`) and **logs path** (`~/.junieviewer/logs/`).
- A brief **troubleshooting** subset (no Sessions found, no live updates, logs location).

### 5.2 What HOW_TO_USE keeps as the detailed reference

- Per-Message-Kind rendering details, Turn layout specifics, rich-content block behaviours, copy/selection nuances, theme details, partial-write/live-tracking edge cases, and the full Known Limitations list. These stay in `HOW_TO_USE.md` as the authoritative deep-dive.

### 5.3 Contradiction risks to manage

- **Shortcut tables** could drift if duplicated verbatim — prefer the README linking to the single table in `HOW_TO_USE.md`, or keep the README copy explicitly marked as a summary.
- **Filter/always-shown-Kinds** lists are detailed in `HOW_TO_USE.md`; the README should summarize, not restate exhaustively, to avoid divergence.
- **JDK version:** README currently says 17; keep a single source of truth (Java 21) across README, badges, and docs.

### 5.4 Recommended cross-linking approach

- README "How to use" section ends with: *"For the complete usage reference, see [docs/HOW_TO_USE.md](docs/HOW_TO_USE.md)."*
- `HOW_TO_USE.md` gains a top note: *"For installation, build, and a quick overview, see the [project README](../README.md)."* (Area 6, task 6.1.)
- **Single source of usage truth:** `HOW_TO_USE.md` is authoritative for detailed behaviour; the README is the front door with a summary.

---

## 6. LogViewer CI Reuse Findings

**Reference availability:** The LogViewer workflow **is available locally** at `../LogViewer/.github/workflows/build.yml` (151 lines) and was read in full — no unavailable-reference limitation applies.

### 6.1 Reusable pieces (adapt)

| Element | LogViewer | Adaptation for Junie |
|---|---|---|
| **JDK setup** | `actions/setup-java@v5`, Temurin, Java 21, `cache: gradle` | Reuse verbatim. |
| **OS matrix (structure)** | `include:` list with `os`, `artifact-suffix`, `package-task`, `dist-path`, `installer-path` | Reuse the shape; change `dist-path`/`installer-path` prefix `app/` → `desktopApp/`. |
| **Per-OS package tasks** | `packageDeb`/`packageMsi`/`packageDmg` | Same task names; change module `:app` → `:desktopApp`. |
| **Linux Xvfb** | `apt-get install xvfb libegl1 libgles2 libgl1`; run tests under `xvfb-run` | Reuse; run `:shared:jvmTest`/`test` and packaging under `xvfb-run`. |
| **createDistributable** | separate step producing the zipped executable | Reuse to also publish the `app/` image (see §3.5). |
| **Artifact upload** | `actions/upload-artifact@v7` with per-OS name | Reuse; base name `JunieConversationViewer`, embed the tag (D3). |
| **SHA256 checksums** | `sha256sum` on tags only | Reuse (optional per FR7). |
| **Release publishing** | `softprops/action-gh-release@v3`, gated on `refs/tags/`, `prerelease: contains(ref_name, '-')`, default `GITHUB_TOKEN` | Reuse directly (matches D2 + Q3). |
| **`permissions: contents: write`** | present | Reuse (Risk R3); `packages: write` not needed (no registry publish). |
| **`chmod +x gradlew`** | present | Reuse on non-Windows. |

### 6.2 Pieces to drop or adapt

- **Detekt steps** (`Run Detekt`, `Upload Detekt Reports`, `Enforce Detekt Quality Gate`): **DROP** — this project has no Detekt/quality-gate plugin (sprint §4.3). 
- **Triggers:** LogViewer runs on `workflow_dispatch` + `push main` + `tags v*` + PRs. Sprint 7 is **tag-only (`v*`)** for the first workflow (see Q1/Q2). Drop `main`/PR triggers unless the HITL opts in.
- **`ubuntu-24.04-arm` matrix row:** LogViewer builds ARM Linux. **HITL decided to include ARM now** (Q5) — **keep** the `ubuntu-24.04-arm` row (also under Xvfb) alongside the x64 runners.
- **Second UI-test step** (`:ui:desktopTest`): LogViewer has a `:ui` module; Junie's tests live in `:shared` (`:shared:jvmTest`) and the aggregate `test`. Use Junie's commands instead.
- **Artifact base name** `KLogViewer` → `JunieConversationViewer`.

### 6.3 Recommended step flow for Junie (Area 2/3)

`checkout → setup-java 21 (+gradle cache) → chmod gradlew (non-Windows) → Xvfb (Linux) → tests (:shared:jvmTest / test, under xvfb-run on Linux) → package (per-OS task) → createDistributable → prepare/rename tag-aware artifacts → upload-artifact → on tags: checksums + gh-release (prerelease if tag contains '-')`.

---

## 7. Open Questions — RESOLVED by HITL (2026-07-22)

The HITL reviewed the findings and resolved every open question. Two decisions deviate from the discovery recommendation (Q5 and Q6) — both are noted below and carried into later Areas.

| ID | Question | **HITL decision** |
|---|---|---|
| **Q1** | Name the workflow `.github/workflows/tag-build.yml`? | **Yes** — use `tag-build.yml`. |
| **Q2** | Also build on `main`/PR for CI feedback now, or stay tag-only this sprint? | **Tag-only this sprint.** PR/`main` CI deferred to a future low-risk follow-up. |
| **Q3** | Treat tags containing `-` (e.g. `v1.0.0-rc1`) as prereleases? | **Yes** — `prerelease: ${{ contains(github.ref_name, '-') }}`. |
| **Q4** | Which docs get cross-linked/updated; who is the usage authority? | **`HOW_TO_USE.md` is authoritative; README summarizes + links.** Cross-link both ways; update `RECAP`/`project_memory` on completion (Area 6). |
| **Q5** | Include ARM Linux runner (`ubuntu-24.04-arm`) like LogViewer? | **Include ARM now** (deviates from discovery's "defer"). Matrix adds `ubuntu-24.04-arm` alongside `ubuntu-latest`, `windows-latest`, `macos-latest`. |
| **Q6** | Add a software license so a License badge can be shown? | **Add a license** (deviates from discovery's "omit"). A license file will be added and a License badge shown in the README. **Follow-up:** the specific license (e.g. MIT / Apache-2.0) must be chosen when the license file is added in Area 5/6. |
| **Q-add** | Installer-only, or installer **and** zipped distributable? | **Both** — publish per-OS installer plus a zipped `createDistributable` app image. |

**Resolved HITL-decision coverage requested by the task:**

- **PR/main CI now vs defer:** defer — tag-only this sprint (Q2).
- **Release artifact expectations:** per-OS installer (`.dmg`/`.msi`/`.deb`) **plus** zipped distributable **plus** SHA256 checksums, attached to a GitHub Release on tags (Q-add, D2).
- **Prerelease tag behavior:** tags with `-` → prerelease (Q3).
- **README/HOW_TO_USE authority:** `HOW_TO_USE.md` authoritative; README summarizes (Q4).
- **ARM Linux runner:** **include now** — add `ubuntu-24.04-arm` to the matrix (Q5).
- **License badge:** **add a license** and show the badge; specific license TBD when the file is created (Q6).

---

## 8. Recommended Next Steps

1. **HITL review complete (2026-07-22)** — Area 1 findings approved and Q1–Q6 (+ Q-add) resolved (§7). Task 1.6 is now checked; Area 1 is closed.
2. On approval, **proceed to Area 2 — GitHub Actions Workflow Design** (trigger/JDK/cache, matrix + per-OS tasks using the §3.4 mapping, artifact naming, Release publishing).
3. **Do not start workflow implementation (Area 3)** until the Area 2 design is approved (task 2.4).
4. Windows/Linux packaging remains to be verified on GitHub runners (Area 7); only macOS packaging was verified locally.

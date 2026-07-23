# Sprint 7 — CI/GitHub Automation and GitHub README: Task Breakdown

## 1. Related Sprint

**Sprint document:** [`docs/sprints/junie-conversation-viewer-sprint-7-ci-github-automation-and-readme.md`](../sprints/junie-conversation-viewer-sprint-7-ci-github-automation-and-readme.md)

**Sprint goal:** Give the Junie Conversation Viewer a GitHub-facing presence — a tag-triggered GitHub Actions workflow that builds and packages the Compose Desktop app across macOS, Windows, and Linux and publishes a GitHub Release on version tags, plus a GitHub-ready `README.md` that subsumes `docs/HOW_TO_USE.md`.

## 2. Related Documents

| Document | Role |
|----------|------|
| [`docs/sprints/junie-conversation-viewer-sprint-7-ci-github-automation-and-readme.md`](../sprints/junie-conversation-viewer-sprint-7-ci-github-automation-and-readme.md) | **Primary source of truth.** Defines the delivery areas and requirements. |
| [`docs/sprints/junie-conversation-viewer-sprint-6-code-quality-remediation.md`](../sprints/junie-conversation-viewer-sprint-6-code-quality-remediation.md) | Preceding sprint; Sprint 7 builds on its green-build baseline. |
| [`docs/tasks/junie-conversation-viewer-tasks-sprint-6-code-quality-remediation.md`](junie-conversation-viewer-tasks-sprint-6-code-quality-remediation.md) | Sprint 6 task breakdown for reference. |
| [`docs/HOW_TO_USE.md`](../HOW_TO_USE.md) | Detailed usage reference the README subsumes and links to. |
| [`docs/UBIQUITOUS-LANGUAGE.md`](../UBIQUITOUS-LANGUAGE.md) | Canonical domain terms. |
| [`docs/RECAP.md`](../RECAP.md) | Chronological project history. |
| [`docs/TESTING.md`](../TESTING.md) | Testing stack, Robot pattern, `testTag` conventions. |
| [`docs/project_memory.md`](../project_memory.md) | Decisions, gotchas, shipped work. |

## 3. Purpose

This document breaks Sprint 7 into concrete, trackable tasks. It serves as:

- **Junie's implementation checklist** — each task has clear completion criteria, dependencies, and testing expectations.
- **HITL's review and progress checklist** — each task has a checkbox, and review-oriented tasks include HITL-visible outcomes.

## 4. How to Use This Task Document

1. **Before starting implementation**, read the Related Documents listed above.
2. **Work through tasks in area order** (1–8). Within each area, tasks are ordered by dependency.
3. **Check off tasks** (`- [x]`) only when all completion criteria are met.
4. **Mark parent tasks complete** only when all subtasks are complete.
5. **Use inline markers** (see Task Status Legend) to flag blocked, deferred, or review-dependent tasks.
6. **Update the Progress Summary** table as areas are completed.

## 5. Progress Summary

| # | Task Area | Status | Task Count |
|---|-----------|--------|------------|
| 1 | Discovery and Scope Confirmation | 6/6 complete | 6 |
| 2 | GitHub Actions Workflow Design | 3/4 complete (awaiting HITL review) | 4 |
| 3 | Tag Build Workflow Implementation | 8/9 in progress (awaiting HITL review) | 9 |
| 4 | Versioning and Artifact Naming | 4/4 complete | 4 |
| 5 | GitHub README | 6/7 complete (awaiting HITL review) | 7 |
| 6 | Documentation Updates | 1/6 in progress | 6 |
| 7 | Testing and Local Verification | 0/6 not started | 6 |
| 8 | Review, Cleanup, and Completion | 0/5 not started | 5 |
| | **Total** | **28/47 in progress** | **47** |

## 6. Task Status Legend

- `- [ ]` — Task not started or not complete.
- `- [x]` — Task complete and reviewed where review is required.

**Inline markers:**

- **`HITL Review`** — Task requires HITL visual or functional review before it can be marked complete.
- **`Blocked`** — Task is blocked by an external dependency or unresolved question.
- **`Deferred`** — Task has been explicitly moved out of this sprint's scope.
- **`Depends on [task]`** — Task depends on another task being completed first.
- **`Test Required`** — Task must have automated test coverage before completion.
- **`Manual Review Required`** — Task requires manual verification.

---

## 7. Implementation Task List

### Area 1 — Discovery and Scope Confirmation

*Source: Delivery Area 1. Confirms Gradle structure, packaging tasks/paths, existing docs, and reusable LogViewer CI elements before any workflow code.*

#### 1.1 Read project and sprint documentation

- [x] Read project and sprint documentation

**Description:** Read the Sprint 7 sprint doc, `UBIQUITOUS-LANGUAGE.md`, `TESTING.md`, `project_memory.md`, `RECAP.md`, and Sprint 6 docs to understand the current baseline.

**Source:** Sprint doc section 2 (Related Documents).

**Dependencies:** None.

**Likely files / areas:** Documentation only.

**Completion criteria:**
- Key terminology, baseline, and confirmed decisions (D1–D4) are understood.

**Testing expectations:** None.

#### 1.2 Confirm exact Compose Desktop packaging/build task names and output paths — `Manual Review Required`

- [x] Confirm exact Compose Desktop packaging/build task names and output paths

**Description:** Run `./gradlew :desktopApp:tasks` (and inspect `desktopApp/build.gradle.kts`) to confirm the real packaging/build task names (`packageDmg`/`packageMsi`/`packageDeb`, `createDistributable`, `packageDistributionForCurrentOS`) and the on-disk output locations under `desktopApp/build/compose/binaries/main/` before hard-coding them into the workflow.

**Source:** Delivery Area 1; Design Findings 5.3; Decision D4; Risk R1.

**Dependencies:** 1.1.

**Likely files / areas:** `desktopApp/build.gradle.kts`, Gradle task listing.

**Completion criteria:**
- Exact task names and output paths per OS are documented and verified.

**Testing expectations:** Gradle task listing confirms names; a local package produces the expected paths.

#### 1.3 Inspect existing README and current badges

- [x] Inspect existing README and current badges

**Description:** Review the current root `README.md`, noting existing sections and the absence of any badges, to plan the rewrite.

**Source:** Delivery Area 1.

**Dependencies:** 1.1.

**Likely files / areas:** `README.md`.

**Completion criteria:**
- Current README content and badge gaps are documented.

**Testing expectations:** None.

#### 1.4 Inspect `docs/HOW_TO_USE.md` for README subsumption

- [x] Inspect `docs/HOW_TO_USE.md` for README subsumption

**Description:** Read `docs/HOW_TO_USE.md` in full and identify which usage content the README should summarize/subsume and which stays as full detail behind a link.

**Source:** Delivery Area 1; FR10.

**Dependencies:** 1.1.

**Likely files / areas:** `docs/HOW_TO_USE.md`.

**Completion criteria:**
- Usage content is mapped to README sections; no contradictory instructions planned.

**Testing expectations:** None.

#### 1.5 Inspect LogViewer workflows and identify reusable elements

- [x] Inspect LogViewer workflows and identify reusable elements

**Description:** Review the LogViewer `.github/workflows/build.yml` and list which CI/release elements (matrix, JDK setup, Xvfb, package steps, artifact upload, `gh-release`) are reusable for Junie and which to drop (e.g. Detekt gate).

**Source:** Delivery Area 1; Design Findings 5.1–5.2.

**Dependencies:** 1.1.

**Likely files / areas:** LogViewer `.github/workflows/` (external reference).

**Completion criteria:**
- Reusable vs. dropped elements are documented with adaptation notes.

**Testing expectations:** None.

#### 1.6 HITL review of discovery findings and open questions — `HITL Review`

- [x] HITL review of discovery findings and open questions

**Description:** Present confirmed packaging tasks/paths, README/HOW_TO_USE subsumption plan, reusable CI elements, and the sprint's open questions (Q1–Q6) to the HITL for decisions.

**Source:** Delivery Area 1; Sprint doc section 18.

**Dependencies:** 1.2, 1.3, 1.4, 1.5.

**Likely files / areas:** Documentation only.

**Completion criteria:**
- HITL approves discovery findings and resolves open questions.

**Testing expectations:** None.

---

### Area 2 — GitHub Actions Workflow Design

*Source: Delivery Area 2. Defines the tag-build workflow shape before implementation.*

#### 2.1 Define workflow trigger, JDK, and cache design

- [x] Define workflow trigger, JDK, and cache design

**Description:** Specify the `v*` tag trigger, `actions/setup-java@v5` (Temurin, Java 21), and Gradle caching for the workflow.

**Source:** Delivery Area 2; FR1, FR2.

**Dependencies:** 1.6.

**Likely files / areas:** Workflow design notes.

**Completion criteria:**
- Trigger/JDK/cache design is documented and agreed.

**Testing expectations:** None.

#### 2.2 Define build matrix and per-OS package tasks — `Depends on 1.2`

- [x] Define build matrix and per-OS package tasks

**Description:** Define the macOS/Windows/Linux matrix (D1) with per-OS package task and output path, plus Linux Xvfb handling, using the confirmed task names from 1.2.

**Source:** Delivery Area 2; Decision D1; FR4, FR5.

**Dependencies:** 1.2, 2.1.

**Likely files / areas:** Workflow design notes.

**Completion criteria:**
- Matrix and per-OS package/path mapping are documented.

**Testing expectations:** None.

#### 2.3 Define artifact naming and Release publishing design

- [x] Define artifact naming and Release publishing design

**Description:** Specify tag-aware artifact/Release names (base `JunieConversationViewer` + tag) and the `gh-release` publishing step gated on `refs/tags/`, including prerelease detection for tags containing `-` (per Q3).

**Source:** Delivery Area 2; Decisions D2, D3; FR6, FR7, FR8.

**Dependencies:** 2.2.

**Likely files / areas:** Workflow design notes.

**Completion criteria:**
- Naming scheme and Release publishing design are documented.

**Testing expectations:** None.

#### 2.4 HITL review of workflow design — `HITL Review`

- [x] HITL review of workflow design

**Description:** Present the full workflow design (trigger, matrix, package tasks, naming, Release publishing) to the HITL for approval before implementation.

**Source:** Delivery Area 2; Sprint doc section 16.

**Dependencies:** 2.3.

**Likely files / areas:** Documentation only.

**Completion criteria:**
- HITL approves the workflow design.

**Testing expectations:** None.

---

### Area 3 — Tag Build Workflow Implementation

*Source: Delivery Area 3. Implements `.github/workflows/tag-build.yml` per the approved design.*

#### 3.1 Create `.github/workflows/tag-build.yml` with `v*` tag trigger — `Depends on 2.4`

- [x] Create workflow file with `v*` tag trigger

**Description:** Add the workflow file triggered on pushes of tags matching `v*`, with an appropriate `name` and `permissions: contents: write` for Release publishing.

**Source:** Delivery Area 3; FR1.

**Dependencies:** 2.4.

**Likely files / areas:** `.github/workflows/tag-build.yml`.

**Completion criteria:**
- Workflow file exists and triggers only on `v*` tags.

**Testing expectations:** YAML syntax review.

#### 3.2 Add checkout and JDK 21 setup with Gradle cache

- [x] Add checkout and JDK 21 setup with Gradle cache

**Description:** Add `actions/checkout`, `actions/setup-java@v5` (Temurin, Java 21, `cache: gradle`), and `chmod +x gradlew` on non-Windows.

**Source:** Delivery Area 3; FR2.

**Dependencies:** 3.1.

**Likely files / areas:** `.github/workflows/tag-build.yml`.

**Completion criteria:**
- JDK 21 and Gradle cache are configured for all matrix OSs.

**Testing expectations:** YAML syntax review.

#### 3.3 Add the build matrix (macOS, Windows, Linux)

- [x] Add the build matrix (macOS, Windows, Linux)

**Description:** Add `strategy.matrix` covering `macos-latest`, `windows-latest`, and `ubuntu-latest` with per-OS `package-task` and output-path variables (D1).

**Source:** Delivery Area 3; Decision D1; FR5.

**Dependencies:** 3.1.

**Likely files / areas:** `.github/workflows/tag-build.yml`.

**Completion criteria:**
- The matrix builds on all three OSs.

**Testing expectations:** YAML syntax review.

#### 3.4 Add Linux Xvfb setup

- [x] Add Linux Xvfb setup

**Description:** Install `xvfb` and required GL libraries on the Linux runner and run tests/packaging under `xvfb-run`.

**Source:** Delivery Area 3; Design Findings 5.1; Risk R2.

**Dependencies:** 3.3.

**Likely files / areas:** `.github/workflows/tag-build.yml`.

**Completion criteria:**
- Linux steps run under a virtual display.

**Testing expectations:** YAML syntax review.

#### 3.5 Add test step before packaging

- [x] Add test step before packaging

**Description:** Run `./gradlew :shared:jvmTest` (and/or `./gradlew test`) before any packaging step so a test failure fails the job.

**Source:** Delivery Area 3; FR3.

**Dependencies:** 3.3.

**Likely files / areas:** `.github/workflows/tag-build.yml`.

**Completion criteria:**
- Tests run and gate packaging.

**Testing expectations:** Test step present before package step; local tests pass.

#### 3.6 Add per-OS package step using confirmed Gradle tasks — `Depends on 1.2`

- [x] Add per-OS package step using confirmed Gradle tasks

**Description:** Add the packaging step invoking the **confirmed** per-OS Gradle task (`packageDmg`/`packageMsi`/`packageDeb`) plus `createDistributable` as needed.

**Source:** Delivery Area 3; Decision D4; FR4.

**Dependencies:** 1.2, 3.5.

**Likely files / areas:** `.github/workflows/tag-build.yml`.

**Completion criteria:**
- Each OS produces its installer/distributable.

**Testing expectations:** Local package task verification (Area 7).

#### 3.7 Add artifact upload with clear names

- [x] Add artifact upload with clear names

**Description:** Use `actions/upload-artifact` to upload the produced installers/distributables from the confirmed output paths, using clear, tag-aware names.

**Source:** Delivery Area 3; FR6.

**Dependencies:** 3.6.

**Likely files / areas:** `.github/workflows/tag-build.yml`.

**Completion criteria:**
- Artifacts upload from each OS with clear names.

**Testing expectations:** YAML syntax review; artifact inspection (Area 7).

#### 3.8 Add GitHub Release publishing on tags — `Depends on 3.7`

- [x] Add GitHub Release publishing on tags

**Description:** Add a `softprops/action-gh-release` step gated on `refs/tags/` that attaches installers/distributables (and optional SHA256 checksums), marking tags containing `-` as prereleases (per Q3).

**Source:** Delivery Area 3; Decision D2; FR7.

**Dependencies:** 3.7.

**Likely files / areas:** `.github/workflows/tag-build.yml`.

**Completion criteria:**
- A tag build publishes a Release with attached artifacts.

**Testing expectations:** Verified via a real tag push or dry run (Area 7/8).

#### 3.9 HITL review of implemented workflow — `HITL Review`

- [x] HITL review of implemented workflow

**Description:** Present the finished workflow to the HITL for review of correctness and maintainability.

**Source:** Delivery Area 3.

**Dependencies:** 3.8.

**Likely files / areas:** `.github/workflows/tag-build.yml`.

**Completion criteria:**
- HITL approves the workflow implementation.

**Testing expectations:** None.

---

### Area 4 — Versioning and Artifact Naming

*Source: Delivery Area 4. Implements tag-in-name versioning (D3) without changing `packageVersion`.*

#### 4.1 Derive the tag string in the workflow

- [x] Derive the tag string in the workflow

**Description:** Extract the git tag (e.g. `v1.2.0`) from `github.ref` into a reusable workflow variable/output for naming.

**Source:** Delivery Area 4; Decision D3; FR8.

**Dependencies:** 3.3.

**Likely files / areas:** `.github/workflows/tag-build.yml`.

**Completion criteria:**
- The tag value is available for artifact/Release naming steps.

**Testing expectations:** YAML syntax review.

#### 4.2 Embed the tag in artifact and Release names

- [x] Embed the tag in artifact and Release names

**Description:** Use the derived tag with base name `JunieConversationViewer` in artifact and Release names (e.g. `JunieConversationViewer-v1.2.0-macos.dmg`).

**Source:** Delivery Area 4; Decision D3; FR6, FR8.

**Dependencies:** 4.1, 3.7.

**Likely files / areas:** `.github/workflows/tag-build.yml`.

**Completion criteria:**
- Artifact/Release names contain the tag and base name.

**Testing expectations:** Artifact inspection (Area 7).

#### 4.3 Confirm `packageVersion` remains unchanged

- [x] Confirm `packageVersion` remains unchanged

**Description:** Verify `desktopApp/build.gradle.kts` still sets `packageVersion = "1.0.0"` and is not modified this sprint.

**Source:** Delivery Area 4; Decision D3.

**Dependencies:** None.

**Likely files / areas:** `desktopApp/build.gradle.kts`.

**Completion criteria:**
- `packageVersion` is unchanged.

**Testing expectations:** Diff review.

#### 4.4 Document deferred tag-driven versioning — `Deferred`

- [x] Document deferred tag-driven versioning

**Description:** Note in the docs that true tag-driven `packageVersion` (e.g. via `-PappVersion`) is deferred and outline the minimal future approach.

**Source:** Delivery Area 4; Sprint doc section 7.

**Dependencies:** None.

**Likely files / areas:** `README.md` / `project_memory.md`.

**Completion criteria:**
- Deferred versioning path is documented.

**Testing expectations:** None.

---

### Area 5 — GitHub README

*Source: Delivery Area 5. Creates/rewrites the root `README.md` per the sprint doc blueprint (section 13).*

#### 5.1 Draft README skeleton (title, description, sections)

- [x] Draft README skeleton

**Description:** Create the section skeleton per the blueprint: title, short description, badges placeholder, screenshot placeholder, features, install/run/build, usage, shortcuts, sessions/logs paths, troubleshooting, dev/test commands, doc links, status, contributing.

**Source:** Delivery Area 5; FR9.

**Dependencies:** 1.3, 1.4.

**Likely files / areas:** `README.md`.

**Completion criteria:**
- README skeleton contains all required sections in order.

**Testing expectations:** Markdown render review.

#### 5.2 Add badges — `Depends on 3.1`

- [x] Add badges

**Description:** Add badges for the tag-build workflow status, Kotlin, Java 21, and Compose Desktop; add a License badge only if a license exists/added (per Q6).

**Source:** Delivery Area 5; FR11.

**Dependencies:** 3.1, 5.1.

**Likely files / areas:** `README.md`.

**Completion criteria:**
- Badges render and point to the correct workflow name and default branch.

**Testing expectations:** Badge/link render review.

#### 5.3 Write features, installation, run, and build sections

- [x] Write features, installation, run, and build sections

**Description:** Populate features, installation (from GitHub Releases), run-from-source (Gradle run), and build/package-locally (confirmed Gradle tasks) sections.

**Source:** Delivery Area 5; FR9.

**Dependencies:** 1.2, 5.1.

**Likely files / areas:** `README.md`.

**Completion criteria:**
- Install/run/build guidance is accurate and uses confirmed tasks.

**Testing expectations:** Commands verified locally (Area 7).

#### 5.4 Write usage section subsuming `HOW_TO_USE.md`

- [x] Write usage section subsuming `HOW_TO_USE.md`

**Description:** Summarize usage (Session selection, toolbar/menu, Filters, sort order, collapse/show all, live auto-refresh) and link to `docs/HOW_TO_USE.md` as the full reference, avoiding contradictions.

**Source:** Delivery Area 5; FR10.

**Dependencies:** 1.4, 5.1.

**Likely files / areas:** `README.md`.

**Completion criteria:**
- Usage summary subsumes `HOW_TO_USE.md` and links to it consistently.

**Testing expectations:** Cross-doc consistency review.

#### 5.5 Add shortcuts, sessions/logs paths, and troubleshooting

- [x] Add shortcuts, sessions/logs paths, and troubleshooting

**Description:** Add the keyboard-shortcuts summary, the Sessions path (`~/.junie/sessions/`), the logs path (`~/.junieviewer/logs/`), and a troubleshooting section.

**Source:** Delivery Area 5; FR9.

**Dependencies:** 5.4.

**Likely files / areas:** `README.md`.

**Completion criteria:**
- Shortcuts, paths, and troubleshooting are present and correct.

**Testing expectations:** Markdown render review.

#### 5.6 Add documentation links, status/limitations, and contributing

- [x] Add documentation links, status/limitations, and contributing

**Description:** Add links to `docs/HOW_TO_USE.md`, `docs/TESTING.md`, `docs/UBIQUITOUS-LANGUAGE.md`, and sprint docs; a status/limitations section; and brief contributing notes.

**Source:** Delivery Area 5; FR9.

**Dependencies:** 5.1.

**Likely files / areas:** `README.md`.

**Completion criteria:**
- Doc links resolve; status and contributing sections are present.

**Testing expectations:** Link render review.

#### 5.7 HITL review of README content — `HITL Review`

- [x] HITL review of README content

**Description:** Present the completed README to the HITL for content, badge, and subsumption review.

**Source:** Delivery Area 5; Sprint doc section 16.

**Dependencies:** 5.2, 5.3, 5.5, 5.6.

**Likely files / areas:** `README.md`.

**Completion criteria:**
- HITL approves the README.

**Testing expectations:** None.

---

### Area 6 — Documentation Updates

*Source: Delivery Area 6. Keeps supporting docs consistent with the new README and CI.*

#### 6.1 Cross-link `HOW_TO_USE.md` with the README

- [ ] Cross-link `HOW_TO_USE.md` with the README

**Description:** Ensure `docs/HOW_TO_USE.md` references the README (and vice versa) so usage guidance has a single authoritative source without contradictions.

**Source:** Delivery Area 6; Decision Q4.

**Dependencies:** 5.4.

**Likely files / areas:** `docs/HOW_TO_USE.md`.

**Completion criteria:**
- The two usage docs cross-reference without conflicting instructions.

**Testing expectations:** Cross-doc consistency review.

#### 6.2 Update `docs/TESTING.md` if CI/test commands change

- [ ] Update `docs/TESTING.md`

**Description:** Note the CI test execution (`:shared:jvmTest`, `test`) and any Xvfb/CI-specific testing considerations.

**Source:** Delivery Area 6.

**Dependencies:** 3.5.

**Likely files / areas:** `docs/TESTING.md`.

**Completion criteria:**
- Testing docs reflect CI execution.

**Testing expectations:** None.

#### 6.3 Update `docs/RECAP.md`

- [ ] Update `docs/RECAP.md`

**Description:** Add the Sprint 7 CI/README milestone to the project recap.

**Source:** Delivery Area 6.

**Dependencies:** 3.9, 5.7.

**Likely files / areas:** `docs/RECAP.md`.

**Completion criteria:**
- RECAP includes Sprint 7.

**Testing expectations:** None.

#### 6.4 Update `docs/project_memory.md`

- [ ] Update `docs/project_memory.md`

**Description:** Record Sprint 7 shipped work, key decisions (D1–D4), gotchas (Xvfb, Release permissions), and test coverage areas using the `project-memory` skill.

**Source:** Delivery Area 6; project guidelines.

**Dependencies:** 3.9, 5.7.

**Likely files / areas:** `docs/project_memory.md`.

**Completion criteria:**
- Project memory is updated per the required template.

**Testing expectations:** None.

#### 6.5 Update sprint/task docs that reference README state

- [ ] Update sprint/task docs that reference README state

**Description:** Update any sprint/task documents that reference README/CI state so they remain accurate after this sprint.

**Source:** Delivery Area 6.

**Dependencies:** 5.7.

**Likely files / areas:** `docs/sprints/`, `docs/tasks/`.

**Completion criteria:**
- Referencing docs are consistent with the new README/CI.

**Testing expectations:** None.

#### 6.6 Author GitHub setup guide (CI + publish)

- [x] Author GitHub setup guide (CI + publish)

**Description:** Author a standalone, step-by-step operator guide (`docs/GITHUB_SETUP.md`) that walks a maintainer through setting up a GitHub repository for the tag-triggered CI/release workflow: create & push the repo, enable Actions, set Workflow permissions to Read and write (rationale: `contents: write` for Release publishing), confirm runner availability (incl. `windows-11-arm` and `ubuntu-24.04-arm`), publish a release via a `vX.Y.Z` tag (hyphenated tags → prerelease; `packageVersion` stays `1.0.0`), monitor the run, and verify per-OS installers, zipped distributables, and `.sha256` checksums. Includes a troubleshooting section and cross-links to the Area 2 design and `HOW_TO_USE.md`.

**Source:** Delivery Area 6; HITL request (2026-07-22).

**Dependencies:** 2.4 (design source of truth); forward-references Area 3 workflow behaviour.

**Likely files / areas:** `docs/GITHUB_SETUP.md`, `README.md` (single cross-link).

**Completion criteria:**
- `docs/GITHUB_SETUP.md` exists with ordered CI + publish steps consistent with the Area 2 design; README links to it.

**Testing expectations:** Documentation consistency review.

---

### Area 7 — Testing and Local Verification

*Source: Delivery Area 7. Automated and manual verification before completion.*

#### 7.1 Run `./gradlew :shared:jvmTest` — `Test Required`

- [ ] Run `./gradlew :shared:jvmTest`

**Description:** Verify that all shared-module JVM tests pass.

**Source:** Delivery Area 7; Sprint doc section 15.

**Dependencies:** None.

**Likely files / areas:** `shared` module.

**Completion criteria:**
- Shared tests pass (green build).

**Testing expectations:** Green build.

#### 7.2 Run `./gradlew test` — `Test Required`

- [ ] Run `./gradlew test`

**Description:** Execute the full automated test suite.

**Source:** Delivery Area 7; Sprint doc section 15.

**Dependencies:** 7.1.

**Likely files / areas:** Entire project.

**Completion criteria:**
- All tests pass (green build).

**Testing expectations:** Green build.

#### 7.3 Verify local package/build task — `Manual Review Required`

- [ ] Verify local package/build task

**Description:** Run the confirmed local packaging task and verify it produces the expected installer/distributable under the confirmed output path.

**Source:** Delivery Area 7; Decision D4.

**Dependencies:** 1.2.

**Likely files / areas:** `desktopApp` build output.

**Completion criteria:**
- Local package succeeds and outputs to the expected path.

**Testing expectations:** Manual verification.

#### 7.4 Inspect generated artifacts

- [ ] Inspect generated artifacts

**Description:** Inspect the produced/uploaded artifacts to confirm tag-aware names and correct contents.

**Source:** Delivery Area 7; FR6.

**Dependencies:** 4.2, 7.3.

**Likely files / areas:** Build output / workflow artifacts.

**Completion criteria:**
- Artifacts are correctly named and contain the app.

**Testing expectations:** Manual inspection.

#### 7.5 Review workflow YAML syntax and tag-trigger behaviour

- [ ] Review workflow YAML syntax and tag-trigger behaviour

**Description:** Validate the workflow YAML syntax and confirm (by inspection, and a real tag push where feasible) that only `v*` tags trigger it and the Release step is gated on `refs/tags/`.

**Source:** Delivery Area 7; FR1, FR7.

**Dependencies:** 3.8.

**Likely files / areas:** `.github/workflows/tag-build.yml`.

**Completion criteria:**
- YAML is valid; tag-trigger and Release gating behave as intended.

**Testing expectations:** Syntax review; tag-push dry run where feasible.

#### 7.6 Confirm README links and badges render correctly

- [ ] Confirm README links and badges render correctly

**Description:** Verify all README relative links resolve and badges render against the correct workflow name and default branch.

**Source:** Delivery Area 7; NFR2, FR11.

**Dependencies:** 5.2, 5.6.

**Likely files / areas:** `README.md`.

**Completion criteria:**
- Links resolve and badges display correctly.

**Testing expectations:** Rendered-Markdown review.

---

### Area 8 — Review, Cleanup, and Completion

*Source: Delivery Area 8. Final review, complexity check, and completion updates.*

#### 8.1 Run cyclomatic complexity check

- [ ] Run cyclomatic complexity check

**Description:** Run the cyclomatic-complexity check per project guidelines and review results; decide whether remediation is needed (none expected as no production code changes this sprint).

**Source:** Sprint guidelines.

**Dependencies:** 7.2.

**Likely files / areas:** Entire codebase.

**Completion criteria:**
- Complexity check is run and results reviewed.

**Testing expectations:** None.

#### 8.2 Fix review issues

- [ ] Fix review issues

**Description:** Address any findings from tests, manual verification, workflow review, or the complexity check.

**Source:** Delivery Area 8.

**Dependencies:** 7.5, 7.6, 8.1.

**Likely files / areas:** Affected files.

**Completion criteria:**
- Issues are resolved and re-verified.

**Testing expectations:** Re-run affected checks.

#### 8.3 Update `README.md` via the `readme-updater` skill

- [ ] Update `README.md` via the `readme-updater` skill

**Description:** Run the `readme-updater` skill to ensure the README reflects the final state of the application per completion guidelines.

**Source:** Project guidelines (Sprint/Task completion).

**Dependencies:** 5.7, 8.2.

**Likely files / areas:** `README.md`.

**Completion criteria:**
- README reflects the final application state.

**Testing expectations:** None.

#### 8.4 Update `docs/project_memory.md` via the `project-memory` skill

- [ ] Update `docs/project_memory.md` via the `project-memory` skill

**Description:** Record final Sprint 7 outcomes (shipped, decisions, gotchas, date/time, test coverage) per completion guidelines.

**Source:** Project guidelines (Sprint/Task completion).

**Dependencies:** 8.2.

**Likely files / areas:** `docs/project_memory.md`.

**Completion criteria:**
- Project memory is finalized for Sprint 7.

**Testing expectations:** None.

#### 8.5 HITL final approval — `HITL Review`

- [ ] HITL final approval

**Description:** Obtain final sign-off for Sprint 7 (workflow and README reviewed and approved).

**Source:** Delivery Area 8; Sprint doc section 19.

**Dependencies:** 8.3, 8.4.

**Likely files / areas:** Project delivery.

**Completion criteria:**
- HITL approval granted.

**Testing expectations:** None.

---

## 8. HITL Review Checkpoints

| # | Task | Area | HITL-Visible Outcome |
|---|------|------|---------------------|
| 1 | 1.6 | Discovery | Confirmed packaging tasks/paths, README/HOW_TO_USE subsumption plan, reusable CI elements, and resolved open questions (Q1–Q6). |
| 2 | 2.4 | Workflow Design | Approved tag-build workflow design: trigger, matrix, package tasks, naming, and Release publishing. |
| 3 | 3.9 | Workflow Implementation | Working `tag-build.yml` producing cross-platform installers and a published GitHub Release on tags. |
| 4 | 5.7 | GitHub README | GitHub-ready README with badges, subsuming `HOW_TO_USE.md`, useful to new visitors. |
| 5 | 8.5 | Completion | All tests pass, workflow and README verified, complexity check reviewed, final sign-off. |

## 9. Acceptance Criteria

- All 47 tasks marked complete.
- All **Test Required** tasks have passing automated tests.
- All **HITL Review** tasks have HITL approval.
- All **Manual Review Required** tasks verified.
- `./gradlew :shared:jvmTest` passes.
- `./gradlew test` passes.
- `.github/workflows/tag-build.yml` exists, triggers on `v*`, uses JDK 21, runs tests before packaging, and uses confirmed Gradle tasks.
- The workflow builds/packages on macOS, Windows, and Linux, uploads tag-aware artifacts, and publishes a GitHub Release on tags (D1, D2, D3).
- Gradle `packageVersion` remains `1.0.0` (D3).
- `README.md` is GitHub-ready with relevant badges and subsumes `docs/HOW_TO_USE.md` without contradictions.
- `README.md`, `project_memory.md`, and `RECAP.md` are updated.
- Cyclomatic complexity check run and reviewed.
- All open questions (Q1–Q6) resolved with HITL input.

## 10. Deferred / Out-of-Scope Items

- Automatic tag-driven `packageVersion` (true semantic-version automation).
- Code signing and notarization (macOS); Windows installer signing.
- Linux package-repository publishing (apt/rpm repos).
- Homebrew formula / cask.
- In-app auto-update mechanism.
- Publishing to package registries (Maven Central, GitHub Packages).
- Multi-repository release orchestration.
- PR/`main` CI feedback builds — deferred to a future add per HITL (Q2).

> **In scope after HITL (2026-07-22), moved out of deferred:** ARM Linux runner `ubuntu-24.04-arm` (Q5) and adding a software license + License badge (Q6) are now part of Sprint 7. The specific license (e.g. MIT / Apache-2.0) is TBD when the license file is added in Area 5/6.

## 11. Notes / Decisions Log

| Date | Decision | Context |
|------|----------|---------|
| 2026-07-22 | Sprint 7 planning documents authored | Sprint and task docs created for the CI/GitHub sprint, mirroring the Sprint 5 doc structure. |
| 2026-07-22 | D1 — Full cross-platform matrix | The first tag-build workflow targets macOS (`.dmg`), Windows (`.msi`), and Linux (`.deb`, with Xvfb), mirroring LogViewer's matrix. Confirmed with HITL. |
| 2026-07-22 | D2 — Publish GitHub Release on tags | The tag build publishes a GitHub Release with attached installers/distributables via `softprops/action-gh-release`, gated on `refs/tags/`. Confirmed with HITL. |
| 2026-07-22 | D3 — Tag in artifact/Release names only | The git tag is embedded in artifact/Release names; Gradle `packageVersion` stays `1.0.0`. True tag-driven versioning is deferred. Confirmed with HITL. |
| 2026-07-22 | D4 — Confirm packaging task names before hard-coding | Area 1 must confirm the exact Compose Desktop packaging/build Gradle task names and output paths (via `./gradlew :desktopApp:tasks` and a local package) before they are written into the workflow. |
| 2026-07-22 | Area 1 discovery complete (tasks 1.1–1.5) | Confirmed all packaging tasks (`packageDmg`/`packageMsi`/`packageDeb`/`createDistributable`/`packageDistributionForCurrentOS`) and output paths via `./gradlew :desktopApp:tasks` and a successful local macOS `packageDistributionForCurrentOS` run (DMG + distributable). Documented README/HOW_TO_USE subsumption plan and LogViewer CI reuse (drop Detekt; x64-only first). Findings recorded in [`docs/sprint-7-area-1-discovery-findings.md`](../sprint-7-area-1-discovery-findings.md). Task 1.6 (HITL review of findings + Q1–Q6) remains open pending HITL sign-off. |
| 2026-07-22 | Area 1 closed — HITL resolved open questions Q1–Q6 (+ Q-add) | HITL sign-off obtained; task 1.6 checked and Area 1 marked 6/6 complete. Decisions: Q1 name workflow `tag-build.yml`; Q2 tag-only this sprint (defer PR/`main`); Q3 hyphen tags → prerelease; Q4 `HOW_TO_USE.md` authoritative, README summarizes+links; **Q5 include ARM Linux `ubuntu-24.04-arm` now** (deviates from discovery's defer); **Q6 add a software license + License badge** (deviates from discovery's omit; specific license TBD in Area 5/6); Q-add publish installer **and** zipped distributable + SHA256 checksums. Resolutions recorded in [`docs/sprint-7-area-1-discovery-findings.md`](../sprint-7-area-1-discovery-findings.md) §7. |
| 2026-07-22 | Area 2 workflow design complete (tasks 2.1–2.3) | Documented the full `tag-build.yml` design (no YAML written): `v*` tag-only trigger, `contents: write`, `actions/setup-java@v5` Temurin JDK 21 + Gradle cache, `./gradlew test` gate before packaging (under `xvfb-run` on Linux), 4-row matrix (`macos-latest`/`windows-latest`/`ubuntu-latest`/`ubuntu-24.04-arm`) with per-OS `packageDmg/Msi/Deb` + `createDistributable`, tag-aware `JunieConversationViewer-<tag>-<suffix>` naming, per-file `.sha256` sidecars, and `softprops/action-gh-release` publishing with `prerelease` on hyphenated tags. `packageVersion` stays `1.0.0`. Design recorded in [`docs/sprint-7-area-2-workflow-design.md`](../sprint-7-area-2-workflow-design.md). Task 2.4 (HITL review of the design) remains open pending sign-off. |
| 2026-07-22 | Windows ARM64 added to the build matrix (design) | HITL asked CI to support **Windows x64 and Windows ARM** packages (or a universal binary if one existed). Confirmed Windows has **no universal binary** — `jpackage`/Compose Desktop emits an arch-specific `.msi`, so two native runners are used: `windows-latest` (windows-x64) and **`windows-11-arm` (windows-arm64)** (GitHub-hosted, GA since Apr 2025). Caveat: **Temurin ships no Windows `aarch64` JDK 21** ([temurin#271](https://github.com/adoptium/temurin/issues/271)), so the ARM64 job uses `distribution: microsoft` (Microsoft Build of OpenJDK) via a new `matrix.java-distribution`; other jobs stay on Temurin. Matrix now 5 rows; added artifact names `windows-x64`/`windows-arm64`, risks R8–R10. Design updated in [`docs/sprint-7-area-2-workflow-design.md`](../sprint-7-area-2-workflow-design.md); Windows ARM64 packaging/runner still to be verified on the runner in Area 7. Task 2.4 remains open pending HITL sign-off. |
| 2026-07-22 | Added GitHub setup guide (task 6.6) | HITL requested step-by-step GitHub setup guidelines for the CI and publish steps. Created standalone [`docs/GITHUB_SETUP.md`](../GITHUB_SETUP.md) (create & push repo, enable Actions + Read and write workflow permissions, confirm runners incl. `windows-11-arm`/`ubuntu-24.04-arm`, publish via `vX.Y.Z` tag with hyphen→prerelease, monitor, verify installers/`-distributable.zip`/`.sha256`, troubleshooting), restating Area 2 design facts and marking `tag-build.yml`-dependent behaviour as Area 3. Added task 6.6 (Area 6 now 1/6, total 47), and a single README cross-link (minimal, to avoid pre-empting the Area 5 rewrite). |
| 2026-07-22 | Area 3 workflow implemented (tasks 3.1–3.8) | Created `.github/workflows/tag-build.yml` (`Tag Build and Release`) per the approved Area 2 design: `v*` tag-only trigger, `permissions: contents: write`, `defaults.run` with `shell: bash` + `working-directory: JunieConversationViewer` (git repo root is the parent, gradlew lives in `JunieConversationViewer/`). `strategy: fail-fast: false` 5-row matrix (`macos-latest`/`windows-latest`/`windows-11-arm`/`ubuntu-latest`/`ubuntu-24.04-arm`) with `windows-11-arm` on `distribution: microsoft`. Steps: checkout@v4 → setup-java@v5 (JDK 21, `cache: gradle`) → `chmod +x gradlew` (non-Windows) → Linux Xvfb+GL install → `./gradlew test` (under `xvfb-run` on Linux) → per-OS `packageDmg/Msi/Deb` → `createDistributable` → prepare/rename installer + zip app image (`JunieConversationViewer-<tag>-<suffix>`; bash on Linux/macOS, pwsh on Windows) → per-file `.sha256` sidecars → `upload-artifact@v4` → tag-gated `softprops/action-gh-release@v2` (`prerelease` on hyphenated tags, `GITHUB_TOKEN`). YAML validated; `packageVersion` untouched. Task 3.9 (HITL review) left open; full run verification deferred to Area 7 (needs a real tag push). |

| 2026-07-22 | Area 4 complete (tasks 4.1–4.4) | Made tag-in-name versioning explicit in `.github/workflows/tag-build.yml`: added a "Derive release tag" step (`id: tag`) exporting the pushed tag (`github.ref_name`, leading `v` preserved) as both a step output (`steps.tag.outputs.tag`) and an env var (`TAG`), then rewired installer/zip/`.sha256` names and the Release title/body to use it, so every artifact and Release name follows `JunieConversationViewer-<tag>-<suffix>`. Confirmed `desktopApp/build.gradle.kts` still sets `packageVersion = "1.0.0"` (unmodified). Recorded in `docs/project_memory.md` that true tag-driven `packageVersion` (e.g. `-PappVersion=<version>` wired into `packageVersion`) is deferred. YAML re-validated (14 steps); Area 3 behaviour (matrix, checksums, prerelease detection) unchanged. |
| 2026-07-22 | Area 5 GitHub README drafted (tasks 5.1–5.6) | Rewrote root `README.md` GitHub-ready: title/description, four badges (tag-build workflow status pointing at `.github/workflows/tag-build.yml`, Kotlin 2.4.0, Java 21, Compose Desktop 1.11.1), a "Screenshot coming soon." placeholder, features (using Session/Conversation/Message/Human/Junie terminology), installation from GitHub Releases (tag-aware `JunieConversationViewer-<tag>-<platform>` names + `.sha256` sidecars; no notarization/signing/Homebrew/auto-update claims), run-from-source (Java 21 + `./gradlew :desktopApp:run`, Windows `.\gradlew.bat`), local build/package (confirmed `packageDmg/Msi/Deb`/`createDistributable`/`packageDistributionForCurrentOS`), usage overview linking `docs/HOW_TO_USE.md` as the full reference, keyboard shortcuts, sessions (`~/.junie/sessions/`) and logs (`~/.junieviewer/logs/`) paths, troubleshooting, dev/test commands, documentation links, status/limitations, and contributing. **No License badge/section** — no LICENSE file exists yet (Q6 license still TBD). Task 5.7 (HITL review) left open. |
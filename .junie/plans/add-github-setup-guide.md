---
sessionId: session-260722-142211-wah7
---

# Requirements

### Overview & Goals

Add a clear, step-by-step guide that explains how to set up a GitHub repository so the Sprint 7 tag-triggered CI/release workflow (`tag-build.yml`) can build and publish the Junie Conversation Viewer. The guide targets a maintainer wiring the project up on GitHub for the first time.

### Scope

**In scope**
- New standalone document `docs/GITHUB_SETUP.md` with numbered setup steps for **CI** and **publish/release**.
- Cross-links from the guide to the Area 2 workflow design and `docs/HOW_TO_USE.md`, and a short link into the guide from `README.md`.
- A new tracked task under **Area 6 — Documentation Updates** (e.g. `6.6`) plus Progress Summary / total-count updates and a Notes / Decisions Log entry.

**Out of scope**
- Implementing `.github/workflows/tag-build.yml` (still Area 3).
- Rewriting `README.md` (still Area 5); only a single small cross-link is added.
- Changing `desktopApp/build.gradle.kts` or `packageVersion` (stays `1.0.0`).

### User Stories
- As a maintainer, I want ordered instructions to enable GitHub Actions and grant the workflow release permissions, so the automated build can publish a Release.
- As a maintainer, I want to know exactly how to cut a release (tag naming, prerelease convention), so I can trigger the pipeline predictably.
- As a contributor, I want to know where to find and how to verify the produced artifacts and checksums.

### Functional Requirements
- The guide documents, in order: pushing the repo to GitHub, enabling Actions, setting **Workflow permissions → Read and write** (so `GITHUB_TOKEN` can publish Releases per `contents: write`), and confirming hosted runner availability (incl. `windows-11-arm` and `ubuntu-24.04-arm`).
- The guide documents the **release trigger**: create and push a `vX.Y.Z` tag; tags containing a hyphen (e.g. `v1.0.0-rc1`) publish as **prereleases**.
- The guide documents **monitoring** a run, and **downloading/verifying** per-OS installers, zipped distributables, and `.sha256` checksums.
- The guide notes that **no custom secrets** are required (the built-in `GITHUB_TOKEN` suffices) and that `packageVersion` stays `1.0.0` — the tag only affects artifact/Release names.
- A short troubleshooting section covers common failures (read-only token, ARM runner unavailable, first-run Gradle config-cache warnings).

### Non-Functional Requirements
- Documentation-only change; consistent with the Area 1 findings and Area 2 design; no contradictions with `HOW_TO_USE.md`.
- Forward-looking notes are clearly marked where they depend on the not-yet-implemented `tag-build.yml`.

# Technical Design

### Current Implementation
- Sprint 7 CI/release work is captured in `docs/sprint-7-area-1-discovery-findings.md` (confirmed packaging tasks/paths) and `docs/sprint-7-area-2-workflow-design.md` (full `tag-build.yml` design: `v*` tag-only trigger, `contents: write`, 5-row matrix incl. `windows-11-arm` and `ubuntu-24.04-arm`, installers + zipped distributables + `.sha256`, `softprops/action-gh-release` with hyphen→prerelease).
- The workflow YAML itself is **not** implemented yet (Area 3). No GitHub setup guide exists.
- The task tracker `docs/tasks/junie-conversation-viewer-tasks-sprint-7-ci-github-automation-and-readme.md` has Area 6 (Documentation Updates) with tasks `6.1`–`6.5` and a Progress Summary + Notes/Decisions Log.

### Key Decisions
- **Standalone `docs/GITHUB_SETUP.md`** (chosen with HITL): keeps the guide authoritative and README concise; README only gets a one-line cross-link (deferred to Area 5's README rewrite where possible).
- **Track as new Area 6 task** (chosen with HITL): add `6.6` and update counts, keeping the sprint tracker accurate.
- **Source of truth reuse**: the guide restates facts from the Area 2 design rather than inventing new ones; runner labels, artifact names, and prerelease behavior must match `sprint-7-area-2-workflow-design.md`.

### Proposed Changes
1. Create `docs/GITHUB_SETUP.md` structured as numbered steps:
   - **Prerequisites** — GitHub account/repo, local git, JDK 21 for local verification.
   - **1. Create & push the repository** to GitHub.
   - **2. Enable GitHub Actions** and set **Settings → Actions → General → Workflow permissions → Read and write** (rationale: Release publishing needs `contents: write`).
   - **3. Confirm runner availability** — macOS/Windows x64/Linux x64 plus `windows-11-arm` and `ubuntu-24.04-arm` (free for public repos); note fallback if ARM runners are unavailable.
   - **4. Publish a release** — `git tag vX.Y.Z && git push origin vX.Y.Z`; hyphenated tags → prerelease; `packageVersion` stays `1.0.0`.
   - **5. Monitor the workflow run** in the Actions tab.
   - **6. Verify artifacts** — per-OS installers (`.dmg`/`.msi`/`.deb`), `-distributable.zip`, and `.sha256` verification commands.
   - **7. Troubleshooting** — read-only token, ARM runner unavailable, Linux Xvfb/display, first-run config-cache warnings.
   - **Cross-links** to `docs/sprint-7-area-2-workflow-design.md` and `docs/HOW_TO_USE.md`.
2. Update `docs/tasks/...-sprint-7-...md`:
   - Add task **`6.6` — Author GitHub setup guide (CI + publish)** under Area 6 with description/dependencies/completion criteria.
   - Bump Area 6 count `5 → 6` and the total task count accordingly in the Progress Summary.
   - Add a Notes / Decisions Log entry linking `docs/GITHUB_SETUP.md`.
3. Add a single cross-link into the guide from `README.md` (kept minimal to avoid pre-empting the Area 5 rewrite).

### File Structure
- **Added:** `docs/GITHUB_SETUP.md`
- **Modified:** `docs/tasks/junie-conversation-viewer-tasks-sprint-7-ci-github-automation-and-readme.md`; `README.md` (one cross-link only)

### Risks
- **Forward references:** the guide describes behavior of `tag-build.yml` before it exists — mark such steps as depending on Area 3 so they aren't mistaken for verified behavior.
- **Drift:** guide facts must stay in sync with the Area 2 design if that design changes; keep the design doc authoritative and the guide a distilled operator view.
- **README scope creep:** limit README edits to a single link so the Area 5 rewrite is not pre-empted.

# Delivery Steps

### ✓ Step 1: Author docs/GITHUB_SETUP.md step-by-step guide
A new standalone operator guide exists that walks a maintainer through enabling CI and publishing releases on GitHub.

- Create `docs/GITHUB_SETUP.md` with a Prerequisites section and numbered steps: create & push repo, enable Actions, set Workflow permissions to Read and write (rationale: `contents: write` for Release publishing).
- Document runner availability (macOS, Windows x64, `windows-11-arm`, Linux x64, `ubuntu-24.04-arm`) and note the public-repo/free-minutes context.
- Document the release trigger: push a `vX.Y.Z` tag; hyphenated tags publish as prereleases; `packageVersion` stays `1.0.0` (tag only affects artifact/Release names).
- Document monitoring the run and verifying per-OS installers, `-distributable.zip`, and `.sha256` checksums; note no custom secrets are needed (`GITHUB_TOKEN` suffices).
- Add a troubleshooting subsection (read-only token, ARM runner unavailable, Linux Xvfb/display, first-run config-cache warnings) and cross-links to `docs/sprint-7-area-2-workflow-design.md` and `docs/HOW_TO_USE.md`.
- Clearly mark steps that depend on the not-yet-implemented `tag-build.yml` (Area 3).

### ✓ Step 2: Track the guide in the Sprint 7 task document and cross-link README
The sprint tracker reflects the new guide and the README points to it.

- Add task `6.6` — Author GitHub setup guide (CI + publish) — under Area 6 with description, dependencies, and completion criteria, following the existing task format.
- Update the Progress Summary: bump Area 6 count from `5` to `6` and update the overall total task count.
- Add a Notes / Decisions Log entry summarizing the addition and linking `docs/GITHUB_SETUP.md`.
- Add a single minimal cross-link from `README.md` to `docs/GITHUB_SETUP.md` (kept small to avoid pre-empting the Area 5 README rewrite).
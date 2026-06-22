# Task: Rolling File Logging

## Overview
Implement rolling file logging based on date and size, configurable via an external Logback configuration file.

## Status: [x] Completed

## Tasks
- [x] Add Logback and Kermit-SLF4J dependencies to `libs.versions.toml`
- [x] Add dependencies to `desktopApp/build.gradle.kts`
- [x] Define `logsPath` in `Platform` and `JVMPlatform`
- [x] Create default `logback.xml` with `SizeAndTimeBasedRollingPolicy` in `desktopApp/src/main/resources`
- [x] Initialize logging in `main.kt` and support external `logback.xml` override
- [x] Verify build and logging setup

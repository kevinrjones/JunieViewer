# Task: Global Error Handling

## Overview
Implement global error handling to catch unhandled exceptions at the top of the application, log them, and present a user-friendly dialog.

## Status: [x] Completed

## Tasks
- [x] Create `FatalErrorManager` to coordinate error reporting between logic and UI
- [x] Create `FatalErrorDialog` Compose component for in-app fatal error messages
- [x] Set `Thread.setDefaultUncaughtExceptionHandler` in `main.kt` for top-level catch-all
- [x] Implement fallback `JOptionPane` dialog for cases where Compose runtime is unstable
- [x] Integrate error reporting into `ConversationViewModel` (coroutines and actions)
- [x] Add defensive `try-catch` and validation to `SessionRepository.listSessions` to prevent common crashes
- [x] Verify that errors are logged to both console and file
- [x] Prevent duplicate error dialogs by tracking reported state in `FatalErrorManager`
- [x] Suppress default framework dialogs by using a custom `Dialog` implementation for `FatalErrorDialog`
- [x] Add defensive timezone handling in session selection UI

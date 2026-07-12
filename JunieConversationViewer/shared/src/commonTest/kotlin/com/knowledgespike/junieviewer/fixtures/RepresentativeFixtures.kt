package com.knowledgespike.junieviewer.fixtures

import com.knowledgespike.junieviewer.domain.Message
import com.knowledgespike.junieviewer.domain.MessageContent
import com.knowledgespike.junieviewer.domain.MessageKind
import com.knowledgespike.junieviewer.domain.Sender

/**
 * Representative fixture data exercising every Message Kind for use in implementation and testing.
 * Covers: Human Text, Junie Text, fenced code, Patch/Diff, Terminal Output (as Code),
 * Tool Call (as Text), Thought (as Text), and error content.
 *
 * These fixtures are designed to be used in both unit tests and Compose UI tests to verify
 * that every Message Kind renders without crashing and with correct Kind markers.
 */
object RepresentativeFixtures {

    /** A short Human text prompt */
    val humanTextMessage = Message(
        id = "fixture-human-text",
        sender = Sender.Human,
        content = MessageContent.Text("Please refactor the authentication module to use JWT tokens."),
        kind = MessageKind.Text,
        timestamp = 1000L
    )

    /** A Junie text response (plain text / Markdown-like) */
    val junieTextMessage = Message(
        id = "fixture-junie-text",
        sender = Sender.Junie,
        content = MessageContent.Text(
            """I'll refactor the authentication module to use JWT tokens. Here's my plan:

1. Replace session-based auth with JWT token generation
2. Add token validation middleware
3. Update the login endpoint to return JWT tokens
4. Add refresh token support

Let me start with the token generation logic."""
        ),
        kind = MessageKind.Text,
        timestamp = 1001L
    )

    /** A Junie fenced code block */
    val junieCodeMessage = Message(
        id = "fixture-junie-code",
        sender = Sender.Junie,
        content = MessageContent.Code(
            code = """fun generateToken(userId: String, secret: String): String {
    val claims = mapOf(
        "sub" to userId,
        "iat" to System.currentTimeMillis() / 1000,
        "exp" to (System.currentTimeMillis() / 1000) + 3600
    )
    return Jwts.builder()
        .setClaims(claims)
        .signWith(SignatureAlgorithm.HS256, secret)
        .compact()
}""",
            language = "kotlin"
        ),
        kind = MessageKind.Text,
        timestamp = 1002L
    )

    /** A Junie Patch/Diff message */
    val junieDiffMessage = Message(
        id = "fixture-junie-diff",
        sender = Sender.Junie,
        content = MessageContent.Diff(
            diff = """--- a/src/main/kotlin/auth/AuthService.kt
+++ b/src/main/kotlin/auth/AuthService.kt
@@ -10,7 +10,9 @@ class AuthService {
-    fun authenticate(username: String, password: String): Session {
-        return sessionStore.createSession(username)
+    fun authenticate(username: String, password: String): TokenPair {
+        val userId = userRepository.validateCredentials(username, password)
+        val accessToken = generateToken(userId, accessSecret)
+        val refreshToken = generateToken(userId, refreshSecret)
+        return TokenPair(accessToken, refreshToken)
     }
 }"""
        ),
        kind = MessageKind.Patch,
        timestamp = 1003L
    )

    /** A Junie Terminal Output message (rendered as Code with shell language) */
    val junieTerminalMessage = Message(
        id = "fixture-junie-terminal",
        sender = Sender.Junie,
        content = MessageContent.Code(
            code = """$ ./gradlew :auth:test
> Task :auth:compileKotlin UP-TO-DATE
> Task :auth:compileTestKotlin
> Task :auth:test

BUILD SUCCESSFUL in 4s
3 actionable tasks: 2 executed, 1 up-to-date""",
            language = "bash"
        ),
        kind = MessageKind.Terminal,
        timestamp = 1004L
    )

    /** A Junie Tool Call message */
    val junieToolCallMessage = Message(
        id = "fixture-junie-tool",
        sender = Sender.Junie,
        content = MessageContent.Text(
            """Tool: search_contents_by_grep
Arguments: {"path": "src/main/kotlin", "regex": "class AuthService", "file_extension_list": "[*.kt]"}"""
        ),
        kind = MessageKind.Tool,
        timestamp = 1005L
    )

    /** A Junie Thought message */
    val junieThoughtMessage = Message(
        id = "fixture-junie-thought",
        sender = Sender.Junie,
        content = MessageContent.Text(
            "I need to check if the project already has a JWT dependency before adding one. " +
                "Let me search the build files first."
        ),
        kind = MessageKind.Thought,
        timestamp = 1006L
    )

    /** A Junie error/warning message (rendered as Text with error kind indicator) */
    val junieErrorMessage = Message(
        id = "fixture-junie-error",
        sender = Sender.Junie,
        content = MessageContent.Text(
            "Error: Could not resolve dependency 'io.jsonwebtoken:jjwt-api:0.12.0'. " +
                "Check your network connection and repository configuration."
        ),
        kind = MessageKind.Text,
        timestamp = 1007L
    )

    /**
     * A complete representative Conversation exercising every Message Kind.
     * Suitable for rendering tests and fixture-based UI tests.
     */
    val allMessageKinds: List<Message> = listOf(
        humanTextMessage,
        junieThoughtMessage,
        junieToolCallMessage,
        junieTerminalMessage,
        junieCodeMessage,
        junieDiffMessage,
        junieTextMessage,
        junieErrorMessage
    )
}

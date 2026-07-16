package com.knowledgespike.junieviewer.fixtures

import com.knowledgespike.junieviewer.domain.Message
import com.knowledgespike.junieviewer.domain.MessageContent
import com.knowledgespike.junieviewer.domain.MessageKind
import com.knowledgespike.junieviewer.domain.Sender

/**
 * Representative fixture data exercising every Message Kind for use in implementation and testing.
 * Covers: Human Text, Junie Text, Markdown, fenced code, Patch/Diff, Terminal Output,
 * Tool Call, Thought, Structured Output, Error, Warning, malformed/unsupported content.
 *
 * These fixtures are designed to be used in both unit tests and Compose UI tests to verify
 * that every Message Kind renders without crashing and with correct Kind markers.
 */
object RepresentativeFixtures {

    /** A sub-agent message with name and status */
    val subAgentMessage = Message(
        id = "fixture-sub-agent",
        sender = Sender.Junie,
        content = MessageContent.Text("android-qa-agent [STARTED]"),
        kind = MessageKind.SubAgent,
        timestamp = 999L
    )

    /** A sub-agent message with missing name and status */
    val subAgentMessageMissingFields = Message(
        id = "fixture-sub-agent-missing",
        sender = Sender.Junie,
        content = MessageContent.Text("Unnamed sub-agent [unknown]"),
        kind = MessageKind.SubAgent,
        timestamp = 998L
    )

    /** A short Human text prompt */
    val humanTextMessage = Message(
        id = "fixture-human-text",
        sender = Sender.Human,
        content = MessageContent.Text("Please refactor the authentication module to use JWT tokens."),
        kind = MessageKind.Text,
        timestamp = 1000L
    )

    /** A Junie plain text response (no Markdown markers) */
    val junieTextMessage = Message(
        id = "fixture-junie-text",
        sender = Sender.Junie,
        content = MessageContent.Text(
            "I have completed the refactoring. The authentication module now uses JWT tokens " +
                "for both access and refresh flows. All existing tests pass."
        ),
        kind = MessageKind.Text,
        timestamp = 1001L
    )

    /** A Junie Markdown response with headings, bold, italic, lists, inline code, and a link */
    val junieMarkdownMessage = Message(
        id = "fixture-junie-markdown",
        sender = Sender.Junie,
        content = MessageContent.Text(
            """## Refactoring Plan

Here's my plan for the **authentication module**:

1. Replace session-based auth with *JWT token generation*
2. Add `TokenValidator` middleware
3. Update the login endpoint to return JWT tokens
4. Add refresh token support

See [JWT docs](https://jwt.io) for details.

### Next Steps

- Review the `AuthService` class
- Update integration tests"""
        ),
        kind = MessageKind.Text,
        timestamp = 1002L
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
        timestamp = 1003L
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
        timestamp = 1004L
    )

    /** A Junie Terminal Output message */
    val junieTerminalMessage = Message(
        id = "fixture-junie-terminal",
        sender = Sender.Junie,
        content = MessageContent.Terminal(
            output = """$ ./gradlew :auth:test
> Task :auth:compileKotlin UP-TO-DATE
> Task :auth:compileTestKotlin
> Task :auth:test

BUILD SUCCESSFUL in 4s
3 actionable tasks: 2 executed, 1 up-to-date"""
        ),
        kind = MessageKind.Terminal,
        timestamp = 1005L
    )

    /** A Junie Tool Call message */
    val junieToolCallMessage = Message(
        id = "fixture-junie-tool",
        sender = Sender.Junie,
        content = MessageContent.Code(
            code = """{"name": "search_contents_by_grep", "arguments": {"path": "src/main/kotlin", "regex": "class AuthService", "file_extension_list": "[*.kt]"}}""",
            language = "json"
        ),
        kind = MessageKind.Tool,
        timestamp = 1006L
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
        timestamp = 1007L
    )

    /** A Junie Structured Output message */
    val junieStructuredOutputMessage = Message(
        id = "fixture-junie-structured",
        sender = Sender.Junie,
        content = MessageContent.Structured(
            data = """{
  "status": "complete",
  "files_changed": 3,
  "tests_passed": 12,
  "tests_failed": 0
}"""
        ),
        kind = MessageKind.StructuredOutput,
        timestamp = 1008L
    )

    /** A Junie error message */
    val junieErrorMessage = Message(
        id = "fixture-junie-error",
        sender = Sender.Junie,
        content = MessageContent.Text(
            "Could not resolve dependency 'io.jsonwebtoken:jjwt-api:0.12.0'. " +
                "Check your network connection and repository configuration."
        ),
        kind = MessageKind.Error,
        timestamp = 1009L
    )

    /** A Junie warning message */
    val junieWarningMessage = Message(
        id = "fixture-junie-warning",
        sender = Sender.Junie,
        content = MessageContent.Text(
            "The deprecated SessionStore API is still referenced in 2 test files. " +
                "These should be updated before the next release."
        ),
        kind = MessageKind.Warning,
        timestamp = 1010L
    )

    /** A Junie MCP message */
    val junieMcpMessage = Message(
        id = "fixture-junie-mcp",
        sender = Sender.Junie,
        content = MessageContent.Text("MCP tool call: database-query"),
        kind = MessageKind.Mcp,
        timestamp = 1011L
    )

    /** A Junie TestRun message */
    val junieTestRunMessage = Message(
        id = "fixture-junie-testrun",
        sender = Sender.Junie,
        content = MessageContent.Terminal(output = "Running tests...\n3 passed, 0 failed"),
        kind = MessageKind.TestRun,
        timestamp = 1012L
    )

    /** A Question message */
    val questionMessage = Message(
        id = "fixture-question",
        sender = Sender.Junie,
        content = MessageContent.Text("Should I proceed with the refactoring?"),
        kind = MessageKind.Question,
        timestamp = 1013L
    )

    /** A Choice message */
    val choiceMessage = Message(
        id = "fixture-choice",
        sender = Sender.Junie,
        content = MessageContent.Text("Option A: Refactor now\nOption B: Defer"),
        kind = MessageKind.Choice,
        timestamp = 1014L
    )

    /** A SystemMessage */
    val systemMessage = Message(
        id = "fixture-system",
        sender = Sender.Junie,
        content = MessageContent.Text("Session started"),
        kind = MessageKind.SystemMessage,
        timestamp = 1015L
    )

    /** A Cancelled message */
    val cancelledMessage = Message(
        id = "fixture-cancelled",
        sender = Sender.Junie,
        content = MessageContent.Text("Operation cancelled by user"),
        kind = MessageKind.Cancelled,
        timestamp = 1016L
    )

    /** A Status message */
    val statusMessage = Message(
        id = "fixture-status",
        sender = Sender.Junie,
        content = MessageContent.Text("Processing..."),
        kind = MessageKind.Status,
        timestamp = 1017L
    )

    /** A large Patch/Diff message used to verify no vertical truncation */
    val largeDiffMessage: Message = run {
        val lines = buildString {
            appendLine("diff --git a/large.kt b/large.kt")
            appendLine("--- a/large.kt")
            appendLine("+++ b/large.kt")
            appendLine("@@ -1,100 +1,100 @@")
            appendLine("+FIRST_LINE_MARKER")
            repeat(98) { i -> appendLine(" context line $i") }
            appendLine("+LAST_LINE_MARKER")
        }
        Message(
            id = "fixture-large-diff",
            sender = Sender.Junie,
            content = MessageContent.Diff(diff = lines),
            kind = MessageKind.Patch,
            timestamp = 1019L
        )
    }

    /** A malformed/unsupported content fallback message */
    val malformedContentMessage = Message(
        id = "fixture-malformed",
        sender = Sender.Junie,
        content = MessageContent.Text("Unsupported event: SomeNewEventKind"),
        kind = MessageKind.Unsupported,
        timestamp = 1018L
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
        junieMarkdownMessage,
        junieTextMessage,
        junieStructuredOutputMessage,
        junieErrorMessage,
        junieWarningMessage,
        junieMcpMessage,
        junieTestRunMessage,
        subAgentMessage,
        questionMessage,
        choiceMessage,
        systemMessage,
        cancelledMessage,
        statusMessage,
        malformedContentMessage
    )
}

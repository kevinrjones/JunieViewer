import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

dependencies {
    implementation(projects.shared)

    implementation(compose.desktop.currentOs)
    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.coroutinesSwing)

    implementation(libs.compose.uiToolingPreview)
    implementation(libs.androidx.lifecycle.viewmodelCompose)
    implementation(libs.androidx.lifecycle.runtimeCompose)
    implementation(libs.kermit)
    implementation(libs.slf4j.api)
    implementation(libs.logback.classic)
    testImplementation(kotlin("test"))
    testImplementation(libs.strikt.core)
}

compose.desktop {
    application {
        mainClass = "com.knowledgespike.junieviewer.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "JunieViewer"
            packageVersion = "1.0.0"

            // Per-OS application/installer icons. Each platform requires its
            // own native icon format, generated from src/main/resources/icons.
            macOS {
                iconFile.set(project.file("src/main/resources/icons/icon.icns"))
            }
            windows {
                iconFile.set(project.file("src/main/resources/icons/icon.ico"))
            }
            linux {
                iconFile.set(project.file("src/main/resources/icons/icon.png"))
            }
        }
    }
}
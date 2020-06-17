import org.gradle.api.internal.HasConvention
import org.jetbrains.intellij.IntelliJPluginExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompile
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

plugins {
    idea
    java
    kotlin("jvm").version("1.3.72")
    id("org.jetbrains.intellij").version("0.4.18")
}
repositories {
    mavenCentral()
}

sourceSets {
    main {
        kotlin.srcDirs("./src")
        resources.srcDirs("./resources")
    }
    test {
        kotlin.srcDirs("./test")
    }
}

dependencies {
    testImplementation("org.mockito:mockito-inline:3.3.3")
}

tasks.withType<KotlinJvmCompile> {
    kotlinOptions {
        jvmTarget = "11"
        apiVersion = "1.3"
        languageVersion = "1.3"
        // Compiler flag to allow building against pre-released versions of Kotlin
        // because IJ EAP can be built using pre-released Kotlin but it's still worth doing to check API compatibility
        freeCompilerArgs = freeCompilerArgs + listOf("-Xskip-metadata-version-check")
    }
}

configure<IntelliJPluginExtension> {
    // See https://www.jetbrains.com/intellij-repository/releases for a list of available IDEA builds
    val ideVersion = System.getenv().getOrDefault("IJ_VERSION",
            "IC-193.5233.102"
//        "LATEST-EAP-SNAPSHOT"
    )
    println("Using ide version: $ideVersion")
    version = ideVersion
    pluginName = "Scratch"
    downloadSources = true
    sameSinceUntilBuild = false
    updateSinceUntilBuild = false
}

val SourceSet.kotlin: SourceDirectorySet
    get() = (this as HasConvention).convention.getPlugin<KotlinSourceSet>().kotlin

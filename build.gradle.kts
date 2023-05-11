import org.gradle.api.internal.HasConvention
import org.jetbrains.intellij.IntelliJPluginExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompile
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

plugins {
    idea
    java
    kotlin("jvm").version("1.6.21")
    id("org.jetbrains.intellij").version("1.13.3")
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
    testImplementation("org.mockito:mockito-inline:4.8.0")
}

tasks.withType<KotlinJvmCompile> {
    kotlinOptions {
        jvmTarget = "17"
        apiVersion = "1.6"
        languageVersion = "1.6"
        // Compiler flag to allow building against pre-released versions of Kotlin
        // because IJ EAP can be built using pre-released Kotlin but it's still worth doing to check API compatibility
        freeCompilerArgs = freeCompilerArgs + listOf("-Xskip-metadata-version-check")
    }
}

configure<IntelliJPluginExtension> {
    // See https://www.jetbrains.com/intellij-repository/releases for a list of available IDEA builds
    val ideVersion = System.getenv().getOrDefault("IJ_VERSION",
        "222.3345.118"
//        "LATEST-EAP-SNAPSHOT"
    )
    println("Using ide version: $ideVersion")
    version.set(ideVersion)
    pluginName.set("Scratch")
    downloadSources.set(true)
    sameSinceUntilBuild.set(false)
    updateSinceUntilBuild.set(false)
}

val SourceSet.kotlin: SourceDirectorySet
    get() = (this as HasConvention).convention.getPlugin<KotlinSourceSet>().kotlin

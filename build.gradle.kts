import com.adarshr.gradle.testlogger.theme.ThemeType
import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage
import io.github.vacxe.buildtimetracker.reporters.markdown.MarkdownConfiguration
import java.time.Duration
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.9.20"
    id("org.jetbrains.kotlin.plugin.allopen") version "1.9.20"
    id("com.google.devtools.ksp") version "1.9.20-1.0.14"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("io.micronaut.application") version "4.2.0"
    id("gg.jte.gradle") version "3.1.5"
    id("io.micronaut.aot") version "4.2.0"
    // Apply GraalVM Native Image plugin. Micronaut already adds it, but
    // adding it explicitly allows to control which version is used.
    id("org.graalvm.buildtools.native") version "0.9.28"
    // Provides better test output
    id("com.adarshr.test-logger") version "4.0.0"
    // Code Coverage:
    // https://github.com/Kotlin/kotlinx-kover
    id("org.jetbrains.kotlinx.kover") version "0.7.4"
    // Code Inspections
    // https://detekt.dev/
    id("io.gitlab.arturbosch.detekt") version "1.23.3"
    // Task graph utility
    // https://github.com/dorongold/gradle-task-tree
    id("com.dorongold.task-tree") version "2.1.1"
    // Easily add new test sets
    // https://github.com/unbroken-dome/gradle-testsets-plugin
    id("org.unbroken-dome.test-sets") version "4.1.0"
    // Report task timings
    // https://github.com/vacxe/build-time-tracker
    id("io.github.vacxe.build-time-tracker") version "0.0.4"
    // To check dependency updates
    // https://github.com/ben-manes/gradle-versions-plugin
    id("com.github.ben-manes.versions") version "0.50.0"
}

val ci: Boolean = System.getenv().getOrDefault("CI", "false").toBoolean()
val releasing: Boolean = System.getenv().getOrDefault("RELEASING", "false").toBoolean()

val javaVersion: Int = 21
val dockerImage: String = "ghcr.io/liber-ufpe/project-starter"

val kotlinVersion: String = project.properties["kotlinVersion"] as String
val micronautVersion: String = project.properties["micronautVersion"] as String
val jteVersion: String = project.properties["jteVersion"] as String

version = "0.1"
group = "br.ufpe.liber"

repositories {
    mavenCentral()
}

application {
    mainClass.set("br.ufpe.liber.Application")
}

java {
    sourceCompatibility = JavaVersion.toVersion(javaVersion)
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    // See https://kotest.io/docs/extensions/system_extensions.html#system-environment
    jvmArgs("--add-opens=java.base/java.util=ALL-UNNAMED")
}

testSets {
    create("accessibilityTest")
}

val accessibilityTestImplementation: Configuration = configurations["accessibilityTestImplementation"]

tasks.withType<DockerBuildImage> {
    images.addAll(
        "$dockerImage:latest",
        "$dockerImage:${project.version}",
        "${project.name}:latest",
        "${project.name}:$version",
    )
}

// See https://graalvm.github.io/native-build-tools/latest/gradle-plugin.html
graalvmNative {
    toolchainDetection.set(false)
    binaries {
        named("main") {
            fallback.set(false)
            richOutput.set(true)
            buildArgs.add("--verbose")
            if (ci) {
                // A little extra verbose on CI to prevent jobs being killed
                // due to the lack of output (since native-image creation can
                // take a long time to complete).
                jvmArgs.add("-Xlog:gc*")
            }

            // Do a quick/un-optimized build. The intention is to validate
            // it is possible to create a native-image. But, if it is running
            // a `release` job, then it must create an optimized image, hence
            // quickBuild must be false.
            quickBuild.set(ci && !releasing)
        }
    }
}

micronaut {
    runtime("netty")
    testRuntime("kotest5")
    processing {
        incremental(true)
        annotations("br.ufpe.liber.*")
    }
    aot {
        // Please review carefully the optimizations enabled below
        // Check https://micronaut-projects.github.io/micronaut-aot/latest/guide/ for more details
        optimizeServiceLoading.set(false)
        convertYamlToJava.set(false)
        precomputeOperations.set(true)
        cacheEnvironment.set(true)
        optimizeClassLoading.set(true)
        deduceEnvironment.set(true)
        optimizeNetty.set(true)
    }
}

jte {
    sourceDirectory.set(file("src/main/jte").toPath())
    targetDirectory.set(layout.buildDirectory.dir("jte-classes").get().asFile.toPath())
    trimControlStructures.set(true)
    packageName.set(project.group.toString())
    generate()
    jteExtension("gg.jte.nativeimage.NativeResourcesExtension")
    jteExtension("gg.jte.models.generator.ModelExtension") {
        property("language", "Kotlin")
    }
}
// Gradle requires that generateJte is run before some tasks
tasks.configureEach {
    if (name == "inspectRuntimeClasspath" || name == "kspKotlin") {
        mustRunAfter("generateJte")
    }
}
tasks.named<Jar>("jar") {
    dependsOn.add("precompileJte")
    from(fileTree(layout.buildDirectory.file("jte-classes").get().asFile.absolutePath)) {
        include("**/.*.class")
    }
}

testlogger {
    theme = ThemeType.MOCHA
    showExceptions = true
    showStackTraces = true
}

buildTimeTracker {
    markdownConfiguration.set(
        MarkdownConfiguration(
            reportFile = project.reporting.file("build-times.md").absolutePath,
            minDuration = Duration.ofMillis(0),
            withTableLabels = true,
            sorted = true,
            take = Int.MAX_VALUE,
        ),
    )
}

// Outputs releaseDate in the format `2023 Nov 24 15:20:28`.
tasks.register("releaseDate") {
    doLast {
        println(
            ZonedDateTime
                .now()
                .format(
                    DateTimeFormatter
                        .ofLocalizedDateTime(FormatStyle.MEDIUM)
                        .withLocale(Locale.Builder().setLanguage("pt-BR").build()),
                ),
        )
    }
}

// Install pre-commit git hooks to run ktlint and detekt
// https://docs.gradle.org/current/userguide/working_with_files.html#sec:copying_single_file_example
tasks.register<Copy>("installGitHooks") {
    from(layout.projectDirectory.file("scripts/pre-commit"))
    into(layout.projectDirectory.dir(".git/hooks/"))
}

dependencies {
    ksp("io.micronaut:micronaut-http-validation")
    ksp("io.micronaut.serde:micronaut-serde-processor")
    implementation("io.micronaut.kotlin:micronaut-kotlin-extension-functions")
    implementation("io.micronaut.kotlin:micronaut-kotlin-runtime")
    implementation("io.micronaut.serde:micronaut-serde-jackson")
    implementation("io.micronaut.views:micronaut-views-jte")
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
    compileOnly("io.micronaut:micronaut-http-client")
    runtimeOnly("ch.qos.logback:logback-classic")
    runtimeOnly("com.fasterxml.jackson.module:jackson-module-kotlin")
    testImplementation("io.micronaut:micronaut-http-client")

    // jte dependencies
    jteGenerate("gg.jte:jte-models:$jteVersion")
    jteGenerate("gg.jte:jte-native-resources:$jteVersion")
    implementation("gg.jte:jte:$jteVersion")
    implementation("gg.jte:jte-kotlin:$jteVersion")
}

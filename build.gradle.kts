import com.adarshr.gradle.testlogger.theme.ThemeType
import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.9.20"
    id("org.jetbrains.kotlin.plugin.allopen") version "1.9.20"
    // If the project needs to add Serialization (to JSON, for example)
    // id("org.jetbrains.kotlin.plugin.serialization") version "1.9.20"
    id("com.google.devtools.ksp") version "1.9.20-1.0.14"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("io.micronaut.application") version "4.2.0"
    id("gg.jte.gradle") version "3.1.5"
    // Provides better test output
    id("com.adarshr.test-logger") version "4.0.0"
    // Code Coverage:
    // https://github.com/Kotlin/kotlinx-kover
    id("org.jetbrains.kotlinx.kover") version "0.7.4"
    // Code Inspections
    // https://detekt.dev/
    id("io.gitlab.arturbosch.detekt") version ("1.23.3")
    // Task graph utility
    // https://github.com/dorongold/gradle-task-tree
    id("com.dorongold.task-tree") version "2.1.1"
    // To generate a git.properties file containing git repository metadata
    id("com.gorylenko.gradle-git-properties") version "2.4.1"
    // Easily add new test sets
    id("org.unbroken-dome.test-sets") version "4.1.0"
}

val javaVersion: Int = 17
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
    toolchain {
        languageVersion = JavaLanguageVersion.of(javaVersion)
    }
}

tasks.named("test", Test::class) {
    useJUnitPlatform()
    // See https://kotest.io/docs/extensions/system_extensions.html#system-environment
    jvmArgs("--add-opens=java.base/java.util=ALL-UNNAMED")
}

testSets {
    create("accessibilityTest")
}

val accessibilityTestImplementation: Configuration = configurations["accessibilityTestImplementation"]

tasks.withType<DockerBuildImage>() {
    images.addAll(
        "${dockerImage}:latest",
        "${dockerImage}:${project.version}",
        "${project.name}:latest",
        "${project.name}:${version}"
    )
}

graalvmNative {
    toolchainDetection.set(false)
    binaries {
        named("main") {
            buildArgs.add("--verbose")
            // 7GB is what is available when using Github-hosted runners:
            // https://docs.github.com/en/actions/using-github-hosted-runners/about-github-hosted-runners#supported-runners-and-hardware-resources
            buildArgs.add("-J-Xmx7G")
            buildArgs.add("--initialize-at-build-time=kotlin.coroutines.intrinsics.CoroutineSingletons")
        }
    }
}

micronaut {
    runtime("netty")
    testRuntime("kotest5")
    processing {
        incremental(true)
        annotations("br.ufpe.*")
    }
}

jte {
    sourceDirectory = layout.projectDirectory.dir("src/main/jte").asFile.toPath()
    targetDirectory = layout.buildDirectory.dir("jte-classes").get().asFile.toPath()
    trimControlStructures = true
    packageName = "br.ufpe.liber"
    generate()
    jteExtension("gg.jte.models.generator.ModelExtension") {
        property("language", "Kotlin")
    }
}

tasks.configureEach {
    if (listOf("kspKotlin", "inspectRuntimeClasspath").contains(name)) {
        mustRunAfter("generateJte")
    }
}

tasks.named("jar", Jar::class) {
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

tasks.register("releaseDate") {
    doLast {
        println(
            ZonedDateTime
                .now()
                .format(
                    DateTimeFormatter
                        .ofLocalizedDateTime(FormatStyle.MEDIUM)
                        .withLocale(Locale("pt-BR"))
                )
        )
    }
}

dependencies {
    ksp("io.micronaut.serde:micronaut-serde-processor")
    implementation("io.micronaut:micronaut-aop")
    implementation("io.micronaut:micronaut-http-client")
    implementation("io.micronaut.serde:micronaut-serde-jackson")
    implementation("io.micronaut.kotlin:micronaut-kotlin-runtime")
    implementation("io.micronaut.kotlin:micronaut-kotlin-extension-functions")

    compileOnly("org.graalvm.nativeimage:svm")
    runtimeOnly("ch.qos.logback:logback-classic")
    runtimeOnly("com.fasterxml.jackson.module:jackson-module-kotlin")

    // Views Dependencies
    implementation("gg.jte:jte-kotlin:${jteVersion}")
    implementation("gg.jte:jte-runtime:${jteVersion}")
    implementation("io.micronaut.views:micronaut-views-jte")
    jteGenerate("gg.jte:jte-models:${jteVersion}")
    jteGenerate("gg.jte:jte-native-resources:$jteVersion")

    implementation("org.jetbrains.kotlin:kotlin-reflect:${kotlinVersion}")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${kotlinVersion}")

    // If the project needs to add Serialization (to JSON, for example). Also check the
    // plugins section.
    // implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

    // Netty security overrides
    implementation("io.netty:netty-handler:4.1.101.Final")
    implementation("io.netty:netty-codec-http2:4.1.101.Final")

    // Accessibility Tests
    accessibilityTestImplementation("org.seleniumhq.selenium:selenium-java:4.15.0")
    accessibilityTestImplementation("com.deque.html.axe-core:selenium:4.8.0")

    // Kotest manual update
    testImplementation("io.micronaut.test:micronaut-test-kotest5:4.1.0")
}
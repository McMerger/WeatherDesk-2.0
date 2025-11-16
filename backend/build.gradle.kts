plugins {
    kotlin("jvm") version "2.2.20"
    application
    id("org.jetbrains.kotlin.plugin.serialization") version "2.2.20"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    google()
}

dependencies {
    // Client-side, serialization, and server-side
    testImplementation(kotlin("test"))
    implementation("io.ktor:ktor-client-core:3.1.1")
    implementation("io.ktor:ktor-client-cio:3.1.1")
    implementation("io.ktor:ktor-client-content-negotiation:3.1.1")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.1.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
    implementation("io.ktor:ktor-server-core:3.1.1")
    implementation("io.ktor:ktor-server-netty:3.1.1")
    implementation("io.ktor:ktor-server-content-negotiation:3.1.1")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.1.1")
    implementation("io.ktor:ktor-server-cors:3.1.1")

    // Logging
    implementation("ch.qos.logback:logback-classic:1.5.13")
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")

    // Firebase
    implementation("com.google.firebase:firebase-admin:9.2.0")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(23)
}
application {
    mainClass.set("MainKt")
}
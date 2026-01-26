import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("java")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

group = "com.gmoon.ai"
description = "AI module"

tasks {
    named<BootJar>("bootJar") { enabled = false }
    named<Jar>("jar") { enabled = false }
}

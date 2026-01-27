import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension
import org.apache.tools.ant.filters.ReplaceTokens
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("java")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

group = "com.gmoon.springcloud.spring-cloud-bus"
description = "spring-cloud-bus"

tasks {
    named<BootJar>("bootJar") { enabled = false }
    named<Jar>("jar") { enabled = false }

    // 집합 모듈의 빌드 태스크가 모든 하위 프로젝트의 빌드에 의존
    listOf("build", "clean")
        .forEach { taskName ->
            run {
                named(taskName) {
                    dependsOn(subprojects.map { it.tasks.named(name) })
                }
            }
        }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")

    repositories {
        mavenCentral()
    }

    configure<DependencyManagementExtension> {
        imports {
            // Spring Boot version 호환성 고려
            // https://spring.io/projects/spring-cloud
            // https://github.com/spring-cloud/spring-cloud-release/wiki/Spring-Cloud-2025.0-Release-Notes
            mavenBom("org.springframework.cloud:spring-cloud-dependencies:2025.0.1")
        }
    }

    dependencies {
        implementation("org.springframework.boot:spring-boot-starter-web")

        implementation("org.springframework.cloud:spring-cloud-starter-bus-amqp")

        compileOnly("org.projectlombok:lombok")
        testCompileOnly("org.projectlombok:lombok")
        annotationProcessor("org.projectlombok:lombok")
        testAnnotationProcessor("org.projectlombok:lombok")

        testImplementation("org.springframework.boot:spring-boot-starter-test")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}

project(":spring-cloud:spring-cloud-bus:spring-cloud-bus-common") {
    description = "spring-cloud-bus-common"

    tasks {
        named<BootJar>("bootJar") { enabled = false }
        named<Jar>("jar") { enabled = true }
    }
}

project(":spring-cloud:spring-cloud-bus:spring-cloud-bus-client") {
    description = "spring-cloud-bus-client"

    dependencies {
        implementation(project(":spring-cloud:spring-cloud-bus:spring-cloud-bus-common"))

        implementation("org.springframework.cloud:spring-cloud-starter-config")
        implementation("org.springframework.boot:spring-boot-starter-actuator")
    }
}

project(":spring-cloud:spring-cloud-bus:spring-cloud-bus-server") {
    description = "spring-cloud-bus-server"

    dependencies {
        implementation(project(":spring-cloud:spring-cloud-bus:spring-cloud-bus-common"))

        implementation("org.springframework.cloud:spring-cloud-config-server")
        implementation("org.springframework.boot:spring-boot-starter-actuator")
    }

    project.afterEvaluate {
        ext.set("projectDir", project.projectDir.absolutePath)
        ext.set("rootDir", project.rootDir.absolutePath)

        println("projectDir=${project.projectDir.absolutePath}")
        println("rootDir=${project.rootDir.absolutePath}")
    }

    tasks {
        withType<ProcessResources> {
            filesMatching("**/application.yml") {
                filter(
                    ReplaceTokens::class,
                    mapOf(
                        "tokens" to project.properties.mapValues { it.value.toString() },
                        "beginToken" to "@",
                        "endToken" to "@"
                    )
                )
            }
        }
    }
}

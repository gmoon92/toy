import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension
import org.gradle.launcher.daemon.protocol.Build
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("java")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

group = "com.gmoon.springcloud.spring-configserver"
description = "spring-configserver"

tasks {
    named<BootJar>("bootJar") {
        enabled = false
    }

    named<Jar>("jar") {
        enabled = false
    }

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

        testImplementation("org.springframework.boot:spring-boot-starter-test")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}

/***
 * path 형식:
 * - 절대 경로 ":" (콜론)으로 시작하면 절대 경로 (루트 프로젝트 기준)
 *     project(":spring-cloud:spring-configserver:spring-configserver-client")
 * - 상대 경로 ":" 없이 시작하면 상대 경로 (현재 프로젝트 기준)
 *     project("spring-configserver-client")
 *
 * 모듈명:
 * - settings.gradle.kts에 정의된 프로젝트 이름을 사용
 */
project(":spring-cloud:spring-configserver:spring-configserver-client") {
    description = "spring-configserver-client"

    dependencies {
        implementation("org.springframework.cloud:spring-cloud-starter-config")
        implementation("org.springframework.boot:spring-boot-starter-actuator")
    }
}

project(":spring-cloud:spring-configserver:spring-configserver-server") {
    description = "spring-configserver-server"

    dependencies {
        // Config Server 전용 의존성
        implementation("org.springframework.cloud:spring-cloud-config-server")
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
                expand(project.properties)
            }
        }
    }
}

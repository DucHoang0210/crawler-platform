plugins {
    java
    id("io.spring.dependency-management") version "1.1.6" apply false
    id("org.springframework.boot") version "3.2.4" apply false
}

allprojects {
    group = "com.dev"
    version = "1.0.0-SNAPSHOT"

    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")

    configure<JavaPluginExtension> {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
        enabled = false
    }

    tasks.named<Jar>("jar") {
        enabled = true
    }

    dependencies {
        // Lombok dùng chung cho tất cả modules
        compileOnly("org.projectlombok:lombok:1.18.30")
        annotationProcessor("org.projectlombok:lombok:1.18.30")
        testCompileOnly("org.projectlombok:lombok:1.18.30")
        testAnnotationProcessor("org.projectlombok:lombok:1.18.30")

        // Test Framework
        testImplementation("org.springframework.boot:spring-boot-starter-test")
    }
}

project(":crawler-api-app") {
    tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
        enabled = true
    }
}
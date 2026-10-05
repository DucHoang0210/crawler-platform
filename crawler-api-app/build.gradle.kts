import org.gradle.api.tasks.compile.JavaCompile

plugins {
    id("java")
}

group = "com.dev"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:6.0.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation ("org.springframework.boot:spring-boot-starter-quartz")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation ("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.flywaydb:flyway-core")
    implementation(project(":crawler-core"))
    implementation(project(":crawler-scheduler"))
    implementation(project(":crawler-multitenancy"))
    implementation(project(":crawler-scraper"))
    implementation(project(":crawler-common"))
    implementation(project(":crawler-ai"))
    implementation(project(":crawler-webhook"))
    implementation(project(":crawler-notification"))
    implementation("org.jsoup:jsoup:1.17.2")
    implementation("com.mchange:c3p0:0.9.5.5")
    implementation("org.springframework.security:spring-security-crypto:6.1.5")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.5.0")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-parameters")
}
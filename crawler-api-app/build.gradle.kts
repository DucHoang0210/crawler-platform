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
    implementation(project(":crawler-core"))
    implementation(project(":crawler-scheduler"))
    implementation(project(":crawler-multitenancy"))
    implementation(project(":crawler-scraper"))
    implementation(project(":crawler-common"))
}

tasks.test {
    useJUnitPlatform()
}
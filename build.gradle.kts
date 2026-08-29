plugins {
    java
    id("org.springframework.boot") version "4.1.1"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "faang.school"
version = "1.0"

val javaVersion = 25
val springCloudVersion = "2025.1.3"
val testcontainersVersion = "2.0.5"
val mapstructVersion = "1.6.3"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(javaVersion)
    }
}

repositories {
    mavenCentral()
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:$springCloudVersion")
        mavenBom("org.testcontainers:testcontainers-bom:$testcontainersVersion")
    }
}

dependencies {
    /**
     * Spring boot starters
     */
    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-liquibase")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign")
    implementation ("org.springframework.retry:spring-retry")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    // Swagger
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.1.0")

    /**
     * Database
     */
    runtimeOnly("org.postgresql:postgresql")
    implementation("org.liquibase:liquibase-core")
    implementation("redis.clients:jedis")

    /**
     * Utils & Logging
     */ 
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    implementation("org.mapstruct:mapstruct:$mapstructVersion")
    annotationProcessor("org.mapstruct:mapstruct-processor:$mapstructVersion")
    annotationProcessor("org.projectlombok:lombok-mapstruct-binding:0.2.0")

    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("org.codehaus.janino:janino")
    implementation("com.github.f4b6a3:uuid-creator:6.1.1")

    /**
     * Test Containers
     */
    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")

    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "com.vaadin.external.google", module = "android-json")
    } 
    testImplementation("org.springframework.boot:spring-boot-starter-data-redis") 
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test") 
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")

    testImplementation("org.testcontainers:testcontainers")
    testImplementation("org.testcontainers:testcontainers-junit-jupiter")
    testImplementation("org.testcontainers:testcontainers-postgresql")
    testImplementation("com.redis:testcontainers-redis:2.2.4")
    
    testImplementation("org.assertj:assertj-core")
}

tasks.withType<Test> {
    useJUnitPlatform()
    // jvmArgs("--enable-native-access=ALL-UNNAMED")
}

tasks.test {

    testLogging {
        events("passed", "skipped", "failed", "standardOut", "standardError")
        showStandardStreams = true
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}

tasks.bootJar {
    archiveFileName.set("service.jar")
}

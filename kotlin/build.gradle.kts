import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.72"
    kotlin("plugin.spring") version "1.3.72"
    kotlin("kapt") version "1.3.72"
    id("org.springframework.boot") version "2.3.1.RELEASE"
    id("io.spring.dependency-management") version "1.0.9.RELEASE"
    id("com.google.cloud.tools.jib") version "1.7.0"
}

group = "com.artemkaxboy"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

val kotlinLoggingVersion by extra("1.7.10")
val ktTelegramBotVersion by extra("1.3.0-beta")
val swaggerSnapshotVersion by extra("3.0.0-SNAPSHOT")
val swaggerStableVersion by extra("2.10.5")
val tag by extra(System.getenv("TAG") ?: project.version)

repositories {
    mavenCentral()
    // telegram bot   https://github.com/elbekD/kt-telegram-bot
    maven("https://jitpack.io")
    // to use swagger with flux we need 3.0.0-SNAPSHOT version
    maven("https://oss.jfrog.org/artifactory/oss-snapshot-local/")
}

dependencies {
    // common
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))

    // spring
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("org.postgresql:postgresql")
    testRuntimeOnly("com.h2database:h2:1.4.200")

    // implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

    // swagger
    implementation("io.springfox:springfox-swagger2:$swaggerSnapshotVersion")
    implementation("io.springfox:springfox-spring-webflux:$swaggerSnapshotVersion")
    implementation("io.springfox:springfox-swagger-ui:$swaggerStableVersion")

    // logging
    implementation("io.github.microutils:kotlin-logging:$kotlinLoggingVersion")

    // validation       https://www.baeldung.com/javax-validation
    implementation("javax.validation:validation-api")
    implementation("org.hibernate.validator:hibernate-validator")

    // telegram bot   https://github.com/elbekD/kt-telegram-bot
    implementation("com.github.elbekD:kt-telegram-bot:$ktTelegramBotVersion") {
        // it's conflicting with netty, and we don't need webhook mode
        exclude("org.eclipse.jetty")
    }

    // spring test
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.springframework.security:spring-security-test")

    // kapt
    kapt("org.springframework.boot:spring-boot-configuration-processor")
    compileOnly("org.springframework.boot:spring-boot-configuration-processor")
}

// https://peterevans.dev/posts/containerising-kotlin-with-jib/
jib {

    container {
        labels = mapOf("maintainer" to "Artem Kolin <artemkaxboy@gmail.com>")
        environment = mapOf("APPLICATION_VERSION" to "${project.version}")
        ports = listOf("8080")
        volumes = listOf("/config", "/application.properties", "/application.yml")
        user = "999"
        creationTime = "USE_CURRENT_TIMESTAMP"
    }

    to {
        image = "artemkaxboy/${rootProject.name}:${tag}"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        @Suppress("SpellCheckingInspection")
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}

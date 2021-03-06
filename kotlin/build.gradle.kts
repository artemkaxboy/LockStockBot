import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.72"
    kotlin("plugin.spring") version "1.3.72"
    kotlin("kapt") version "1.3.72"
    kotlin("plugin.noarg") version "1.3.72"
    id("org.springframework.boot") version "2.3.1.RELEASE"
    id("io.spring.dependency-management") version "1.0.9.RELEASE"
    id("com.google.cloud.tools.jib") version "1.7.0"
    id("org.jlleitschuh.gradle.ktlint") version "9.2.1"
}

group = "com.artemkaxboy"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

val kotlinLoggingVersion by extra("1.7.10")
val ktTelegramBotVersion by extra("1.3.5")
val swaggerVersion by extra("1.4.3")
val modelMapperVersion by extra("2.3.8")
val mockkVersion by extra("1.10.0")
val tag by extra(System.getenv("TAG") ?: project.version)

sourceSets {
    test {
        java {
            srcDirs("src/test/kotlin", "src/test/kotlinIntegration")
        }
    }
}

repositories {
    mavenCentral()
    // telegram bot   https://github.com/elbekD/kt-telegram-bot
    maven("https://jitpack.io")
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

    // https://github.com/springdoc/springdoc-openapi
    // swagger
    implementation("org.springdoc:springdoc-openapi-webflux-ui:$swaggerVersion")

    // logging
    implementation("io.github.microutils:kotlin-logging:$kotlinLoggingVersion")

    // validation       https://www.baeldung.com/javax-validation
    implementation("javax.validation:validation-api")
    implementation("org.hibernate.validator:hibernate-validator")
    implementation("org.glassfish:javax.el:3.0.0")

    // entity dto mapper    https://habr.com/ru/post/438808/
    // implementation("org.modelmapper:modelmapper:$modelMapperVersion")
    // locally fixed https://github.com/modelmapper/modelmapper/issues/553
    implementation(files("libs/modelmapper-2.3.9-SNAPSHOT.jar"))

    /*--------------------- telegram bot ---------------------------------------*/
    // @source https://github.com/elbekD/kt-telegram-bot
    implementation("com.github.elbekD:kt-telegram-bot:$ktTelegramBotVersion") {
        // it's conflicting with netty, and we don't need webhook mode
        exclude("org.eclipse.jetty")
    }
    /*--------------------- end of telegram bot ---------------------------------------*/

    // --------- chars
    implementation("org.jfree:jfreechart:1.5.0")

    // --------- dom parsing
    implementation("org.jsoup:jsoup:1.13.1")

    // spring test
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("io.mockk:mockk:$mockkVersion")

    // kapt
    kapt("org.springframework.boot:spring-boot-configuration-processor")
    kaptTest("org.springframework.boot:spring-boot-configuration-processor")
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
        image = "server.home:5000/artemkaxboy/${rootProject.name}"
        tags = setOf(tag as String)
        setAllowInsecureRegistries(true)
    }
}

noArg {
    annotation("javax.persistence.Entity")
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

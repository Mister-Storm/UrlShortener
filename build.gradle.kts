plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.4.3"
    id("io.spring.dependency-management") version "1.1.7"
    id("jacoco")
}

group = "br.com.misterstorm"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    developmentOnly("org.springframework.boot:spring-boot-docker-compose")
    runtimeOnly("org.postgresql:postgresql")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}


sourceSets {
    val intTest by creating {
        compileClasspath += sourceSets["main"].output + sourceSets["test"].output
        runtimeClasspath += sourceSets["main"].output + sourceSets["test"].output
    }
}

configurations {
    val intTestImplementation by getting {
        extendsFrom(configurations["testImplementation"])
    }
    val intTestRuntimeOnly by getting {
        extendsFrom(configurations["testRuntimeOnly"])
    }
}

tasks.register<Test>("integrationTest") {
    description = "Runs integration tests."
    group = "verification"
    testClassesDirs = sourceSets["intTest"].output.classesDirs
    classpath = sourceSets["intTest"].runtimeClasspath
    shouldRunAfter(tasks.test)
}

tasks.jacocoTestReport {
    dependsOn(tasks.named<Test>("test"), tasks.named<Test>("integrationTest"))

    reports {
        xml.required.set(true)
        html.required.set(true)
    }

    executionData.setFrom(
        executionData.setFrom(
            fileTree(layout.buildDirectory).include(
                "jacoco/test.exec",
                "jacoco/integrationTest.exec"
            )
        )
    )

    sourceSets(sourceSets["main"])
}

tasks.named<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
    dependsOn(tasks.named<Test>("test"), tasks.named<Test>("integrationTest"))

    violationRules {
        rule {
            element = "CLASS" // Check coverage per class
            limit {
                counter = "LINE"  // Check line coverage
                value = "COVEREDRATIO"
                minimum = "0.7".toBigDecimal() // 70% minimum
            }
        }
    }
}

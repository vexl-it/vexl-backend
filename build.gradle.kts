plugins {
    val springBootVersion = "2.7.0"
    `maven-publish` apply true
    `java-library` apply true

    id("org.springframework.boot") version springBootVersion
    id("io.spring.dependency-management") version "1.0.13.RELEASE"
}

allprojects {
    buildscript {
        project.extra.apply {
            set("springBootVersion", "2.7.3")
            set("liquibaseVersion", "4.12.0")
            set("lombokVersion", "1.18.24")
            set("jsonVersion", "20220924")
            set("firebaseVersion", "9.0.0")
        }
    }

    repositories {
        mavenLocal()
        maven {
            url = uri("https://repo.maven.apache.org/maven2/")
        }
    }
}

subprojects {
    group = "com.cleevio" // TODO change

    apply {
        plugin("java-library")
        plugin("org.springframework.boot")
        plugin("io.spring.dependency-management")
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    configurations {
        all {
            exclude(group= "org.springframework.boot", module= "spring-boot-starter-logging")
        }
    }

    dependencies {
        val lombokVersion: String by project.extra

        implementation("org.springframework.boot:spring-boot-starter-data-jpa")
        implementation("org.springframework.boot:spring-boot-starter-validation")
        implementation("org.springframework.boot:spring-boot-starter")
        implementation("org.springframework.boot:spring-boot-starter-web")
        implementation("org.springframework.boot:spring-boot-configuration-processor")
        implementation("org.springframework.boot:spring-boot-starter-log4j2")
        implementation("org.springframework.boot:spring-boot-starter-security")
        implementation("org.springframework.boot:spring-boot-starter-actuator")
        implementation("org.postgresql:postgresql:42.3.6")
        implementation("org.springframework.boot:spring-boot-devtools")
        implementation("io.sentry:sentry-spring-boot-starter:6.4.0")
        implementation("io.sentry:sentry-log4j2:6.4.0")
        implementation("org.springdoc:springdoc-openapi-ui:1.6.11")
        implementation("org.liquibase:liquibase-core:4.15.0")
        implementation("org.apache.pdfbox:pdfbox:2.0.26")
        implementation("io.micrometer:micrometer-registry-prometheus:1.10.3")

        if(name != "crypto") {
            implementation(project(":common:crypto"))
        }

        testImplementation("org.springframework.boot:spring-boot-starter-test")
        testImplementation("org.springframework.security:spring-security-test:5.7.1")
        testImplementation("org.testcontainers:postgresql:1.17.3")

        compileOnly("org.projectlombok:lombok:$lombokVersion") // oba
        annotationProcessor("org.projectlombok:lombok:$lombokVersion")

        testCompileOnly("org.projectlombok:lombok:$lombokVersion")
        testAnnotationProcessor("org.projectlombok:lombok:$lombokVersion")
    }

    tasks.withType<JavaCompile>() {
        options.encoding = "UTF-8"
        options.release.set(17)
    }

    tasks.withType<Javadoc>() {
        options.encoding = "UTF-8"
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    tasks.getByName<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
        this.archiveFileName.set("application.jar")
    }
}

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}

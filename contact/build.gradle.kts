val springBootVersion: String by project.extra
val jsonVersion: String by project.extra
val firebaseVersion: String by project.extra

dependencies {
    implementation("org.springdoc:springdoc-openapi-security:1.6.11")
    implementation("org.springframework.boot:spring-boot-starter-webflux:$springBootVersion")
    implementation("com.restfb:restfb:2022.7.0")
    implementation("com.vladmihalcea:hibernate-types-52:2.20.0")
    implementation("com.google.zxing:core:3.5.0")
    implementation("com.google.firebase:firebase-admin:$firebaseVersion")
    implementation("com.google.zxing:javase:3.5.0")
    implementation("org.json:json:$jsonVersion")

    testImplementation("com.jayway.jsonpath:json-path:2.7.0")
}



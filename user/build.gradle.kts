dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webflux:2.7.2")
    implementation("org.springframework:spring-context:5.3.22")
    implementation("org.springdoc:springdoc-openapi-security:1.6.11")
    implementation("org.springframework.boot:spring-boot-starter-cache:2.7.2")
    implementation("com.github.ben-manes.caffeine:caffeine:3.1.1")
    implementation("com.twilio.sdk:twilio:8.35.0")
    implementation("com.googlecode.libphonenumber:libphonenumber:8.13.13")

    testImplementation("com.google.guava:guava-testlib:31.1-jre")
    testImplementation("com.jayway.jsonpath:json-path:2.7.0")
}

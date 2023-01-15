val firebaseVersion: String by project.extra

dependencies {
    implementation("com.google.firebase:firebase-admin:$firebaseVersion")
    implementation("org.springdoc:springdoc-openapi-security:1.6.11")
    testImplementation("com.jayway.jsonpath:json-path:2.7.0")
}


val jsonVersion: String by project.extra
val firebaseVersion: String by project.extra
dependencies {
    implementation("org.json:json:$jsonVersion")
    implementation("com.google.firebase:firebase-admin:$firebaseVersion") // oba není v user
}

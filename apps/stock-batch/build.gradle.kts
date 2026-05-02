dependencies {
    // add-ons
    implementation(project(":core:stock"))
    implementation(project(":modules:jpa"))
    implementation(project(":supports:logging"))

    // web
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // batch
    implementation("org.springframework.boot:spring-boot-starter-batch")
    testImplementation("org.springframework.batch:spring-batch-test")

    // test-fixtures
    testImplementation(testFixtures(project(":modules:jpa")))
}

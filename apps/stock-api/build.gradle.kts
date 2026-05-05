dependencies {
    // add-ons
    implementation(project(":modules:jpa"))
    implementation(project(":core:stock"))
    implementation(project(":supports:logging"))

    // web
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:${project.properties["springDocOpenApiVersion"]}")

    // test-fixtures
    testImplementation(testFixtures(project(":modules:jpa")))
}

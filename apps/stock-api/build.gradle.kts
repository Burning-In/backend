dependencies {
    // add-ons
    implementation(project(":modules:jpa"))
    implementation(project(":supports:logging"))
    implementation(project(":shared-kernel"))

    // web
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:${project.properties["springDocOpenApiVersion"]}")

    // security
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("io.jsonwebtoken:jjwt-api:${project.properties["jjwtVersion"]}")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:${project.properties["jjwtVersion"]}")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:${project.properties["jjwtVersion"]}")

    // querydsl (Q-class 생성용 APT — annotationProcessor는 전이되지 않아 모듈별로 선언)
    annotationProcessor("com.querydsl:querydsl-apt::jakarta")
    annotationProcessor("jakarta.persistence:jakarta.persistence-api")
    annotationProcessor("jakarta.annotation:jakarta.annotation-api")

    // test-fixtures
    testImplementation(testFixtures(project(":modules:jpa")))
    testImplementation("org.springframework.security:spring-security-test")
}

dependencies {
    // add-ons
    implementation(project(":modules:jpa"))
    implementation(project(":supports:logging"))
    implementation(project(":shared-kernel"))

    // web
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // batch
    implementation("org.springframework.boot:spring-boot-starter-batch")

    // querydsl (Q-class 생성용 APT — annotationProcessor는 전이되지 않아 모듈별로 선언)
    annotationProcessor("com.querydsl:querydsl-apt::jakarta")
    annotationProcessor("jakarta.persistence:jakarta.persistence-api")
    annotationProcessor("jakarta.annotation:jakarta.annotation-api")
    testImplementation("org.springframework.batch:spring-batch-test")

    // test-fixtures
    testImplementation(testFixtures(project(":modules:jpa")))
}

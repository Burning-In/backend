plugins {
    `java-library`
}

dependencies {
    api(project(":modules:jpa"))

    // web
    implementation("org.springframework.boot:spring-boot-starter-web")

    // querydsl
    annotationProcessor("com.querydsl:querydsl-apt::jakarta")
    annotationProcessor("jakarta.persistence:jakarta.persistence-api")
    annotationProcessor("jakarta.annotation:jakarta.annotation-api")

    // test-fixtures
    testImplementation(testFixtures(project(":modules:jpa")))
}

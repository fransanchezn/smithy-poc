plugins {
    `java-library`
}

val springBootVersion = "4.0.3"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

dependencies {
    // Spring Boot dependencies for ErrorResponseException and ProblemDetail
    implementation("org.springframework.boot:spring-boot-starter-web:$springBootVersion")

    // Test dependencies
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testImplementation("org.assertj:assertj-core:3.25.3")
    testImplementation("com.fasterxml.jackson.core:jackson-databind:2.17.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

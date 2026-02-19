plugins {
    `java-library`
    id("software.amazon.smithy.gradle.smithy-jar")
}

val smithyVersion: String by project

dependencies {
    implementation("software.amazon.smithy:smithy-model:$smithyVersion")
    implementation("software.amazon.smithy:smithy-openapi:$smithyVersion")
}

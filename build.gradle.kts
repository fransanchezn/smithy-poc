plugins {
    id("software.amazon.smithy.gradle.smithy-jar") version "1.1.0" apply false
    id("software.amazon.smithy.gradle.smithy-base") version "1.1.0" apply false
}

val smithyVersion: String by project

subprojects {
    repositories {
        mavenCentral()
    }
}

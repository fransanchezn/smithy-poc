plugins {
    `java-library`
    id("software.amazon.smithy.gradle.smithy-base")
}

val smithyVersion: String by project

// The smithy-base plugin creates a 'smithyBuild' configuration
val smithyBuild by configurations.getting

tasks.named("compileJava") {
    dependsOn("smithyBuild")
}

sourceSets {
    main {
        java {
            srcDirs("model/")
        }
    }
}

dependencies {
    implementation("software.amazon.smithy:smithy-model:$smithyVersion")
    implementation("software.amazon.smithy:smithy-aws-traits:$smithyVersion")
    implementation(project(":error-example-trait"))

    smithyBuild(project(":error-example-trait"))
    smithyBuild("software.amazon.smithy:smithy-openapi:$smithyVersion")
    smithyBuild("software.amazon.smithy:smithy-aws-traits:$smithyVersion")
}

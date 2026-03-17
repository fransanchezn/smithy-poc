plugins {
    `java-library`
    id("software.amazon.smithy.gradle.smithy-base")
}

val smithyVersion: String by project
val alloyVersion: String by project

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
    implementation("com.disneystreaming.alloy:alloy-core:$alloyVersion")
    implementation(project(":error-trait"))

    smithyBuild(project(":error-trait"))
    smithyBuild("software.amazon.smithy:smithy-openapi:$smithyVersion")
    smithyBuild("com.disneystreaming.alloy:alloy-core:$alloyVersion")
    smithyBuild("com.disneystreaming.alloy:alloy-openapi_2.13:$alloyVersion")
}

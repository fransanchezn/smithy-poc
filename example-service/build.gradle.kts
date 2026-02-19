plugins {
    `java-library`
    id("software.amazon.smithy.gradle.smithy-base")
}

val smithyVersion: String by project
val alloyCoreVersion = "0.3.36"
val alloyOpenapiVersion = "0.3.21"

// The smithy-base plugin creates a 'smithyBuild' configuration
val smithyBuild by configurations.getting

dependencies {
    implementation("software.amazon.smithy:smithy-model:$smithyVersion")
    implementation("software.amazon.smithy:smithy-aws-traits:$smithyVersion")
    implementation("com.disneystreaming.alloy:alloy-core:$alloyCoreVersion")

    smithyBuild("com.disneystreaming.alloy:alloy-core:$alloyCoreVersion")
    smithyBuild("com.disneystreaming.alloy:alloy-openapi_2.13:$alloyOpenapiVersion")
    smithyBuild("software.amazon.smithy:smithy-openapi:$smithyVersion")
    smithyBuild("software.amazon.smithy:smithy-aws-traits:$smithyVersion")
}

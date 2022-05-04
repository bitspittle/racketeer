import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.shadow)
    application
}

group = "dev.bitspittle.racketeer.console"
version = "1.0-SNAPSHOT"

dependencies {
    implementation(libs.kotter)
    implementation(libs.yamlkt)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.appdirs)
    implementation(project(":model"))
    implementation(project(":scripting"))

    // For uploading data from playtesters.
    // Note: API key must be defined in a secrets.properties file or these dependencies are USELESS!
    implementation(libs.google.api.client)
    implementation(libs.google.api.services.drive)

    testImplementation(libs.junit)
    testImplementation(libs.truthish.jvm)
}

tasks {
    named<ShadowJar>("shadowJar") {
        minimize()
    }

    build {
        dependsOn(shadowJar)
    }
}

application {
    mainClass.set("MainKt")
}
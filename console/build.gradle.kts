plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlinx.serialization)
    application
}

group = "dev.bitspittle.racketeer.console"
version = "1.0-SNAPSHOT"

dependencies {
    implementation(libs.kotter)
    implementation(libs.yamlkt)
    implementation(libs.kotlinx.coroutines.core)
    implementation(project(":model"))
    implementation(project(":scripting"))
    testImplementation(libs.junit)
    testImplementation(libs.truthish.jvm)
}

application {
    mainClass.set("MainKt")
}
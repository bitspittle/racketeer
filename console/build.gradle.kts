plugins {
    alias(libs.plugins.kotlin.jvm)
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
}

application {
    mainClass.set("MainKt")
}
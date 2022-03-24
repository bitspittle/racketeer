plugins {
    alias(libs.plugins.kotlin.jvm)
    application
}

group = "dev.bitspittle.racketeer.console"
version = "1.0-SNAPSHOT"

dependencies {
    implementation(project(":model"))
}

application {
    mainClass.set("MainKt")
}
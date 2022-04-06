plugins {
    alias(libs.plugins.kotlin.jvm)
    application
}

group = "dev.bitspittle.limp.interpreter"
version = "1.0-SNAPSHOT"

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(project(":limp"))
}

application {
    mainClass.set("MainKt")
}

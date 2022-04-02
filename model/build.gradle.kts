plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlinx.serialization)
}

group = "dev.bitspittle.racketeer.model"

repositories {
    maven("https://us-central1-maven.pkg.dev/varabyte-repos/public")
}

kotlin {
    jvm()
    js(IR) {
        browser()
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.yamlkt)
                implementation(libs.uuid)
                implementation(project(":limp"))
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test.common)
                implementation(libs.kotlin.test.annotations.common)
            }
        }
    }
}
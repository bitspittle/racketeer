plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlinx.serialization)
}

group = "dev.bitspittle.racketeer.scripting"

repositories {
    maven("https://us-central1-maven.pkg.dev/varabyte-repos/public")
}

kotlin {
    jvm()
    js(IR) {
        browser()
    }
    sourceSets {
        all {
            // For "runTest"
            languageSettings.optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
        }
        val commonMain by getting {
            dependencies {
                implementation(project(":limp"))
                implementation(project(":model"))
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test.common)
                implementation(libs.kotlin.test.annotations.common)
                implementation(libs.truthish.common)
                implementation(libs.kotlinx.coroutines.test)
            }
        }
    }
}
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

group = "dev.bitspittle.limp"

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
        }

        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test.common)
                implementation(libs.kotlin.test.annotations.common)
                implementation(libs.truthish.common)
                implementation(libs.kotlinx.coroutines.test)
            }
        }

        val jvmTest by getting {
            dependencies {
                implementation(libs.truthish.jvm)
            }
        }

        val jsTest by getting {
            dependencies {
                implementation(libs.truthish.js)
            }
        }
    }
}
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
        val commonMain by getting {
        }

        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test.common)
                implementation(libs.kotlin.test.annotations.common)
                implementation(libs.truthish.common)
            }
        }

        val jvmTest by getting {
            dependencies {
                implementation(libs.truthish.jvm)
            }
        }
    }
}
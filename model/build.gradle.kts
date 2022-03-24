plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

group = "dev.bitspittle.racketeer.model"

kotlin {
    jvm()
    js(IR) {
        browser()
    }
//    sourceSets {
//        val commonMain by getting {
//            dependencies {
//            }
//        }
//
//        val jsMain by getting {
//            dependencies {
//            }
//        }
//
//        val jvmMain by getting {
//            dependencies {
//            }
//        }
//    }
}
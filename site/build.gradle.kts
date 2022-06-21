//import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.kobweb.application)
    alias(libs.plugins.kobwebx.markdown)
}

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
    maven("https://us-central1-maven.pkg.dev/varabyte-repos/public")
}

group = "dev.bitspittle.racketeer.site"
version = "1.0-SNAPSHOT"

kobweb {
    index {
        description.set("Powered by Kobweb")
    }
}

kotlin {
//    jvm {
//        tasks.withType<KotlinCompile> {
//            kotlinOptions.jvmTarget = "11"
//        }
//
//        tasks.named("jvmJar", Jar::class.java).configure {
//            archiveFileName.set("site.jar")
//        }
//    }
    js(IR) {
        moduleName = "docrimes"
        browser {
            commonWebpackConfig {
                outputFileName = "docrimes.js"
            }
        }
        binaries.executable()
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
            }
        }

        val jsMain by getting {
            dependencies {
                implementation(compose.web.core)
                implementation(libs.kobweb.core)
                implementation(libs.kobweb.silk.core)
                implementation(libs.kobweb.silk.icons.fa)
             }
        }

//        val jvmMain by getting {
//            dependencies {
//                implementation(libs.kobweb.api)
//             }
//        }
    }
}
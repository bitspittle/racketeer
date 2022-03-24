// import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import kotlinx.html.link
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.kobweb.application)
    alias(libs.plugins.kobwebx.markdown)
}

group = "dev.bitspittle.racketeer.site"
version = SimpleDateFormat("yyyyMMdd.kkmm").apply {
    timeZone = TimeZone.getTimeZone("UTC")
}.format(Date())

kobweb {
    appGlobals.put("version", version.toString())

    index {
        description.set("A Dominion-like game you can play by yourself -- powered by Kobweb")
    }
}

kotlin {
    /*
    jvm {
        tasks.withType<KotlinCompile> {
            kotlinOptions.jvmTarget = "11"
        }

        tasks.named("jvmJar", Jar::class.java).configure {
            archiveFileName.set("racketeer.jar")
        }
    }
    */
    js(IR) {
        moduleName = "racketeer"
        browser {
            commonWebpackConfig {
                outputFileName = "racketeer.js"
            }
        }
        binaries.executable()
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(project(":model"))
            }
        }

        val jsMain by getting {
            dependencies {
                implementation(compose.web.core)
                implementation(libs.kobweb.core)
                implementation(libs.kobweb.silk.core)
                implementation(libs.kobweb.silk.icons.fa)
                implementation(libs.kobwebx.markdown)
             }
        }

        /*
        val jvmMain by getting {
            dependencies {
                implementation(libs.kobweb.api)
             }
        }
        */
    }
}
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.shadow)
    application
}

val rootFolder = "do-crimes"
group = "dev.bitspittle.racketeer.console"
version = "0.3.1"

dependencies {
    implementation(libs.kotter)
    implementation(libs.yamlkt)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.appdirs)
    implementation(project(":model"))
    implementation(project(":scripting"))

    // For uploading data from playtesters.
    // Note: API key must be defined in a secrets.properties file or these dependencies are USELESS!
    implementation(libs.google.api.client)
    implementation(libs.google.api.services.drive)

    testImplementation(libs.junit)
    testImplementation(libs.truthish.jvm)
}

tasks {
    named<ShadowJar>("shadowJar") {
        archiveBaseName.set(rootFolder)
        archiveClassifier.set("")
        archiveVersion.set("")
        minimize {
            // Leave this jar in or else Windows always shows the virtual terminal
            exclude(dependency("org.jline:jline-terminal-jansi:.*"))
        }
    }

    named<Zip>("distZip") {
        archiveFileName.set("${rootFolder}.zip")
    }

    named<Zip>("shadowDistZip") {
        archiveFileName.set("${rootFolder}.zip")
    }

    build {
        dependsOn(shadowJar)
    }

    val projectProperties by registering(WriteProperties::class) {
        outputFile = layout.buildDirectory.file("project.properties").get().asFile
        encoding = "UTF-8"

        property("version", project.version)
    }

    processResources {
        from(projectProperties)
    }
}

application {
    applicationName = rootFolder
    mainClass.set("MainKt")
}
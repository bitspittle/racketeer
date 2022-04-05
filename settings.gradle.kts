pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://us-central1-maven.pkg.dev/varabyte-repos/public")
    }
}

rootProject.name = "racketeer"

include(":limp")
include(":model")
include(":scripting")
include(":console")
include(":site")


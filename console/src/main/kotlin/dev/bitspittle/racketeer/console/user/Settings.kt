package dev.bitspittle.racketeer.console.user

import dev.bitspittle.racketeer.console.command.commands.system.UserData
import kotlinx.serialization.Serializable
import net.mamoe.yamlkt.Yaml
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText

@Serializable
data class Settings(
    // Normal user settings

    // Admin user settings
    var maskCards: Boolean = true,
    var highlightNewCards: Boolean = true,
    var showDebugInfo: Boolean = false,
    var enableAdminFeatures: Boolean = false,
) {
    fun setFrom(other: Settings) {
        this.maskCards = other.maskCards
        this.highlightNewCards = other.highlightNewCards
        this.showDebugInfo = other.showDebugInfo
        this.enableAdminFeatures = other.enableAdminFeatures
    }
}

fun Settings.save(userData: UserData) {
    val self = this
    userData.pathForSettings().apply {
        parent.createDirectories()
        writeText(Yaml.encodeToString(self))
    }
}
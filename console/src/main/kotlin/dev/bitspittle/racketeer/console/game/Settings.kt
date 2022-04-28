package dev.bitspittle.racketeer.console.game

import dev.bitspittle.racketeer.console.command.commands.system.UserDataSupport
import kotlinx.serialization.Serializable
import net.mamoe.yamlkt.Yaml
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText

@Serializable
data class Settings(
    var showDebugInfo: Boolean = false,
    var enableAdminFeatures: Boolean = false,
) {
    fun setFrom(other: Settings) {
        this.showDebugInfo = other.showDebugInfo
        this.enableAdminFeatures = other.enableAdminFeatures
    }
}

fun Settings.save() {
    val self = this
    UserDataSupport.pathForSettings().apply {
        parent.createDirectories()
        writeText(Yaml.encodeToString(self))
    }
}
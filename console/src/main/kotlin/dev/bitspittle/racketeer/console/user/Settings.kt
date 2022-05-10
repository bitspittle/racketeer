package dev.bitspittle.racketeer.console.user

import dev.bitspittle.racketeer.console.command.commands.system.UserData
import dev.bitspittle.racketeer.console.utils.encodeToYaml
import kotlinx.serialization.Serializable
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText

@Serializable
data class Settings(
    var admin: Admin = Admin(),
) {
    @Serializable
    data class Admin(
        var maskCards: Boolean = true,
        var highlightNewCards: Boolean = true,
        var showDebugInfo: Boolean = false,
        var enabled: Boolean = false,
    )

    fun setFrom(other: Settings) {
       admin = other.admin.copy()
    }
}

val Settings.inAdminModeAndShowDebugInfo get() = admin.enabled && admin.showDebugInfo

fun Settings.save(userData: UserData) {
    val self = this
    userData.pathForSettings().apply {
        parent.createDirectories()
        writeText(self.encodeToYaml())
    }
}
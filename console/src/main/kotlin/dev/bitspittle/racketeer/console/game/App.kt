package dev.bitspittle.racketeer.console.game

import dev.bitspittle.limp.types.Logger
import dev.bitspittle.racketeer.console.command.commands.system.UserData
import dev.bitspittle.racketeer.console.utils.UploadService
import java.util.Properties

interface App {
    fun quit()
    val properties: Properties
    val logger: Logger
    val userData: UserData
    val uploadService: UploadService
}

val App.version: String get() = properties.getProperty("version")
package dev.bitspittle.racketeer.console.game

import dev.bitspittle.limp.types.Logger
import dev.bitspittle.racketeer.console.command.commands.system.UserDataDir
import dev.bitspittle.racketeer.console.utils.UploadService
import java.util.Properties
import kotlin.io.path.absolutePathString

interface App {
    fun quit()
    val properties: Properties
    val logger: Logger
    val userDataDir: UserDataDir
    val uploadService: UploadService
}

val App.version: String get() = properties.getProperty("version")

// We need a semi-permanent ID that won't change across playruns. Online resources recommend MAC addresses but
// no guarantee that won't crash the user if they're offline or some other reason (it crashed me!). So, just
// use the current user data directory. Should be good enough for our needs!
val App.playtestId get() = (userDataDir.path.absolutePathString().hashCode().toUInt() % 1000000u).toString().padStart(6, '0')
package dev.bitspittle.racketeer.console.game

import dev.bitspittle.limp.types.Logger
import dev.bitspittle.racketeer.console.command.commands.system.UserData
import dev.bitspittle.racketeer.console.utils.UploadService

interface App {
    fun quit()
    val logger: Logger
    val userData: UserData
    val uploadService: UploadService
}
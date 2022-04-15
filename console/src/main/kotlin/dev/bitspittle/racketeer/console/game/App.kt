package dev.bitspittle.racketeer.console.game

import dev.bitspittle.limp.types.Logger

interface App {
    fun quit()
    val logger: Logger
}
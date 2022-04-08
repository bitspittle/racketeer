package dev.bitspittle.racketeer.console.command

import dev.bitspittle.racketeer.console.game.GameContext

abstract class Command(protected val ctx: GameContext) {
    abstract val title: String
    open val description: String? = null

    open suspend fun invoke() = false
}
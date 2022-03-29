package dev.bitspittle.racketeer.console.command

import dev.bitspittle.racketeer.console.GameContext

abstract class Command(protected val ctx: GameContext) {
    abstract val title: String
    open val description: String? = null

    open fun invoke() = Unit
}
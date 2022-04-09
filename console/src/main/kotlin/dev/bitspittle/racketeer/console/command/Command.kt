package dev.bitspittle.racketeer.console.command

import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.model.card.Card

abstract class Command(protected val ctx: GameContext) {
    abstract val title: String
    open val description: String? = null

    open suspend fun invoke() = false

    protected fun describeForTitle(item: Any): String {
        return when (item) {
            is Card -> ctx.describer.describe(item, concise = true)
            else -> item.toString()
        }
    }

    protected fun describeForDescription(item: Any): String? {
        return when (item) {
            is Card -> ctx.describer.describe(item, concise = false )
            else -> null
        }
    }

}
package dev.bitspittle.racketeer.console.command

import com.varabyte.kotter.foundation.text.*
import com.varabyte.kotter.runtime.render.RenderScope
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.model.card.Card

abstract class Command(protected val ctx: GameContext) {
    enum class Type {
        /** A command which reads the game state without changing anything. Super safe! */
        Read,
        /** A command which can't currently be invoked. */
        Disabled,
        /** A command which represents a normal game action that modifies the game state somehow, e.g. playing a card. */
        Modify,
        /** A command which represents a bigger action that requires a bit of thought. Proceed with caution! */
        ModifyAlt,
        /** A command which represents a dangerous, potentially game-destroying action. Are you sure??? */
        Danger,
    }

    open val type: Type = Type.Read

    abstract val title: String
    open val description: String? = null

    fun renderTitleInto(scope: RenderScope) {
        scope.apply {
            scopedState {
                when (type) {
                    Type.Read -> {}
                    Type.Modify -> { bold() }
                    Type.Disabled -> black(isBright = true)
                    Type.ModifyAlt -> cyan()
                    Type.Danger -> red()
                }
                textLine(title)
            }
        }
    }

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
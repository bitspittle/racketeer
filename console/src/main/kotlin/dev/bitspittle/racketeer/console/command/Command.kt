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
        /** A command which recommends caution, often leading to a dangerous or permanent effect on the next screen. */
        Warning,
        /** A command which represents a dangerous, potentially game-destroying action. Are you sure??? */
        Danger,
    }

    open val type: Type = Type.Read

    abstract val title: String
    /** Extra information to show to the right of the title, aligned with all other commands in this section */
    open val meta: String? = null
    open val description: String? = null

    fun renderTitleInto(scope: RenderScope, padding: Int) {
        scope.apply {
            scopedState {
                when (type) {
                    Type.Read -> {}
                    Type.Modify -> { bold() }
                    Type.Disabled -> black(isBright = true)
                    Type.ModifyAlt -> cyan()
                    Type.Warning -> yellow()
                    Type.Danger -> red()
                }
                // padding + 1 for a space between the title and the metadata
                text(title.padEnd(padding + 1)); textLine(meta ?: "")
            }
        }
    }

    open suspend fun invoke() = false

    protected fun describeForTitle(item: Any): String {
        return when (item) {
            is Card -> ctx.describer.describeCard(item, concise = true)
            else -> item.toString()
        }
    }

    protected fun describeForDescription(item: Any): String? {
        return when (item) {
            is Card -> ctx.describer.describeCard(item, concise = false )
            else -> null
        }
    }

}
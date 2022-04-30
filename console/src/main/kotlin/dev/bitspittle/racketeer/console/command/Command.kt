package dev.bitspittle.racketeer.console.command

import com.varabyte.kotter.foundation.text.*
import com.varabyte.kotter.runtime.render.RenderScope
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.model.card.Card
import dev.bitspittle.racketeer.scripting.methods.collection.FormattedItem

abstract class Command(protected val ctx: GameContext) {
    enum class Type {
        /** A command which reads the game state without changing anything. Super safe! */
        Normal,
        /** A command which can't currently be invoked. */
        Disabled,
        /** A command which represents a normal game action that modifies the game state somehow, e.g. playing a card. */
        Emphasized,
        /**
         * A command which is worth drawing the user's attention to as a recommended way to move the game forward.
         * Still, users should proceed with caution if they're not ready.
         *
         * There should either be a single accented item per menu or none.
         */
        Accented,
        /** A command which recommends caution, often leading to a dangerous or permanent effect on the next screen. */
        Warning,
        /** A command which represents a dangerous, potentially game-destroying action. Are you sure??? */
        Danger,
        /**
         * A special type which causes this command to get filtered from the final list, which can sometimes be the
         * most convenient way to remove an element from the middle of a bunch of options.
         */
        Hidden,
    }

    open val type: Type = Type.Normal

    abstract val title: String
    /** Extra information to show to the right of the title, aligned with all other commands in this section */
    open val extra: String? = null
    open val description: String? = null

    fun renderTitleInto(scope: RenderScope, padding: Int) {
        scope.apply {
            scopedState {
                when (type) {
                    Type.Normal, Type.Hidden -> {}
                    Type.Emphasized -> { bold() }
                    Type.Disabled -> black(isBright = true)
                    Type.Accented -> cyan()
                    Type.Warning -> yellow()
                    Type.Danger -> red()
                }
                // padding + 1 for a space between the title and the extra data
                text(title.padEnd(padding + 1)); textLine(extra ?: "")
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
            is FormattedItem -> describeForDescription(item.wrapped)
            else -> null
        }
    }

}
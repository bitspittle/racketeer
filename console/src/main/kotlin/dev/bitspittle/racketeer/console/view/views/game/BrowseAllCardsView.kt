package dev.bitspittle.racketeer.console.view.views.game

import com.varabyte.kotter.foundation.input.Key
import com.varabyte.kotter.foundation.input.Keys
import com.varabyte.kotter.foundation.text.cyan
import com.varabyte.kotter.foundation.text.text
import com.varabyte.kotter.foundation.text.textLine
import com.varabyte.kotter.runtime.render.RenderScope
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.command.commands.game.ViewCardTemplateCommand

class BrowseAllCardsView(ctx: GameContext, private val sortingOrder: SortingOrder = SortingOrder.NAME) : GameView(ctx) {
    enum class SortingOrder {
        NAME,
        TIER;

        fun next(): SortingOrder {
            return when (this) {
                NAME -> TIER
                TIER -> NAME
            }
        }
    }

    private val sortedCards = ctx.data.cards.sortedBy { it.name }.let { cards ->
        // Even if we sort by tier, it should still be name-sorted secondarily
        if (sortingOrder == SortingOrder.TIER) cards.sortedBy { it.tier } else cards
    }

    override fun createCommands(): List<Command> =
        sortedCards.map { ViewCardTemplateCommand(ctx, it) }

    override suspend fun handleAdditionalKeys(key: Key): Boolean {
        return if (key == Keys.SPACE) {
            ctx.viewStack.replaceView(BrowseAllCardsView(ctx, sortingOrder.next()))
            true
        } else false
    }

    override fun RenderScope.renderUpperFooter() {
        val sortingWord = if (sortingOrder == SortingOrder.NAME) {
            "tier"
        } else {
            "name"
        }
        text("Press "); cyan { text("SPACE") }; textLine(" to sort by $sortingWord.")
    }
}
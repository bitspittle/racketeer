package dev.bitspittle.racketeer.console.view.views.game

import com.varabyte.kotter.foundation.input.Key
import com.varabyte.kotter.foundation.input.Keys
import com.varabyte.kotter.foundation.text.cyan
import com.varabyte.kotter.foundation.text.text
import com.varabyte.kotter.foundation.text.textLine
import com.varabyte.kotter.runtime.render.RenderScope
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.command.commands.admin.CreateCardCommand
import dev.bitspittle.racketeer.console.command.commands.game.ViewCardCommand
import dev.bitspittle.racketeer.console.command.commands.game.ViewCardTemplateCommand
import dev.bitspittle.racketeer.console.view.View
import dev.bitspittle.racketeer.model.card.Card

enum class CardSortingOrder {
    NAME,
    TIER;

    fun next(): CardSortingOrder {
        return when (this) {
            NAME -> TIER
            TIER -> NAME
        }
    }
}

class BrowseAllCardsView(ctx: GameContext, private val sortingOrder: CardSortingOrder = CardSortingOrder.NAME) : View(ctx) {
    private val sortedCards = ctx.data.cards.sortedBy { it.name }.let { cards ->
        // Even if we sort by tier, it should still be name-sorted secondarily
        if (sortingOrder == CardSortingOrder.TIER) cards.sortedBy { it.tier } else cards
    }

    override fun createCommands(): List<Command> =
        sortedCards.map { ViewCardTemplateCommand(ctx, it) }

    override suspend fun handleAdditionalKeys(key: Key): Boolean {
        return if (key == Keys.SPACE) {
            ctx.viewStack.replaceView(BrowseAllCardsView(ctx, sortingOrder.next()))
            true
        } else false
    }

    override fun RenderScope.renderFooter() {
        val sortingWord = if (sortingOrder == CardSortingOrder.NAME) {
            "tier"
        } else {
            "name"
        }
        text("Press "); cyan { text("SPACE") }; textLine(" to sort by $sortingWord.")
    }
}
package dev.bitspittle.racketeer.console.view.views.game

import com.varabyte.kotter.foundation.input.CharKey
import com.varabyte.kotter.foundation.input.Key
import com.varabyte.kotter.foundation.input.Keys
import com.varabyte.kotter.foundation.text.black
import com.varabyte.kotter.foundation.text.cyan
import com.varabyte.kotter.foundation.text.text
import com.varabyte.kotter.foundation.text.textLine
import com.varabyte.kotter.runtime.MainRenderScope
import com.varabyte.kotter.runtime.render.RenderScope
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.command.commands.game.ViewCardTemplateCommand
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.utils.CardSearcher

class BrowseAllCardsView(ctx: GameContext, private var sortingOrder: SortingOrder = SortingOrder.NAME) : GameView(ctx) {
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

    private val cardSearcher = CardSearcher(ctx.data.cards)
    private var searchPrefix = ""

    private val sortedCards = ctx.data.cards.sortedBy { it.name }.let { cards ->
        // Even if we sort by tier, it should still be name-sorted secondarily
        if (sortingOrder == SortingOrder.TIER) cards.sortedBy { it.tier } else cards
    }

    override fun createCommands(): List<Command> =
        cardSearcher.cards
            .let { cards -> if (sortingOrder == SortingOrder.TIER) cards.sortedBy { it.tier } else cards }
            .map { ViewCardTemplateCommand(ctx, it, "(Tier ${it.tier + 1})") }

    override suspend fun handleAdditionalKeys(key: Key): Boolean {
        return if (key is CharKey && (key.code.isLetter() || key.code == ' ') && sortingOrder == SortingOrder.NAME) {
            searchPrefix += key.code.lowercase()
            currIndex = cardSearcher.search(searchPrefix)
            true
        } else if (key == Keys.BACKSPACE && sortingOrder == SortingOrder.NAME) {
            searchPrefix = searchPrefix.dropLast(1)
            true
        } else if (key == Keys.TAB) {
            currIndex = 0
            sortingOrder = sortingOrder.next()
            searchPrefix = ""
            refreshCommands()
            true
        } else {
            false
        }
    }

    override fun MainRenderScope.renderContentUpper() {
        if (searchPrefix != "") {
            black(isBright = true) { textLine("Search: " + searchPrefix.lowercase()) }
            textLine()
        }
    }

    override fun RenderScope.renderUpperFooter() {
        val sortingWord = if (sortingOrder == SortingOrder.NAME) {
            "tier"
        } else {
            "name"
        }
        text("Press "); cyan { text("TAB") }; textLine(" to sort by $sortingWord.")
        if (sortingOrder == SortingOrder.NAME) {
            text("Press "); cyan { text("A-Z") }; textLine(" to jump to cards by name.")
        }
    }
}
package dev.bitspittle.racketeer.console.view.views.game.cards

import com.varabyte.kotter.foundation.input.CharKey
import com.varabyte.kotter.foundation.input.Key
import com.varabyte.kotter.foundation.input.Keys
import com.varabyte.kotter.foundation.text.*
import com.varabyte.kotter.runtime.MainRenderScope
import com.varabyte.kotter.runtime.render.RenderScope
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.command.commands.game.cards.ViewCardTemplateCommand
import dev.bitspittle.racketeer.console.command.commands.game.cards.shouldMask
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.utils.CardSearcher
import dev.bitspittle.racketeer.console.view.View

class CardListView(ctx: GameContext, private var sortingOrder: SortingOrder = SortingOrder.NAME) : View(ctx) {
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

    private val cards = ctx.data.cards.sortedBy { it.name }

    private val cardSearcher = CardSearcher(cards.filter { !it.shouldMask(ctx) })
    private var searchPrefix = ""

    override fun createCommands(): List<Command> =
        // Even if we sort by tier, it should still be name-sorted secondarily
        cards
            .let { cards -> if (sortingOrder == SortingOrder.TIER) cards.sortedBy { it.tier } else cards }
            .map { ViewCardTemplateCommand(ctx, it, if (sortingOrder == SortingOrder.TIER) "(Tier ${it.tier + 1})" else null) }

    override suspend fun handleAdditionalKeys(key: Key): Boolean {
        return if (key is CharKey && (key.code.isLetter() || key.code == ' ') && sortingOrder == SortingOrder.NAME) {
            searchPrefix += key.code.lowercase()
            currIndex = cardSearcher.search(searchPrefix)?.let { found -> cards.indexOf(found) } ?: 0
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

    private val numCardsUsed = ctx.data.cards.count { ctx.userStats.cards.contains(it.name) }
    override fun MainRenderScope.renderContentUpper() {
        yellow { textLine("You have discovered $numCardsUsed out of ${ctx.data.cards.size} cards.") }
        textLine()

        textLine("Sorted by: ${sortingOrder.name.lowercase().capitalize()}")
        textLine()

        if (searchPrefix.isNotEmpty()) {
            black(isBright = true) { textLine("Search: " + searchPrefix.lowercase()) }
            textLine()
        }
    }

    override fun RenderScope.renderFooterUpper() {
        val sortingWord = sortingOrder.next().name.lowercase()
        text("Press "); cyan { text("TAB") }; textLine(" to sort by $sortingWord.")
        if (sortingOrder == SortingOrder.NAME) {
            text("Press "); cyan { text("A-Z") }; textLine(" to jump to cards by name.")
        }
    }
}
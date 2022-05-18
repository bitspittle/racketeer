package dev.bitspittle.racketeer.console.view.views.game.cards

import com.varabyte.kotter.foundation.input.Key
import com.varabyte.kotter.foundation.input.Keys
import com.varabyte.kotter.foundation.text.cyan
import com.varabyte.kotter.foundation.text.text
import com.varabyte.kotter.foundation.text.textLine
import com.varabyte.kotter.runtime.MainRenderScope
import com.varabyte.kotter.runtime.render.RenderScope
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.command.commands.game.cards.ViewCardCommand
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.view.View
import dev.bitspittle.racketeer.model.card.Card
import dev.bitspittle.racketeer.model.card.vpTotal

/** A browse view for a large list of cards that you can sort and filter in different ways */
class BrowseManyCardsView(ctx: GameContext, cards: List<Card>) : View(ctx) {
    enum class SortingOrder {
        NAME,
        TIER,
        PILE,
        VICTORY_POINTS,
    }

    // Always sort the cards at least by name a bit, to avoid giving away meaningful order information. For other
    // sort patterns, they will still be sorted by name as a secondary sort
    private val cards = cards.sortedBy { it.template.name }
    private val pileNames = cards.associateWith { ctx.describer.describePileTitle(ctx.state, ctx.state.pileFor(it)!!) }
    private var sortingOrder = SortingOrder.PILE
    private var typeFilter: String? = null
    private val typeFilters = listOf<String?>(null) + ctx.data.cardTypes

    override fun createCommands() = when (sortingOrder) {
        SortingOrder.PILE -> cards.sortedBy { card -> pileNames.getValue(card) }
        SortingOrder.TIER -> cards.sortedBy { it.template.tier }
        // For cards with same VP total and same name, sort by pile
        SortingOrder.VICTORY_POINTS -> cards.sortedBy { card -> pileNames.getValue(card) }.sortedByDescending { it.vpTotal }
        SortingOrder.NAME -> cards
    }.filter { card ->
        if (typeFilter == null) true else card.template.types.any { it.equals(typeFilter, ignoreCase = true) }
    }.map { card ->
        ViewCardCommand(
            ctx, card, when (sortingOrder) {
                SortingOrder.NAME -> null
                SortingOrder.TIER -> "(Tier ${card.template.tier + 1})"
                SortingOrder.PILE, SortingOrder.VICTORY_POINTS -> "(${pileNames.getValue(card)})"
            }
        )
    }
        .takeIf { it.isNotEmpty() }
        ?: listOf(object : Command(ctx) {
            override val type = Type.Disabled
            override val title = "(No match)"
        })

    override fun MainRenderScope.renderContentUpper() {
        textLine("Sorted by: ${sortingOrder.name.replace('_', ' ').lowercase().capitalize()}")
        textLine("Filtered by: ${typeFilter ?: "(No filter)"}")
        textLine()
    }

    override fun RenderScope.renderFooterUpper() {
        text("Press "); cyan { text("1") }; textLine( " to sort by name.")
        text("Press "); cyan { text("2") }; textLine( " to sort by tier.")
        text("Press "); cyan { text("3") }; textLine( " to sort by pile.")
        text("Press "); cyan { text("4") }; textLine( " to sort by victory points.")
        text("Press "); cyan { text("LEFT") }; text(" and "); cyan { text("RIGHT") }; textLine( " to change the current type filter.")
    }

    override suspend fun handleAdditionalKeys(key: Key): Boolean {
        when (key) {
            Keys.DIGIT_1 -> sortingOrder = SortingOrder.NAME
            Keys.DIGIT_2 -> sortingOrder = SortingOrder.TIER
            Keys.DIGIT_3 -> sortingOrder = SortingOrder.PILE
            Keys.DIGIT_4 -> sortingOrder = SortingOrder.VICTORY_POINTS
            Keys.LEFT -> typeFilter = typeFilters[(typeFilters.indexOf(typeFilter) - 1 + typeFilters.size) % typeFilters.size]
            Keys.RIGHT -> typeFilter = typeFilters[(typeFilters.indexOf(typeFilter) + 1) % typeFilters.size]
            else -> return false
        }

        currIndex = 0
        refreshCommands()
        return true
    }
}
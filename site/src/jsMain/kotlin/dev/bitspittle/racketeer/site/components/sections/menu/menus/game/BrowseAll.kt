package dev.bitspittle.racketeer.site.components.sections.menu.menus.game

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.css.TextAlign
import com.varabyte.kobweb.compose.css.WhiteSpace
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.foundation.layout.RowScope
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.silk.components.layout.SimpleGrid
import com.varabyte.kobweb.silk.components.layout.numColumns
import com.varabyte.kobweb.silk.components.style.*
import com.varabyte.kobweb.silk.components.text.SpanText
import dev.bitspittle.racketeer.model.card.vpTotal
import dev.bitspittle.racketeer.model.game.getOwnedCards
import dev.bitspittle.racketeer.model.game.isGameOver
import dev.bitspittle.racketeer.site.components.sections.ReadOnlyStyle
import dev.bitspittle.racketeer.site.components.sections.menu.Menu
import dev.bitspittle.racketeer.site.components.sections.menu.MenuActions
import dev.bitspittle.racketeer.site.components.util.installPopup
import dev.bitspittle.racketeer.site.components.widgets.ArrowButton
import dev.bitspittle.racketeer.site.components.widgets.ArrowDirection
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

class BrowseAllCardsMenu(
    private val params: GameMenuParams,
    initialSortingOrder: SortingOrder = if (params.ctx.state.isGameOver) SortingOrder.VICTORY_POINTS else SortingOrder.PILE,
    initialTypeFilter: String? = null
) : Menu {
    enum class SortingOrder {
        NAME,
        TIER,
        PILE,
        COST,
        VICTORY_POINTS,
    }

    override val title = "Browse All Cards"

    private var sortingOrder by mutableStateOf<SortingOrder>(initialSortingOrder)
    private var typeFilter by mutableStateOf(initialTypeFilter)
    private val typeFilters = listOf<String?>(null) + params.ctx.data.cardTypes

    override val topRow: @Composable RowScope.() -> Unit = {
        SimpleGrid(
            numColumns(4), Modifier
                .fillMaxWidth()
                .gap(10.px)
                .whiteSpace(WhiteSpace.NoWrap)
                .gridTemplateColumns("min-content min-content 1fr min-content")
        ) {
            SpanText("Sorted by:")
            ArrowButton(ArrowDirection.LEFT) {
                sortingOrder =
                    SortingOrder.values()[(sortingOrder.ordinal - 1 + SortingOrder.values().size) % SortingOrder.values().size]
            }
            SpanText(
                sortingOrder.name.replace('_', ' ').lowercase().capitalize(),
                Modifier.textAlign(TextAlign.Center)
            )
            ArrowButton(ArrowDirection.RIGHT) {
                sortingOrder = SortingOrder.values()[(sortingOrder.ordinal + 1) % SortingOrder.values().size]
            }

            SpanText("Filtered by:")
            ArrowButton(ArrowDirection.LEFT) {
                typeFilter =
                    typeFilters[(typeFilters.indexOf(typeFilter) - 1 + typeFilters.size) % typeFilters.size]
            }
            SpanText(
                typeFilter ?: "(No filter)",
                Modifier.textAlign(TextAlign.Center)
            )
            ArrowButton(ArrowDirection.RIGHT) {
                typeFilter = typeFilters[(typeFilters.indexOf(typeFilter) + 1) % typeFilters.size]
            }
        }
    }

    @Composable
    override fun renderContent(actions: MenuActions) {
        with(params) {
            var cards = ctx.state.getOwnedCards().sortedBy { it.template.name }
            val pileNames =
                cards.associateWith { ctx.describer.describePileTitle(ctx.state, ctx.state.pileFor(it)!!) }

            cards = when (sortingOrder) {
                SortingOrder.NAME -> cards
                SortingOrder.TIER -> cards.sortedBy { card -> card.template.tier }
                SortingOrder.PILE -> cards.sortedBy { card -> pileNames.getValue(card) }
                SortingOrder.COST -> cards.sortedBy { card -> card.template.cost }
                SortingOrder.VICTORY_POINTS -> cards
                    // For cards with same VP total and same name, sort by pile
                    .sortedBy { card -> pileNames.getValue(card) }
                    .sortedByDescending { card -> card.vpTotal }
            }

            if (typeFilter != null) {
                cards = cards
                    .filter { card ->
                        card.template.types
                            .any { type -> type.equals(typeFilter, ignoreCase = true) }
                    }
            }

            cards.forEach { card ->
                val extraText = when (sortingOrder) {
                    SortingOrder.NAME -> null
                    SortingOrder.TIER -> "Tier ${card.template.tier + 1}"
                    SortingOrder.COST -> ctx.describer.describeCash(card.template.cost)
                    SortingOrder.PILE, SortingOrder.VICTORY_POINTS -> pileNames.getValue(card)
                }

                Div(ReadOnlyStyle.toAttrs()) {
                    val cardTitle = ctx.describer.describeCardTitle(card)
                    if (extraText != null) {
                        // No wrap because sometimes button names were getting squished despite extra space!
                        Row(Modifier.gap(5.px).fillMaxWidth().whiteSpace(WhiteSpace.NoWrap)) {
                            SpanText(cardTitle)
                            SpanText("($extraText)", Modifier.textAlign(TextAlign.End))
                        }
                    } else {
                        Text(cardTitle)
                    }
                }
                installPopup(ctx, card)
            }
        }
    }
}

package dev.bitspittle.racketeer.site.components.sections.menu.menus.user

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.css.TextAlign
import com.varabyte.kobweb.compose.css.WhiteSpace
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.foundation.layout.RowScope
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.thenUnless
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.silk.components.layout.SimpleGrid
import com.varabyte.kobweb.silk.components.layout.numColumns
import com.varabyte.kobweb.silk.components.style.*
import com.varabyte.kobweb.silk.components.style.common.DisabledStyle
import com.varabyte.kobweb.silk.components.text.SpanText
import dev.bitspittle.racketeer.site.FullWidthChildrenStyle
import dev.bitspittle.racketeer.site.components.sections.ReadOnlyStyle
import dev.bitspittle.racketeer.site.components.sections.menu.Menu
import dev.bitspittle.racketeer.site.components.sections.menu.MenuActions
import dev.bitspittle.racketeer.site.components.sections.menu.menus.game.BrowseAllCardsMenu
import dev.bitspittle.racketeer.site.components.util.PopupParams
import dev.bitspittle.racketeer.site.components.util.installPopup
import dev.bitspittle.racketeer.site.components.widgets.ArrowButton
import dev.bitspittle.racketeer.site.components.widgets.ArrowDirection
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

class CardListMenu(private val params: PopupParams) : Menu {
    enum class SortingOrder {
        NAME,
        TIER,
    }

    override val title = "Card List"

    private var sortingOrder by mutableStateOf(SortingOrder.NAME)

    override val topRow: @Composable RowScope.() -> Unit = {
        Column(Modifier.fillMaxWidth().gap(20.px).margin(topBottom = 10.px), horizontalAlignment = Alignment.CenterHorizontally) {
            SpanText("You have owned ${params.userStats.cards.size} out of ${params.data.cards.size} cards.")

            SimpleGrid(
                numColumns(4), Modifier
                    .gap(10.px)
                    .whiteSpace(WhiteSpace.NoWrap)
                    .gridTemplateColumns("min-content min-content 60px min-content")
            ) {
                SpanText("Sorted by:")
                ArrowButton(ArrowDirection.LEFT) {
                    sortingOrder =
                        SortingOrder.values()[(sortingOrder.ordinal - 1 + SortingOrder.values().size) % SortingOrder.values().size]
                }
                SpanText(
                    sortingOrder.name.lowercase().capitalize(),
                    Modifier.textAlign(TextAlign.Center)
                )
                ArrowButton(ArrowDirection.RIGHT) {
                    sortingOrder = SortingOrder.values()[(sortingOrder.ordinal + 1) % SortingOrder.values().size]
                }
            }
        }
    }

    @Composable
    override fun renderContent(actions: MenuActions) = with(params) {
        var sortedCards = data.cards.sortedBy { it.name }
        if (sortingOrder == SortingOrder.TIER) {
            sortedCards = sortedCards.sortedBy { it.tier }
        }
        sortedCards.forEach { card ->
            val knownCard = (userStats.cards.contains(card.name))

            Div(ReadOnlyStyle.toModifier().thenUnless(knownCard, DisabledStyle.toModifier()).toAttrs()) {
                var text = describer.describeCardTitle(card)
                if (!knownCard) {
                    text = "?".repeat(text.length)
                }

                when (sortingOrder) {
                    SortingOrder.NAME -> Text(text)
                    SortingOrder.TIER -> Row(Modifier.gap(5.px).fillMaxWidth()) {
                        SpanText(text)
                        SpanText("(Tier ${card.tier + 1})", Modifier.textAlign(TextAlign.End).flexGrow(1))
                    }
                }
            }
            if (knownCard) {
                installPopup(params, card)
            }
        }
    }
}

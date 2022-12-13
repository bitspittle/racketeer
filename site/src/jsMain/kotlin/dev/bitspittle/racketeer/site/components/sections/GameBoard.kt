package dev.bitspittle.racketeer.site.components.sections

import com.varabyte.kobweb.silk.components.forms.Button
import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.silk.components.layout.SimpleGrid
import com.varabyte.kobweb.silk.components.layout.numColumns
import com.varabyte.kobweb.silk.components.text.SpanText
import dev.bitspittle.racketeer.site.G
import dev.bitspittle.racketeer.site.components.widgets.Card
import dev.bitspittle.racketeer.site.components.widgets.CardGroup
import dev.bitspittle.racketeer.site.model.GameContext
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

private val GAP = 20.px

@Composable
fun GameBoard(ctx: GameContext) {
    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxWidth()) {
            Row(Modifier.align(Alignment.CenterHorizontally).margin(top = 10.px, bottom = 15.px)) {
                SpanText(
                    ctx.describer.describeCash(ctx.state.cash) + " "
                            + ctx.describer.describeInfluence(ctx.state.influence) + " "
                            + ctx.describer.describeLuck(ctx.state.luck),
                    Modifier.margin(right = 30.px)
                )
                SpanText(ctx.describer.describeVictoryPoints(ctx.state.vp))
            }

            SimpleGrid(
                numColumns(2),
                Modifier
                    .fillMaxSize()
                    .gap(GAP).padding(GAP)
                    .gridTemplateColumns("${G.Sizes.Card.w} 1fr")
            ) {
                Div() // Empty space
                Row(Modifier.gap(GAP)) {
                    CardGroup("Shop (Tier ${ctx.state.shop.tier + 1})", Modifier.flexGrow(1)) {}
                    Column(Modifier
                        .fillMaxHeight()
                        .padding(GAP).gap(GAP)
                        .border(width = 1.px, style = LineStyle.Solid, color = Colors.Black)
                    ) {
                        Button(onClick = {}, Modifier.width(100.px).flexGrow(1)) {
                            Text("Expand"); Br()
                            Text(ctx.describer.describeInfluence(ctx.data.shopPrices[ctx.state.shop.tier]))
                        }
                        Button(onClick = {}, Modifier.width(100.px).flexGrow(1)) {
                            Text("Reroll"); Br()
                            Text(ctx.data.icons.luck)
                        }
                    }
                }

                Div(Modifier.backgroundColor(Colors.Grey).toAttrs())
                CardGroup("Street") {
                    ctx.state.street.cards.forEach { card ->
                        Card(ctx, card, onClick = {})
                    }
                }

                Div(Modifier.backgroundColor(Colors.Grey).toAttrs())
                CardGroup("Hand") {
                    ctx.state.hand.cards.forEach { card ->
                        Card(ctx, card, onClick = {})
                    }
                }

                Div(Modifier.backgroundColor(Colors.Grey).toAttrs())
                Row(Modifier.gap(GAP)) {
                    CardGroup("Buildings", Modifier.flexGrow(1)) {}
                    Button(onClick = {}, Modifier.width(300.px).fillMaxHeight()) {
                        Text("End Turn")
                    }
                }
            }
        }
    }
}
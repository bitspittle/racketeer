package dev.bitspittle.racketeer.site.components.sections

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.css.UserSelect
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.thenIf
import com.varabyte.kobweb.silk.components.forms.Button
import com.varabyte.kobweb.silk.components.layout.SimpleGrid
import com.varabyte.kobweb.silk.components.layout.numColumns
import com.varabyte.kobweb.silk.components.text.SpanText
import dev.bitspittle.racketeer.model.game.GameProperty
import dev.bitspittle.racketeer.model.game.GameStateChange
import dev.bitspittle.racketeer.model.game.isGameOver
import dev.bitspittle.racketeer.site.G
import dev.bitspittle.racketeer.site.components.widgets.*
import dev.bitspittle.racketeer.site.model.GameContext
import dev.bitspittle.racketeer.site.model.runStateChangingAction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

private val GAP = 20.px

@Composable
fun GameBoard(scope: CoroutineScope, ctx: GameContext, onContextUpdated: () -> Unit) {
    fun runStateChangingActions(vararg blocks: suspend () -> Unit) {
        scope.launch {
            ctx.logger.clear()

            var changed = false
            for (block in blocks) {
                if (ctx.runStateChangingAction { block() }) { changed = true }
            }

            if (changed) {
                onContextUpdated()
            }
        }
    }
    fun runStateChangingAction(block: suspend () -> Unit) = runStateChangingActions(block)

    // UserSelect.None, because the game feels cheap if you allow users to drag highlight text on stuff
    Box(Modifier.fillMaxSize().minWidth(500.px).userSelect(UserSelect.None)) {
        Column(Modifier.fillMaxWidth()) {
            Row(Modifier
                .align(Alignment.CenterHorizontally)
                .margin(top = 10.px, bottom = 15.px)
                .gap(30.px)
            ) {
                SpanText("Turn ${ctx.state.turn + 1}")
                Row(Modifier.gap(5.px)) {
                    // TODO(#2): This should only work if you're in admin mode
                    SpanText(ctx.describer.describeCash(ctx.state.cash), Modifier.onClick { evt ->
                        if (evt.ctrlKey && evt.shiftKey) {
                            runStateChangingAction {
                                ctx.state.apply(GameStateChange.AddGameAmount(GameProperty.CASH, if (evt.altKey) 10 else 1))
                            }
                        }
                    })
                    SpanText(ctx.describer.describeInfluence(ctx.state.influence), Modifier.onClick { evt ->
                        if (evt.ctrlKey && evt.shiftKey) {
                            runStateChangingAction {
                                ctx.state.apply(GameStateChange.AddGameAmount(GameProperty.INFLUENCE, if (evt.altKey) 10 else 1))
                            }
                        }
                    })
                    SpanText(ctx.describer.describeLuck(ctx.state.luck), Modifier.onClick { evt ->
                        if (evt.ctrlKey && evt.shiftKey) {
                            runStateChangingAction {
                                ctx.state.apply(GameStateChange.AddGameAmount(GameProperty.LUCK, if (evt.altKey) 10 else 1))
                            }
                        }
                    })
                }
                SpanText(ctx.describer.describeVictoryPoints(ctx.state.vp))
            }

            SimpleGrid(
                numColumns(2),
                Modifier
                    .fillMaxSize()
                    .gap(GAP).padding(GAP)
                    .gridTemplateColumns("auto 1fr")
            ) {
                Div() // Empty space
                Row(Modifier.gap(GAP)) {
                    CardGroup("Shop (Tier ${ctx.state.shop.tier + 1})", Modifier.flexGrow(1)) {
                        ctx.state.shop.stock.forEach { card ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Top) {
                                if (card != null) {
                                    Card(ctx.describer, card, enabled = ctx.state.cash >= card.template.cost, onClick = {
                                        runStateChangingAction {
                                            ctx.state.apply(GameStateChange.Buy(card))
                                        }
                                    })
                                } else {
                                    CardPlaceholder()
                                }

                                SpanText(
                                    if (card != null) ctx.describer.describeCash(card.template.cost) else "SOLD OUT",
                                    Modifier
                                        .margin(top = 10.px)
                                        .thenIf(card == null || ctx.state.cash < card.template.cost) {
                                            Modifier.opacity(G.Colors.DisabledOpacity)
                                        }
                                )
                            }
                        }
                    }

                    Column(Modifier
                        .fillMaxHeight()
                        .padding(GAP).gap(GAP)
                        .border(width = 1.px, style = LineStyle.Solid, color = Colors.Black)
                    ) {
                        val shopPrice = ctx.data.shopPrices.getOrNull(ctx.state.shop.tier)
                        Button(
                            onClick = {
                                runStateChangingAction {
                                    ctx.state.apply(GameStateChange.UpgradeShop())
                                    // shopPrice to be non-null if button is enabled
                                    @Suppress("NAME_SHADOWING") val shopPrice = shopPrice!!
                                    ctx.state.apply(GameStateChange.AddGameAmount(GameProperty.INFLUENCE, -shopPrice))
                                }
                            },
                            Modifier.width(100.px).flexGrow(1),
                            enabled = shopPrice != null && ctx.state.influence >= shopPrice
                        ) {
                            Text("Expand"); Br()
                            if (shopPrice != null) {
                                Text(ctx.describer.describeInfluence(ctx.data.shopPrices[ctx.state.shop.tier]))
                            } else {
                                Text("MAX")
                            }
                        }
                        Button(
                            onClick = {
                                runStateChangingAction {
                                    ctx.state.apply(GameStateChange.RestockShop())
                                    ctx.state.apply(GameStateChange.AddGameAmount(GameProperty.LUCK, -1))
                                }
                            },
                            Modifier.width(100.px).flexGrow(1),
                            enabled = ctx.state.luck > 0
                        ) {
                            Text("Reroll"); Br()
                            Text(ctx.data.icons.luck)
                        }
                    }
                }

                CardPile(ctx, ctx.state.discard)
                CardGroup("Street") {
                    ctx.state.street.cards.forEach { card ->
                        Card(ctx.describer, card)
                    }
                }

                CardPile(ctx, ctx.state.deck)
                CardGroup("Hand") {
                    ctx.state.hand.cards.forEach { card ->
                        Card(ctx.describer, card, onClick = {
                            runStateChangingAction {
                                ctx.state.apply(GameStateChange.Play(card))
                            }
                        })
                    }
                }

                CardPile(ctx, ctx.state.jail)
                Row(Modifier.gap(GAP)) {
                    CardGroup("Buildings", Modifier.flexGrow(1)) {
                        ctx.state.buildings.forEach { building ->
                            Building(ctx, building, onClick = {
                                runStateChangingAction {
                                    ctx.state.apply(GameStateChange.Activate(building))
                                }
                            })
                        }
                        ctx.state.blueprints.forEach { blueprint ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Top) {
                                Blueprint(ctx, blueprint, onClick = {
                                    runStateChangingAction {
                                        ctx.state.apply(GameStateChange.Build(blueprint))
                                    }
                                })

                                Row(
                                    Modifier
                                        .margin(top = 10.px)
                                        .gap(5.px)
                                        .thenIf(ctx.state.cash < blueprint.buildCost.cash || ctx.state.influence < blueprint.buildCost.influence) {
                                            Modifier.opacity(G.Colors.DisabledOpacity)
                                        }

                                ) {
                                    if (blueprint.buildCost.cash > 0) {
                                        SpanText(ctx.describer.describeCash(blueprint.buildCost.cash))
                                    }
                                    if (blueprint.buildCost.influence > 0) {
                                        SpanText(ctx.describer.describeInfluence(blueprint.buildCost.influence))
                                    }
                                }
                            }
                        }
                    }
                    Button(
                        onClick = {
                            runStateChangingActions(
                                {
                                    ctx.state.apply(GameStateChange.EndTurn())
                                },
                                // Break up into two state changing actions for a better state diff report around reshuffling cards
                                {
                                    if (!ctx.state.isGameOver) {
                                        ctx.state.apply(GameStateChange.Draw())
                                    }
                                }
                            )
                        },
                        Modifier.width(300.px).fillMaxHeight(),
                        enabled = !ctx.state.isGameOver
                    ) {
                        Text("End Turn")
                    }
                }
            }

            ctx.logger.messages.forEach { message ->
                SpanText(message, Modifier.fillMaxWidth().padding(left = GAP))
            }

        }
    }
}
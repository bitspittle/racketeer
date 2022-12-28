package dev.bitspittle.racketeer.site.components.sections

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.silk.components.forms.Button
import com.varabyte.kobweb.silk.components.layout.SimpleGrid
import com.varabyte.kobweb.silk.components.layout.numColumns
import com.varabyte.kobweb.silk.components.text.SpanText
import dev.bitspittle.racketeer.model.game.GameProperty
import dev.bitspittle.racketeer.model.game.GameStateChange
import dev.bitspittle.racketeer.model.game.isGameOver
import dev.bitspittle.racketeer.site.components.sections.menu.GameMenu
import dev.bitspittle.racketeer.site.components.widgets.*
import dev.bitspittle.racketeer.site.inputRef
import dev.bitspittle.racketeer.site.model.GameContext
import dev.bitspittle.racketeer.site.model.GameUpdater
import kotlinx.coroutines.CoroutineScope
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

private val GAP = 20.px

@Composable
fun GameBoard(scope: CoroutineScope, ctx: GameContext, onContextUpdated: () -> Unit) {
    var showMenu by remember { mutableStateOf(false) }
    val gameUpdater = GameUpdater(scope, ctx, onContextUpdated)

    Box(
        Modifier.fillMaxSize().minWidth(500.px),
        ref = inputRef { code ->
            if (code == "Escape") {
                showMenu = !showMenu
                true
            } else false
        }
    ) {
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
                            gameUpdater.runStateChangingAction {
                                ctx.state.apply(GameStateChange.AddGameAmount(GameProperty.CASH, if (evt.altKey) 10 else 1))
                            }
                        }
                    })
                    SpanText(ctx.describer.describeInfluence(ctx.state.influence), Modifier.onClick { evt ->
                        if (evt.ctrlKey && evt.shiftKey) {
                            gameUpdater.runStateChangingAction {
                                ctx.state.apply(GameStateChange.AddGameAmount(GameProperty.INFLUENCE, if (evt.altKey) 10 else 1))
                            }
                        }
                    })
                    SpanText(ctx.describer.describeLuck(ctx.state.luck), Modifier.onClick { evt ->
                        if (evt.ctrlKey && evt.shiftKey) {
                            gameUpdater.runStateChangingAction {
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
                            if (card != null) {
                                Card(
                                    ctx.describer,
                                    ctx.tooltipParser,
                                    card,
                                    label = ctx.describer.describeCash(card.template.cost),
                                    enabled = ctx.state.cash >= card.template.cost, onClick = {
                                        gameUpdater.runStateChangingAction {
                                            ctx.state.apply(GameStateChange.Buy(card))
                                        }
                                    })
                            } else {
                                CardPlaceholder(label = "SOLD OUT", enabled = false)
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
                                gameUpdater.runStateChangingAction {
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
                                gameUpdater.runStateChangingAction {
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
                CardGroup("Street (${ctx.state.street.cards.size})") {
                    ctx.state.street.cards.forEach { card ->
                        Card(ctx.describer, ctx.tooltipParser, card)
                    }
                }

                CardPile(ctx, ctx.state.deck)
                CardGroup("Hand (${ctx.state.hand.cards.size})") {
                    ctx.state.hand.cards.forEach { card ->
                        Card(ctx.describer, ctx.tooltipParser, card, onClick = {
                            gameUpdater.runStateChangingAction {
                                ctx.state.apply(GameStateChange.Play(card))
                            }
                        })
                    }
                }

                CardPile(ctx, ctx.state.jail)
                Row(Modifier.gap(GAP)) {
                    CardGroup("Buildings & Blueprints", Modifier.flexGrow(1)) {
                        ctx.state.buildings.forEach { building ->
                            Building(ctx, building, onClick = {
                                gameUpdater.runStateChangingAction {
                                    ctx.state.apply(GameStateChange.Activate(building))
                                }
                            })
                        }
                        ctx.state.blueprints.forEach { blueprint ->
                            Blueprint(ctx, blueprint, onClick = {
                                gameUpdater.runStateChangingAction {
                                    ctx.state.apply(GameStateChange.Build(blueprint))
                                }
                            })
                        }
                    }
                    Button(
                        onClick = {
                            gameUpdater.runStateChangingActions(
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

    if (showMenu) {
        GameMenu(ctx, gameUpdater, closeRequested = { showMenu = false })
    }
}
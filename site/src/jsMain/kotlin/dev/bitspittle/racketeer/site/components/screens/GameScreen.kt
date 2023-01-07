package dev.bitspittle.racketeer.site.components.screens

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.css.FontWeight
import com.varabyte.kobweb.compose.css.UserSelect
import com.varabyte.kobweb.compose.dom.ElementTarget
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
import com.varabyte.kobweb.silk.components.overlay.Tooltip
import com.varabyte.kobweb.silk.components.style.*
import com.varabyte.kobweb.silk.components.text.SpanText
import dev.bitspittle.racketeer.model.building.vpTotal
import dev.bitspittle.racketeer.model.card.vpTotal
import dev.bitspittle.racketeer.model.game.*
import dev.bitspittle.racketeer.model.score.from
import dev.bitspittle.racketeer.model.serialization.GameSnapshot
import dev.bitspittle.racketeer.site.FullWidthChildrenRecursiveStyle
import dev.bitspittle.racketeer.site.components.sections.ReadOnlyStyle
import dev.bitspittle.racketeer.site.components.sections.menu.Menu
import dev.bitspittle.racketeer.site.components.sections.menu.menus.game.Admin
import dev.bitspittle.racketeer.site.components.sections.menu.menus.game.BrowseAllCardsMenu
import dev.bitspittle.racketeer.site.components.sections.menu.menus.game.GameMenuParams
import dev.bitspittle.racketeer.site.components.sections.menu.menus.game.MainMenu
import dev.bitspittle.racketeer.site.components.util.Data
import dev.bitspittle.racketeer.site.components.util.installPopup
import dev.bitspittle.racketeer.site.components.util.locked
import dev.bitspittle.racketeer.site.components.util.unlock
import dev.bitspittle.racketeer.site.components.widgets.*
import dev.bitspittle.racketeer.site.inputRef
import dev.bitspittle.racketeer.site.model.*
import dev.bitspittle.racketeer.site.model.user.GameStats
import dev.bitspittle.racketeer.site.model.user.totalVp
import kotlinx.coroutines.CoroutineScope
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

private val GAP = 20.px

private class VpProvider(val source: Any, val amount: Int)

@Composable
private fun renderGameScreen(
    gameUpdater: GameUpdater,
    ctx: GameContext,
    gameMenuParams: GameMenuParams,
    showMenu: (Menu) -> Unit,
    onRestartRequested: () -> Unit,
    onQuitRequested: () -> Unit
) {
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier
            .align(Alignment.CenterHorizontally)
            .margin(top = 10.px, bottom = 15.px)
            .gap(30.px)
        ) {
            SpanText("Turn ${ctx.state.turn + 1} / ${ctx.data.numTurns}")
            SpanText("\uD83C\uDCCF ${ctx.state.getOwnedCards().count()}")
            Tooltip(ElementTarget.PreviousSibling, "The total number of cards you own (doesn't include jailed cards).")
            Row(Modifier.gap(5.px)) {
                SpanText(ctx.describer.describeCash(ctx.state.cash), Modifier.onClick { evt ->
                    if (ctx.settings.admin.enabled && evt.shiftKey) {
                        gameUpdater.runStateChangingAction {
                            ctx.state.addChange(GameStateChange.AddGameAmount(GameProperty.CASH, if (evt.altKey) 10 else 1))
                        }
                    }
                })
                SpanText(ctx.describer.describeInfluence(ctx.state.influence), Modifier.onClick { evt ->
                    if (ctx.settings.admin.enabled && evt.shiftKey) {
                        gameUpdater.runStateChangingAction {
                            ctx.state.addChange(GameStateChange.AddGameAmount(GameProperty.INFLUENCE, if (evt.altKey) 10 else 1))
                        }
                    }
                })
                SpanText(ctx.describer.describeLuck(ctx.state.luck), Modifier.onClick { evt ->
                    if (ctx.settings.admin.enabled && evt.shiftKey) {
                        gameUpdater.runStateChangingAction {
                            ctx.state.addChange(GameStateChange.AddGameAmount(GameProperty.LUCK, if (evt.altKey) 10 else 1))
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
            Row(Modifier.gap(GAP).gridColumn("span 2")) {
                CardGroup("Shop (Tier ${ctx.state.shop.tier + 1})", Modifier.flexGrow(1)) {
                    ctx.state.shop.stock.forEach { card ->
                        if (card != null) {
                            Card(
                                ctx.data,
                                ctx.userStats,
                                ctx.describer,
                                ctx.tooltipParser,
                                card,
                                label = ctx.describer.describeCash(card.template.cost),
                                enabled = ctx.state.cash >= card.template.cost, onClick = {
                                    gameUpdater.runStateChangingAction {
                                        ctx.state.addChange(GameStateChange.Buy(card))
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
                                ctx.state.addChange(GameStateChange.UpgradeShop())
                                // shopPrice to be non-null if button is enabled
                                @Suppress("NAME_SHADOWING") val shopPrice = shopPrice!!
                                ctx.state.addChange(GameStateChange.AddGameAmount(GameProperty.INFLUENCE, -shopPrice))
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
                                ctx.state.addChange(GameStateChange.RestockShop())
                                ctx.state.addChange(GameStateChange.AddGameAmount(GameProperty.LUCK, -1))
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
                    Card(ctx.data, ctx.userStats, ctx.describer, ctx.tooltipParser, card, enabled = false)
                }
            }

            CardPile(ctx, ctx.state.deck)
            CardGroup("Hand (${ctx.state.hand.cards.size})") {
                ctx.state.hand.cards.forEach { card ->
                    Card(ctx.data, ctx.userStats, ctx.describer, ctx.tooltipParser, card, onClick = {
                        gameUpdater.runStateChangingAction {
                            ctx.state.addChange(GameStateChange.Play(card))
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
                                ctx.state.addChange(GameStateChange.Activate(building))
                            }
                        })
                    }
                    ctx.state.blueprints.forEach { blueprint ->
                        Blueprint(ctx, blueprint, onClick = {
                            gameUpdater.runStateChangingAction {
                                ctx.state.addChange(GameStateChange.Build(blueprint))
                            }
                        })
                    }
                }

                run {
                    Button(
                        onClick = {
                            gameUpdater.runStateChangingActions(
                                {
                                    ctx.state.addChange(GameStateChange.EndTurn())
                                },
                                // Break up into two state changing actions for a better state diff report around reshuffling cards
                                {
                                    if (!ctx.state.isGameOver) {
                                        ctx.state.addChange(GameStateChange.Draw())

                                        try {
                                            // Force an auto-save so user's don't lose their progress if they
                                            // crash or their program freezes
                                            Data.save(
                                                Data.Keys.Quicksave,
                                                GameSnapshot.from(ctx.describer, ctx.state)
                                            )
                                            ctx.logger.debug("Game auto-saved.")
                                        } catch (ignored: Exception) {
                                            // Shouldn't ever happen, but we don't want to risk an autosave failure
                                            // stopping someone from playing through the rest of the game
                                        }
                                    } else {
                                        // Game is over! No more need to keep a save around
                                        Data.delete(Data.Keys.Quicksave)

                                        val prevTotalVp = ctx.userStats.games.totalVp
                                        ctx.userStats.games.add(GameStats.from(ctx.state))
                                        Data.save(Data.Keys.UserStats, ctx.userStats)

                                        val toUnlock = ctx.data.unlocks.locked(ctx.settings, ctx.state.vp + prevTotalVp)
                                        if (toUnlock.isNotEmpty()) {
                                            toUnlock.forEach { unlock ->
                                                ctx.logger.info("Congratulations! You unlocked: ${unlock.resolvedName(ctx.data)}")
                                                unlock.unlock(ctx.settings)
                                            }
                                            Data.save(Data.Keys.Settings, ctx.settings)
                                        }
                                    }
                                }
                            )
                        },
                        Modifier.width(300.px).fillMaxHeight(),
                        enabled = !ctx.state.isGameOver
                    ) {
                        Text("End Turn")
                    }

                    if (ctx.state.isGameOver) {
                        Modal(
                            title = "Summary",
                            bottomRow = {
                                Button(onClick = { onQuitRequested() }) { Text("Quit to Title") }
                                Button(onClick = { onRestartRequested() }) { Text("Play Again") }
                            }
                        ) {
                            Column(FullWidthChildrenRecursiveStyle.toModifier().gap(30.px).margin(top = 10.px)) {
                                SpanText("You ended the game with ${ctx.describer.describeVictoryPoints(ctx.state.vp)}, to earn a ranking of:")
                                SpanText(ctx.data.rankings.from(ctx.state.vp).name, Modifier.fontWeight(FontWeight.Bold))

                                val topCards = (ctx.state.getOwnedCards().map { VpProvider(it, it.vpTotal) } +
                                        ctx.state.buildings.map { VpProvider(it, it.vpTotal) })
                                    .filter { it.amount > 0 }
                                    .sortedByDescending { it.amount }
                                    .take(3)
                                    .toList()

                                if (topCards.isNotEmpty()) {
                                    Column(Modifier.gap(5.px)) {
                                        SpanText("Your top sources of victory points:")
                                        topCards.forEach { vpProvider ->
                                            Div(ReadOnlyStyle.toAttrs()) {
                                                // No wrap because sometimes button names were getting squished despite extra space!
                                                SpanText(ctx.describer.describeItem(vpProvider.source))
                                            }
                                            installPopup(ctx, vpProvider.source)
                                        }
                                    }
                                }
                                Button(onClick = {
                                    showMenu(BrowseAllCardsMenu(gameMenuParams))
                                }) { Text("Browse All Cards") }
                            }
                        }
                    }
                }
            }
        }


        Column(Modifier.userSelect(UserSelect.Text).fillMaxWidth().padding(left = GAP).fontFamily("monospace")) {
            ctx.logger.messages.forEach { message ->
                if (message.isNotEmpty()) {
                    SpanText(message)
                } else {
                    Br()
                }
            }
        }
    }
}


@Composable
fun GameScreen(scope: CoroutineScope, events: Events, ctx: GameContext, onRestartRequested: () -> Unit, onQuitRequested: () -> Unit) {
    var showMenu by remember { mutableStateOf(false) }
    var initialMenu by remember { mutableStateOf<Menu?>(null) }
    val gameUpdater = GameUpdater(scope, events, ctx)

    var gameUpdateCount by remember { mutableStateOf(0) }

    val gameMenuParams = GameMenuParams(scope, ctx, gameUpdater, onRestartRequested, onQuitRequested)

    LaunchedEffect(Unit) {
        events.collect { evt ->
            when (evt) {
                is Event.GameStateUpdated -> gameUpdateCount++
                else -> {}
            }
        }
    }

    Box(
        Modifier.fillMaxSize().minWidth(500.px),
        ref = inputRef {
            when (code) {
                "Escape" -> { showMenu = !showMenu; true }
                "Backquote" -> {
                    if (!showMenu && ctx.settings.admin.enabled) {
                        showMenu = true
                        initialMenu = Admin(gameMenuParams)
                        true
                    } else false
                }
                "Equal" -> {
                    if (!showMenu) {
                        showMenu = true
                        initialMenu = BrowseAllCardsMenu(gameMenuParams)
                        true
                    } else false
                }
                else -> { println(code); false }
            }
        }
    ) {
        key(gameUpdateCount) {
            renderGameScreen(
                gameUpdater,
                ctx,
                gameMenuParams,
                showMenu = {
                    showMenu = true
                    initialMenu = it
                },
                onRestartRequested,
                onQuitRequested
            )
        }
   }

    if (showMenu) {
        Menu(
            closeRequested = { showMenu = false; initialMenu = null },
            initialMenu ?: MainMenu(gameMenuParams)
        )
    }
}
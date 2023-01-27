package dev.bitspittle.racketeer.site.components.screens

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.css.FontWeight
import com.varabyte.kobweb.compose.css.UserSelect
import com.varabyte.kobweb.compose.css.Width
import com.varabyte.kobweb.compose.dom.ElementTarget
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.thenIf
import com.varabyte.kobweb.silk.components.forms.Button
import com.varabyte.kobweb.silk.components.icons.fa.FaBars
import com.varabyte.kobweb.silk.components.icons.fa.FaCopy
import com.varabyte.kobweb.silk.components.layout.SimpleGrid
import com.varabyte.kobweb.silk.components.layout.numColumns
import com.varabyte.kobweb.silk.components.overlay.Tooltip
import com.varabyte.kobweb.silk.components.style.toAttrs
import com.varabyte.kobweb.silk.components.style.toModifier
import com.varabyte.kobweb.silk.components.text.SpanText
import dev.bitspittle.firebase.analytics.Analytics
import dev.bitspittle.racketeer.model.building.Building
import dev.bitspittle.racketeer.model.building.vpTotal
import dev.bitspittle.racketeer.model.card.Card
import dev.bitspittle.racketeer.model.card.vpTotal
import dev.bitspittle.racketeer.model.game.GameProperty
import dev.bitspittle.racketeer.model.game.GameStateChange
import dev.bitspittle.racketeer.model.game.getOwnedCards
import dev.bitspittle.racketeer.model.game.isGameOver
import dev.bitspittle.racketeer.model.score.from
import dev.bitspittle.racketeer.model.serialization.GameSnapshot
import dev.bitspittle.racketeer.site.FullWidthChildrenRecursiveStyle
import dev.bitspittle.racketeer.site.components.sections.ReadOnlyStyle
import dev.bitspittle.racketeer.site.components.sections.menu.Menu
import dev.bitspittle.racketeer.site.components.sections.menu.menus.game.*
import dev.bitspittle.racketeer.site.components.util.Data
import dev.bitspittle.racketeer.site.components.util.Payload
import dev.bitspittle.racketeer.site.components.util.Uploads
import dev.bitspittle.racketeer.site.components.util.installPopup
import dev.bitspittle.racketeer.site.components.widgets.*
import dev.bitspittle.racketeer.site.inputRef
import dev.bitspittle.racketeer.site.model.*
import dev.bitspittle.racketeer.site.model.user.GameStats
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.LineStyle
import org.jetbrains.compose.web.css.Position
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.Br
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text

private val GAP = 20.px

private class VpProvider(val source: Any, val amount: Int)

@Composable
private fun renderGameScreen(
    scope: CoroutineScope,
    gameUpdater: GameUpdater,
    ctx: GameContext,
    gameMenuParams: GameMenuParams,
    showMenu: (Menu) -> Unit,
    onRestartRequested: () -> Unit,
    onQuitRequested: () -> Unit
) {
    val gs = remember { GameStateViewModel(scope, ctx.events, ctx.state) }

    Column(Modifier.fillMaxWidth()) {
        Row(Modifier
            .align(Alignment.CenterHorizontally)
            .margin(top = 10.px, bottom = 15.px)
            .gap(30.px)
        ) {
            SpanText("Turn ${gs.turn + 1} / ${ctx.data.numTurns}")
            SpanText("\uD83C\uDCCF ${ctx.state.getOwnedCards().count()}")
            Tooltip(ElementTarget.PreviousSibling, "The total number of cards you own (doesn't include jailed cards).")
            Row(Modifier.gap(5.px)) {
                SpanText(ctx.describer.describeCash(gs.cash), Modifier.onClick { evt ->
                    if (ctx.account.isAdmin && evt.shiftKey) {
                        gameUpdater.runStateChangingAction {
                            ctx.state.addChange(GameStateChange.AddGameAmount(GameProperty.CASH, if (evt.altKey) 10 else 1))
                        }
                    }
                })
                SpanText(ctx.describer.describeInfluence(gs.influence), Modifier.onClick { evt ->
                    if (ctx.account.isAdmin && evt.shiftKey) {
                        gameUpdater.runStateChangingAction {
                            ctx.state.addChange(GameStateChange.AddGameAmount(GameProperty.INFLUENCE, if (evt.altKey) 10 else 1))
                        }
                    }
                })
                SpanText(ctx.describer.describeLuck(gs.luck), Modifier.onClick { evt ->
                    if (ctx.account.isAdmin && evt.shiftKey) {
                        gameUpdater.runStateChangingAction {
                            ctx.state.addChange(GameStateChange.AddGameAmount(GameProperty.LUCK, if (evt.altKey) 10 else 1))
                        }
                    }
                })
            }
            SpanText(ctx.describer.describeVictoryPoints(gs.vp))
        }

        SimpleGrid(
            numColumns(2),
            Modifier
                .fillMaxSize()
                .gap(GAP).padding(GAP)
                .gridTemplateColumns("auto 1fr")
        ) {
            Row(Modifier.gap(GAP).gridColumn("span 2")) {
                CardGroup("Shop (Tier ${gs.shop.tier + 1})", Modifier.flexGrow(1)) {
                    gs.shop.stock.forEach { card ->
                        if (card != null) {
                            val cost = gs.shop.prices.getValue(card.id)

                            Card(
                                ctx.data,
                                ctx.userStats,
                                ctx.describer,
                                ctx.tooltipParser,
                                card,
                                label = ctx.describer.describeCash(cost),
                                enabled = gs.cash >= cost, onClick = {
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
                    val shopPrice = ctx.data.shopPrices.getOrNull(gs.shop.tier)
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
                        enabled = shopPrice != null && gs.influence >= shopPrice
                    ) {
                        Text("Expand"); Br()
                        if (shopPrice != null) {
                            Text(ctx.describer.describeInfluence(ctx.data.shopPrices[gs.shop.tier]))
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
                        enabled = gs.luck > 0
                    ) {
                        Text("Reroll"); Br()
                        Text(ctx.data.icons.luck)
                    }
                }
            }

            CardPile(ctx, gs.discard)
            CardGroup("Street (${gs.street.cards.size})") {
                gs.street.cards.forEach { card ->
                    Card(ctx.data, ctx.userStats, ctx.describer, ctx.tooltipParser, card, enabled = false)
                }
            }

            CardPile(ctx, gs.deck)
            CardGroup("Hand (${gs.hand.cards.size})") {
                gs.hand.cards.forEach { card ->
                    Card(ctx.data, ctx.userStats, ctx.describer, ctx.tooltipParser, card, onClick = {
                        gameUpdater.runStateChangingAction {
                            ctx.state.addChange(GameStateChange.Play(card))
                        }
                    })
                }
            }

            CardPile(ctx, gs.jail)
            Row(Modifier.gap(GAP)) {
                CardGroup("Buildings & Blueprints", Modifier.flexGrow(1)) {
                    gs.buildings.forEach { building ->
                        Building(ctx, building, onClick = {
                            gameUpdater.runStateChangingAction {
                                ctx.state.addChange(GameStateChange.Activate(building))
                            }
                        })
                    }
                    gs.blueprints.forEach { blueprint ->
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
                                    if (!gs.isGameOver) {
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
                                        scope.launch { Uploads.upload(Payload.Finish(ctx)) }

                                        // Game is over! No more need to keep a save around
                                        Data.delete(Data.Keys.Quicksave)

                                        ctx.userStats.games.add(GameStats.from(ctx.state))
                                    }
                                }
                            )
                        },
                        Modifier.width(300.px).fillMaxHeight(),
                        enabled = !gs.isGameOver
                    ) {
                        Text("End Turn")
                    }

                    if (gs.isGameOver) {
                        ctx.firebase.analytics.log(Analytics.Event.LevelEnd(gs.id.toString(), success = true))

                        Modal(
                            dialogModifier = Modifier.position(Position.Relative),
                            title = "Summary",
                            bottomRow = {
                                Button(onClick = { onQuitRequested() }) { Text("Quit to Title") }
                                Button(onClick = { onRestartRequested() }) { Text("Play Again") }
                            }
                        ) {
                            val topCards = (gs.getOwnedCards().map { VpProvider(it, it.vpTotal) } +
                                    gs.buildings.map { VpProvider(it, it.vpTotal) })
                                .filter { it.amount > 0 }
                                .sortedByDescending { it.amount }
                                .take(3)
                                .toList()

                            var showCopiedMessage by remember { mutableStateOf(false) }
                            Button(
                                onClick = {
                                    val summaryText = buildString {
                                        val vpText = ctx.describer.describeVictoryPoints(gs.vp)

                                        appendLine("I just finished a game of ${ctx.data.title} with $vpText!")
                                        appendLine()
                                        appendLine("My top cards were:")
                                        appendLine()
                                        val descriptions = mutableMapOf<String, String>()
                                        topCards.forEach { vpProvider ->
                                            appendLine("• ${ctx.describer.describeItem(vpProvider.source)}")
                                            (vpProvider.source as? Card)?.let { card -> descriptions[card.template.name] = card.template.description.ability }
                                            (vpProvider.source as? Building)?.let { bldg -> descriptions[bldg.blueprint.name] = bldg.blueprint.description.ability }
                                        }
                                        appendLine()
                                        descriptions.keys.sorted().forEach { name ->
                                            appendLine("$name: ${ctx.describer.convertIcons(descriptions.getValue(name))}")
                                        }
                                    }
                                    window.navigator.clipboard.writeText(summaryText)

                                    showCopiedMessage = true
                                },
                                Modifier
                                    .position(Position.Absolute)
                                    .right(10.px)
                                    .top(10.px)
                                    .width(Width.FitContent)
                                    .padding(5.px)
                                    .onMouseLeave { showCopiedMessage = false }
                                    .thenIf(gs.vp == 0, Modifier.display(DisplayStyle.None))
                            ) { FaCopy() }
                            if (showCopiedMessage) {
                                Tooltip(ElementTarget.PreviousSibling, "Copied!")
                            }

                            Column(FullWidthChildrenRecursiveStyle.toModifier().gap(30.px).margin(top = 10.px)) {
                                SpanText("You ended the game with ${ctx.describer.describeVictoryPoints(gs.vp)}, to earn a ranking of:")
                                SpanText(ctx.data.rankings.from(gs.vp).name, Modifier.fontWeight(FontWeight.Bold))

                                if (topCards.isNotEmpty()) {
                                    Column(Modifier.gap(5.px)) {
                                        SpanText("Your top sources of victory points:")
                                        topCards.forEach { vpProvider ->
                                            Div(ReadOnlyStyle.toAttrs()) {
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
            key(gs.history.lastOrNull()) {
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
}


@Composable
fun GameScreen(scope: CoroutineScope, events: Events, ctx: GameContext, onRestartRequested: () -> Unit, onQuitRequested: () -> Unit) {
    var showMenu by remember { mutableStateOf(false) }
    var initialMenu by remember { mutableStateOf<Menu?>(null) }
    val gameUpdater = GameUpdater(scope, events, ctx)

    val gameMenuParams = GameMenuParams(scope, ctx, gameUpdater, onRestartRequested, onQuitRequested)

    Box(
        Modifier.fillMaxSize().minWidth(500.px).position(Position.Relative),
        ref = inputRef {
            when (code) {
                "Escape" -> { showMenu = !showMenu; true }
                "Backquote" -> {
                    if (!showMenu && ctx.account.isAdmin) {
                        showMenu = true
                        initialMenu = Admin(gameMenuParams)
                        true
                    } else false
                }
                "Equal" -> {
                    if (!showMenu) {
                        showMenu = true
                        initialMenu = ReviewHistory(gameMenuParams)
                        true
                    } else false
                }
                "Backslash" -> {
                    if (!showMenu) {
                        showMenu = true
                        initialMenu = BrowseAllCardsMenu(gameMenuParams)
                        true
                    } else false
                }
                else -> false
            }
        }
    ) {
        Button(onClick = { showMenu = true }, Modifier.padding(5.px).position(Position.Absolute).left(10.px).top(10.px)) { FaBars() }

        renderGameScreen(
            scope,
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

    if (showMenu) {
        Menu(
            closeRequested = { showMenu = false; initialMenu = null },
            initialMenu ?: MainMenu(gameMenuParams)
        )
    }
}
package dev.bitspittle.racketeer.site.components.sections.menu

import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateMap
import com.varabyte.kobweb.compose.css.TextAlign
import com.varabyte.kobweb.compose.css.WhiteSpace
import com.varabyte.kobweb.compose.dom.ElementTarget
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.foundation.layout.RowScope
import com.varabyte.kobweb.compose.foundation.layout.Spacer
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.thenIf
import com.varabyte.kobweb.silk.components.forms.Button
import com.varabyte.kobweb.silk.components.layout.SimpleGrid
import com.varabyte.kobweb.silk.components.layout.numColumns
import com.varabyte.kobweb.silk.components.overlay.Tooltip
import com.varabyte.kobweb.silk.components.style.*
import com.varabyte.kobweb.silk.components.text.SpanText
import dev.bitspittle.limp.types.ListStrategy
import dev.bitspittle.racketeer.model.card.Card
import dev.bitspittle.racketeer.model.card.vpTotal
import dev.bitspittle.racketeer.model.game.*
import dev.bitspittle.racketeer.model.pile.Pile
import dev.bitspittle.racketeer.model.serialization.GameSnapshot
import dev.bitspittle.racketeer.model.text.Describer
import dev.bitspittle.racketeer.site.KeyScope
import dev.bitspittle.racketeer.site.components.sections.ReadOnlyStyle
import dev.bitspittle.racketeer.site.components.util.Data
import dev.bitspittle.racketeer.site.components.util.downloadSnapshotToDisk
import dev.bitspittle.racketeer.site.components.util.installPopup
import dev.bitspittle.racketeer.site.components.util.loadSnapshotFromDisk
import dev.bitspittle.racketeer.site.components.widgets.Modal
import dev.bitspittle.racketeer.site.components.widgets.YesNo
import dev.bitspittle.racketeer.site.components.widgets.YesNoDialog
import dev.bitspittle.racketeer.site.inputRef
import dev.bitspittle.racketeer.site.model.GameContext
import dev.bitspittle.racketeer.site.model.GameUpdater
import dev.bitspittle.racketeer.site.model.describeItem
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

@Composable
private fun MenuButton(params: GameMenuEntry.Params, entry: GameMenuEntry) {
    Button(onClick = { params.visit(entry) }) { Text(entry.title) }
}

private val ArrowButtonModifier = Modifier.padding(topBottom = 1.px, leftRight = 4.px)

enum class ArrowDirection {
    LEFT,
    RIGHT
}

@Composable
private fun ArrowButton(dir: ArrowDirection, onClick: () -> Unit) {
    Button(onClick = onClick, ArrowButtonModifier) {
        Text(when (dir) {
            ArrowDirection.LEFT -> "<"
            ArrowDirection.RIGHT -> ">"
        })
    }
}


private fun GameStateChange.toTitle(): String {
    return when (this) {
        is GameStateChange.Activate -> "Activate \"${building.blueprint.name}\""
        is GameStateChange.AddBlueprint -> "Add \"${blueprint.name}\""
        is GameStateChange.AddGameAmount -> "Add game resource(s)"
        is GameStateChange.Build -> "Build \"${blueprint.name}\""
        is GameStateChange.Buy -> "Buy \"${card.template.name}\""
        is GameStateChange.Draw -> "Draw ${cards.size} card(s)"
        is GameStateChange.EndTurn -> "End turn"
        is GameStateChange.GameStart -> "Start"
        is GameStateChange.MoveCard -> "Move \"${card.template.name}\""
        is GameStateChange.MoveCards -> cards.values.flatten().let { cards ->
            if (cards.size > 1) {
                "Move ${cards.size} cards."
            } else {
                "Move \"${cards[0].template.name}\""
            }
        }

        is GameStateChange.Play -> "Play \"${card.template.name}\""
        is GameStateChange.RestockShop -> "Restock shop"
        is GameStateChange.UpgradeShop -> "Upgrade shop: Tier ${tier + 1}"
        else -> "⚠️ ${this::class.simpleName}"
    }
}

private fun GameStateChanges.toTitle() = this.items.first().toTitle()

private fun GameStateChange.intoItems(): List<Any> {
    return when (this) {
        is GameStateChange.Activate -> listOf(building.blueprint)
        is GameStateChange.AddBlueprint -> listOf(blueprint)
        is GameStateChange.AddBuildingAmount -> listOf(building.blueprint)
        is GameStateChange.AddCardAmount -> listOf(card.template)
        is GameStateChange.AddTrait -> listOf(card.template)
        is GameStateChange.Build -> listOf(blueprint)
        is GameStateChange.Buy -> listOf(card.template)
        is GameStateChange.Draw -> cards.map { it.template }
        is GameStateChange.MoveCard -> listOf(card.template)
        is GameStateChange.MoveCards -> cards.values.flatten().map { it.template }
        is GameStateChange.Play -> listOf(card.template)
        is GameStateChange.RemoveTrait -> listOf(card.template)
        is GameStateChange.UpgradeCard -> listOf(card.template)

        is GameStateChange.AddEffect,
        is GameStateChange.AddGameAmount,
        is GameStateChange.AddGameTweak,
        is GameStateChange.AddShopTweak,
        is GameStateChange.EndTurn,
        is GameStateChange.GameOver,
        is GameStateChange.GameStart,
        is GameStateChange.RestockShop,
        is GameStateChange.SetGameData,
        is GameStateChange.Shuffle,
        is GameStateChange.ShuffleDiscardIntoDeck,
        is GameStateChange.UpgradeShop -> listOf()
    }
}

private fun GameStateChanges.flattenIntoItems(): List<Any> = items.flatMap { it.intoItems() }

interface GameMenuEntry {
    class Params(
        val scope: CoroutineScope,
        val ctx: GameContext,
        val updater: GameUpdater,
        val visit: (GameMenuEntry) -> Unit,
        val requestClose: () -> Unit,
        val requestQuit: () -> Unit,
    )

    val title: String

    val topRow: (@Composable RowScope.() -> Unit)? get() = null

    fun KeyScope.handleKey(): Boolean = false

    @Composable
    fun renderContent(params: Params)

    @Composable
    fun renderExtraBottomButtons(params: Params) {}

    object Main : GameMenuEntry {
        override val title = "Main"

        @Composable
        override fun renderContent(params: Params) {
            if (params.ctx.settings.admin.enabled) {
                MenuButton(params, Admin)
            }

            MenuButton(params, BrowseAllCards(params.ctx.data))
            MenuButton(params, ReviewHistory(params.ctx.state.turn))

            run {
                var showConfirmQuestion by remember { mutableStateOf(false) }
                Button(onClick = { showConfirmQuestion = true }) { Text("Save and Quit") }

                if (showConfirmQuestion) {
                    YesNoDialog(
                        "Are you sure?",
                    ) { yesNo ->
                        showConfirmQuestion = false
                        if (yesNo == YesNo.YES) {
                            with(params) {
                                Data.save(Data.Keys.Quicksave, GameSnapshot.from(ctx.describer, ctx.state))
                                requestQuit()
                            }
                        }
                    }
                }
            }
        }

        object Admin : GameMenuEntry {
            override val title = "Admin"

            @Composable
            override fun renderContent(params: Params) {
                MenuButton(params, CreateCard)
                MenuButton(params, BuildBuilding)
                MenuButton(params, MoveCards.FromPile)
                MenuButton(params, Snapshot)
                Button(
                    onClick = {
                        window.open("https://docs.google.com/spreadsheets/d/1iG38W0xl2UzRHhQX_GvJWg3zZndqY-UkKAVaWzNiLKg/edit#gid=200941839", target = "_blank")
                        params.requestClose()
                    },
                ) { Text("Open API Sheet") }
            }

            object CreateCard : GameMenuEntry {
                override val title = "Create Card"

                @Composable
                override fun renderContent(params: Params) {
                    with(params) {
                        ctx.data.cards.sortedBy { it.name }.forEach { card ->
                            Button(onClick = {
                                updater.runStateChangingAction {
                                    ctx.state.apply(
                                        GameStateChange.MoveCard(
                                            ctx.state,
                                            card.instantiate(),
                                            ctx.state.hand,
                                            ListStrategy.FRONT
                                        )
                                    )
                                }
                                requestClose()
                            }) { Text(card.name) }
                            installPopup(ctx, card)
                        }
                    }
                }
            }

            object BuildBuilding : GameMenuEntry {
                override val title = "Build Building"

                @Composable
                override fun renderContent(params: Params) {
                    with(params) {
                        ctx.data.blueprints.sortedBy { it.name }.forEach { blueprint ->
                            Button(
                                onClick = {
                                    // Run this command in two separate state changing actions; you need to own the blueprint before you can
                                    // build it.
                                    updater.runStateChangingActions(
                                        {
                                            if (!ctx.state.blueprints.contains(blueprint)) {
                                                ctx.state.apply(GameStateChange.AddBlueprint(blueprint))
                                            }
                                        },
                                        {
                                            check(ctx.state.blueprints.indexOf(blueprint) >= 0)
                                            ctx.state.apply(GameStateChange.Build(blueprint, free = true))
                                        },
                                    )
                                    requestClose()
                                },
                                enabled = ctx.state.buildings.none { it.blueprint === blueprint }
                            ) { Text(blueprint.name) }
                            installPopup(params.ctx, blueprint)
                        }
                    }
                }
            }

            object MoveCards {
                object FromPile : GameMenuEntry {
                    override val title = "Move Cards"

                    @Composable
                    override fun renderContent(params: Params) {
                        with(params) {
                            ctx.state.allPiles.forEach { pile ->
                                Button(
                                    onClick = {
                                        params.visit(ChooseCards(ctx.state, ctx.describer, pile))
                                    },
                                    enabled = pile.cards.isNotEmpty(),
                                ) { Text(ctx.describer.describePileTitle(ctx.state, pile, withSize = true)) }
                            }
                        }
                    }
                }

                class ChooseCards(state: GameState, describer: Describer, val pile: Pile) : GameMenuEntry {
                    override val title = "From ${describer.describePileTitle(state, pile)}"

                    private lateinit var selected: SnapshotStateMap<Card, Unit>

                    override fun KeyScope.handleKey(): Boolean {
                        return if (code == "KeyA") {
                            if (selected.count() < pile.cards.size) {
                                pile.cards.forEach { card -> selected[card] = Unit }
                            } else {
                                selected.clear()
                            }
                            true
                        } else false
                    }

                    @Composable
                    override fun renderContent(params: Params) {
                        selected = remember { mutableStateMapOf() }

                        with(params) {
                            pile.cards.forEach { card ->
                                Button(
                                    onClick = {
                                        if (selected.remove(card) == null) {
                                            selected[card] = Unit
                                        }
                                    },
                                    Modifier.thenIf(selected.contains(card)) {
                                        Modifier.outline(1.px, LineStyle.Solid, Colors.Black)
                                    }
                                ) { Text(card.template.name) }
                                installPopup(ctx, card)
                            }
                        }
                    }

                    @Composable
                    override fun renderExtraBottomButtons(params: Params) {
                        Button(
                            onClick = {
                                params.visit(ToPile(selected.keys.toList(), excludedPile = pile))
                            },
                            enabled = selected.isNotEmpty(),
                        ) { Text("Continue")}
                    }
                }

                class ToPile(val cards: List<Card>, val excludedPile: Pile) : GameMenuEntry {
                    override val title = "To Pile"

                    @Composable
                    override fun renderContent(params: Params) {
                        with(params) {
                            ctx.state.allPiles.forEach { pile ->
                                Button(
                                    onClick = {
                                        updater.runStateChangingAction {
                                            ctx.state.apply(
                                                GameStateChange.MoveCards(
                                                    ctx.state,
                                                    cards,
                                                    pile,
                                                    ListStrategy.FRONT
                                                )
                                            )
                                        }
                                        requestClose()
                                    },
                                    enabled = pile !== excludedPile,
                                ) { Text(ctx.describer.describePileTitle(ctx.state, pile, withSize = true)) }
                            }
                        }
                    }
                }
            }

            object Snapshot : GameMenuEntry {
                override val title = "Snapshot"

                @Composable
                override fun renderContent(params: Params) {
                    Button(
                        onClick = {
                            with(params) {
                                Data.save(Data.Keys.Quicksave, GameSnapshot.from(ctx.describer, ctx.state))
                                ctx.logger.debug("Game saved.")
                                requestClose()
                            }
                        },
                    ) { Text("Save Now") }

                    Button(
                        onClick = {
                            params.ctx.downloadSnapshotToDisk()
                            params.requestClose()
                        },
                    ) { Text("Download Snapshot") }
                    Tooltip(ElementTarget.PreviousSibling, "Note: This also saves the game for your convenience.")

                    Button(
                        onClick = {
                            with(params) {
                                ctx.loadSnapshotFromDisk(scope) {
                                    requestClose()
                                }
                            }
                        },
                    ) { Text("Load Snapshot") }
                }
            }
        }

        class BrowseAllCards(data: GameData, initialSortingOrder: SortingOrder = SortingOrder.PILE, initialTypeFilter: String? = null) : GameMenuEntry {
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
            private val typeFilters = listOf<String?>(null) + data.cardTypes

            override val topRow: @Composable RowScope.() -> Unit = {
                SimpleGrid(numColumns(4), Modifier
                    .fillMaxWidth()
                    .gap(10.px)
                    .whiteSpace(WhiteSpace.NoWrap)
                    .gridTemplateColumns("min-content min-content 1fr min-content")
                ) {
                    SpanText("Sorted by:")
                    ArrowButton(ArrowDirection.LEFT) {
                        sortingOrder = SortingOrder.values()[(sortingOrder.ordinal - 1 + SortingOrder.values().size) % SortingOrder.values().size]
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
                        typeFilter = typeFilters[(typeFilters.indexOf(typeFilter) - 1 + typeFilters.size) % typeFilters.size]
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
            override fun renderContent(params: Params) {
                with(params) {
                    var cards = ctx.state.getOwnedCards().sortedBy { it.template.name }
                    val pileNames =
                        cards.associateWith { ctx.describer.describePileTitle(ctx.state, ctx.state.pileFor(it)!!) }

                    cards = when(sortingOrder) {
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

        class ReviewHistory(private val maxTurn: Int = 19, initialTurn: Int = maxTurn) : GameMenuEntry {
            override val title = "Review History"

            private var turn by mutableStateOf(initialTurn)

            private val maxTurnPlus1 = maxTurn + 1 // One-indexed, useful for modding
            override val topRow: @Composable RowScope.() -> Unit = {
                Spacer()
                SpanText("Turn:", Modifier.margin(right = 15.px))
                Row {
                    ArrowButton(ArrowDirection.LEFT) {
                        turn = (turn - 1 + maxTurnPlus1) % maxTurnPlus1
                    }
                    SpanText(
                        (turn + 1).toString(),
                        Modifier.width(30.px).textAlign(TextAlign.Center)
                    )
                    ArrowButton(ArrowDirection.RIGHT) {
                        turn = (turn + 1) % maxTurnPlus1
                    }
                }
                Spacer()
            }

            private lateinit var historyByTurn: List<List<GameStateChanges>>

            override fun KeyScope.handleKey(): Boolean {
                turn = when (code) {
                    "ArrowLeft" -> (turn - 1 + maxTurnPlus1) % maxTurnPlus1
                    "ArrowRight" -> (turn + 1) % maxTurnPlus1
                    "Digit1" -> (if (isShift) 10 else 0).coerceAtMost(maxTurn)
                    "Digit2" -> (if (isShift) 11 else 1).coerceAtMost(maxTurn)
                    "Digit3" -> (if (isShift) 12 else 2).coerceAtMost(maxTurn)
                    "Digit4" -> (if (isShift) 13 else 3).coerceAtMost(maxTurn)
                    "Digit5" -> (if (isShift) 14 else 4).coerceAtMost(maxTurn)
                    "Digit6" -> (if (isShift) 15 else 5).coerceAtMost(maxTurn)
                    "Digit7" -> (if (isShift) 16 else 6).coerceAtMost(maxTurn)
                    "Digit8" -> (if (isShift) 17 else 7).coerceAtMost(maxTurn)
                    "Digit9" -> (if (isShift) 18 else 8).coerceAtMost(maxTurn)
                    "Digit0" -> (if (isShift) 19 else 9).coerceAtMost(maxTurn)
                    else -> return false
                }
                return true
            }

            @Composable
            override fun renderContent(params: Params) {
                with(params) {
                    if (!::historyByTurn.isInitialized) {
                        val _historyByTurn = mutableListOf<List<GameStateChanges>>()

                        var remaining = ctx.state.history
                        while (remaining.isNotEmpty()) {
                            val breakIndex = remaining.indexOfFirst { changes ->
                                changes.items.any { it is GameStateChange.EndTurn }
                            }.takeIf { it >= 0 } ?: remaining.lastIndex

                            _historyByTurn.add(
                                remaining.subList(0, breakIndex + 1)
                                    .filter { changes -> changes.items.none { it is GameStateChange.GameStart } }
                            )
                            remaining = remaining.subList(breakIndex + 1, remaining.size)
                        }

                        historyByTurn = _historyByTurn
                    }

                    historyByTurn[turn].let { allChangeGroupsThisTurn ->
                        allChangeGroupsThisTurn.forEachIndexed { i, changeGroup ->
                            val prevChangeGroup = if (i > 0) {
                                allChangeGroupsThisTurn[i - 1]
                            } else if (turn > 0) {
                                historyByTurn[turn - 1].last()
                            } else {
                                null
                            }

                            Button(
                                onClick = {
                                    params.visit(ReviewChanges(changeGroup))
                                },
                                enabled = changeGroup.flattenIntoItems().isNotEmpty()
                            ) {
                                Row(Modifier.gap(5.px).fillMaxWidth().whiteSpace(WhiteSpace.NoWrap)) {
                                    SpanText(changeGroup.toTitle(), Modifier.textAlign(TextAlign.Start))
                                    if ((changeGroup.vp - (prevChangeGroup?.vp ?: 0)) != 0) {
                                        SpanText(buildString {
                                            append('(')
                                            this.append(ctx.describer.describeVictoryPoints(changeGroup.vp))
                                            append(')')
                                        }, Modifier.textAlign(TextAlign.End))
                                    }
                                }
                            }

                            Tooltip(ElementTarget.PreviousSibling, buildString {
                                append(ctx.describer.describeCash(changeGroup.cash))
                                append(' ')
                                append(ctx.describer.describeInfluence(changeGroup.influence))
                                append(' ')
                                append(ctx.describer.describeLuck(changeGroup.luck))
                                append(' ')
                                append(ctx.describer.describeVictoryPoints(changeGroup.vp))
                                changeGroup.toSummaryText(ctx.describer, ctx.state, prevChangeGroup)?.let { summaryText ->
                                    appendLine()
                                    appendLine()
                                    append(summaryText.split("\n").joinToString("\n") { "• $it" })
                                }
                            })
                        }
                    }
                }
            }
        }

        class ReviewChanges(private val gameStateChanges: GameStateChanges) : GameMenuEntry {
            override val title = gameStateChanges.toTitle()
            @Composable
            override fun renderContent(params: Params) {
                gameStateChanges.flattenIntoItems()
                    .distinct()
                    .map { item -> item to params.ctx.describer.describeItem(item) }
                    .sortedBy { (_, title) -> title }
                    .forEach { (item, title) ->
                        Div(ReadOnlyStyle.toAttrs()) { Text(title) }
                        installPopup(params.ctx, item)
                }
            }
        }
    }
}

fun GameMenuEntry.handleKey(keyScope: KeyScope): Boolean = keyScope.handleKey()

@Composable
fun GameMenu(scope: CoroutineScope, ctx: GameContext, gameUpdater: GameUpdater, closeRequested: () -> Unit, quitRequested: () -> Unit) {
    val menuStack = remember { mutableStateListOf<GameMenuEntry>(GameMenuEntry.Main) }

    fun goBack() {
        if (menuStack.size >= 2) {
            menuStack.removeLast()
        } else closeRequested()
    }

    val params = GameMenuEntry.Params(
        scope,
        ctx,
        gameUpdater,
        { entry -> menuStack.add(entry) },
        closeRequested,
        requestQuit = {
            closeRequested()
            quitRequested()
        }
    )

    Modal(
        // A reasonable min width that can grow if necessary but prevents menu sizes jumping around otherwise
        dialogModifier = Modifier.minWidth(400.px),
        ref = inputRef {
            if (!menuStack.last().handleKey(this)) {
                if (code == "Escape") goBack()
            }
            true // Prevent keys from inadvertently affected game behind the modal
        },
        titleRow = {
            Spacer(); Text(menuStack.joinToString(" > ") { it.title }); Spacer()
        },
        topRow = menuStack.last().topRow,
        bottomRow = {
            Button(onClick = { goBack() }) {
                Text(if (menuStack.size >= 2) "Go Back" else "Close")
            }
            menuStack.last().renderExtraBottomButtons(params)
        },
    ) {
        menuStack.last().renderContent(params)
    }

}

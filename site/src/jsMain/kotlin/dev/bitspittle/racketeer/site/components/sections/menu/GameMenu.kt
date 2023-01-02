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
import dev.bitspittle.racketeer.site.components.sections.ReadOnlyChoiceStyle
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
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

@Composable
private fun MenuButton(params: GameMenuEntry.Params, entry: GameMenuEntry) {
    Button(onClick = { params.visit(entry) }) { Text(entry.title) }
}

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

    fun handleKey(code: String): Boolean = false

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

                    override fun handleKey(code: String): Boolean {
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

            private val ArrowButtonModifier = Modifier.padding(topBottom = 1.px, leftRight = 4.px)

            override val topRow: @Composable RowScope.() -> Unit = {
                SimpleGrid(numColumns(4), Modifier
                    .fillMaxWidth()
                    .gap(10.px)
                    .whiteSpace(WhiteSpace.NoWrap)
                    .gridTemplateColumns("min-content min-content 1fr min-content")
                ) {
                    SpanText("Sorted by:")
                    Button(onClick = {
                        sortingOrder = SortingOrder.values()[(sortingOrder.ordinal - 1 + SortingOrder.values().size) % SortingOrder.values().size]
                    }, ArrowButtonModifier) {
                        Text("<")
                    }
                    SpanText(
                        sortingOrder.name.replace('_', ' ').lowercase().capitalize(),
                        Modifier.textAlign(TextAlign.Center)
                    )
                    Button(onClick = {
                        sortingOrder = SortingOrder.values()[(sortingOrder.ordinal + 1) % SortingOrder.values().size]
                    }, ArrowButtonModifier) {
                        Text(">")
                    }

                    SpanText("Filtered by:")
                    Button(onClick = {
                        typeFilter = typeFilters[(typeFilters.indexOf(typeFilter) - 1 + typeFilters.size) % typeFilters.size]
                    }, ArrowButtonModifier) {
                        Text("<")
                    }
                    SpanText(
                        typeFilter ?: "(No filter)",
                        Modifier.textAlign(TextAlign.Center)
                    )
                    Button(onClick = {
                        typeFilter = typeFilters[(typeFilters.indexOf(typeFilter) + 1) % typeFilters.size]
                    }, ArrowButtonModifier) {
                        Text(">")
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

                        Div(ReadOnlyChoiceStyle.toAttrs()) {
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
    }
}

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
        ref = inputRef { code ->
            if (!menuStack.last().handleKey(code)) {
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

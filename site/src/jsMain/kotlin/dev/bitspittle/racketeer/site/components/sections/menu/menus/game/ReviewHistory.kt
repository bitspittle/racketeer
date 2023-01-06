package dev.bitspittle.racketeer.site.components.sections.menu.menus.game

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.css.TextAlign
import com.varabyte.kobweb.compose.css.WhiteSpace
import com.varabyte.kobweb.compose.dom.ElementTarget
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.foundation.layout.RowScope
import com.varabyte.kobweb.compose.foundation.layout.Spacer
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.silk.components.forms.Button
import com.varabyte.kobweb.silk.components.overlay.Tooltip
import com.varabyte.kobweb.silk.components.style.*
import com.varabyte.kobweb.silk.components.text.SpanText
import dev.bitspittle.racketeer.model.game.GameStateChange
import dev.bitspittle.racketeer.model.game.GameStateChanges
import dev.bitspittle.racketeer.site.KeyScope
import dev.bitspittle.racketeer.site.components.sections.ReadOnlyStyle
import dev.bitspittle.racketeer.site.components.sections.menu.Menu
import dev.bitspittle.racketeer.site.components.sections.menu.MenuActions
import dev.bitspittle.racketeer.site.components.util.installPopup
import dev.bitspittle.racketeer.site.components.widgets.ArrowButton
import dev.bitspittle.racketeer.site.components.widgets.ArrowDirection
import dev.bitspittle.racketeer.site.model.describeItem
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

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

class ReviewHistory(private val params: GameMenuParams) : Menu {
    override val title = "Review History"

    private var turn by mutableStateOf(params.ctx.state.turn)

    private val turnUpper = turn + 1 // One-indexed, useful for modding
    override val topRow: @Composable RowScope.() -> Unit = {
        Spacer()
        SpanText("Turn:", Modifier.margin(right = 15.px))
        Row {
            ArrowButton(ArrowDirection.LEFT) {
                turn = (turn - 1 + turnUpper) % turnUpper
            }
            SpanText(
                "${turn + 1} / $turnUpper",
                Modifier.minWidth(60.px).textAlign(TextAlign.Center)
            )
            ArrowButton(ArrowDirection.RIGHT) {
                turn = (turn + 1) % turnUpper
            }
        }
        Spacer()
    }

    private lateinit var historyByTurn: List<List<GameStateChanges>>

    override fun KeyScope.handleKey(): Boolean {
        turn = when (code) {
            "ArrowLeft" -> (turn - 1 + turnUpper) % turnUpper
            "ArrowRight" -> (turn + 1) % turnUpper
            "Digit1" -> (if (isShift) 10 else 0).coerceAtMost(turnUpper)
            "Digit2" -> (if (isShift) 11 else 1).coerceAtMost(turnUpper)
            "Digit3" -> (if (isShift) 12 else 2).coerceAtMost(turnUpper)
            "Digit4" -> (if (isShift) 13 else 3).coerceAtMost(turnUpper)
            "Digit5" -> (if (isShift) 14 else 4).coerceAtMost(turnUpper)
            "Digit6" -> (if (isShift) 15 else 5).coerceAtMost(turnUpper)
            "Digit7" -> (if (isShift) 16 else 6).coerceAtMost(turnUpper)
            "Digit8" -> (if (isShift) 17 else 7).coerceAtMost(turnUpper)
            "Digit9" -> (if (isShift) 18 else 8).coerceAtMost(turnUpper)
            "Digit0" -> (if (isShift) 19 else 9).coerceAtMost(turnUpper)
            else -> return false
        }
        return true
    }

    @Composable
    override fun renderContent(actions: MenuActions) {
        with(params) {
            if (!::historyByTurn.isInitialized) {
                @Suppress("LocalVariableName") // Allow underscore as this will get cast later to immutable
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
                            actions.visit(ReviewChanges(params, changeGroup))
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

    class ReviewChanges(private val params: GameMenuParams, private val gameStateChanges: GameStateChanges) : Menu {
        override val title = gameStateChanges.toTitle()

        @Composable
        override fun renderContent(actions: MenuActions) {
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

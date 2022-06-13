package dev.bitspittle.racketeer.console.view.views.game.history

import com.varabyte.kotter.foundation.input.Key
import com.varabyte.kotter.foundation.input.Keys
import com.varabyte.kotter.foundation.text.cyan
import com.varabyte.kotter.foundation.text.text
import com.varabyte.kotter.foundation.text.textLine
import com.varabyte.kotter.runtime.MainRenderScope
import com.varabyte.kotter.runtime.render.RenderScope
import dev.bitspittle.limp.utils.ifTrue
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.command.commands.buildings.ViewBlueprintCommand
import dev.bitspittle.racketeer.console.command.commands.game.cards.ViewCardTemplateCommand
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.view.View
import dev.bitspittle.racketeer.model.game.GameState
import dev.bitspittle.racketeer.model.game.GameStateChange
import dev.bitspittle.racketeer.model.game.GameStateChanges

private class HistoryCommand(
    ctx: GameContext,
    private val changes: GameStateChanges,
    private val prevChanges: GameStateChanges?,
) : Command(ctx) {
    override val title = with(changes.items.first()) {
        when (this) {
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

    override val description = buildString {
        append(ctx.describer.describeCash(changes.cash))
        append(' ')
        append(ctx.describer.describeInfluence(changes.influence))
        append(' ')
        append(ctx.describer.describeLuck(changes.luck))
        append(' ')
        append(ctx.describer.describeVictoryPoints(changes.vp))
        changes.toSummaryText(ctx.describer, ctx.state, prevChanges)?.let { summaryText ->
            appendLine()
            appendLine()
            append(summaryText)
        }
    }

    override val extra = (changes.vp > (prevChanges?.vp ?: 0)).ifTrue { ctx.describer.describeVictoryPoints(changes.vp) }

    override suspend fun invoke(): Boolean {
        val commands = mutableListOf<Command>()
        changes.items.forEach { change ->
            when (change) {
                is GameStateChange.Activate -> commands.add(ViewBlueprintCommand(ctx, change.building.blueprint))
                is GameStateChange.AddBlueprint -> commands.add(ViewBlueprintCommand(ctx, change.blueprint))
                is GameStateChange.AddBuildingAmount -> commands.add(ViewBlueprintCommand(ctx, change.building.blueprint))
                is GameStateChange.AddCardAmount -> commands.add(ViewCardTemplateCommand(ctx, change.card.template))
                is GameStateChange.AddEffect -> Unit
                is GameStateChange.AddGameAmount -> Unit
                is GameStateChange.AddGameTweak -> Unit
                is GameStateChange.AddShopTweak -> Unit
                is GameStateChange.AddTrait -> commands.add(ViewCardTemplateCommand(ctx, change.card.template))
                is GameStateChange.Build -> commands.add(ViewBlueprintCommand(ctx, change.blueprint))
                is GameStateChange.Buy -> commands.add(ViewCardTemplateCommand(ctx, change.card.template))
                is GameStateChange.Draw -> commands.addAll(change.cards.map { ViewCardTemplateCommand(ctx, it.template) })
                is GameStateChange.EndTurn -> Unit
                is GameStateChange.GameOver -> Unit
                is GameStateChange.GameStart -> Unit
                is GameStateChange.MoveCard -> commands.add(ViewCardTemplateCommand(ctx, change.card.template))
                is GameStateChange.MoveCards -> commands.addAll(change.cards.values.flatten().map { ViewCardTemplateCommand(ctx, it.template) })
                is GameStateChange.Play -> commands.add(ViewCardTemplateCommand(ctx, change.card.template))
                is GameStateChange.RemoveTrait -> commands.add(ViewCardTemplateCommand(ctx, change.card.template))
                is GameStateChange.RestockShop -> Unit
                is GameStateChange.SetGameData -> Unit
                is GameStateChange.Shuffle -> Unit
                is GameStateChange.ShuffleDiscardIntoDeck -> Unit
                is GameStateChange.UpgradeCard -> commands.add(ViewCardTemplateCommand(ctx, change.card.template))
                is GameStateChange.UpgradeShop -> Unit
            }
        }

        if (commands.isEmpty()) return false

        ctx.viewStack.pushView(object : View(ctx) {
            override fun createCommands() = commands.distinctBy { it.title }.sortedBy { it.title }
        })
        return true
    }
}

/** Browse the target [state]'s history so far. */
// Always start the current index at the very last item, since that represents the user's most recent action
class ReviewHistoryView(ctx: GameContext, private val state: GameState = ctx.state) : View(ctx, initialCurrIndex = Int.MAX_VALUE) {
    private var turn = ctx.state.turn

    private val historyByTurn: List<List<GameStateChanges>> = run {
        val historyByTurn = mutableListOf<List<GameStateChanges>>()

        var remaining = state.history
        while (remaining.isNotEmpty()) {
            val breakIndex = remaining.indexOfFirst { changes ->
                changes.items.any { it is GameStateChange.EndTurn }
            }.takeIf { it >= 0 } ?: remaining.lastIndex

            historyByTurn.add(
                remaining.subList(0, breakIndex + 1)
                    .filter { changes -> changes.items.none { it is GameStateChange.GameStart } }
            )
            remaining = remaining.subList(breakIndex + 1, remaining.size)
        }

        historyByTurn
    }

    override fun MainRenderScope.renderContentUpper() {
        textLine("Turn: ${turn + 1}")
        textLine()
    }

    override fun createCommands(): List<Command> = historyByTurn[turn].let { turnChanges ->
        turnChanges.mapIndexed { i, changes ->
            val prevChanges = if (i > 0) {
                turnChanges[i - 1]
            } else if (turn > 0) {
                historyByTurn[turn - 1].last()
            } else {
                null
            }

            HistoryCommand(ctx, changes, prevChanges)
        }
    }

    override fun RenderScope.renderFooterUpper() {
        text("Press "); cyan { text("LEFT") }; text(" and "); cyan { text("RIGHT") }; textLine( " to change the current turn.")
        text("Press "); cyan { text("0-9") }; textLine(" to select turns 1 through 10.");
        text("Press "); cyan { text("SHIFT + 0-9") }; textLine(" to select turns 11 through 20.");
        text("Press "); cyan { text("ENTER") }; textLine(" to review affected cards / buildings.");
    }

    override suspend fun handleAdditionalKeys(key: Key): Boolean {
        val numTurnsSoFar = (state.turn + 1)
        val currTurn = turn
        when (key) {
            Keys.LEFT -> turn = (turn - 1 + numTurnsSoFar) % numTurnsSoFar
            Keys.RIGHT -> turn = (turn + 1) % numTurnsSoFar
            Keys.DIGIT_1 -> turn = 0.coerceAtMost(numTurnsSoFar)
            Keys.DIGIT_2 -> turn = 1.coerceAtMost(numTurnsSoFar)
            Keys.DIGIT_3 -> turn = 2.coerceAtMost(numTurnsSoFar)
            Keys.DIGIT_4 -> turn = 3.coerceAtMost(numTurnsSoFar)
            Keys.DIGIT_5 -> turn = 4.coerceAtMost(numTurnsSoFar)
            Keys.DIGIT_6 -> turn = 5.coerceAtMost(numTurnsSoFar)
            Keys.DIGIT_7 -> turn = 6.coerceAtMost(numTurnsSoFar)
            Keys.DIGIT_8 -> turn = 7.coerceAtMost(numTurnsSoFar)
            Keys.DIGIT_9 -> turn = 8.coerceAtMost(numTurnsSoFar)
            Keys.DIGIT_0 -> turn = 9.coerceAtMost(numTurnsSoFar)
            Keys.EXCLAMATION_MARK -> turn = 10.coerceAtMost(numTurnsSoFar)
            Keys.AT -> turn = 11.coerceAtMost(numTurnsSoFar)
            Keys.POUND -> turn = 12.coerceAtMost(numTurnsSoFar)
            Keys.DOLLAR -> turn = 13.coerceAtMost(numTurnsSoFar)
            Keys.PERCENT -> turn = 14.coerceAtMost(numTurnsSoFar)
            Keys.HAT -> turn = 15.coerceAtMost(numTurnsSoFar)
            Keys.AMPERSAND -> turn = 16.coerceAtMost(numTurnsSoFar)
            Keys.ASTERISK -> turn = 17.coerceAtMost(numTurnsSoFar)
            Keys.LEFT_PARENS -> turn = 18.coerceAtMost(numTurnsSoFar)
            Keys.RIGHT_PARENS -> turn = 19.coerceAtMost(numTurnsSoFar)
            else -> return false
        }

        return if (turn != currTurn) {
            currIndex = 0
            refreshCommands()
            true
        } else false
    }
}
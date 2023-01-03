package dev.bitspittle.racketeer.console.command.commands.game.play

import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.command.commands.system.SaveGameCommand
import dev.bitspittle.racketeer.console.command.commands.system.UserDataDir
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.utils.runStateChangingAction
import dev.bitspittle.racketeer.console.view.views.game.play.ConfirmEndTurnView
import dev.bitspittle.racketeer.console.view.views.game.play.GameSummaryView
import dev.bitspittle.racketeer.console.view.views.game.play.PlayCardsView
import dev.bitspittle.racketeer.model.effect.Tweak
import dev.bitspittle.racketeer.model.effect.isNotSet
import dev.bitspittle.racketeer.model.game.GameStateChange
import dev.bitspittle.racketeer.model.game.isGameOver
import kotlin.io.path.deleteIfExists

class EndTurnCommand(ctx: GameContext, private val showConfirmationIfNecessary: Boolean = true) : Command(ctx) {
    override val type = Type.Accented

    override val title = "End turn"

    override val description =
        "Finish this turn. Cards in your hand and the street will move to the discard pile, any leftover money will be " +
                "lost, and the shop will get restocked with random items."

    private fun canStillBuyStuff() = run {
        ctx.state.shop.stock.filterNotNull().any { it.template.cost <= ctx.state.cash }
    }

    override suspend fun invoke(): Boolean {
        if (showConfirmationIfNecessary && canStillBuyStuff() && ctx.state.tweaks.isNotSet<Tweak.Game.KeepUnspent>()) {
            ctx.viewStack.pushView(ConfirmEndTurnView(ctx))
        } else {
            ctx.runStateChangingAction {
                ctx.state.addChange(GameStateChange.EndTurn())
            }

            if (!ctx.state.isGameOver) {
                // Break up into two state changing actions for a better state diff report around reshuffling cards
                ctx.runStateChangingAction {
                    ctx.state.addChange(GameStateChange.Draw())
                    ctx.viewStack.replaceView(PlayCardsView(ctx))
                }

                try {
                    // Force an auto-save so user's don't lose their progress if they
                    // crash or their program freezes
                    SaveGameCommand(ctx, UserDataDir.QUICKSAVE_SLOT).invoke()
                    ctx.app.logger.warn("\nGame auto-saved.")
                } catch (ignored: Exception) {
                    // Shouldn't ever happen but we don't want to risk an autosave failure stopping
                    // someone from playing through the rest of the game
                }
            } else {
                ctx.app.userDataDir.pathForSlot(UserDataDir.QUICKSAVE_SLOT).deleteIfExists()
                ctx.viewStack.replaceView(GameSummaryView(ctx))
            }
        }
        return true
    }
}


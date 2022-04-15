package dev.bitspittle.racketeer.console.command.commands.game

import dev.bitspittle.limp.exceptions.EvaluationException
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.scripting.types.CancelPlayException

class PlayCardCommand(ctx: GameContext, private val handIndex: Int) : Command(ctx) {
    override val type = Type.Modify
    private val card = ctx.state.hand.cards[handIndex]

    override val title = "Play: ${ctx.describer.describe(card, concise = true)}"

    override val description = ctx.describer.describe(card)

    override suspend fun invoke(): Boolean {
        val prevState = ctx.state
        ctx.state = prevState.copy()

        ctx.env.scoped {
            try {
                ctx.state.play(handIndex)
            } catch (ex: EvaluationException) {
                ctx.state = prevState
                if (ex.cause !is CancelPlayException) {
                    throw ex
                }
            }
            finally {
                ctx.viewStack.currentView.refreshCommands()
            }
        }

        return true
    }
}


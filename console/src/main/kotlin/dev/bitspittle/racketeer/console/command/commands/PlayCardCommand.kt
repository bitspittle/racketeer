package dev.bitspittle.racketeer.console.command.commands

import dev.bitspittle.limp.exceptions.EvaluationException
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.view.views.PlayCardsView
import dev.bitspittle.racketeer.scripting.types.CancelPlayException
import dev.bitspittle.racketeer.scripting.utils.addVariableTo
import dev.bitspittle.racketeer.scripting.utils.addVariablesInto

class PlayCardCommand(ctx: GameContext, private val handIndex: Int) : Command(ctx) {
    private val card = ctx.state.hand.cards[handIndex]

    override val title = "Play: ${ctx.describer.describe(card, concise = true)}"

    override val description = ctx.describer.describe(card)

    override suspend fun invoke(): Boolean {
        val prevState = ctx.state
        ctx.state = prevState.copy()

        ctx.env.scoped {
            ctx.state.addVariablesInto(this)
            card.addVariableTo(this)
            try {
                ctx.state.play(ctx.cardRunner, handIndex)
                // Cause a "refresh" with an update on what cards are in your hand
                ctx.viewStack.replaceView(PlayCardsView(ctx))
            } catch (ex: EvaluationException) {
                ctx.state = prevState
                if (ex.cause !is CancelPlayException) {
                    ctx.app.log(ex.message!!)
                }
            }
        }

        return true
    }
}


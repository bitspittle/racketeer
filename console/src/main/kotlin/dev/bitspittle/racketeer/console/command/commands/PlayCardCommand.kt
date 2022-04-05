package dev.bitspittle.racketeer.console.command.commands

import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.exceptions.EvaluationException
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.view.views.PlayCardsView
import kotlinx.coroutines.runBlocking

class PlayCardCommand(ctx: GameContext, private val handIndex: Int) : Command(ctx) {
    private val card = ctx.state.hand.cards[handIndex]

    override val title = "Play: ${ctx.describers.describe(card, concise = true)}"

    override val description = ctx.describers.describe(card)

    override fun invoke(): Boolean {
        val prevState = ctx.state
        ctx.state = prevState.copy()

        try {
            val evaluator = Evaluator()
            // TODO: MAKE INVOKE A SUSPEND FUN PROBABLY? THIS SHOULD NOT BE RUN BLOCKING
            runBlocking {
                ctx.compiledActions.getValue(card.template).forEach { expr ->
                    evaluator.evaluate(ctx.env, expr)
                }
            }

            ctx.state.move(card, ctx.state.street)
            ctx.viewStack.replaceView(PlayCardsView(ctx))
        }
        catch (ex: EvaluationException) {
            ctx.app.log(ex.message!!)
        }

        return true
    }
}


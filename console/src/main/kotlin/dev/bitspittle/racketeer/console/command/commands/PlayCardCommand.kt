package dev.bitspittle.racketeer.console.command.commands

import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.exceptions.EvaluationException
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.view.views.PlayCardsView
import dev.bitspittle.racketeer.scripting.addVariableTo
import dev.bitspittle.racketeer.scripting.addVariablesInto
import kotlinx.coroutines.runBlocking

class PlayCardCommand(ctx: GameContext, private val handIndex: Int) : Command(ctx) {
    private val card = ctx.state.hand.cards[handIndex]

    override val title = "Play: ${ctx.describer.describe(card, concise = true)}"

    override val description = ctx.describer.describe(card)

    override fun invoke(): Boolean {
        val prevState = ctx.state
        ctx.state = prevState.copy()

        try {
            ctx.state.move(card, ctx.state.street)

            val evaluator = Evaluator()
            // TODO: MAKE INVOKE A SUSPEND FUN PROBABLY? THIS SHOULD NOT BE RUN BLOCKING
            ctx.env.scoped {
                ctx.state.addVariablesInto(this)
                card.addVariableTo(this)

                runBlocking {
                    ctx.compiledActions.getValue(card.template).forEach { expr ->
                        evaluator.evaluate(ctx.env, expr)
                    }
                }
            }

            ctx.viewStack.replaceView(PlayCardsView(ctx))
        }
        catch (ex: EvaluationException) {
            ctx.app.log(ex.message!!)
            ctx.state = prevState
        }

        return true
    }
}


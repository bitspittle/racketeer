package dev.bitspittle.racketeer.console.command.commands.game.play

import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.utils.runStateChangingAction
import dev.bitspittle.racketeer.model.card.Card
import dev.bitspittle.racketeer.model.game.GameStateChange

class PlayCardCommand(ctx: GameContext, private val card: Card) : Command(ctx) {
    override val type get() = if (ctx.enqueuers.actionQueue.isRunning) Type.Blocked else Type.Emphasized

    override val title = "Play: ${ctx.describer.describeCardTitle(card)}"

    override val description = ctx.describer.describeCardBody(card)

    override suspend fun invoke(): Boolean {
        ctx.runStateChangingAction {
            ctx.state.apply(GameStateChange.Play(
                ctx.state.hand.cards.indexOfFirst { it.id == card.id }
            ))
        }

        return true
    }
}


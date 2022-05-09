package dev.bitspittle.racketeer.console.command.commands.game.play

import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.utils.runStateChangingAction
import dev.bitspittle.racketeer.model.game.GameStateChange
import dev.bitspittle.racketeer.model.game.getOwnedCards

class PlayCardCommand(ctx: GameContext, private val handIndex: Int) : Command(ctx) {
    override val type get() = if (ctx.cardQueue.isRunning) Type.Blocked else Type.Emphasized
    private val card = ctx.state.hand.cards[handIndex]

    override val title = "Play: ${ctx.describer.describeCard(card, concise = true)}"

    override val description = ctx.describer.describeCard(card)

    override suspend fun invoke(): Boolean {
        ctx.runStateChangingAction {
            ctx.state.apply(GameStateChange.Play(handIndex))
        }

        return true
    }
}


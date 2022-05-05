package dev.bitspittle.racketeer.console.command.commands.game

import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.utils.runStateChangingAction
import dev.bitspittle.racketeer.model.game.GameStateDelta

class PlayCardCommand(ctx: GameContext, private val handIndex: Int) : Command(ctx) {
    override val type = Type.Emphasized
    private val card = ctx.state.hand.cards[handIndex]

    override val title = "Play: ${ctx.describer.describeCard(card, concise = true)}"

    override val description = ctx.describer.describeCard(card)

    override suspend fun invoke(): Boolean {
        ctx.runStateChangingAction {
            ctx.state.apply(GameStateDelta.Play(handIndex))
        }

        return true
    }
}


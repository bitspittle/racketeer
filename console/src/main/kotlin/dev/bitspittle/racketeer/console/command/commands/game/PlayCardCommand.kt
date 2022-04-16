package dev.bitspittle.racketeer.console.command.commands.game

import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.utils.runStateChangingAction

class PlayCardCommand(ctx: GameContext, private val handIndex: Int) : Command(ctx) {
    override val type = Type.Modify
    private val card = ctx.state.hand.cards[handIndex]

    override val title = "Play: ${ctx.describer.describe(card, concise = true)}"

    override val description = ctx.describer.describe(card)

    override suspend fun invoke(): Boolean {
        ctx.runStateChangingAction {
            ctx.state.play(handIndex)
        }

        return true
    }
}


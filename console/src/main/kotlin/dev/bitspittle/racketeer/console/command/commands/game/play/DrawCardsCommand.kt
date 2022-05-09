package dev.bitspittle.racketeer.console.command.commands.game.play

import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.utils.runStateChangingAction
import dev.bitspittle.racketeer.console.view.views.game.play.PlayCardsView
import dev.bitspittle.racketeer.model.game.GameStateChange

class DrawCardsCommand(ctx: GameContext) : Command(ctx) {
    override val type: Type = Type.Emphasized
    override val title = "Draw cards"

    override val description = "Draw ${ctx.state.handSize} cards and put them into your hand."

    override suspend fun invoke(): Boolean {
        ctx.runStateChangingAction {
            ctx.state.apply(GameStateChange.Draw(ctx.state.handSize))
            ctx.viewStack.replaceView(PlayCardsView(ctx))
        }
        return true
    }

}


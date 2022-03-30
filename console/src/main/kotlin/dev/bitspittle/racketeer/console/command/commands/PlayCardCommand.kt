package dev.bitspittle.racketeer.console.command.commands

import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.view.views.PlayCardsView

class PlayCardCommand(ctx: GameContext, private val handIndex: Int) : Command(ctx) {
    private val card = ctx.state.hand.cards[handIndex]

    override val title = "Play: ${ctx.describers.describe(card, concise = true)}"

    override val description = ctx.describers.describe(card)

    override fun invoke() {
        ctx.state.play(handIndex)
        ctx.viewStack.replaceView(PlayCardsView(ctx))
    }
}


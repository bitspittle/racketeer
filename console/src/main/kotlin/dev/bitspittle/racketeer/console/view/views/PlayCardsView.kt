package dev.bitspittle.racketeer.console.view.views

import com.varabyte.kotter.foundation.text.textLine
import com.varabyte.kotter.runtime.render.RenderScope
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.command.commands.*
import dev.bitspittle.racketeer.console.view.View

class PlayCardsView(ctx: GameContext) : View(ctx) {
    override fun createCommands(): List<Command> =
        List(ctx.state.hand.cards.size) { i -> PlayCardCommand(ctx, i) } + listOf(
            BrowseShopCommand(ctx),
            BrowseDeckCommand(ctx),
            BrowseDiscardCommand(ctx),
            EndTurnCommand(ctx),
        )

    override fun RenderScope.renderContent() {
        if (ctx.state.street.cards.isNotEmpty()) {
            textLine("The Street:")
            ctx.state.street.cards.forEach { card ->
                textLine("- ${ctx.describer.describe(card, concise = true)}")
            }
            textLine()
        }
    }
}
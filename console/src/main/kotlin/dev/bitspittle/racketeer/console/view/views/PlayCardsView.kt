package dev.bitspittle.racketeer.console.view.views

import com.varabyte.kotter.foundation.text.text
import com.varabyte.kotter.foundation.text.textLine
import com.varabyte.kotter.runtime.render.RenderScope
import dev.bitspittle.racketeer.console.GameContext
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.command.commands.BrowseDeckCommand
import dev.bitspittle.racketeer.console.command.commands.BrowseShopCommand
import dev.bitspittle.racketeer.console.command.commands.EndTurnCommand
import dev.bitspittle.racketeer.console.command.commands.PlayCardCommand
import dev.bitspittle.racketeer.console.view.View

class PlayCardsView(ctx: GameContext) : View(ctx) {
    override val commands: List<Command> =
        List(ctx.state.hand.cards.size) { i -> PlayCardCommand(ctx, i) } + listOf(
            BrowseShopCommand(ctx),
            BrowseDeckCommand(ctx),
            EndTurnCommand(ctx)
        )

    override fun RenderScope.renderContent() {
        if (ctx.state.street.cards.isNotEmpty()) {
            textLine("The Street:")
            ctx.state.street.cards.forEach { card ->
                textLine("- ${ctx.describers.describe(card, concise = true)}")
            }
            textLine()
        }
    }
}
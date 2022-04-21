package dev.bitspittle.racketeer.console.view.views.game

import com.varabyte.kotter.foundation.text.textLine
import com.varabyte.kotter.runtime.MainRenderScope
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.command.commands.game.ViewCardCommand
import dev.bitspittle.racketeer.console.game.GameContext

class BrowseStreetView(ctx: GameContext) : GameView(ctx) {
    init {
        check(ctx.state.street.cards.isNotEmpty())
    }

    override val subtitle = "The Street"

    override fun createCommands(): List<Command> =
        ctx.state.street.cards.map { card -> ViewCardCommand(ctx, card) }

    override fun MainRenderScope.renderContentUpper() {
        if (ctx.state.streetEffects.isNotEmpty()) {
            textLine("Active effects:")
            ctx.state.streetEffects.forEach { effect ->
                textLine("- $effect")
            }
            textLine()
        }
    }
}
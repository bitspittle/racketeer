package dev.bitspittle.racketeer.console.view.views

import com.varabyte.kotter.foundation.text.textLine
import com.varabyte.kotter.runtime.render.RenderScope
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.command.commands.ViewCardCommand
import dev.bitspittle.racketeer.console.command.commands.ViewCardGroupCommand
import dev.bitspittle.racketeer.console.view.View

class BrowseStreetView(ctx: GameContext) : View(ctx) {
    init {
        check(ctx.state.street.cards.isNotEmpty())
    }

    override val subtitle = "The Street"

    override fun createCommands(): List<Command> =
        ctx.state.street.cards.map { card -> ViewCardCommand(ctx, card) }

    override fun RenderScope.renderContent() {
        if (ctx.state.streetEffects.isNotEmpty()) {
            textLine("Active effects:")
            ctx.state.streetEffects.forEach { effect ->
                textLine("- $effect")
            }
            textLine()
        }
    }
}
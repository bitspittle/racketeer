package dev.bitspittle.racketeer.console.view.views.game.cards

import com.varabyte.kotter.foundation.text.textLine
import com.varabyte.kotter.runtime.MainRenderScope
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.command.commands.game.cards.ViewCardCommand
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.user.inAdminModeAndShowDebugInfo
import dev.bitspittle.racketeer.console.view.views.game.GameView

class BrowseStreetView(ctx: GameContext) : GameView(ctx) {
    init {
        check(ctx.state.street.cards.isNotEmpty())
    }

    override val subtitle = "The Street"

    override fun createCommands(): List<Command> =
        ctx.state.street.cards.map { card -> ViewCardCommand(ctx, card) }

    override fun MainRenderScope.renderContentUpper() {
        if (ctx.state.effects.items.isNotEmpty()) {
            textLine("Active effects:")
            ctx.state.effects.items.forEach { effect ->
                textLine("- ${effect.desc ?: effect.expr}")
                if (ctx.settings.inAdminModeAndShowDebugInfo) {
                    textLine("  Lifetime: ${effect.lifetime.name.lowercase()}, Event: ${effect.event.name.lowercase()}")
                    if (effect.data != null) {
                        textLine("  Data: ${effect.data}")
                    }
                    if (effect.testExpr != null) {
                        textLine("  Test: ${effect.testExpr}")
                    }
                    if (effect.desc != effect.expr) {
                        textLine("  ${effect.expr}")
                    }
                }
            }
            textLine()
        }
    }
}
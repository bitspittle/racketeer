package dev.bitspittle.racketeer.console.view.views.game.cards

import com.varabyte.kotter.foundation.text.textLine
import com.varabyte.kotter.runtime.MainRenderScope
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.command.commands.buildings.BrowseBuildingsCommand
import dev.bitspittle.racketeer.console.command.commands.game.cards.*
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.user.inAdminModeAndShowCode
import dev.bitspittle.racketeer.console.view.View
import dev.bitspittle.racketeer.model.game.isGameOver
import dev.bitspittle.racketeer.model.game.warningExpr

class BrowsePilesView(ctx: GameContext) : View(ctx) {
    override fun MainRenderScope.renderContentUpper() {
        if (ctx.state.effects.items.isNotEmpty()) {
            textLine("Active effects:")
            ctx.state.effects.items.forEach { effect ->
                textLine("- ${effect.desc ?: effect.warningExpr}")
                if (ctx.settings.inAdminModeAndShowCode) {
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

    override fun createCommands() = (if (!ctx.state.isGameOver) {
        listOf(BrowseShopCommand(ctx))
    } else emptyList<Command>()) + listOf(
        BrowseBuildingsCommand(ctx),
        BrowseStreetCommand(ctx),
        BrowseHandCommand(ctx),
        BrowseDeckCommand(ctx),
        BrowseDiscardCommand(ctx),
        BrowseJailCommand(ctx),
        BrowseOwnedCardsCommand(ctx),
    )
}
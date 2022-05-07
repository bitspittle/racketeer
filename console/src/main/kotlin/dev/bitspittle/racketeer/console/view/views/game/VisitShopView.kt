package dev.bitspittle.racketeer.console.view.views.game

import com.varabyte.kotter.foundation.text.textLine
import com.varabyte.kotter.runtime.MainRenderScope
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.command.commands.game.*
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.user.inAdminModeAndShowDebugInfo

class VisitShopView(ctx: GameContext) : GameView(ctx) {
    override val subtitle get() = "Shop (Tier ${ctx.state.shop.tier + 1})"

    override fun createCommands(): List<Command> = run {
        ctx.state.shop.stock.map { card ->
            if (card != null) BuyCardCommand(ctx, card) else SoldOutCardCommand(ctx)
        } + listOf(
            RestockShopCommand(ctx),
            if (ctx.state.shop.tier < ctx.data.maxTier) ExpandShopCommand(ctx) else MaxedOutShopCommand(ctx)
        )
    }

    override fun refreshCursorPosition(oldIndex: Int, oldCommand: Command): Int {
        // If we expanded the shop, our number of items grew by one (or if it didn't for some reason, still, it's the
        // last item in the list, so "oldIndex + 1" will just get clamped anyway
        return if (oldCommand is ExpandShopCommand) oldIndex + 1 else super.refreshCursorPosition(oldIndex, oldCommand)
    }

    override fun MainRenderScope.renderContentUpper() {
        if (ctx.settings.inAdminModeAndShowDebugInfo && ctx.state.shop.exclusions.isNotEmpty()) {
            textLine("Exclusions:")
            ctx.state.shop.exclusions.forEach { exclusion ->
                textLine("- ${exclusion.expr}")
            }
            textLine()
        }
    }

    override fun MainRenderScope.renderContentLower() {
        (currCommand as? BuyCardCommand)?.renderHelpInto(this)
    }
}
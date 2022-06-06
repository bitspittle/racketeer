package dev.bitspittle.racketeer.console.view.views.game.cards

import com.varabyte.kotter.foundation.text.textLine
import com.varabyte.kotter.runtime.MainRenderScope
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.view.View

class BrowseShopView(ctx: GameContext) : View(ctx) {
    init {
        check(ctx.state.shop.stock.filterNotNull().isNotEmpty())
    }

    override val subtitle get() = "Shop (Tier ${ctx.state.shop.tier + 1})"

    override fun MainRenderScope.renderContentUpper() {
        if (ctx.state.shop.tweaks.items.isNotEmpty()) {
            textLine("Active effects:")
            ctx.state.shop.tweaks.items.forEach { tweak ->
                textLine("- ${tweak.desc}")
            }
        }
    }

    override fun createCommands(): List<Command> =
        ctx.state.shop.stock.filterNotNull()
            .map { card ->
                object : Command(ctx) {
                    override val title = ctx.describer.describeCardTitle(card)
                    override val extra = ctx.describer.describeCash(card.template.cost)
                    override val description = ctx.describer.describeCardBody(card.template)
                }
            }
}
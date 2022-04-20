package dev.bitspittle.racketeer.console.view.views.game

import com.varabyte.kotter.foundation.text.black
import com.varabyte.kotter.foundation.text.textLine
import com.varabyte.kotter.runtime.MainRenderScope
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.command.commands.game.*
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.view.View

class PlayCardsView(ctx: GameContext) : View(ctx) {
    override fun refreshCursorPosition(oldIndex: Int, oldCommand: Command): Int {
        // Keep the cursor in place, except when we're playing cards -- then, make sure the cursor stays on cards
        // until we run out. This way, if the user plays the last card in their hand, their cursor will be on the next
        // last card in their hand.
        return if (oldCommand is PlayCardCommand && ctx.state.hand.cards.isNotEmpty()) {
            oldIndex.coerceIn(0, ctx.state.hand.cards.lastIndex)
        } else super.refreshCursorPosition(oldIndex, oldCommand)
    }

    override fun createCommands(): List<Command> =
        List(ctx.state.hand.cards.size) { i -> PlayCardCommand(ctx, i) } + listOf(
            BrowseStreetCommand(ctx),
            BrowseShopCommand(ctx),
            BrowseDeckCommand(ctx),
            BrowseDiscardCommand(ctx),
            BrowseJailCommand(ctx),
            EndTurnCommand(ctx),
        )

    override fun MainRenderScope.renderContentUpper() {
        textLine("The Street:")
        if (ctx.state.street.cards.isNotEmpty()) {
            if (ctx.state.streetEffects.isNotEmpty()) {
                ctx.state.streetEffects.forEach { effect ->
                    textLine("+ $effect")
                }
            }
            ctx.state.street.cards.forEach { card ->
                textLine("- ${ctx.describer.describeCard(card, concise = true)}")
            }
        } else {
            black(isBright = true) { textLine("(Empty)") }
        }

        textLine()
    }
}
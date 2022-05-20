package dev.bitspittle.racketeer.console.view.views.game.play

import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.command.commands.game.play.EndTurnCommand
import dev.bitspittle.racketeer.console.command.commands.game.play.PlayCardCommand
import dev.bitspittle.racketeer.console.command.commands.game.play.VisitShopCommand
import dev.bitspittle.racketeer.console.command.commands.buildings.ActivateBuildingCommand
import dev.bitspittle.racketeer.console.command.commands.buildings.VisitBlueprintsCommand
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
        List(ctx.state.hand.cards.size) { i -> PlayCardCommand(ctx, i) } +
        ctx.state.buildings.map { building -> ActivateBuildingCommand(ctx, building) } + listOf(
            VisitShopCommand(ctx),
            VisitBlueprintsCommand(ctx),
            EndTurnCommand(ctx),
        )
}
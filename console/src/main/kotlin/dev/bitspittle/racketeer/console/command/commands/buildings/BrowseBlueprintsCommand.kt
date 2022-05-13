package dev.bitspittle.racketeer.console.command.commands.buildings

import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.view.views.game.buildings.BrowseBlueprintsView

class BrowseBlueprintsCommand(ctx: GameContext) : Command(ctx) {
    private val affordableCount = ctx.state.blueprints.count { it.canAfford(ctx) }

    override val type: Type = when {
        affordableCount > 0 -> Type.Emphasized
        ctx.state.blueprints.isNotEmpty() -> Type.Normal
        else -> Type.Hidden
    }
    override val title = "Blueprints"

    override val description = "Blueprints can be used to construct buildings when you have enough resources."

    override suspend fun invoke(): Boolean {
        ctx.viewStack.pushView(BrowseBlueprintsView(ctx))
        return true
    }
}


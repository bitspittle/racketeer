package dev.bitspittle.racketeer.console.command.commands.locations

import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.view.views.game.locations.BrowseBlueprintsView

class BrowseBlueprintsCommand(ctx: GameContext) : Command(ctx) {
    private val affordableCount = ctx.state.blueprints.count { it.canAfford(ctx) }

    override val type: Type = when {
        affordableCount > 0 -> Type.Emphasized
        ctx.state.blueprints.isNotEmpty() -> Type.Normal
        else -> Type.Hidden
    }
    override val title = "Blueprints (${ctx.state.blueprints.size})"

    override val description = "Blueprints are needed for building locations into the street.\n\nYou can currently build $affordableCount of them."

    override suspend fun invoke(): Boolean {
        ctx.viewStack.pushView(BrowseBlueprintsView(ctx))
        return true
    }
}


package dev.bitspittle.racketeer.console.command.commands.buildings

import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.view.popAll
import dev.bitspittle.racketeer.console.view.views.game.buildings.VisitBlueprintsView

class VisitBlueprintsCommand(ctx: GameContext) : Command(ctx) {
    private val affordableCount = ctx.state.blueprints.count { it.canAffordBuildCost(ctx) }

    override val type: Type = when {
        affordableCount > 0 -> Type.Emphasized
        ctx.state.blueprints.isNotEmpty() -> Type.Normal
        else -> Type.Hidden
    }
    override val title = "View Blueprints"

    override val description = "Blueprints can be used to construct buildings when you have enough resources."

    override suspend fun invoke(): Boolean {
        ctx.viewStack.popAll() // Blueprints should always be anchored to the top level
        ctx.viewStack.pushView(VisitBlueprintsView(ctx))
        return true
    }
}


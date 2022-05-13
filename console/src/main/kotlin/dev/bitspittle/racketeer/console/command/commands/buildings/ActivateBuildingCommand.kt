package dev.bitspittle.racketeer.console.command.commands.buildings

import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.utils.runStateChangingAction
import dev.bitspittle.racketeer.model.game.GameProperty
import dev.bitspittle.racketeer.model.game.GameStateChange
import dev.bitspittle.racketeer.model.building.Building

fun Building.canActivate(ctx: GameContext) =
    ctx.state.cash >= blueprint.activationCost.cash && ctx.state.influence >= blueprint.activationCost.influence && ctx.state.luck >= blueprint.activationCost.luck

class ActivateBuildingCommand(ctx: GameContext, private val building: Building) : Command(ctx) {
    override val type = when {
        building.isActivated -> Type.Hidden
        building.canActivate(ctx) -> Type.Emphasized
        else -> Type.Disabled
    }

    override val title = "Activate: " + ctx.describer.describeBuilding(building, concise = true)
    override val description = ctx.describer.describeBuilding(building, concise = false)
    override val extra: String = ctx.describer.describeActivationCost(building.blueprint)

    override suspend fun invoke(): Boolean {
        ctx.runStateChangingAction {
            val cost = building.blueprint.activationCost
            if (cost.cash > 0) {
                ctx.state.apply(GameStateChange.AddGameAmount(GameProperty.CASH, -cost.cash))
            }
            if (cost.influence > 0) {
                ctx.state.apply(GameStateChange.AddGameAmount(GameProperty.INFLUENCE, -cost.influence))
            }
            if (cost.luck > 0) {
                ctx.state.apply(GameStateChange.AddGameAmount(GameProperty.LUCK, -cost.luck))
            }

            ctx.state.apply(GameStateChange.Activate(ctx.state.buildings.indexOfFirst { it.id == building.id }))
        }
        return true
    }
}

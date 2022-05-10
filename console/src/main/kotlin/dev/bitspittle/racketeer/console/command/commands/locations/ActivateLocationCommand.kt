package dev.bitspittle.racketeer.console.command.commands.locations

import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.utils.runStateChangingAction
import dev.bitspittle.racketeer.model.game.GameProperty
import dev.bitspittle.racketeer.model.game.GameStateChange
import dev.bitspittle.racketeer.model.location.Blueprint
import dev.bitspittle.racketeer.model.location.Location

fun Location.canActivate(ctx: GameContext) =
    ctx.state.cash >= blueprint.activationCost.cash && ctx.state.influence >= blueprint.activationCost.influence && ctx.state.luck >= blueprint.activationCost.luck

/**
 * A no-op command used when read-only viewing a blueprint
 */
class ActivateLocationCommand(ctx: GameContext, private val location: Location) : Command(ctx) {
    override val type = when {
        location.isActivated -> Type.Hidden
        location.canActivate(ctx) -> Type.Emphasized
        else -> Type.Disabled
    }

    override val title = "Activate: " + ctx.describer.describeLocation(location, concise = true)
    override val description = ctx.describer.describeLocation(location, concise = false)
    override val extra: String = ctx.describer.describeActivationCost(location)

    override suspend fun invoke(): Boolean {
        ctx.runStateChangingAction {
            val cost = location.blueprint.activationCost
            if (cost.cash > 0) {
                ctx.state.apply(GameStateChange.AddGameAmount(GameProperty.CASH, -cost.cash))
            }
            if (cost.influence > 0) {
                ctx.state.apply(GameStateChange.AddGameAmount(GameProperty.INFLUENCE, -cost.influence))
            }
            if (cost.luck > 0) {
                ctx.state.apply(GameStateChange.AddGameAmount(GameProperty.LUCK, -cost.luck))
            }

            ctx.state.apply(GameStateChange.Activate(ctx.state.locations.indexOfFirst { it.id == location.id }))
        }
        return true
    }
}

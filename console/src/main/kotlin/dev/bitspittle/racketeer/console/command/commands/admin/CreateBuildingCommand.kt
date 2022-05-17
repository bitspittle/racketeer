package dev.bitspittle.racketeer.console.command.commands.admin

import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.utils.runStateChangingAction
import dev.bitspittle.racketeer.console.view.popPast
import dev.bitspittle.racketeer.console.view.views.admin.AdminMenuView
import dev.bitspittle.racketeer.model.building.Blueprint
import dev.bitspittle.racketeer.model.game.GameStateChange

class CreateBuildingCommand(ctx: GameContext, private val blueprint: Blueprint) : Command(ctx) {
    override val type = Type.Warning
    override val title = ctx.describer.describeBlueprint(blueprint, concise = true)
    override val description = ctx.describer.describeBlueprint(blueprint, concise = false)

    override suspend fun invoke(): Boolean {
        // Run this command in two separate state changing actions; otherwise, it gets reported weird since the
        // blueprintIndex (below) is referring to a building that the gamestatediff code can't see since it was created
        // and then removed before the report got a chance to look at it
        if (!ctx.state.blueprints.contains(blueprint)) {
            ctx.runStateChangingAction {
                ctx.state.apply(GameStateChange.AddBlueprint(blueprint))
            }
        }

        ctx.runStateChangingAction {
            val blueprintIndex = ctx.state.blueprints.indexOf(blueprint)
            check(blueprintIndex >= 0)
            ctx.state.apply(GameStateChange.Build(blueprintIndex))
            ctx.viewStack.popPast { view -> view is AdminMenuView }
        }
        return true
    }
}


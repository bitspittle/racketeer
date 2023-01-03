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
    override val title = ctx.describer.describeBlueprintTitle(blueprint)
    override val description = ctx.describer.describeBlueprintBody(blueprint)

    override suspend fun invoke(): Boolean {
        // Run this command in two separate state changing actions; you need to own the blueprint before you can
        // build it.
        if (!ctx.state.blueprints.contains(blueprint)) {
            ctx.runStateChangingAction {
                ctx.state.addChange(GameStateChange.AddBlueprint(blueprint))
            }
        }

        ctx.runStateChangingAction {
            check(ctx.state.blueprints.indexOf(blueprint) >= 0)
            ctx.state.addChange(GameStateChange.Build(blueprint, free = true))
            ctx.viewStack.popPast { view -> view is AdminMenuView }
        }
        return true
    }
}


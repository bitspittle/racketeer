package dev.bitspittle.racketeer.console.command.commands.buildings

import com.varabyte.kotter.runtime.render.RenderScope
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.utils.runStateChangingAction
import dev.bitspittle.racketeer.model.building.Blueprint
import dev.bitspittle.racketeer.model.game.GameProperty
import dev.bitspittle.racketeer.model.game.GameStateChange

/**
 * A no-op command used when read-only viewing a blueprint
 */
class BuildBlueprintCommand(ctx: GameContext, private val blueprint: Blueprint) : Command(ctx) {

    override val type = if (blueprint.canAffordBuildCost(ctx)) Type.Normal else Type.Disabled

    override val title = ctx.describer.describeBlueprintTitle(blueprint)
    override val description = ctx.describer.describeBlueprintBody(blueprint)
    override val extra = ctx.describer.describeBuildCost(blueprint)

    override suspend fun invoke(): Boolean {
        ctx.runStateChangingAction {
            ctx.state.apply(GameStateChange.Build(blueprint))

            if (ctx.state.blueprints.isEmpty()) ctx.viewStack.popView()
        }

        return true
    }

    override fun renderContentLowerInto(scope: RenderScope) {
        renderContentLowerInto(scope, blueprint)
    }
}

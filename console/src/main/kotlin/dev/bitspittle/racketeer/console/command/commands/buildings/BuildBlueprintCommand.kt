package dev.bitspittle.racketeer.console.command.commands.buildings

import com.varabyte.kotter.foundation.text.textLine
import com.varabyte.kotter.foundation.text.yellow
import com.varabyte.kotter.runtime.render.RenderScope
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.utils.runStateChangingAction
import dev.bitspittle.racketeer.model.game.GameProperty
import dev.bitspittle.racketeer.model.game.GameStateChange
import dev.bitspittle.racketeer.model.building.Blueprint

/**
 * A no-op command used when read-only viewing a blueprint
 */
class BuildBlueprintCommand(ctx: GameContext, private val blueprint: Blueprint) : Command(ctx) {
    private val shouldMarkNew get() = !ctx.userStats.buildings.contains(blueprint.name)

    override val type = if (blueprint.canAffordBuildCost(ctx)) Type.Normal else Type.Disabled

    override val title = ctx.describer.describeBlueprintTitle(blueprint)
    override val description = ctx.describer.describeBlueprintBody(blueprint)
    override val extra = ctx.describer.describeBuildCost(blueprint)

    override suspend fun invoke(): Boolean {
        ctx.runStateChangingAction {
            if (blueprint.buildCost.cash > 0) {
                ctx.state.apply(GameStateChange.AddGameAmount(GameProperty.CASH, -blueprint.buildCost.cash))
            }
            if (blueprint.buildCost.influence > 0) {
                ctx.state.apply(GameStateChange.AddGameAmount(GameProperty.INFLUENCE, -blueprint.buildCost.influence))
            }
            ctx.state.apply(GameStateChange.Build(blueprint))

            if (ctx.state.blueprints.isEmpty()) ctx.viewStack.popView()
        }

        return true
    }

    override fun renderContentLowerInto(scope: RenderScope) {
        scope.apply {
            if (shouldMarkNew) {
                yellow { textLine("You have never built this building before.") }
                textLine()
            }
        }
    }
}

package dev.bitspittle.racketeer.console.command.commands.buildings

import com.varabyte.kotter.foundation.text.textLine
import com.varabyte.kotter.foundation.text.yellow
import com.varabyte.kotter.runtime.render.RenderScope
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.utils.wrap
import dev.bitspittle.racketeer.model.building.Blueprint
import dev.bitspittle.racketeer.model.building.Building
import dev.bitspittle.racketeer.model.game.GameState
import dev.bitspittle.racketeer.model.text.Describer

fun Blueprint.canAffordBuildCost(ctx: GameContext) =
    ctx.state.cash >= this.buildCost.cash && ctx.state.influence >= this.buildCost.influence

fun Building.canAffordActivationCost(state: GameState) =
    state.cash >= blueprint.activationCost.cash &&
            state.influence >= blueprint.activationCost.influence &&
            state.luck >= blueprint.activationCost.luck

fun Building.renderCannotActivateReason(describer: Describer, state: GameState, scope: RenderScope) {
    val self = this
    scope.apply {
        if (!isActivated) {
            val msg = if (!state.canActivate(self) && blueprint.cannotActivateReason != null) {
                "This building can't be activated because ${describer.convertIcons(blueprint.cannotActivateReason!!)}."
            } else if (!self.canAffordActivationCost(state)) {
                "This building can't be activated because you can't afford the activation cost."
            } else null

            if (msg != null) {
                yellow { textLine(msg.wrap()) }
                textLine()
            }
        }
    }
}

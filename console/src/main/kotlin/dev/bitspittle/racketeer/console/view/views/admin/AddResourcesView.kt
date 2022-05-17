package dev.bitspittle.racketeer.console.view.views.admin

import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.utils.runStateChangingAction
import dev.bitspittle.racketeer.console.view.View
import dev.bitspittle.racketeer.model.game.GameProperty
import dev.bitspittle.racketeer.model.game.GameStateChange

private const val TINY_RESOURCE_INCREMENT = 1
private const val MINOR_RESOURCE_INCREMENT = 5
private const val MAJOR_RESOURCE_INCREMENT = 100

class AddResourcesView(ctx: GameContext) : View(ctx) {
    override fun createCommands(): List<Command> =
        listOf(
            object : Command(ctx) {
                override val type = Type.Warning
                override val title = "Add ${ctx.describer.describeCash(MINOR_RESOURCE_INCREMENT)}"
                override val description = "Increase game resources (cash)."

                override suspend fun invoke(): Boolean {
                    ctx.runStateChangingAction {
                        ctx.state.apply(GameStateChange.AddGameAmount(GameProperty.CASH, MINOR_RESOURCE_INCREMENT))
                    }
                    return true
                }
            },
            object : Command(ctx) {
                override val type = Type.Warning
                override val title = "Add ${ctx.describer.describeInfluence(MINOR_RESOURCE_INCREMENT)}"
                override val description = "Increase game resources (influence)."
                override suspend fun invoke(): Boolean {
                    ctx.runStateChangingAction {
                        ctx.state.apply(GameStateChange.AddGameAmount(GameProperty.INFLUENCE, MINOR_RESOURCE_INCREMENT))
                    }
                    return true
                }
            },
            object : Command(ctx) {
                override val type = Type.Warning
                override val title = "Add ${ctx.describer.describeLuck(TINY_RESOURCE_INCREMENT)}"
                override val description = "Increase game resources (luck)."
                override suspend fun invoke(): Boolean {
                    ctx.runStateChangingAction {
                        ctx.state.apply(GameStateChange.AddGameAmount(GameProperty.LUCK, TINY_RESOURCE_INCREMENT))
                    }
                    return true
                }
            },
            object : Command(ctx) {
                override val type = Type.Warning
                override val title = "Add ${ctx.describer.describeCash(MAJOR_RESOURCE_INCREMENT)} ${ctx.describer.describeInfluence(MAJOR_RESOURCE_INCREMENT)} ${ctx.describer.describeLuck(MAJOR_RESOURCE_INCREMENT)}"
                override val description = "Increase all game resources (cash, influence, and luck)."

                override suspend fun invoke(): Boolean {
                    ctx.runStateChangingAction {
                        ctx.state.apply(GameStateChange.AddGameAmount(GameProperty.CASH, MAJOR_RESOURCE_INCREMENT))
                        ctx.state.apply(GameStateChange.AddGameAmount(GameProperty.INFLUENCE, MAJOR_RESOURCE_INCREMENT))
                        ctx.state.apply(GameStateChange.AddGameAmount(GameProperty.LUCK, MAJOR_RESOURCE_INCREMENT))
                    }
                    return true
                }
            },
        )
}
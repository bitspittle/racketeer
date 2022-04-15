package dev.bitspittle.racketeer.console.view.views.admin

import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.view.View

private const val RESOURCE_INCREMENTS = 5

class AdminMenuView(ctx: GameContext) : View(ctx) {
    override val title: String = "Admin"

    override fun createCommands(): List<Command> =
        listOf(
            object : Command(ctx) {
                override val type = Type.Warning
                override val title = "Create a card"
                override val description: String = "Create any card in the game, putting it into your hand immediately."
                override suspend fun invoke(): Boolean {
                    ctx.viewStack.pushView(AdminCreateCardView(ctx))
                    return true
                }
            },
            object : Command(ctx) {
                override val type = Type.Warning
                override val title = "Move cards"
                override val description: String = "Move cards across piles."
                override suspend fun invoke(): Boolean {
                    ctx.viewStack.pushView(ChoosePileFromView(ctx))
                    return true
                }
            },
            object : Command(ctx) {
                override val type = Type.Warning
                override val title = "Add ${ctx.describer.describeCash(RESOURCE_INCREMENTS)}"
                override val description = "Increase game resources (cash)."

                override suspend fun invoke(): Boolean {
                    ctx.state.cash += RESOURCE_INCREMENTS
                    return true
                }
            },
            object : Command(ctx) {
                override val type = Type.Warning
                override val title = "Add ${ctx.describer.describeInfluence(RESOURCE_INCREMENTS)}"
                override val description = "Increase game resources (influence)."
                override suspend fun invoke(): Boolean {
                    ctx.state.influence += RESOURCE_INCREMENTS
                    return true
                }
            },
            object : Command(ctx) {
                override val type = Type.Warning
                override val title = "Add ${ctx.describer.describeLuck(RESOURCE_INCREMENTS)}"
                override val description = "Increase game resources (luck)."
                override suspend fun invoke(): Boolean {
                    ctx.state.luck += RESOURCE_INCREMENTS
                    return true
                }
            },
        )
}
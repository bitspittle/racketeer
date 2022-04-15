package dev.bitspittle.racketeer.console.view.views.admin

import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.view.View

class AdminMenuView(ctx: GameContext) : View(ctx) {
    override val title: String = "Admin"

    override fun createCommands(): List<Command> =
        listOf(
            object : Command(ctx) {
                override val title = "Create a card"
                override val description: String = "Create any card in the game, putting it into your hand immediately."
                override suspend fun invoke(): Boolean {
                    ctx.viewStack.pushView(AdminCreateCardView(ctx))
                    return true
                }
            },
            object : Command(ctx) {
                override val title = "Move cards"
                override val description: String = "Move cards across piles."
                override suspend fun invoke(): Boolean {
                    ctx.viewStack.pushView(ChoosePileFromView(ctx))
                    return true
                }
            },
            object : Command(ctx) {
                override val title = "Add game resources"
                override val description = "Increase spendable game resources, e.g. cash, influence, and/or luck."

                override suspend fun invoke(): Boolean {
                    ctx.viewStack.pushView(AddResourcesView(ctx))
                    return true
                }
            },
        )
}
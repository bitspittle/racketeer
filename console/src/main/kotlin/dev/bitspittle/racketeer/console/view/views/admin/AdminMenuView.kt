package dev.bitspittle.racketeer.console.view.views.admin

import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.view.View

class AdminMenuView(ctx: GameContext) : View(ctx) {
    override val title: String = "ADMIN MENU"

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
            }
        )
}
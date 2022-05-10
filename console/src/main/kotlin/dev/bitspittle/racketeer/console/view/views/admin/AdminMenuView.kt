package dev.bitspittle.racketeer.console.view.views.admin

import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.command.commands.system.UserData
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.view.views.game.GameView
import dev.bitspittle.racketeer.console.view.views.system.LoadGameView
import dev.bitspittle.racketeer.console.view.views.system.SaveGameView
import dev.bitspittle.racketeer.console.view.views.system.SettingsView

class AdminMenuView(ctx: GameContext) : GameView(ctx) {
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
                override val title = "Add game resources"
                override val description = "Increase spendable game resources, e.g. cash, influence, and/or luck."

                override suspend fun invoke(): Boolean {
                    ctx.viewStack.pushView(AddResourcesView(ctx))
                    return true
                }
            },
            object : Command(ctx) {
                override val title = "Admin Settings"
                override val description: String = "Modify admin settings"
                override suspend fun invoke(): Boolean {
                    ctx.viewStack.pushView(SettingsView(ctx, listOf(SettingsView.Category.ADMIN)))
                    return true
                }
            },
            object : Command(ctx) {
                override val title = "Save game"
                override val description: String = "Save this game into the desired save slot."
                override suspend fun invoke(): Boolean {
                    ctx.viewStack.pushView(SaveGameView(ctx))
                    return true
                }
            },
            object : Command(ctx) {
                override val type get() = if (ctx.app.userData.firstFreeSlot() > 0) Type.Warning else Type.Disabled
                override val title = "Load game"
                override val description: String = "Load a game. This will interrupt your current game!"
                override suspend fun invoke(): Boolean {
                    ctx.viewStack.pushView(LoadGameView(ctx))
                    return true
                }
            },
            object : Command(ctx) {
                override val type = Type.Accented
                override val title = "Open scripting console"
                override val description: String = "Run scripts live against the current game state."
                override suspend fun invoke(): Boolean {
                    ctx.viewStack.pushView(ScriptingView(ctx))
                    return true
                }
            },
            )
}
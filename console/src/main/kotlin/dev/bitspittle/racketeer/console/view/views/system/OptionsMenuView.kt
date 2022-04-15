package dev.bitspittle.racketeer.console.view.views.system

import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.view.View
import dev.bitspittle.racketeer.console.view.views.game.BrowseAllCardsView

class OptionsMenuView(ctx: GameContext) : View(ctx) {
    override val title: String = "Options"

    override fun createCommands(): List<Command> =
        listOf(
            object : Command(ctx) {
                override val title = "Browse all cards"
                override val description: String = "Look through all the cards in the game and see what you've used."
                override suspend fun invoke(): Boolean {
                    ctx.viewStack.pushView(BrowseAllCardsView(ctx))
                    return true
                }
            },
            object : Command(ctx) {
                override val type = Type.Warning
                override val title = "Restart a new game"
                override val description: String = "End this game and start a new one. You will have a chance to confirm."
                override suspend fun invoke(): Boolean {
                    ctx.viewStack.pushView(ConfirmNewGameView(ctx))
                    return true
                }
            },
            object : Command(ctx) {
                override val type = Type.Warning
                override val title = "Quit"
                override val description: String = "End this game and quit the program. You will have a chance to confirm."
                override suspend fun invoke(): Boolean {
                    ctx.viewStack.pushView(ConfirmQuitView(ctx))
                    return true
                }
            }
        )
}
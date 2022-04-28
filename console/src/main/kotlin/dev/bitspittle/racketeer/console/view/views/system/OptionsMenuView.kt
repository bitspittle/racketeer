package dev.bitspittle.racketeer.console.view.views.system

import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.view.views.game.GameView
import dev.bitspittle.racketeer.console.view.views.game.BrowseAllCardsView

class OptionsMenuView(ctx: GameContext) : GameView(ctx) {
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
                override val title = "Restart"
                override val description: String = "End this game and start a new one. You will have one last chance to confirm."
                override suspend fun invoke(): Boolean {
                    ctx.viewStack.pushView(ConfirmRestartView(ctx))
                    return true
                }
            },
            object : Command(ctx) {
                override val type = Type.Normal
                override val title = "Quick save & exit"
                override val description: String = "End this game and quit the program. You will have one last chance to confirm."
                override suspend fun invoke(): Boolean {
                    ctx.viewStack.pushView(ConfirmQuitView(ctx))
                    return true
                }
            }
        )
}
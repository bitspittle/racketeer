package dev.bitspittle.racketeer.console.view.views.system

import com.varabyte.kotter.foundation.text.magenta
import com.varabyte.kotter.foundation.text.textLine
import com.varabyte.kotter.runtime.render.RenderScope
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.command.commands.system.CardListCommand
import dev.bitspittle.racketeer.console.command.commands.system.UserDataCommand
import dev.bitspittle.racketeer.console.command.commands.system.playtestId
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.view.views.game.GameView
import dev.bitspittle.racketeer.model.game.hasGameStarted
import dev.bitspittle.racketeer.model.game.isGameInProgress
import dev.bitspittle.racketeer.model.game.isGameOver

class OptionsMenuView(ctx: GameContext) : GameView(ctx) {
    override val title: String = "Options"

    override fun createCommands(): List<Command> =
        listOf(
            CardListCommand(ctx),
            UserDataCommand(ctx),
            object : Command(ctx) {
                override val type = if (ctx.state.isGameInProgress) Type.Warning else Type.Hidden
                override val title = "Restart"
                override val description: String = "End this game and start a new one. You will have one last chance to confirm."
                override suspend fun invoke(): Boolean {
                    ctx.viewStack.pushView(ConfirmRestartView(ctx))
                    return true
                }
            },
            object : Command(ctx) {
                override val type = if (ctx.state.isGameInProgress) Type.Normal else Type.Hidden
                override val title = "Quick save & exit"
                override val description: String = "End this game and quit the program. You will have one last chance to confirm."
                override suspend fun invoke(): Boolean {
                    ctx.viewStack.pushView(ConfirmQuitView(ctx))
                    return true
                }
            },
            object : Command(ctx) {
                override val type = if (!ctx.state.isGameInProgress) Type.Normal else Type.Hidden
                override val title = "Quit"
                override val description: String = "Quit the program. It is safe to do at this moment because no game is currently in session."
                override suspend fun invoke(): Boolean {
                    ctx.app.quit()
                    return true
                }
            }
        )

    override fun RenderScope.renderLowerFooter() {
        textLine()
        magenta { textLine("Playtest ID: ${ctx.app.userData.playtestId}") }
    }
}
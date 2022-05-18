package dev.bitspittle.racketeer.console.view.views.system

import com.varabyte.kotter.foundation.input.CharKey
import com.varabyte.kotter.foundation.input.Key
import com.varabyte.kotter.foundation.text.magenta
import com.varabyte.kotter.foundation.text.textLine
import com.varabyte.kotter.runtime.render.RenderScope
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.command.commands.system.CardListCommand
import dev.bitspittle.racketeer.console.command.commands.system.UserDataCommand
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.game.playtestId
import dev.bitspittle.racketeer.console.game.version
import dev.bitspittle.racketeer.console.user.saveInto
import dev.bitspittle.racketeer.console.view.View
import dev.bitspittle.racketeer.model.game.isGameInProgress

class OptionsMenuView(ctx: GameContext) : View(ctx) {
    override val title: String = "Options"

    private var secretCode = ""

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

    override suspend fun handleAdditionalKeys(key: Key): Boolean {
        if (key is CharKey) {
            secretCode += key.code

            if (secretCode.equals("admin", ignoreCase = true)) {
                if (!ctx.settings.admin.enabled) {
                    ctx.settings.admin.enabled = true
                    ctx.settings.saveInto(ctx.app.userDataDir)
                    ctx.app.logger.info("You now have admin privileges!")
                    secretCode = ""
                    return true
                }
            }
            else if (secretCode.equals("noadmin", ignoreCase = true)) {
                if (ctx.settings.admin.enabled) {
                    ctx.settings.admin.enabled = false
                    ctx.settings.saveInto(ctx.app.userDataDir)
                    ctx.app.logger.info("You have removed your admin privileges.")
                    secretCode = ""
                    return true
                }
            }
        } else {
            secretCode = ""
        }
        return false
    }

    override fun RenderScope.renderFooterLower() {
        textLine()
        magenta {
            textLine("${ctx.data.title} v${ctx.app.version}")
            textLine("Playtest ID: ${ctx.app.playtestId}")
        }
    }
}
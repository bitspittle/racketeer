package dev.bitspittle.racketeer.console.view.views.system

import com.varabyte.kotter.foundation.text.textLine
import com.varabyte.kotter.runtime.render.RenderScope
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.view.View
import java.awt.Desktop
import kotlin.io.path.absolutePathString

class UserDataView(ctx: GameContext) : View(ctx) {
    override fun createCommands(): List<Command> = listOf(
        object : Command(ctx) {
            override val title = "Show user data"
            override val description: String =
                "Open a file browser window to the root folder containing your user data."

            override suspend fun invoke(): Boolean {
                Desktop.getDesktop().browse(ctx.app.userDataDir.path.toUri())
                return true
            }
        },
        object : Command(ctx) {
            override val type: Type = Type.Warning
            override val title = "Wipe out user data"
            override val description: String = "Completely clear out all saved user data. You will have one last chance to confirm."

            override suspend fun invoke(): Boolean {
                ctx.viewStack.pushView(ConfirmDeleteUserDataView(ctx))
                return true
            }
        }
    )

    override fun RenderScope.renderFooterUpper() {
        textLine("Your user data is located at: ${ctx.app.userDataDir.path.absolutePathString()}")
        textLine()
    }
}
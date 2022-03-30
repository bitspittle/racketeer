package dev.bitspittle.racketeer.console.view.views

import com.varabyte.kotter.foundation.text.bold
import com.varabyte.kotter.foundation.text.cyan
import com.varabyte.kotter.foundation.text.text
import com.varabyte.kotter.foundation.text.textLine
import com.varabyte.kotter.runtime.render.RenderScope
import com.varabyte.kotterx.decorations.bordered
import dev.bitspittle.racketeer.console.GameContext
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.command.commands.*
import dev.bitspittle.racketeer.console.view.View

class GameOverView(ctx: GameContext) : View(ctx) {
    override val allowQuit = false

    override val commands: List<Command> =
        listOf(
            NewGameCommand(ctx),
            object : Command(ctx) {
                override val title = "Exit"
                override fun invoke() {
                    ctx.quit()
                }
            }
        )

    override fun RenderScope.renderContent() {
        textLine("You ended the game with ${ctx.state.vp} victory points, to earn a ranking of: ")
        textLine()
        bold { textLine(" D") }
        textLine()

        text("Press "); cyan { text("New Game") }; text(" to play again or "); cyan { text("Exit") }; textLine(" to quit.")
        textLine()
    }
}
package dev.bitspittle.racketeer.console.view.views

import com.varabyte.kotter.foundation.text.bold
import com.varabyte.kotter.foundation.text.cyan
import com.varabyte.kotter.foundation.text.text
import com.varabyte.kotter.foundation.text.textLine
import com.varabyte.kotter.runtime.render.RenderScope
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.command.commands.NewGameCommand
import dev.bitspittle.racketeer.console.view.View
import dev.bitspittle.racketeer.model.game.Rating

class GameOverView(ctx: GameContext) : View(ctx) {
    override val allowQuit = false

    override val commands: List<Command> =
        listOf(
            NewGameCommand(ctx),
            object : Command(ctx) {
                override val title = "Exit"
                override fun invoke(): Boolean {
                    ctx.app.quit()
                    return true
                }
            }
        )

    override fun RenderScope.renderContent() {
        textLine("You ended the game with ${ctx.data.icons.vp} ${ctx.state.vp}, to earn a ranking of: ")
        textLine()
        bold { textLine(" ${Rating.from(ctx.data, ctx.state.vp)}") }
        textLine()

        text("Press "); cyan { text("New Game") }; text(" to play again or "); cyan { text("Exit") }; textLine(" to quit.")
        textLine()
    }
}
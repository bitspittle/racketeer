package dev.bitspittle.racketeer.console.view.views.game

import com.varabyte.kotter.foundation.text.bold
import com.varabyte.kotter.foundation.text.cyan
import com.varabyte.kotter.foundation.text.text
import com.varabyte.kotter.foundation.text.textLine
import com.varabyte.kotter.runtime.MainRenderScope
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.command.commands.system.NewGameCommand
import dev.bitspittle.racketeer.console.command.commands.system.UserDataSupport
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.user.save
import dev.bitspittle.racketeer.model.game.from
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText

class GameSummaryView(ctx: GameContext) : GameView(ctx) {
    init {
        ctx.cardStats.values.save()
    }

    override fun createCommands(): List<Command> =
        listOf(
            NewGameCommand(ctx),
            object : Command(ctx) {
                override val type = Type.Normal

                override val title = "Exit"
                override suspend fun invoke(): Boolean {
                    ctx.app.quit()
                    return true
                }
            }
        )

    override fun MainRenderScope.renderContentUpper() {
        textLine("You ended the game with ${ctx.describer.describeVictoryPoints(ctx.state.vp)}, to earn a ranking of: ")
        textLine()
        bold { textLine(" ${ctx.data.rankings.from(ctx.state.vp).name}") }
        textLine()

        text("Press "); cyan { text("New Game") }; text(" to play again or "); cyan { text("Exit") }; textLine(" to quit.")
        textLine()
    }
}
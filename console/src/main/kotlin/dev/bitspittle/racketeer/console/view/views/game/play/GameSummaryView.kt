package dev.bitspittle.racketeer.console.view.views.game.play

import com.varabyte.kotter.foundation.text.bold
import com.varabyte.kotter.foundation.text.cyan
import com.varabyte.kotter.foundation.text.text
import com.varabyte.kotter.foundation.text.textLine
import com.varabyte.kotter.runtime.MainRenderScope
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.command.commands.system.NewGameCommand
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.game.playtestId
import dev.bitspittle.racketeer.console.game.version
import dev.bitspittle.racketeer.console.user.saveInto
import dev.bitspittle.racketeer.console.utils.UploadService
import dev.bitspittle.racketeer.console.utils.encodeToYaml
import dev.bitspittle.racketeer.console.view.View
import dev.bitspittle.racketeer.model.game.from
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class GameSummaryView(ctx: GameContext) : View(ctx) {
    init {
        ctx.cardStats.values.saveInto(ctx.app.userDataDir)

        // Admins might be playing with broken in progress cards. Don't save their data!
        if (!ctx.settings.admin.enabled) {
            ctx.app.uploadService.upload(
                buildString {
                    append("versions:${ctx.app.version}:")
                    append("users:${ctx.app.playtestId}:")
                    val utcNow =
                        Instant.now().atOffset(ZoneOffset.UTC)
                            .format(DateTimeFormatter.ofPattern("MM-dd-yyyy|HH:mm:ss"))
                    append("endstates:$utcNow:")
                    append("vp:${ctx.state.vp}")
                    append(".yaml")
                },
                UploadService.MimeTypes.YAML
            ) { ctx.encodeToYaml() }
        }
    }

    override fun createCommands(): List<Command> =
        listOf(
            NewGameCommand(ctx),
            object : Command(ctx) {
                override val type = Type.Normal
                override val description = "Thank you for playing!"
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
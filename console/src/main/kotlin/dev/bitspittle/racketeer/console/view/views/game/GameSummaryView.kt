package dev.bitspittle.racketeer.console.view.views.game

import com.varabyte.kotter.foundation.text.bold
import com.varabyte.kotter.foundation.text.cyan
import com.varabyte.kotter.foundation.text.text
import com.varabyte.kotter.foundation.text.textLine
import com.varabyte.kotter.runtime.MainRenderScope
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.command.commands.system.NewGameCommand
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.user.save
import dev.bitspittle.racketeer.model.game.from
import dev.bitspittle.racketeer.model.snapshot.GameSnapshot
import net.mamoe.yamlkt.Yaml
import java.nio.file.Paths
import kotlin.io.path.absolutePathString
import kotlin.io.path.createDirectories
import kotlin.io.path.name
import kotlin.io.path.writeText

class GameSummaryView(ctx: GameContext) : GameView(ctx) {
    init {
        ctx.cardStats.values.save(ctx.app.userData)

        val endstates = ctx.app.userData.pathForEndStates().also { it.createDirectories() }
        val endstate = endstates.resolve("${System.currentTimeMillis()}.yaml")
        val payload = Yaml.encodeToString(
            GameSnapshot.from(
                ctx.state,
                isPreDraw = false
            )
        )

        // Write it to disk so the user can see what we're doing / they can send files to us as a backup if
        // auto-uploading files
        endstate.writeText(payload)

        // We need a semi-permanent ID that won't change across playruns. Online resources recommend MAC addresses but
        // no guarantee that won't crash the user if they're offline or some other reason (it crashed me!). So, just
        // use the current working directory. Should be good enough for our needs!
        val machineHash = Paths.get(".").absolutePathString().hashCode().toUInt()
        ctx.app.uploadService.upload("users:$machineHash:endstates:${endstate.name}", endstate)
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
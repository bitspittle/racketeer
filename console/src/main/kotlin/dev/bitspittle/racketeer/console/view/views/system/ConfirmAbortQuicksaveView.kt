package dev.bitspittle.racketeer.console.view.views.system

import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.command.commands.system.NewGameCommand
import dev.bitspittle.racketeer.console.command.commands.system.UserDataDir.Companion.QUICKSAVE_SLOT
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.game.playtestId
import dev.bitspittle.racketeer.console.game.version
import dev.bitspittle.racketeer.console.user.GameCancelReason
import dev.bitspittle.racketeer.console.user.GameStats
import dev.bitspittle.racketeer.console.user.saveInto
import dev.bitspittle.racketeer.console.utils.CloudFileService
import dev.bitspittle.racketeer.console.utils.encodeToYaml
import dev.bitspittle.racketeer.console.view.View
import dev.bitspittle.racketeer.model.game.stub
import dev.bitspittle.racketeer.model.serialization.GameSnapshot
import net.mamoe.yamlkt.Yaml
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlin.io.path.readText

class ConfirmAbortQuicksaveView(ctx: GameContext) : View(ctx) {
    override val showUpdateMessage = true // Let the user know there's a new version BEFORE they start a new game

    override fun createCommands(): List<Command> = listOf(
        object : Command(ctx) {
            override val type = Type.Warning
            override val title = "Confirm"

            override val description = "Once you confirm, the existing quick save from your last game will be deleted. If you don't want this to happen, go back!"

            override suspend fun invoke(): Boolean {
                try {
                    // Grab the game that the person aborted, saving info about it before discarding it forever
                    val path = ctx.app.userDataDir.pathForSlot(QUICKSAVE_SLOT)
                    val snapshot = Yaml.decodeFromString(GameSnapshot.serializer(), path.readText())
                    snapshot.create(ctx.data, ctx.env, ctx.enqueuers) { state ->
                        ctx.state = state
                    }

                    // The user restarted. Why? Let's send some data, maybe we can find out.
                    if (!ctx.settings.admin.enabled) {
                        ctx.app.cloudFileService.upload(
                            buildString {
                                append("versions:${ctx.app.version}:")
                                append("users:${ctx.app.playtestId}:")
                                val utcNow =
                                    Instant.now().atOffset(ZoneOffset.UTC)
                                        .format(DateTimeFormatter.ofPattern("MM-dd-yyyy|HH:mm:ss"))
                                append("aborts:$utcNow:")
                                append("turn:${ctx.state.turn}:vp:${ctx.state.vp}")
                                append(".yaml")
                            },
                            CloudFileService.MimeTypes.YAML
                        ) { ctx.encodeToYaml() }
                    }

                    ctx.userStats.games.add(GameStats.from(ctx.state, GameCancelReason.ABORTED))
                    ctx.userStats.games.saveInto(ctx.app.userDataDir)
                } catch (ex: Exception) {
                    // It's possible the file format changed or something between versions. Oh well, we'll lose this
                    // one!
                } finally {
                    // Clear state just to make sure we don't leak the aborted quicksave game
                    ctx.state = ctx.state.stub()
                }

                return NewGameCommand(ctx).invoke()
            }
        }
    )
}
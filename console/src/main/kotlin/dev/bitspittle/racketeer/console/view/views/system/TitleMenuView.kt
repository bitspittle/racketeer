package dev.bitspittle.racketeer.console.view.views.system

import com.varabyte.kotter.foundation.text.*
import com.varabyte.kotter.runtime.MainRenderScope
import com.varabyte.kotter.runtime.render.RenderScope
import com.varabyte.kotterx.decorations.BorderCharacters
import com.varabyte.kotterx.decorations.bordered
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.command.commands.system.BuildingListCommand
import dev.bitspittle.racketeer.console.command.commands.system.CardListCommand
import dev.bitspittle.racketeer.console.command.commands.system.ConfirmLoadCommand
import dev.bitspittle.racketeer.console.command.commands.system.UserDataCommand
import dev.bitspittle.racketeer.console.command.commands.system.UserDataDir.Companion.QUICKSAVE_SLOT
import dev.bitspittle.racketeer.console.command.commands.system.unlock.UnlockListCommand
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.game.playtestId
import dev.bitspittle.racketeer.console.game.version
import dev.bitspittle.racketeer.console.view.View
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists

class TitleMenuView(ctx: GameContext) : View(ctx) {
    override val allowEsc = false // No options access from title screen
    override val showUpdateMessage = true // Grab people's attention when they are starting a new game

    override fun MainRenderScope.renderContentUpper() {
        bold {
            red {
                bordered(borderCharacters = BorderCharacters.CURVED, paddingLeftRight = 1) {
                    textLine(ctx.data.title)
                    rgb(150, 0, 0) { textLine("v" + ctx.app.version) }
                }
            }
        }

        textLine()
    }

    override fun createCommands(): List<Command> =
        listOf(
            object : Command(ctx) {
                override val type = if (ctx.app.userDataDir.pathForSlot(QUICKSAVE_SLOT).exists()) Type.Normal else Type.Accented
                override val title = "New Game"
                override val description = "Command your cronies, expand your turf, and become the most powerful crime boss in the city. Start a new game!"
                override suspend fun invoke(): Boolean {
                    if (!ctx.app.userDataDir.pathForSlot(QUICKSAVE_SLOT).exists()) {
                        ChooseFeaturesView.enter(ctx)
                    } else {
                        ctx.viewStack.pushView(ConfirmAbortQuicksaveView(ctx))
                    }
                    return true
                }
            },
            object : Command(ctx) {
                override val type = if (ctx.app.userDataDir.pathForSlot(QUICKSAVE_SLOT).exists()) Type.Accented else Type.Hidden
                override val title = "Restore"
                override val description = "Resume a game that was previously started but not finished."

                override suspend fun invoke(): Boolean {
                    return if (ConfirmLoadCommand(ctx, QUICKSAVE_SLOT).invoke()) {
                        ctx.app.userDataDir.pathForSlot(QUICKSAVE_SLOT).deleteIfExists()
                        true
                    } else {
                        false
                    }
                }
            },
            object : Command(ctx) {
                override val type = if (ctx.app.userDataDir.firstFreeSlot() > 0) Type.Normal else Type.Hidden
                override val title = "Load Game"
                override val description = "Load previously saved game data."
                override suspend fun invoke(): Boolean {
                    ctx.viewStack.pushView(LoadGameView(ctx))
                    return true
                }
            },
            CardListCommand(ctx),
            BuildingListCommand(ctx),
            UnlockListCommand(ctx),
            UserDataCommand(ctx),
            object : Command(ctx) {
                override val title = "Quit"
                override val description = "Actually, I'm feeling scared. Maybe this crime stuff isn't for me..."
                override suspend fun invoke(): Boolean {
                    ctx.app.quit()
                    return true
                }
            }
        )

    override fun RenderScope.renderFooterLower() {
        textLine()
        magenta { textLine("Playtest ID: ${ctx.app.playtestId}") }
    }
}
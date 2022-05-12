package dev.bitspittle.racketeer.console.view.views.system

import com.varabyte.kotter.foundation.text.*
import com.varabyte.kotter.runtime.render.RenderScope
import com.varabyte.kotterx.decorations.BorderCharacters
import com.varabyte.kotterx.decorations.bordered
import dev.bitspittle.limp.Environment
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.command.commands.system.CardListCommand
import dev.bitspittle.racketeer.console.command.commands.system.ConfirmLoadCommand
import dev.bitspittle.racketeer.console.command.commands.system.UserDataCommand
import dev.bitspittle.racketeer.console.command.commands.system.UserData.Companion.QUICKSAVE_SLOT
import dev.bitspittle.racketeer.console.command.commands.system.playtestId
import dev.bitspittle.racketeer.console.game.App
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.game.version
import dev.bitspittle.racketeer.console.user.CardStats
import dev.bitspittle.racketeer.console.user.Settings
import dev.bitspittle.racketeer.console.user.inAdminModeAndShowDebugInfo
import dev.bitspittle.racketeer.console.view.View
import dev.bitspittle.racketeer.console.view.ViewStack
import dev.bitspittle.racketeer.console.view.views.game.play.PreDrawView
import dev.bitspittle.racketeer.model.action.ActionQueue
import dev.bitspittle.racketeer.model.action.ExprCache
import dev.bitspittle.racketeer.model.game.GameData
import dev.bitspittle.racketeer.model.game.MutableGameState
import dev.bitspittle.racketeer.model.text.Describer
import dev.bitspittle.racketeer.scripting.types.CardEnqueuerImpl
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists

class TitleMenuView(
    data: GameData,
    exprCache: ExprCache,
    settings: Settings,
    cardStats: Iterable<CardStats>,
    app: App,
    viewStack: ViewStack,
    env: Environment,
) : View(settings, viewStack, app) {
    val ctx = run {
        @Suppress("NAME_SHADOWING")
        val cardStats = cardStats.associateBy { it.name }.toMutableMap()

        val actionQueue = ActionQueue()
        val cardEnqueuer = CardEnqueuerImpl(env, exprCache, actionQueue)

        GameContext(
            data,
            settings,
            cardStats,
            Describer(data, showDebugInfo = { settings.inAdminModeAndShowDebugInfo }),
            MutableGameState(data, exprCache, actionQueue, cardEnqueuer),
            env,
            exprCache,
            actionQueue,
            cardEnqueuer,
            viewStack,
            app
        )
    }

    override fun RenderScope.renderHeader() {
        textLine()

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
                override val type = if (ctx.app.userData.pathForSlot(QUICKSAVE_SLOT).exists()) Type.Normal else Type.Accented
                override val title = "New Game"
                override val description = "Command your cronies, expand your turf, and become the most powerful crime boss in the city. Start a new game!"
                override suspend fun invoke(): Boolean {
                    if (!ctx.app.userData.pathForSlot(QUICKSAVE_SLOT).exists()) {
                        ctx.viewStack.replaceView(PreDrawView(ctx))
                    } else {
                        ctx.viewStack.pushView(ConfirmNewGameView(ctx))
                    }
                    return true
                }
            },
            object : Command(ctx) {
                override val type = if (ctx.app.userData.pathForSlot(QUICKSAVE_SLOT).exists()) Type.Accented else Type.Hidden
                override val title = "Restore"
                override val description = "Resume a game that was previously started but not finished."

                override suspend fun invoke(): Boolean {
                    return if (ConfirmLoadCommand(ctx, QUICKSAVE_SLOT).invoke()) {
                        ctx.app.userData.pathForSlot(QUICKSAVE_SLOT).deleteIfExists()
                        true
                    } else {
                        false
                    }
                }
            },
            object : Command(ctx) {
                override val type = if (ctx.app.userData.firstFreeSlot() > 0) Type.Normal else Type.Hidden
                override val title = "Load Game"
                override val description = "Load previously saved game data."
                override suspend fun invoke(): Boolean {
                    ctx.viewStack.pushView(LoadGameView(ctx))
                    return true
                }
            },
            CardListCommand(ctx),
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

    override fun RenderScope.renderFooter() {
        magenta { textLine("Playtest ID: ${ctx.app.userData.playtestId}") }
    }
}
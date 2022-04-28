package dev.bitspittle.racketeer.console.view.views.system

import com.varabyte.kotter.foundation.text.bold
import com.varabyte.kotter.foundation.text.red
import com.varabyte.kotter.foundation.text.textLine
import com.varabyte.kotter.runtime.render.RenderScope
import com.varabyte.kotterx.decorations.BorderCharacters
import com.varabyte.kotterx.decorations.bordered
import dev.bitspittle.limp.Environment
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.command.commands.system.LoadGameCommand
import dev.bitspittle.racketeer.console.command.commands.system.SerializationSupport
import dev.bitspittle.racketeer.console.command.commands.system.SerializationSupport.QUICKSAVE_SLOT
import dev.bitspittle.racketeer.console.game.App
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.view.View
import dev.bitspittle.racketeer.console.view.ViewStack
import dev.bitspittle.racketeer.console.view.views.game.PreDrawView
import dev.bitspittle.racketeer.model.card.CardQueue
import dev.bitspittle.racketeer.model.game.GameData
import dev.bitspittle.racketeer.model.game.GameState
import dev.bitspittle.racketeer.model.random.CopyableRandom
import dev.bitspittle.racketeer.model.text.Describer
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists

class TitleMenuView(
    data: GameData,
    app: App,
    viewStack: ViewStack,
    env: Environment,
    cardQueue: CardQueue,
    random: CopyableRandom
) : View(viewStack, app) {
    val ctx = GameContext(
        data,
        Describer(data),
        GameState(data, cardQueue, random),
        env,
        cardQueue,
        viewStack,
        app
    )

    override fun RenderScope.renderHeader() {
        textLine()

        bold {
            red {
                bordered(borderCharacters = BorderCharacters.CURVED, paddingLeftRight = 1) {
                    textLine(ctx.data.title)
                }
            }
        }

        textLine()
    }

    override fun createCommands(): List<Command> =
        listOf(
            object : Command(ctx) {
                override val type = Type.Normal
                override val title = "New Game"
                override val description = "Command your cronies, expand your turf, and become the most powerful crime boss in the city. Start a new game!"
                override suspend fun invoke(): Boolean {
                    if (!SerializationSupport.pathForSlot(QUICKSAVE_SLOT).exists()) {
                        ctx.viewStack.replaceView(PreDrawView(ctx))
                    } else {
                        ctx.viewStack.pushView(ConfirmNewGameView(ctx))
                    }
                    return true
                }
            },
            object : Command(ctx) {
                override val type = if (SerializationSupport.pathForSlot(QUICKSAVE_SLOT).exists()) Type.Accented else Type.Hidden
                override val title = "Restore"
                override val description = "Resume a game that was previously started but not finished."

                override suspend fun invoke(): Boolean {
                    return if (LoadGameCommand(ctx, QUICKSAVE_SLOT).invoke()) {
                        SerializationSupport.pathForSlot(QUICKSAVE_SLOT).deleteIfExists()
                        true
                    } else {
                        false
                    }
                }
            },
            object : Command(ctx) {
                override val type = if (SerializationSupport.firstFreeSlot() > 0) Type.Normal else Type.Hidden
                override val title = "Load Game"
                override val description = "Load previously saved game data."
                override suspend fun invoke(): Boolean {
                    ctx.viewStack.pushView(LoadGameView(ctx))
                    return true
                }
            },
            object : Command(ctx) {
                override val title = "Quit"
                override val description = "Actually, I'm feeling scared. Maybe this crime stuff isn't for me..."
                override suspend fun invoke(): Boolean {
                    ctx.app.quit()
                    return true
                }
            }
        )
}
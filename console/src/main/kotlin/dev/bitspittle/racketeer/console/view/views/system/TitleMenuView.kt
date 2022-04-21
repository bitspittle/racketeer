package dev.bitspittle.racketeer.console.view.views.system

import com.varabyte.kotter.foundation.text.bold
import com.varabyte.kotter.foundation.text.red
import com.varabyte.kotter.foundation.text.textLine
import com.varabyte.kotter.runtime.render.RenderScope
import com.varabyte.kotterx.decorations.BorderCharacters
import com.varabyte.kotterx.decorations.bordered
import dev.bitspittle.limp.Environment
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.game.App
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.view.View
import dev.bitspittle.racketeer.console.view.ViewStack
import dev.bitspittle.racketeer.console.view.views.game.PreDrawView
import dev.bitspittle.racketeer.model.card.CardQueue
import dev.bitspittle.racketeer.model.game.GameData
import dev.bitspittle.racketeer.model.game.GameState
import dev.bitspittle.racketeer.model.random.CloneableRandom
import dev.bitspittle.racketeer.model.text.Describer
import kotlin.random.Random

class TitleMenuView(
    data: GameData,
    app: App,
    viewStack: ViewStack,
    env: Environment,
    cardQueue: CardQueue,
    random: CloneableRandom
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
                override val title = "New Game"
                override suspend fun invoke(): Boolean {
                    ctx.viewStack.replaceView(PreDrawView(ctx))
                    return true
                }
            },
            object : Command(ctx) {
                override val type = Type.Warning
                override val title = "Quit"
                override suspend fun invoke(): Boolean {
                    ctx.app.quit()
                    return true
                }
            }
        )
}
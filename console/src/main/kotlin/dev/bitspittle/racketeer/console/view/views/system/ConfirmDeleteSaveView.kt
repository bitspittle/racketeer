package dev.bitspittle.racketeer.console.view.views.system

import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.view.View
import kotlin.io.path.deleteIfExists

class ConfirmDeleteSaveView(ctx: GameContext, private val slot: Int) : View(ctx) {
    override fun createCommands(): List<Command> = listOf(
        object : Command(ctx) {
            override val type = Type.Danger
            override val title = "Confirm"

            override val description = "Press ENTER if you're sure you want to delete the save data in slot #${slot + 1}. Otherwise, go back!"

            override suspend fun invoke(): Boolean {
                if (ctx.app.userDataDir.pathForSlot(slot).deleteIfExists()) {
                    ctx.app.logger.info("Slot #${slot + 1} deleted!")
                }
                ctx.viewStack.popView()
                return true
            }
        }
    )
}
package dev.bitspittle.racketeer.console.view.views.system.unlock

import com.varabyte.kotter.foundation.input.CharKey
import com.varabyte.kotter.foundation.input.Key
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.command.commands.system.unlock.UnlockSettingHandlers
import dev.bitspittle.racketeer.console.command.commands.system.unlock.ViewUnlockCommand
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.user.saveInto
import dev.bitspittle.racketeer.console.view.View

class UnlockListView(ctx: GameContext) : View(ctx) {
    private var secretCode = ""

    override val title = "Unlocks"

    override suspend fun handleAdditionalKeys(key: Key): Boolean {
        if (key is CharKey) {
            secretCode += key.code

            ctx.data.unlocks.forEach { unlock ->
                if (secretCode.equals(unlock.codename, ignoreCase = true)) {
                    UnlockSettingHandlers.instance[unlock.id]?.let { settingsHandler ->
                        if (!settingsHandler.get(ctx.settings.unlocks)) {
                            settingsHandler.set(ctx.settings.unlocks, true)
                            ctx.settings.saveInto(ctx.app.userDataDir)
                            ctx.app.logger.info("You have manually unlocked: ${unlock.name}")
                            secretCode = ""
                            refreshCommands()
                            return true
                        }
                    }
                }
            }
        } else {
            secretCode = ""
        }
        return false
    }

    override fun createCommands(): List<Command> = ctx.data.unlocks
        .map { unlock -> ViewUnlockCommand(ctx, unlock) }
}
package dev.bitspittle.racketeer.console.view.views.admin

import com.varabyte.kotter.foundation.input.Key
import com.varabyte.kotter.foundation.input.Keys
import com.varabyte.kotter.foundation.text.cyan
import com.varabyte.kotter.foundation.text.text
import com.varabyte.kotter.foundation.text.textLine
import com.varabyte.kotter.runtime.render.RenderScope
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.command.commands.game.SelectItemCommand
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.user.Settings
import dev.bitspittle.racketeer.console.user.save
import dev.bitspittle.racketeer.console.view.views.game.GameView

class AdminSettingsView(ctx: GameContext) : GameView(ctx) {
    private val debugInfoSetting = SelectItemCommand(
        ctx,
        "Show debug info",
        ctx.settings.showDebugInfo,
        "Set true to surface things like game code inside the UI"
    )

    override suspend fun handleAdditionalKeys(key: Key): Boolean {
        return when (key) {
            Keys.SPACE -> (currCommand as? SelectItemCommand)?.invoke()?.let { true } ?: false
            else -> false
        }
    }

    override fun RenderScope.renderUpperFooter() {
        if (currCommand is SelectItemCommand) {
            text("Press "); cyan { text("SPACE") }; textLine(" to toggle the selected setting.")
        }
    }

    private fun createNewSettings() = Settings().apply {
        setFrom(ctx.settings)
        showDebugInfo = debugInfoSetting.selected
    }

    override fun createCommands(): List<Command> = listOf(
        debugInfoSetting,
        object : Command(ctx) {
            override val type get() = if (createNewSettings() != ctx.settings) Type.Normal else Type.Disabled
            override val title: String = "Confirm"
            override val description = "Press ENTER to confirm the above choice(s)."

            override suspend fun invoke(): Boolean {
                ctx.settings.setFrom(createNewSettings())
                ctx.settings.save()
                ctx.app.logger.info("User settings updated and saved!")
                ctx.viewStack.popView()
                return true
            }
        }
    )
}

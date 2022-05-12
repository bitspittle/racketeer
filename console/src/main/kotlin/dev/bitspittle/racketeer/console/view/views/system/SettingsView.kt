package dev.bitspittle.racketeer.console.view.views.system

import com.varabyte.kotter.foundation.input.Key
import com.varabyte.kotter.foundation.input.Keys
import com.varabyte.kotter.foundation.text.cyan
import com.varabyte.kotter.foundation.text.text
import com.varabyte.kotter.foundation.text.textLine
import com.varabyte.kotter.runtime.render.RenderScope
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.command.commands.game.choose.SelectItemCommand
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.user.Settings
import dev.bitspittle.racketeer.console.user.save
import dev.bitspittle.racketeer.console.view.views.game.GameView

class SettingsView(ctx: GameContext, categories: List<Category>) : GameView(ctx) {
    enum class Category {
        ADMIN,
    }

    private class Entry(
        ctx: GameContext,
        name: String,
        desc: String,
        get: Settings.() -> Boolean,
        val set: Settings.(Boolean) -> Unit,
        type: Command.Type = Command.Type.Normal
    ) {
        val command = SelectItemCommand(
            ctx,
            name,
            selected = ctx.settings.get(),
            description = desc,
            type = type
        )
    }

    private var category = categories.first()

    private val entries = mutableMapOf(
        Category.ADMIN to listOf(
            Entry(
                ctx,
                "Mask cards",
                "If true, card list screens will hide card names and descriptions until the first time you buy one.",
                { admin.maskCards },
                { value -> admin.maskCards = value },
            ),
            Entry(
                ctx,
                "Show debug info",
                "Set true to surface things like game code inside the UI.",
                { admin.showDebugInfo },
                { value -> admin.showDebugInfo = value },
            ),
            Entry(
                ctx,
                "Enable admin features",
                "Uncheck this to disable access to the admin menu and clear other admin-specific features.\n" +
                        "\n" +
                        "You can restore them by going into the options mode and typing \"admin\".",
                { admin.enabled },
                { value -> admin.enabled = value },
            )
        )
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
        entries.values.flatten().forEach { entry ->
            entry.set(this, entry.command.selected)
        }
    }

    override fun createCommands(): List<Command> =
        entries.getValue(category).map { it.command } +
                object : Command(ctx) {
                    override val type get() = if (createNewSettings() != ctx.settings) Type.Normal else Type.Disabled
                    override val title: String = "Confirm"
                    override val description = "Press ENTER to confirm the above choice(s)."

                    override suspend fun invoke(): Boolean {
                        ctx.settings.setFrom(createNewSettings())
                        ctx.settings.save(ctx.app.userData)
                        ctx.app.logger.info("User settings updated and saved!")
                        ctx.viewStack.popView()
                        return true
                    }
                }
}

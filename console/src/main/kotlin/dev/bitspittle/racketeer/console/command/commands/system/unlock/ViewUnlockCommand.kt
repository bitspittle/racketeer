package dev.bitspittle.racketeer.console.command.commands.system.unlock

import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.user.Settings
import dev.bitspittle.racketeer.console.user.totalVp
import dev.bitspittle.racketeer.model.game.Unlock

class UnlockSettingHandlers(
    val get: Settings.Unlocks.() -> Boolean,
    val set: Settings.Unlocks.(Boolean) -> Unit,
) {
    companion object {
        val instance = mapOf(
            "buildings" to UnlockSettingHandlers(
                get = { buildings },
                set = { value -> buildings = value }
            ),
            "discord" to UnlockSettingHandlers(
                get = { discord },
                set = { value -> discord = value }
            )
        )
    }
}

private val INVOKE_HANDLERS: Map<String, suspend () -> Unit> = mapOf(
    "discord" to {}
)

fun Unlock.isForcefullyUnlocked(ctx: GameContext): Boolean {
    return UnlockSettingHandlers.instance[this.id]?.let { settingsHandler ->
        settingsHandler.get(ctx.settings.unlocks)
    } ?: false
}

class ViewUnlockCommand(ctx: GameContext, private val unlock: Unlock) : Command(ctx) {
    private val totalVp = ctx.userStats.games.totalVp

    override val type = when {
        !unlock.isForcefullyUnlocked(ctx) && unlock.vp > totalVp -> Type.Disabled
        INVOKE_HANDLERS.containsKey(unlock.id) -> Type.Accented
        else -> Type.Normal
    }
    override val title = if (type != Type.Disabled) unlock.name else "?".repeat(unlock.name.length)
    override val description = if (type != Type.Disabled) {
        buildString {
            append(unlock.description)
            if (!unlock.isForcefullyUnlocked(ctx)) {
                append("\n\n")
                append("This was unlocked after earning ${unlock.vp} victory points.")
            }
        }
    } else {
        "You will unlock this feature after earning ${unlock.vp - totalVp} more victory point(s)."
    }

    override suspend fun invoke(): Boolean {
        return INVOKE_HANDLERS[unlock.id]?.let { handler ->
            handler.invoke()
            true
        } ?: false
    }
}

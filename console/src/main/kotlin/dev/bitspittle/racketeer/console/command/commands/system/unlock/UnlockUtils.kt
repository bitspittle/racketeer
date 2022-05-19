package dev.bitspittle.racketeer.console.command.commands.system.unlock

import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.user.Settings
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

fun Unlock.isUnlocked(ctx: GameContext): Boolean {
    return UnlockSettingHandlers.instance[this.id]?.let { settingsHandler ->
        settingsHandler.get(ctx.settings.unlocks)
    } ?: false
}

fun Iterable<Unlock>.locked(ctx: GameContext, totalVp: Int) = this.filter { !it.isUnlocked(ctx) && it.vp > totalVp }

fun Unlock.unlock(ctx: GameContext): Boolean {
    return UnlockSettingHandlers.instance[this.id]?.let { settingsHandler ->
        if (!settingsHandler.get(ctx.settings.unlocks)) {
            settingsHandler.set(ctx.settings.unlocks, true)
            true
        } else false
    } ?: false
}
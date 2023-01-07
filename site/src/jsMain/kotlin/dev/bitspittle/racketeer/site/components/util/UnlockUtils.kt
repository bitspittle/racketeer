package dev.bitspittle.racketeer.site.components.util

import dev.bitspittle.racketeer.model.score.Unlock
import dev.bitspittle.racketeer.site.model.Settings

private class UnlockSettingHandlers(
    val get: Settings.Unlocks.() -> Boolean,
    val set: Settings.Unlocks.(Boolean) -> Unit,
)

private val UnlockSettingsHandlers = mapOf(
    "buildings" to UnlockSettingHandlers(
        get = { buildings },
        set = { value -> buildings = value }
    ),
)

fun Unlock.isUnlocked(settings: Settings): Boolean {
    return UnlockSettingsHandlers[this.id]?.let { handler ->
        handler.get(settings.unlocks)
    } ?: false
}

fun Iterable<Unlock>.locked(settings: Settings, vpCutoff: Int = Int.MAX_VALUE) = this.filter { !it.isUnlocked(settings) && it.vp <= vpCutoff }

fun Unlock.unlock(settings: Settings): Boolean {
    return UnlockSettingsHandlers[this.id]?.let { handler ->
        if (!handler.get(settings.unlocks)) {
            handler.set(settings.unlocks, true)
            true
        } else false
    } ?: false
}

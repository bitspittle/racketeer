package dev.bitspittle.racketeer.console.command.commands.system.feature

import dev.bitspittle.racketeer.console.command.commands.system.unlock.UnlockSettingHandlers
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.user.Settings
import dev.bitspittle.racketeer.model.game.Feature
import dev.bitspittle.racketeer.model.game.Unlock

fun Feature.isUnlocked(ctx: GameContext): Boolean {
    // A feature is considered unlocked if an unlock with the same ID is also unlocked. Note that this is different from
    // the user actually using the feature or not.
    return UnlockSettingHandlers.instance[this.id]?.let { settingsHandler ->
        settingsHandler.get(ctx.settings.unlocks)
    } ?: false
}

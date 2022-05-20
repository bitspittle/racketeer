package dev.bitspittle.racketeer.console.command.commands.system.unlock

import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.user.totalVp
import dev.bitspittle.racketeer.console.view.views.system.unlock.UnlockListView

class UnlockListCommand(ctx: GameContext) : Command(ctx) {
    private val totalVp = ctx.userStats.games.totalVp
    private val nextUnlock = ctx.data.unlocks.locked(ctx).firstOrNull()

    override val title = "Unlocks"
    override val description: String =
        if (nextUnlock != null) {
            "See the features you've unlocked or have yet to unlock.\n\n" +
                "You will unlock the next feature after earning ${nextUnlock.vp - totalVp} more victory point(s)."
        } else {
            "See the features you've unlocked."
        }

    override suspend fun invoke(): Boolean {
        ctx.viewStack.pushView(UnlockListView(ctx))
        return true
    }
}

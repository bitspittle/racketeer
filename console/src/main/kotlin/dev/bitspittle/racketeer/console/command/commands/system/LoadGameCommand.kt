package dev.bitspittle.racketeer.console.command.commands.system

import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.view.views.system.ConfirmLoadView
import kotlin.io.path.*

class LoadGameCommand(ctx: GameContext, private val slot: Int) : Command(ctx) {
    override val type: Type get() = if (ctx.app.userDataDir.pathForSlot(slot).exists()) Type.Warning else Type.Disabled
    override val title = "Load #${slot + 1}:"
    override val extra: String get() = ctx.app.userDataDir.modifiedTime(slot)

    override suspend fun invoke(): Boolean {
        ctx.viewStack.pushView(ConfirmLoadView(ctx, slot))
        return true
    }
}

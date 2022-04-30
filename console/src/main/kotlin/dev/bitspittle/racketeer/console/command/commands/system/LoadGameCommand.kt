package dev.bitspittle.racketeer.console.command.commands.system

import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.view.popAll
import dev.bitspittle.racketeer.console.view.views.game.PlayCardsView
import dev.bitspittle.racketeer.console.view.views.game.PreDrawView
import dev.bitspittle.racketeer.console.view.views.system.ConfirmLoadView
import dev.bitspittle.racketeer.model.snapshot.GameSnapshot
import net.mamoe.yamlkt.Yaml
import kotlin.io.path.*

class LoadGameCommand(ctx: GameContext, private val slot: Int) : Command(ctx) {
    override val type: Type get() = if (UserDataSupport.pathForSlot(slot).exists()) Type.Warning else Type.Disabled
    override val title = "Load #${slot + 1}:"
    override val extra: String get() = UserDataSupport.modifiedTime(slot)

    override suspend fun invoke(): Boolean {
        ctx.viewStack.pushView(ConfirmLoadView(ctx, slot))
        return true
    }
}

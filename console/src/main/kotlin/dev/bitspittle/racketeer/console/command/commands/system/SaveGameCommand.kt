package dev.bitspittle.racketeer.console.command.commands.system

import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.user.save
import dev.bitspittle.racketeer.console.utils.encodeToYaml
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.writeText

class SaveGameCommand(ctx: GameContext, private val slot: Int) : Command(ctx) {
    override val type: Type get() = if (ctx.app.userData.pathForSlot(slot).exists()) Type.Warning else Type.Normal
    override val title = "Save #${slot + 1}:"
    override val extra: String get() = ctx.app.userData.modifiedTime(slot)

    override suspend fun invoke(): Boolean {
        val path = ctx.app.userData.pathForSlot(slot)
        val overwritten = path.exists()
        ctx.app.userData.pathForSlot(slot).apply {
            parent.createDirectories()
            writeText(ctx.encodeToYaml())
        }
        // While we're here, save card stats too
        ctx.cardStats.values.save(ctx.app.userData)

        if (slot >= 0) {
            ctx.app.logger.info("Slot #${slot + 1} successfully " + (if (overwritten) "overwritten" else "saved") + "!")
        }

        return true
    }
}

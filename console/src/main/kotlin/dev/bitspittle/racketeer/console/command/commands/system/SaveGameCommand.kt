package dev.bitspittle.racketeer.console.command.commands.system

import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.view.views.game.PreDrawView
import dev.bitspittle.racketeer.model.snapshot.GameSnapshot
import net.mamoe.yamlkt.Yaml
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.writeText

class SaveGameCommand(ctx: GameContext, private val slot: Int) : Command(ctx) {
    override val type: Type get() = if (SerializationSupport.pathForSlot(slot).exists()) Type.Warning else Type.Normal
    override val title = "Save #${slot + 1}:"
    override val meta: String get() = SerializationSupport.modifiedTime(slot)

    override suspend fun invoke(): Boolean {
        val path = SerializationSupport.pathForSlot(slot)
        val overwritten = path.exists()
        SerializationSupport.pathForSlot(slot).apply {
            parent.createDirectories()
            writeText(
                Yaml.encodeToString(
                    GameSnapshot.from(
                        ctx.state,
                        isPreDraw = ctx.viewStack.contains { view -> view is PreDrawView })
                )
            )
        }
        if (slot >= 0) {
            ctx.app.logger.info("Slot #${slot + 1} successfully " + (if (overwritten) "overwritten" else "saved") + "!")
        }

        return true
    }
}

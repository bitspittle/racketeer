package dev.bitspittle.racketeer.console.command.commands.system

import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.view.popAll
import dev.bitspittle.racketeer.console.view.views.game.PlayCardsView
import dev.bitspittle.racketeer.console.view.views.game.PreDrawView
import dev.bitspittle.racketeer.model.snapshot.GameSnapshot
import net.mamoe.yamlkt.Yaml
import kotlin.io.path.*

class LoadGameCommand(ctx: GameContext, private val slot: Int) : Command(ctx) {
    override val type: Type get() = if (SerializationSupport.pathForSlot(slot).exists()) Type.Warning else Type.Disabled
    override val title = "Load #${slot + 1}:"
    override val meta: String get() = SerializationSupport.modifiedTime(slot)

    override suspend fun invoke(): Boolean {
        val path = SerializationSupport.pathForSlot(slot)
        val snapshot = Yaml.decodeFromString(GameSnapshot.serializer(), path.readText())
        snapshot.create(ctx.data, ctx.env, ctx.cardQueue) { state ->
            ctx.state = state
        }

        ctx.viewStack.popAll()
        if (snapshot.isPreDraw) {
            ctx.viewStack.replaceView(PreDrawView(ctx))
        } else {
            ctx.viewStack.replaceView(PlayCardsView(ctx))
        }

        if (slot >= 0) {
            ctx.app.logger.info("Slot #${slot + 1} successfully loaded!")
        }
        return true
    }
}

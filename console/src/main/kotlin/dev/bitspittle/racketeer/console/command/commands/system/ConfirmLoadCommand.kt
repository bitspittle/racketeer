package dev.bitspittle.racketeer.console.command.commands.system

import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.view.popAll
import dev.bitspittle.racketeer.console.view.views.game.play.PlayCardsView
import dev.bitspittle.racketeer.console.view.views.game.play.PreDrawView
import dev.bitspittle.racketeer.model.serialization.GameSnapshot
import net.mamoe.yamlkt.Yaml
import kotlin.io.path.*

class ConfirmLoadCommand(ctx: GameContext, private val slot: Int) : Command(ctx) {
    override val type = Type.Warning
    override val title = "Confirm"

    override val description = "Press ENTER if you're sure you want to load the data in save slot #${slot + 1}. Otherwise, go back!"

    override suspend fun invoke(): Boolean {
        val path = ctx.app.userData.pathForSlot(slot)
        val snapshot = Yaml.decodeFromString(GameSnapshot.serializer(), path.readText())
        snapshot.create(ctx.data, ctx.env, ctx.exprCache, ctx.actionQueue, ctx.cardEnqueuer) { state ->
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

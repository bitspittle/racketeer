package dev.bitspittle.racketeer.console.view.views.system

import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.command.commands.system.ConfirmLoadCommand
import dev.bitspittle.racketeer.console.command.commands.system.UserDataSupport
import dev.bitspittle.racketeer.console.game.notifyOwnership
import dev.bitspittle.racketeer.console.view.popAll
import dev.bitspittle.racketeer.console.view.views.game.GameView
import dev.bitspittle.racketeer.console.view.views.game.PlayCardsView
import dev.bitspittle.racketeer.console.view.views.game.PreDrawView
import dev.bitspittle.racketeer.model.snapshot.GameSnapshot
import net.mamoe.yamlkt.Yaml
import kotlin.io.path.readText

class ConfirmLoadView(ctx: GameContext, private val slot: Int) : GameView(ctx) {
    override fun createCommands(): List<Command> = listOf(ConfirmLoadCommand(ctx, slot))
}
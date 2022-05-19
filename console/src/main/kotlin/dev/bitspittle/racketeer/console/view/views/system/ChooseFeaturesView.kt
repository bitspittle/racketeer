package dev.bitspittle.racketeer.console.view.views.system

import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.command.commands.game.choose.SelectItemCommand
import dev.bitspittle.racketeer.console.command.commands.system.feature.isUnlocked
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.utils.createNewGame
import dev.bitspittle.racketeer.console.view.View
import dev.bitspittle.racketeer.console.view.popAll
import dev.bitspittle.racketeer.console.view.views.game.play.PreDrawView
import dev.bitspittle.racketeer.model.game.Feature

private fun GameContext.startNewGame(features: Set<Feature.Type> = emptySet()) {
    this.state = createNewGame(features)
    viewStack.popAll()
    viewStack.replaceView(PreDrawView(this))
}

class ChooseFeaturesView private constructor(ctx: GameContext, private val features: List<Feature>) : View(ctx) {
    companion object {
        fun enter(ctx: GameContext) {
            val availableFeatures = ctx.data.features.sortedBy { it.name }.filter { it.isUnlocked(ctx) }
            if (availableFeatures.isEmpty()) {
                ctx.startNewGame()
            } else {
                ctx.viewStack.pushView(ChooseFeaturesView(ctx, availableFeatures))
            }
        }
    }

    override val heading = "Choose the features you want and start a new game:"

    private val selectFeaturesCommand = features.map { feature -> SelectItemCommand(ctx, feature, false) }
    private val confirmCommand = object : Command(ctx) {
        override val type = Type.Accented
        override val title: String = "Confirm"
        override val description: String
            get() = "Press ENTER to confirm the above choice(s)."

        override suspend fun invoke(): Boolean {
            ctx.startNewGame(features.map { it.type }.toSet())
            return true
        }
    }

    override fun createCommands(): List<Command> = selectFeaturesCommand + confirmCommand
}
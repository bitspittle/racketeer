package dev.bitspittle.racketeer.console.view.views.system

import dev.bitspittle.racketeer.console.command.Command
import dev.bitspittle.racketeer.console.command.commands.game.choose.SelectItemCommand
import dev.bitspittle.racketeer.console.command.commands.system.feature.FeatureSettingHandlers
import dev.bitspittle.racketeer.console.command.commands.system.feature.isUnlocked
import dev.bitspittle.racketeer.console.game.GameContext
import dev.bitspittle.racketeer.console.user.saveInto
import dev.bitspittle.racketeer.console.utils.createNewGame
import dev.bitspittle.racketeer.console.view.View
import dev.bitspittle.racketeer.console.view.popAll
import dev.bitspittle.racketeer.console.view.views.game.play.PlayCardsView
import dev.bitspittle.racketeer.model.game.Feature
import dev.bitspittle.racketeer.model.game.GameStateChange
import dev.bitspittle.racketeer.model.game.recordChanges

private suspend fun GameContext.startNewGame(features: Set<Feature.Type> = emptySet()) {
    createNewGame(features)
    viewStack.popAll()
    viewStack.replaceView(PlayCardsView(this))

    state.recordChanges {
        state.apply(GameStateChange.Draw())
    }
}

class ChooseFeaturesView private constructor(ctx: GameContext, private val features: List<Feature>) : View(ctx, initialCurrIndex = Int.MAX_VALUE) {
    override val showUpdateMessage = true // Let the user know there's a new version BEFORE they start a new game

    companion object {
        suspend fun enter(ctx: GameContext) {
            val availableFeatures = ctx.data.features.sortedBy { it.name }.filter { it.isUnlocked(ctx) }
            if (availableFeatures.isEmpty()) {
                ctx.startNewGame()
            } else {
                ctx.viewStack.pushView(ChooseFeaturesView(ctx, availableFeatures))
            }
        }
    }

    override val heading = "Choose the features you want and start a new game:"

    private val selectFeaturesCommand = features.map { feature ->
        SelectItemCommand(ctx, feature, selected = FeatureSettingHandlers.instance.getValue(feature.id).get(ctx.settings.features))
    }
    private val confirmCommand = object : Command(ctx) {
        override val type = Type.Accented
        override val title: String = "Confirm"
        override val description: String
            get() = "Press ENTER to confirm the above choice(s)."

        override suspend fun invoke(): Boolean {
            val selectedFeatures = features
                .filterIndexed { i, _ -> selectFeaturesCommand[i].selected }
                .map { it.type }.toSet()

            var changedSettings = false
            features.forEachIndexed { i, feature ->
                val handler = FeatureSettingHandlers.instance.getValue(feature.id)
                val isSelected = selectFeaturesCommand[i].selected
                if (handler.get(ctx.settings.features) != isSelected) {
                    handler.set(ctx.settings.features, isSelected)
                    changedSettings = true
                }
            }
            if (changedSettings) {
                ctx.settings.saveInto(ctx.app.userDataDir)
            }

            ctx.startNewGame(selectedFeatures)
            return true
        }
    }

    override fun createCommands(): List<Command> = selectFeaturesCommand + confirmCommand
}
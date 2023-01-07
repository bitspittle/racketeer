package dev.bitspittle.racketeer.site.model

import dev.bitspittle.limp.types.Logger
import dev.bitspittle.racketeer.model.building.Blueprint
import dev.bitspittle.racketeer.model.building.Building
import dev.bitspittle.racketeer.model.card.Card
import dev.bitspittle.racketeer.model.card.CardTemplate
import dev.bitspittle.racketeer.model.game.Feature
import dev.bitspittle.racketeer.model.game.GameData
import dev.bitspittle.racketeer.model.text.Describer
import dev.bitspittle.racketeer.scripting.methods.collection.FormattedItem
import dev.bitspittle.racketeer.site.model.user.MutableUserStats
import dev.bitspittle.racketeer.site.model.user.UserStats
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

fun Describer.describeItem(item: Any): String {
    return when (item) {
        is Blueprint -> this.describeBlueprintTitle(item)
        is Building -> this.describeBuildingTitle(item)
        is Card -> this.describeCardTitle(item)
        is CardTemplate -> this.describeCardTitle(item)
        is Feature -> item.name
        is FormattedItem -> item.displayText ?: describeItem(item.wrapped)
        else -> item.toString()
    }
}

class ChoiceContext(
    val data: GameData,
    val settings: Settings,
    val userStats: MutableUserStats,
    val logger: Logger,
    val describer: Describer,
    val tooltipParser: TooltipParser,
    val prompt: String?,
    val items: List<Any>,
    val range: IntRange,
    val requiredChoice: Boolean,
    private val continuation: Continuation<List<Any>?>
) {
    private var onChosenListeners = mutableListOf<() -> Unit>()
    fun onChosen(listener: () -> Unit) {
        onChosenListeners.add(listener)
    }

    fun describe(item: Any) = describer.describeItem(item)

    fun extra(item: Any): String? {
        return when (item) {
            is FormattedItem -> item.extraText
            else -> null
        }
    }

    fun choose(choices: List<Any>?) {
        continuation.resume(choices)
        onChosenListeners.forEach { evt -> evt() }
    }
}

fun ChoiceContext.cancel() = choose(null)
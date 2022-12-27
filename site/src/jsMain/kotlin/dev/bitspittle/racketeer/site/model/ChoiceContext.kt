package dev.bitspittle.racketeer.site.model

import dev.bitspittle.racketeer.model.building.Blueprint
import dev.bitspittle.racketeer.model.building.Building
import dev.bitspittle.racketeer.model.card.Card
import dev.bitspittle.racketeer.model.card.CardTemplate
import dev.bitspittle.racketeer.model.game.Feature
import dev.bitspittle.racketeer.model.text.Describer
import dev.bitspittle.racketeer.scripting.methods.collection.FormattedItem
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

class ChoiceContext(
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

    fun describe(item: Any): String {
        return when (item) {
            is Blueprint -> describer.describeBlueprintTitle(item)
            is Building -> describer.describeBuildingTitle(item)
            is Card -> describer.describeCardTitle(item)
            is CardTemplate -> describer.describeCardTitle(item)
            is Feature -> item.name
            is FormattedItem -> item.displayText ?: describe(item.wrapped)
            else -> item.toString()
        }
    }

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
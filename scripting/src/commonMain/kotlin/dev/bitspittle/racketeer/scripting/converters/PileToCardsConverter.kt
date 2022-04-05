@file:Suppress("UNCHECKED_CAST")

package dev.bitspittle.racketeer.scripting.converters

import dev.bitspittle.limp.Converter
import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.utils.ifTrue
import dev.bitspittle.racketeer.model.card.Card
import dev.bitspittle.racketeer.model.card.Pile
import dev.bitspittle.racketeer.model.game.GameState
import kotlin.reflect.KClass

class PileToCardsConverter : Converter<List<Card>>(List::class as KClass<List<Card>>) {
    override fun convert(value: Any): List<Card>? {
        return (value as? Pile)?.cards
    }
}
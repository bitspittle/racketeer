@file:Suppress("UNCHECKED_CAST")

package dev.bitspittle.racketeer.scripting.converters

import dev.bitspittle.limp.Converter
import dev.bitspittle.racketeer.model.card.Card
import dev.bitspittle.racketeer.model.pile.Pile
import kotlin.reflect.KClass

class PileToCardsConverter : Converter<List<Card>>(List::class as KClass<List<Card>>) {
    override fun convert(value: Any): List<Card>? {
        return (value as? Pile)?.cards
    }
}
@file:Suppress("UNCHECKED_CAST")

package dev.bitspittle.racketeer.scripting.converters

import dev.bitspittle.limp.Converter
import dev.bitspittle.racketeer.model.card.Card
import dev.bitspittle.racketeer.model.pile.MutablePile
import kotlin.reflect.KClass

// Note: Must define before PileToCardsConverter, or else that one will always win
class MutablePileToCardsConverter : Converter<MutableList<Card>>(MutableList::class as KClass<MutableList<Card>>) {
    override fun convert(value: Any): MutableList<Card>? {
        return (value as? MutablePile)?.cards
    }
}
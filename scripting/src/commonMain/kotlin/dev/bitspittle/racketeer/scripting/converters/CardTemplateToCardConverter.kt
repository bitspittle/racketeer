@file:Suppress("UNCHECKED_CAST")

package dev.bitspittle.racketeer.scripting.converters

import dev.bitspittle.limp.Converter
import dev.bitspittle.racketeer.model.card.Card
import dev.bitspittle.racketeer.model.card.CardTemplate

class CardTemplateToCardConverter : Converter<Card>(Card::class) {
    override fun convert(value: Any): Card? {
        return (value as? CardTemplate)?.instantiate()
    }
}
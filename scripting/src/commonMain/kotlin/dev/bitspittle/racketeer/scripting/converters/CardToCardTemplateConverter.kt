@file:Suppress("UNCHECKED_CAST")

package dev.bitspittle.racketeer.scripting.converters

import dev.bitspittle.limp.Converter
import dev.bitspittle.racketeer.model.card.Card
import dev.bitspittle.racketeer.model.card.CardTemplate

class CardToCardTemplateConverter : Converter<CardTemplate>(CardTemplate::class) {
    override fun convert(value: Any): CardTemplate? {
        return (value as? Card)?.template
    }
}
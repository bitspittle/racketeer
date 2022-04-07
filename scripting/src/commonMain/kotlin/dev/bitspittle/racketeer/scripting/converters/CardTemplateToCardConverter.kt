@file:Suppress("UNCHECKED_CAST")

package dev.bitspittle.racketeer.scripting.converters

import dev.bitspittle.limp.Converter
import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.utils.ifTrue
import dev.bitspittle.racketeer.model.card.Card
import dev.bitspittle.racketeer.model.card.CardTemplate
import dev.bitspittle.racketeer.model.card.Pile
import dev.bitspittle.racketeer.model.game.GameState
import kotlin.reflect.KClass

class CardTemplateToCardConverter : Converter<Card>(Card::class) {
    override fun convert(value: Any): Card? {
        return (value as? CardTemplate)?.instantiate()
    }
}
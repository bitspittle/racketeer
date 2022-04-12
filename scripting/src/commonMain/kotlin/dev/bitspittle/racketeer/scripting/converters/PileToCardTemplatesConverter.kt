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

class PileToCardTemplatesConverter : Converter<List<CardTemplate>>(List::class as KClass<List<CardTemplate>>) {
    override fun convert(value: Any): List<CardTemplate>? {
        return (value as? Pile)?.cards?.map { it.template }
    }
}
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
        // Make a copy so we're not exposing direct access to our mutable bits. I know it shouldn't seem necessary but
        // we actually do an immutable-to-mutable cast in gamestate -- the API exposes immutable Piles so callers can't
        // do funny things directly, but when they ask us to do an action on their behalf given some Pile as a reference
        // point, we cast to mutable ourselves as an implementation detail.
        return (value as? Pile)?.cards?.toList()
    }
}
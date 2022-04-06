package dev.bitspittle.racketeer.model.card

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4

/**
 * @param template The read-only template this card is based on
 * @param id A globally unique ID which can act as this specific card's fingerprint
 */
class Card private constructor(val template: CardTemplate, val id: Uuid, vp: Int) {
    internal constructor(template: CardTemplate) : this(template, uuid4(), template.vp)

    /**
     * Cards can earn victory points over the course of the game.
     */
    var vp = vp
        set(value) {
            field = value.coerceAtLeast(0)
        }

    fun copy(id: Uuid = this.id, vp: Int = this.vp) = Card(template, id, vp)
}

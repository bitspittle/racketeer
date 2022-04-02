package dev.bitspittle.racketeer.model.card

import com.benasher44.uuid.uuid4

class Card internal constructor(val template: CardTemplate) {
    /**
     * Cards can earn victory points over the course of the game.
     */
    var vp = template.vp
        private set

    /**
     * A unique, randomly generated ID, which can be used for uniquely referencing this card later.
     */
    val id = uuid4()
}

package dev.bitspittle.racketeer.model.card

class Card internal constructor(val template: CardTemplate) {
    /**
     * Cards can earn victory points over the course of the game.
     */
    var vp = template.vp
        private set
}

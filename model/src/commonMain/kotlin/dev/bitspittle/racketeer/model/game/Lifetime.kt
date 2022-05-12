package dev.bitspittle.racketeer.model.game

enum class Lifetime {
    /** This effect should live for the entire game, once added. */
    GAME,
    /** This effect should live until the end of the turn, once added. */
    TURN,
    /** This effect should only run successfully once and then be removed. */
    ONCE
}
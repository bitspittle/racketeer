package dev.bitspittle.racketeer.model.game

enum class Rating {
    D,
    C,
    B,
    A,
    S;

    companion object {
        fun from(config: GameConfig, vp: Int): Rating {
            return when {
                vp < config.ratingScores[0] -> D
                vp < config.ratingScores[1] -> C
                vp < config.ratingScores[2] -> B
                vp < config.ratingScores[3] -> A
                else -> S
            }
        }
    }
}
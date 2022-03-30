package dev.bitspittle.racketeer.model.game

enum class Rating {
    D,
    C,
    B,
    A,
    S;

    companion object {
        fun from(data: GameData, vp: Int): Rating {
            return when {
                vp < data.ratingScores[0] -> D
                vp < data.ratingScores[1] -> C
                vp < data.ratingScores[2] -> B
                vp < data.ratingScores[3] -> A
                else -> S
            }
        }
    }
}
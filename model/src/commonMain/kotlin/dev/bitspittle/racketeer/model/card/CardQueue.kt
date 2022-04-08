package dev.bitspittle.racketeer.model.card

interface CardQueue {
    fun enqueue(card: Card)
    fun clear()
    suspend fun start()
}

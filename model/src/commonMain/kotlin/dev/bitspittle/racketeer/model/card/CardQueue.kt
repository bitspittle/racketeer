package dev.bitspittle.racketeer.model.card

interface CardQueue {
    fun enqueueInitActions(card: Card)
    fun enqueuePlayActions(card: Card)
    fun enqueuePassiveActions(card: Card)
    fun clear()
    suspend fun start()
}

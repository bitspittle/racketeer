package dev.bitspittle.racketeer.model.card

abstract class CardRunner {
    var cardQueue: CardQueue? = null
        private set

    protected abstract fun createCardQueue(): CardQueue

    suspend fun withCardQueue(block: suspend CardQueue.() -> Unit) {
        require(cardQueue == null) { "Attempt to start running new actions while previous actions haven't finished yet. Use CardQueue.enqueue instead."}
        cardQueue = createCardQueue()
        try {
            cardQueue!!.block()
        } finally {
            cardQueue = null
        }
    }
}

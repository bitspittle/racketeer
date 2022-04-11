package dev.bitspittle.racketeer.scripting.types

import dev.bitspittle.limp.Environment
import dev.bitspittle.racketeer.model.card.CardQueue
import dev.bitspittle.racketeer.model.card.CardRunner

class CardRunnerImpl(private val env: Environment) : CardRunner() {
    override fun createCardQueue(): CardQueue = CardQueueImpl(env)
}

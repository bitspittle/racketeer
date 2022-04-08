package dev.bitspittle.racketeer.scripting.types

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.types.Expr
import dev.bitspittle.racketeer.model.card.Card
import dev.bitspittle.racketeer.model.card.CardQueue
import dev.bitspittle.racketeer.model.card.CardRunner
import dev.bitspittle.racketeer.scripting.utils.compileActions

class CardRunnerImpl(
    private val env: Environment,
    private val produceExprs: (Card) -> List<Expr> = { card -> card.compileActions() }
) : CardRunner() {
    override fun createCardQueue(): CardQueue = CardQueueImpl(env, produceExprs)
}

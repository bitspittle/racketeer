package dev.bitspittle.racketeer.scripting.methods.pile

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.Method
import dev.bitspittle.limp.types.Expr
import dev.bitspittle.limp.utils.toEnum
import dev.bitspittle.racketeer.model.game.GameState
import dev.bitspittle.racketeer.model.pile.Pile
import dev.bitspittle.racketeer.model.text.Describer
import dev.bitspittle.racketeer.scripting.types.PileProperty

class PileGetMethod(private val describer: Describer, private val getGameState: () -> GameState) : Method("pile-get", 2) {
    override suspend fun invoke(
        env: Environment,
        eval: Evaluator,
        params: List<Any>,
        options: Map<String, Any>,
        rest: List<Any>
    ): Any {
        val pile = env.expectConvert<Pile>(params[0])
        val identifier = env.expectConvert<Expr.Identifier>(params[1])
        val property = identifier.toEnum(PileProperty.values())

        return when (property) {
            PileProperty.CARDS -> pile.cards
            PileProperty.DESC -> describer.describePile(getGameState(), pile)
            PileProperty.NAME -> describer.describePileTitle(getGameState(), pile)
            PileProperty.SIZE -> pile.cards.size
        }
    }
}
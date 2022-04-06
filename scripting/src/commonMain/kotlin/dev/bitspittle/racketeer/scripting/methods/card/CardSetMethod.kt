package dev.bitspittle.racketeer.scripting.methods.card

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Method
import dev.bitspittle.limp.exceptions.EvaluationException
import dev.bitspittle.limp.types.Expr
import dev.bitspittle.limp.utils.toEnum
import dev.bitspittle.racketeer.model.card.Card
import dev.bitspittle.racketeer.scripting.types.CardProperty

class CardSetMethod : Method("card-set!", 3) {
    override suspend fun invoke(env: Environment, params: List<Any>, options: Map<String, Any>, rest: List<Any>): Any {
        val card = env.expectConvert<Card>(params[0])
        val identifier = env.expectConvert<Expr.Identifier>(params[1])
        val property = identifier.toEnum(CardProperty.values())
        val amount = env.expectConvert<Int>(params[2])

        when (property) {
            CardProperty.VP -> card.vp = amount
            CardProperty.NAME, CardProperty.ID, CardProperty.COST -> throw EvaluationException(
                identifier.ctx,
                "Cannot set this card's property as it is read-only."
            )
        }

        return Unit
    }
}
package dev.bitspittle.racketeer.scripting.methods.card

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.Method
import dev.bitspittle.limp.types.Expr
import dev.bitspittle.limp.utils.toValue
import dev.bitspittle.racketeer.model.card.Card

class CardHasTypeMethod(private val types: Iterable<String>) : Method("card-has-type?", 2) {
    override suspend fun invoke(
        env: Environment,
        eval: Evaluator,
        params: List<Any>,
        options: Map<String, Any>,
        rest: List<Any>
    ): Any {
        val card = env.expectConvert<Card>(params[0])
        val identifier = env.expectConvert<Expr.Identifier>(params[1])
        identifier.toValue(types) // As a side effect, will assert if the identifier is bad

        return card.template.types.contains(identifier.name)
    }
}
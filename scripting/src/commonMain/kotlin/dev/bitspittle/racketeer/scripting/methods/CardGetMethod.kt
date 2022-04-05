package dev.bitspittle.racketeer.scripting.methods

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Method
import dev.bitspittle.limp.types.Expr
import dev.bitspittle.limp.utils.toEnum
import dev.bitspittle.racketeer.model.card.Card
import dev.bitspittle.racketeer.scripting.types.CardProperty
import dev.bitspittle.racketeer.scripting.types.GameService

class CardGetMethod() : Method("card-get", 2) {
    override suspend fun invoke(env: Environment, params: List<Any>, options: Map<String, Any>, rest: List<Any>): Any {
        val card = env.expectConvert<Card>(params[0])
        val identifier = env.expectConvert<Expr.Identifier>(params[1])
        val property = identifier.toEnum(CardProperty.values())

        return when (property) {
            CardProperty.NAME -> card.template.name
            CardProperty.VP -> card.vp
        }
    }
}
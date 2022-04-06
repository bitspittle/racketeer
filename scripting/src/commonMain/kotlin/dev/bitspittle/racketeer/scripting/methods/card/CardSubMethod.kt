package dev.bitspittle.racketeer.scripting.methods.card

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Method
import dev.bitspittle.racketeer.scripting.types.GameService

class CardSubMethod : Method("card-sub", 3) {
    override suspend fun invoke(env: Environment, params: List<Any>, options: Map<String, Any>, rest: List<Any>): Any {
        return CardAddMethod().invoke(env, listOf(params[0], params[1], -env.expectConvert<Int>(params[2])))
    }
}
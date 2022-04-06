package dev.bitspittle.racketeer.scripting.methods.card

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Method
import dev.bitspittle.racketeer.scripting.types.GameService

class CardAddMethod : Method("card-add!", 3) {
    override suspend fun invoke(env: Environment, params: List<Any>, options: Map<String, Any>, rest: List<Any>): Any {
        val delta = env.expectConvert<Int>(params[2])
        val curr = env.expectConvert<Int>(CardGetMethod().invoke(env, listOf(params[0], params[1])))
        return CardSetMethod().invoke(env, listOf(params[0], params[1], curr + delta))
    }
}
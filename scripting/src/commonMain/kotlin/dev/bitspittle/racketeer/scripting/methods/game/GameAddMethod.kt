package dev.bitspittle.racketeer.scripting.methods.game

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Method
import dev.bitspittle.racketeer.scripting.types.GameService

class GameAddMethod(private val service: GameService) : Method("game-add", 2) {
    override suspend fun invoke(env: Environment, params: List<Any>, options: Map<String, Any>, rest: List<Any>): Any {
        val delta = env.expectConvert<Int>(params[1])
        val curr = env.expectConvert<Int>(GameGetMethod(service).invoke(env, listOf(params[0])))
        return GameSetMethod(service).invoke(env, listOf(params[0], curr + delta))
    }
}
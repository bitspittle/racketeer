package dev.bitspittle.limp.methods.collection

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.Method
import dev.bitspittle.limp.converters.ValueToExprConverter
import dev.bitspittle.limp.types.Expr

class EmptyMethod : Method("empty?", 1) {
    override suspend fun invoke(env: Environment, params: List<Any>, options: Map<String, Any>, rest: List<Any>): Any {
        val list = env.expectConvert<List<*>>(params[0])
        return list.isEmpty()
    }
}
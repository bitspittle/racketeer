package dev.bitspittle.limp.methods.system

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.Method
import dev.bitspittle.limp.listTypeOf
import dev.bitspittle.limp.types.Expr

class RunMethod : Method("run", 0, consumeRest = true) {
    override suspend fun invoke(
        env: Environment,
        eval: Evaluator,
        params: List<Any>,
        options: Map<String, Any>,
        rest: List<Any>
    ): Any {
        val exprs = env.expectConvert<List<Expr>>(rest, listTypeOf())

        var result: Any = Unit

        env.scoped {
            exprs.forEach { expr ->
                result = eval.evaluate(env, expr)
            }
        }

        return result
    }
}
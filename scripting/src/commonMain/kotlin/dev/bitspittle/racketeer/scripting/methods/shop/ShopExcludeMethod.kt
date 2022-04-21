package dev.bitspittle.racketeer.scripting.methods.shop

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.Method
import dev.bitspittle.limp.types.Expr
import dev.bitspittle.racketeer.model.shop.Exclusion
import dev.bitspittle.racketeer.model.shop.Shop

class ShopExcludeMethod(private val getShop: () -> Shop) : Method("shop-exclude!", 1) {
    override suspend fun invoke(env: Environment, eval: Evaluator, params: List<Any>, options: Map<String, Any>, rest: List<Any>): Any {
        val excludeExpr = env.expectConvert<Expr>(params[0])
        val desc = options["desc"]?.let { env.expectConvert(it) } ?: excludeExpr.ctx.text
        getShop().addExclusion(Exclusion(excludeExpr.ctx.text, desc) { card ->
            val evaluator = eval.extend(mapOf("\$card" to card.instantiate()))
            env.expectConvert(evaluator.evaluate(env, excludeExpr))
        })

        return Unit
    }
}
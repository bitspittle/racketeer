package dev.bitspittle.limp.methods.collection

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.Method
import dev.bitspittle.limp.converters.PlaceholderConverter
import dev.bitspittle.limp.types.Expr
import dev.bitspittle.limp.types.ListStrategy
import dev.bitspittle.limp.utils.toEnum
import kotlin.random.Random

/**
 * take --from ('pos) (List) (count: Int) -> List
 *
 * Take some number of elements from a list, returning what was taken.
 */
class TakeMethod(private val random: () -> Random) : Method("take", 2) {
    override suspend fun invoke(
        env: Environment,
        eval: Evaluator,
        params: List<Any>,
        options: Map<String, Any>,
        rest: List<Any>
    ): Any {
        val list = env.expectConvert<List<Any>>(params[0])
        val count = env.scoped {
            env.addConverter(PlaceholderConverter(list.size))
            env.expectConvert<Int>(params[1])
        }.coerceAtMost(list.size)

        val strategy =
            options["from"]?.let { from ->
                env.expectConvert<Expr.Identifier>(from).toEnum(ListStrategy.values())
            } ?: ListStrategy.FRONT

        return when (strategy) {
            ListStrategy.FRONT -> list.take(count)
            ListStrategy.BACK -> list.takeLast(count)
            ListStrategy.RANDOM -> list.shuffled(random()).take(count)
        }
    }
}
package dev.bitspittle.limp.methods.collection

import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.Method
import dev.bitspittle.limp.types.Expr
import dev.bitspittle.limp.types.ListStrategy
import dev.bitspittle.limp.utils.toEnum
import kotlin.random.Random

/**
 * drop --from 'pos (List) (count: Int) -> List
 *
 * Drop some elements from a list, returning that list with those elements removed.
 */
class DropMethod(private val random: () -> Random) : Method("drop", 2) {
    override suspend fun invoke(
        env: Environment,
        eval: Evaluator,
        params: List<Any>,
        options: Map<String, Any>,
        rest: List<Any>
    ): Any {
        val list = env.expectConvert<List<Any>>(params[0])
        val count = env.expectConvert<Int>(params[1]).coerceAtMost(list.size)

        val strategy =
            options["from"]?.let { from ->
                env.expectConvert<Expr.Identifier>(from).toEnum(ListStrategy.values())
            } ?: ListStrategy.FRONT

        return when (strategy) {
            ListStrategy.FRONT -> list.drop(count)
            ListStrategy.BACK -> list.dropLast(count)
            ListStrategy.RANDOM -> list.indices.shuffled(random()).take(count).let { indicesToRemove ->
                list.filterIndexed { index, _ -> index !in indicesToRemove }
            }
        }
    }
}
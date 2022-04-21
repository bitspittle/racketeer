package dev.bitspittle.limp.converters

import com.varabyte.truthish.assertThat
import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.methods.collection.ListGetMethod
import dev.bitspittle.limp.methods.collection.ListMethod
import dev.bitspittle.limp.methods.collection.TakeMethod
import dev.bitspittle.limp.methods.system.DefMethod
import dev.bitspittle.limp.methods.system.SetMethod
import dev.bitspittle.limp.types.ConsoleLogger
import kotlinx.coroutines.test.runTest
import kotlin.random.Random
import kotlin.test.Test

class ConvertersTest {
    @Test
    fun testItemToSingletonListConverter() = runTest {
        val env = Environment()
        env.addConverter(ItemToSingletonListConverter(Int::class))
        env.addMethod(DefMethod())
        env.addMethod(SetMethod(ConsoleLogger()))
        env.addMethod(ListMethod())
        env.addMethod(ListGetMethod())
        env.addMethod(TakeMethod { Random.Default})  // Random doesn't matter -- we don't call `take --from 'random`

        val evaluator = Evaluator()
        // Normally, list-get has to take in a list as its first element, or it's error time baby. However, we added
        // an ItemToSingletonListConverter so if we pass it an integer instead, it will auto-convert.

        evaluator.evaluate(env, "def 'first '\$list '(list-get \$list 0)")
        evaluator.evaluate(env, "set '\$ints (list 1 2 3 4 5)")

        assertThat(evaluator.evaluate(env, "(first \$ints)")).isEqualTo(1)
        assertThat(evaluator.evaluate(env, "(first 5)")).isEqualTo(5)
    }
}
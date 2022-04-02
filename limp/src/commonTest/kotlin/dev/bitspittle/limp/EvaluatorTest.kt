package dev.bitspittle.limp

import com.varabyte.truthish.assertThat
import dev.bitspittle.limp.methods.math.AddMethod
import dev.bitspittle.limp.methods.math.MulMethod
import dev.bitspittle.limp.methods.math.SubMethod
import kotlin.test.Test

class EvaluatorTest {
    @Test
    fun testSimpleEvaluation() {
        val env = Environment()
        env.add(AddMethod())
        env.add(MulMethod())
        env.add(SubMethod())

        val evaluator = Evaluator()
        assertThat(evaluator.evaluate(env, "+ 1 2").wrapped).isEqualTo(3)
        assertThat(evaluator.evaluate(env, "+ 1 * 3 2").wrapped).isEqualTo(7)
        assertThat(evaluator.evaluate(env, "+ 1 * 3 - 8 2").wrapped).isEqualTo(19)
        assertThat(evaluator.evaluate(env, "(+ 1 (* 3 (- 8 2)))").wrapped).isEqualTo(19)

        env.set("\$a", Value(5))
        env.set("\$b", Value(90))
        assertThat(evaluator.evaluate(env, "+ \$a (* 2 \$b)").wrapped).isEqualTo(185)
    }
}
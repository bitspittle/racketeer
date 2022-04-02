package dev.bitspittle.limp

import com.varabyte.truthish.assertThat
import com.varabyte.truthish.assertThrows
import dev.bitspittle.limp.exceptions.EvaluationException
import dev.bitspittle.limp.methods.collection.ListMethod
import dev.bitspittle.limp.methods.math.*
import dev.bitspittle.limp.methods.range.IntRangeMethod
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

    @Test
    fun testEvaluationWithOptionalParameters() {
        val env = Environment()
        env.add(IntRangeMethod())

        val evaluator = Evaluator()
        assertThat(evaluator.evaluate(env, ".. 1 10").wrapped).isEqualTo(1 .. 10)
        assertThat(evaluator.evaluate(env, ".. --step 4 1 20").wrapped).isEqualTo(1 .. 20 step 4)

        assertThrows<EvaluationException> {
            // Whoops, typo!
            assertThat(evaluator.evaluate(env, ".. --stop 4 1 20").wrapped).isEqualTo(1..20 step 4)
        }
    }

    @Test
    fun testEvaluationWithRestParameters() {
        val env = Environment()
        env.add(ListMethod())
        env.add(AddMethod())
        env.add(AddListMethod())
        env.add(MulListMethod())

        val evaluator = Evaluator()
        assertThat(evaluator.evaluate(env, "list 1 2 3 4 5").wrapped as List<Int>).containsExactly(1, 2, 3, 4, 5)
            .inOrder()

        assertThat(evaluator.evaluate(env, "sum list 1 2 3 4 5").wrapped).isEqualTo(15)
        assertThat(evaluator.evaluate(env, "+ (mul list 1 2 3) (sum list 4 5 6)").wrapped).isEqualTo(21)
    }
}
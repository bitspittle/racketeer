package dev.bitspittle.limp.methods

import com.varabyte.truthish.assertThat
import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.Value
import dev.bitspittle.limp.methods.range.IntRangeMethod
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class RangeMethodsTest {
    @Test
    fun testIntRangeMethod() = runTest {
        val env = Environment()
        env.addMethod(IntRangeMethod())
        env.storeValue("_", Value.Placeholder)

        val evaluator = Evaluator()

        assertThat(evaluator.evaluate(env, ".. 1 2").wrapped).isEqualTo(IntRange(1, 2))
        assertThat(evaluator.evaluate(env, ".. _ 2").wrapped).isEqualTo(IntRange(0, 2))
        assertThat(evaluator.evaluate(env, ".. 2 _").wrapped).isEqualTo(IntRange(2, Int.MAX_VALUE))
        assertThat(evaluator.evaluate(env, ".. _ _").wrapped).isEqualTo(IntRange(0, Int.MAX_VALUE))
        assertThat(evaluator.evaluate(env, ".. --step 2 1 10").wrapped).isEqualTo(IntRange(1, 10) step 2)
    }
}
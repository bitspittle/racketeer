package dev.bitspittle.limp.methods

import com.varabyte.truthish.assertThat
import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Value
import dev.bitspittle.limp.methods.range.IntRangeMethod
import kotlin.test.Test

class RangeMethodsTest {
    @Test
    fun testIntRangeMethod() {
        val env = Environment()
        val method = IntRangeMethod()

        assertThat(method.invoke(env, listOf(Value(1), Value(2))).wrapped).isEqualTo(IntRange(1, 2))
        assertThat(method.invoke(env, listOf(Value.Placeholder, Value(2))).wrapped).isEqualTo(IntRange(0, 2))
        assertThat(method.invoke(env, listOf(Value(1), Value.Placeholder)).wrapped).isEqualTo(IntRange(1, Int.MAX_VALUE))
        assertThat(method.invoke(env, listOf(Value.Placeholder, Value.Placeholder)).wrapped).isEqualTo(IntRange(0, Int.MAX_VALUE))

        assertThat(method.invoke(env, listOf(Value(1), Value(10)), mapOf("step" to Value(2))).wrapped).isEqualTo(IntRange(1, 10) step 2)
    }
}
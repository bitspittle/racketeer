package dev.bitspittle.lispish.methods.range

import com.varabyte.truthish.assertThat
import dev.bitspittle.lispish.Environment
import dev.bitspittle.lispish.Value
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
    }
}
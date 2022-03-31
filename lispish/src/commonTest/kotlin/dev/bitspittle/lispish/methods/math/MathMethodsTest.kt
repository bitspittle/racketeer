package dev.bitspittle.lispish.methods.math

import com.varabyte.truthish.assertThat
import dev.bitspittle.lispish.Environment
import dev.bitspittle.lispish.Value
import kotlin.test.Test

class MathMethodsTest {
    @Test
    fun testAddMethod() {
        val env = Environment()
        val method = AddMethod()

        assertThat(method.invoke(env, listOf(Value(1), Value(2))).wrapped).isEqualTo(3)
    }

    @Test
    fun testSubMethod() {
        val env = Environment()
        val method = SubMethod()

        assertThat(method.invoke(env, listOf(Value(5), Value(2))).wrapped).isEqualTo(3)
    }
}
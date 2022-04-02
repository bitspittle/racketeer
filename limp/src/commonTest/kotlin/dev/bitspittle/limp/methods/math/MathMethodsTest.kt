package dev.bitspittle.limp.methods.math

import com.varabyte.truthish.assertThat
import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Value
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

    @Test
    fun testMinMehthod() {
        val env = Environment()
        val method = MinMethod()

        assertThat(method.invoke(env, listOf(Value(5), Value(2))).wrapped).isEqualTo(2)
        assertThat(method.invoke(env, listOf(Value(-5), Value(2))).wrapped).isEqualTo(-5)
    }

    @Test
    fun testMaxMethod() {
        val env = Environment()
        val method = MaxMethod()

        assertThat(method.invoke(env, listOf(Value(5), Value(2))).wrapped).isEqualTo(5)
        assertThat(method.invoke(env, listOf(Value(-5), Value(2))).wrapped).isEqualTo(2)
    }

    @Test
    fun testClampMethod() {
        val env = Environment()
        val method = ClampMethod()

        assertThat(method.invoke(env, listOf(Value(0), Value(2), Value(5))).wrapped).isEqualTo(2)
        assertThat(method.invoke(env, listOf(Value(2), Value(2), Value(5))).wrapped).isEqualTo(2)
        assertThat(method.invoke(env, listOf(Value(4), Value(2), Value(5))).wrapped).isEqualTo(4)
        assertThat(method.invoke(env, listOf(Value(5), Value(2), Value(5))).wrapped).isEqualTo(5)
        assertThat(method.invoke(env, listOf(Value(9), Value(2), Value(5))).wrapped).isEqualTo(5)
    }
}
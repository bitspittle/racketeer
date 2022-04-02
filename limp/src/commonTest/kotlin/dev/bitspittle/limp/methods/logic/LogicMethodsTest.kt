package dev.bitspittle.limp.methods.logic

import com.varabyte.truthish.assertThat
import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Value
import kotlin.test.Test

class LogicMethodsTest {
    @Test
    fun testNotMethod() {
        val env = Environment()
        val method = NotMethod()

        assertThat(method.invoke(env, listOf(Value(true))).wrapped as Boolean).isFalse()
        assertThat(method.invoke(env, listOf(Value(false))).wrapped as Boolean).isTrue()
    }

    @Test
    fun testAndMethod() {
        val env = Environment()
        val method = AndMethod()

        assertThat(method.invoke(env, listOf(Value(true), Value(true))).wrapped).isEqualTo(true)
        assertThat(method.invoke(env, listOf(Value(true), Value(false))).wrapped).isEqualTo(false)
        assertThat(method.invoke(env, listOf(Value(false), Value(true))).wrapped).isEqualTo(false)
        assertThat(method.invoke(env, listOf(Value(false), Value(false))).wrapped).isEqualTo(false)
    }

    @Test
    fun testOrMethod() {
        val env = Environment()
        val method = OrMethod()

        assertThat(method.invoke(env, listOf(Value(true), Value(true))).wrapped).isEqualTo(true)
        assertThat(method.invoke(env, listOf(Value(true), Value(false))).wrapped).isEqualTo(true)
        assertThat(method.invoke(env, listOf(Value(false), Value(true))).wrapped).isEqualTo(true)
        assertThat(method.invoke(env, listOf(Value(false), Value(false))).wrapped).isEqualTo(false)
    }
}
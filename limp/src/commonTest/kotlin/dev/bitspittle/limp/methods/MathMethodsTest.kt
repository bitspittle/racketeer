package dev.bitspittle.limp.methods

import com.varabyte.truthish.assertThat
import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Value
import dev.bitspittle.limp.methods.compare.*
import dev.bitspittle.limp.methods.math.*
import kotlin.test.Test

class MathMethodsTest {
    @Test
    fun testAddMethod() {
        val env = Environment()
        val method = AddMethod()

        assertThat(method.invoke(env, listOf(Value(1), Value(2))).wrapped).isEqualTo(3)
    }

    @Test
    fun testAddListMethod() {
        val env = Environment()
        val method = AddListMethod()

        assertThat(method.invoke(env, listOf(Value(listOf(1, 2, 3)))).wrapped).isEqualTo(6)
        assertThat(method.invoke(env, listOf(Value(listOf<Int>()))).wrapped).isEqualTo(0)
    }

    @Test
    fun testSubMethod() {
        val env = Environment()
        val method = SubMethod()

        assertThat(method.invoke(env, listOf(Value(5), Value(2))).wrapped).isEqualTo(3)
    }

    @Test
    fun testMulMethod() {
        val env = Environment()
        val method = MulMethod()

        assertThat(method.invoke(env, listOf(Value(3), Value(2))).wrapped).isEqualTo(6)
    }

    @Test
    fun testPowMethod() {
        val env = Environment()
        val method = PowMethod()

        assertThat(method.invoke(env, listOf(Value(2), Value(3))).wrapped).isEqualTo(8)
        assertThat(method.invoke(env, listOf(Value(3), Value(2))).wrapped).isEqualTo(9)
        assertThat(method.invoke(env, listOf(Value(1000), Value(0))).wrapped).isEqualTo(1)
        assertThat(method.invoke(env, listOf(Value(20), Value(1))).wrapped).isEqualTo(20)
    }

    @Test
    fun testMulListMethod() {
        val env = Environment()
        val method = MulListMethod()

        assertThat(method.invoke(env, listOf(Value(listOf(1, 2, 3)))).wrapped).isEqualTo(6)
        assertThat(method.invoke(env, listOf(Value(listOf<Int>()))).wrapped).isEqualTo(1)
    }

    @Test
    fun testDivMethod() {
        val env = Environment()
        val method = DivMethod()

        assertThat(method.invoke(env, listOf(Value(9), Value(3))).wrapped).isEqualTo(3)
        assertThat(method.invoke(env, listOf(Value(9), Value(4))).wrapped).isEqualTo(2)
    }

    @Test
    fun testRemainderMethod() {
        val env = Environment()
        val method = RemainderMethod()

        assertThat(method.invoke(env, listOf(Value(9), Value(3))).wrapped).isEqualTo(0)
        assertThat(method.invoke(env, listOf(Value(9), Value(4))).wrapped).isEqualTo(1)
    }

    @Test
    fun testMinMethod() {
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
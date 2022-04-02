package dev.bitspittle.limp.methods.collection

import com.varabyte.truthish.assertThat
import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Value
import kotlin.test.Test

class CollectionMethodsTest {
    @Test
    fun testInMethod() {
        val env = Environment()
        val method = InMethod()

        val ints = listOf(1, 2, 3, 4, 5)
        val letters = listOf('a', 'b', 'c', 'd', 'e')
        val fruits = listOf("Apple", "Banana", "Cantaloupe")

        assertThat(method.invoke(env, listOf(Value(ints), Value(3))).wrapped as Boolean).isTrue()
        assertThat(method.invoke(env, listOf(Value(ints), Value(6))).wrapped as Boolean).isFalse()
        assertThat(method.invoke(env, listOf(Value(ints), Value("3"))).wrapped as Boolean).isFalse()

        assertThat(method.invoke(env, listOf(Value(letters), Value('c'))).wrapped as Boolean).isTrue()
        assertThat(method.invoke(env, listOf(Value(letters), Value('z'))).wrapped as Boolean).isFalse()
        assertThat(method.invoke(env, listOf(Value(letters), Value("c"))).wrapped as Boolean).isFalse()
        assertThat(method.invoke(env, listOf(Value(letters), Value('c'.code))).wrapped as Boolean).isFalse()

        assertThat(method.invoke(env, listOf(Value(fruits), Value("Banana"))).wrapped as Boolean).isTrue()
        assertThat(method.invoke(env, listOf(Value(fruits), Value("Cherry"))).wrapped as Boolean).isFalse()
        assertThat(method.invoke(env, listOf(Value(fruits), Value("banana"))).wrapped as Boolean).isFalse()
    }
}
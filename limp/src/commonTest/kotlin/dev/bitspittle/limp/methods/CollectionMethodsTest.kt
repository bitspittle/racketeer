package dev.bitspittle.limp.methods

import com.varabyte.truthish.assertThat
import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Value
import dev.bitspittle.limp.methods.collection.InMethod
import dev.bitspittle.limp.methods.collection.ShuffleMethod
import dev.bitspittle.limp.methods.collection.UnionMethod
import kotlin.random.Random
import kotlin.test.Test

@Suppress("UNCHECKED_CAST")
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

    @Test
    fun testShuffleMethod() {
        val env = Environment(Random(123)) // Fixed seed so that we get the same shuffle everytime for this test
        val method = ShuffleMethod()

        val ints = listOf(1, 2, 3, 4, 5)

        assertThat(method.invoke(env, listOf(Value(ints))).wrapped as List<Int>)
            .containsExactly(3, 1, 4, 5, 2).inOrder()

        // Original ints are not affected
        assertThat(ints).containsExactly(1, 2, 3, 4, 5).inOrder()
    }

    @Test
    fun testUnionMethod() {
        val env = Environment()
        val method = UnionMethod()

        val ints1 = listOf(1, 2, 3, 4, 5)
        val ints2 = listOf(6, 7, 8, 9, 10)
        val ints3 = listOf(11, 12, 13, 14, 15)

        assertThat(method.invoke(env, listOf(), rest = listOf(Value(ints1), Value(ints2), Value(ints3))).wrapped as List<Int>)
            .containsExactly(1 .. 15).inOrder()

        assertThat(method.invoke(env, listOf()).wrapped as List<Int>).isEmpty()
    }
}
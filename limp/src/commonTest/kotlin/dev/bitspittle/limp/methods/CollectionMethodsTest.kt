package dev.bitspittle.limp.methods

import com.varabyte.truthish.assertThat
import com.varabyte.truthish.assertThrows
import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.Value
import dev.bitspittle.limp.exceptions.EvaluationException
import dev.bitspittle.limp.methods.collection.*
import dev.bitspittle.limp.methods.math.EqualsMethod
import dev.bitspittle.limp.methods.math.GreaterThanMethod
import dev.bitspittle.limp.methods.math.RemainderMethod
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
    fun testTakeMethod() {
        val env = Environment(Random(123)) // Fixed seed so that we get the same shuffle everytime for this test
        env.addMethod(TakeMethod())
        env.storeValue("_", Value.Placeholder)

        val evaluator = Evaluator()

        env.storeValue("ints", Value(listOf(1, 2, 3, 4, 5)))
        env.storeValue("strs", Value(listOf("Aa", "Bb", "Cc", "Dd")))

        assertThat(evaluator.evaluate(env, "take ints 2").wrapped as List<Int>).containsExactly(1, 2).inOrder()
        assertThat(evaluator.evaluate(env, "take ints 8").wrapped as List<Int>).containsExactly(1, 2, 3, 4, 5).inOrder()
        assertThat(evaluator.evaluate(env, "take ints 0").wrapped as List<Int>).isEmpty()
        assertThat(evaluator.evaluate(env, "take ints _").wrapped as List<Int>).containsExactly(1, 2, 3, 4, 5).inOrder()

        assertThat(evaluator.evaluate(env, "take --from 'back ints 2").wrapped as List<Int>).containsExactly(4, 5).inOrder()
        assertThat(evaluator.evaluate(env, "take --from 'back ints _").wrapped as List<Int>).containsExactly(1, 2, 3, 4, 5).inOrder()

        assertThat(evaluator.evaluate(env, "take --from 'random ints 3").wrapped as List<Int>).containsExactly(3, 1, 4).inOrder()

        assertThat(evaluator.evaluate(env, "take strs 2").wrapped as List<String>).containsExactly("Aa", "Bb").inOrder()
        assertThat(evaluator.evaluate(env, "take --from 'back strs 2").wrapped as List<String>).containsExactly("Cc", "Dd").inOrder()
    }

    @Test
    fun testFilterMethod() {
        val env = Environment()
        env.addMethod(FilterMethod())
        env.addMethod(GreaterThanMethod())
        env.addMethod(RemainderMethod())
        env.addMethod(EqualsMethod())
        env.storeValue("false", Value.False)

        val evaluator = Evaluator()

        env.storeValue("ints", Value((1 .. 10).toList()))

        assertThat(evaluator.evaluate(env, "filter ints '(> \$it 5)").wrapped as List<Int>).containsExactly(6, 7, 8, 9, 10).inOrder()
        assertThat(evaluator.evaluate(env, "filter ints '(= % \$it 2 0)").wrapped as List<Int>).containsExactly(2, 4, 6, 8, 10).inOrder()
        assertThat(evaluator.evaluate(env, "filter ints 'false").wrapped as List<Int>).isEmpty()
    }

    @Test
    fun testFirstMethod() {
        val env = Environment()
        env.addMethod(FirstMethod())
        env.addMethod(GreaterThanMethod())
        env.addMethod(RemainderMethod())
        env.addMethod(EqualsMethod())
        env.storeValue("false", Value.False)

        val evaluator = Evaluator()

        env.storeValue("ints", Value((1 .. 10).toList()))

        assertThat(evaluator.evaluate(env, "first ints '(> \$it 5)").wrapped).isEqualTo(6)
        assertThat(evaluator.evaluate(env, "first ints '(= % \$it 2 0)").wrapped).isEqualTo(2)

        assertThrows<EvaluationException> {
            evaluator.evaluate(env, "first ints 'false")
        }
    }

    @Test
    fun testSingleMethod() {
        val env = Environment()
        env.addMethod(SingleMethod())
        env.addMethod(GreaterThanMethod())
        env.addMethod(RemainderMethod())
        env.addMethod(EqualsMethod())
        env.storeValue("false", Value.False)

        val evaluator = Evaluator()

        env.storeValue("ints", Value((1 .. 10).toList()))

        assertThat(evaluator.evaluate(env, "single ints '(= \$it 8)").wrapped).isEqualTo(8)
        assertThat(evaluator.evaluate(env, "single ints '(= % \$it 7 0)").wrapped).isEqualTo(7)

        assertThrows<EvaluationException> {
            // Single must not return an empty list
            evaluator.evaluate(env, "single ints 'false")
        }

        assertThrows<EvaluationException> {
            // Single must not return a list with 2+ elements
            evaluator.evaluate(env, "single ints 'true")
        }
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
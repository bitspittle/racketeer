package dev.bitspittle.limp.methods

import com.varabyte.truthish.assertThat
import com.varabyte.truthish.assertThrows
import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.Value
import dev.bitspittle.limp.exceptions.EvaluationException
import dev.bitspittle.limp.methods.collection.*
import dev.bitspittle.limp.methods.compare.CompareMethod
import dev.bitspittle.limp.methods.compare.EqualsMethod
import dev.bitspittle.limp.methods.compare.GreaterThanMethod
import dev.bitspittle.limp.methods.convert.ToIntMethod
import dev.bitspittle.limp.methods.convert.ToStringMethod
import dev.bitspittle.limp.methods.math.PowMethod
import dev.bitspittle.limp.methods.math.RemainderMethod
import kotlinx.coroutines.test.runTest
import kotlin.random.Random
import kotlin.test.Test

@Suppress("UNCHECKED_CAST")
class CollectionMethodsTest {
    @Test
    fun testInMethod() = runTest {
        val env = Environment()
        env.addMethod(InMethod())

        env.storeValue("ints", Value(listOf(1, 2, 3, 4, 5)))
        env.storeValue("fruits", Value(listOf("Apple", "Banana", "Cantaloupe")))

        val evaluator = Evaluator()

        assertThat(evaluator.evaluate(env, "in ints 3").wrapped as Boolean).isTrue()
        assertThat(evaluator.evaluate(env, "in ints 6").wrapped as Boolean).isFalse()
        assertThat(evaluator.evaluate(env, "in ints \"6\"").wrapped as Boolean).isFalse()

        assertThat(evaluator.evaluate(env, "in fruits \"Banana\"").wrapped as Boolean).isTrue()
        assertThat(evaluator.evaluate(env, "in fruits \"Cherry\"").wrapped as Boolean).isFalse()
        assertThat(evaluator.evaluate(env, "in fruits \"banana\"").wrapped as Boolean).isFalse()
    }

    @Test
    fun testTakeMethod() = runTest {
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
    fun testFilterMethod() = runTest {
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
    fun testMapMethod() = runTest {
        val env = Environment()
        env.addMethod(MapMethod())
        env.addMethod(PowMethod())
        env.addMethod(ToStringMethod())
        env.storeValue("ints", Value((1..5).toList()))

        val evaluator = Evaluator()

        assertThat(evaluator.evaluate(env, "map ints '(^ \$it 2)").wrapped as List<Int>).containsExactly(1, 4, 9, 16, 25).inOrder()
        assertThat(evaluator.evaluate(env, "map ints '(to-string \$it)").wrapped as List<String>).containsExactly("1", "2", "3", "4", "5").inOrder()
        assertThat(evaluator.evaluate(env, "map ints '\$it").wrapped as List<Int>).containsExactly(1, 2, 3, 4, 5).inOrder()
    }

    @Test
    fun testFirstMethod() = runTest {
        val env = Environment()
        env.storeValue("_", Value.Placeholder)
        env.addMethod(FirstMethod())
        env.addMethod(GreaterThanMethod())
        env.addMethod(RemainderMethod())
        env.addMethod(EqualsMethod())
        env.storeValue("false", Value.False)

        val evaluator = Evaluator()

        env.storeValue("ints", Value((1 .. 10).toList()))

        assertThat(evaluator.evaluate(env, "first ints '(> \$it 5)").wrapped).isEqualTo(6)
        assertThat(evaluator.evaluate(env, "first ints '(= % \$it 2 0)").wrapped).isEqualTo(2)
        // Placeholder just means get the very first item in the list (although you can use list-get 0 as well)
        assertThat(evaluator.evaluate(env, "first ints _").wrapped).isEqualTo(1)

        assertThrows<EvaluationException> {
            evaluator.evaluate(env, "first ints 'false")
        }
    }

    @Test
    fun testSingleMethod() = runTest {
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
    fun testShuffleMethod() = runTest {
        val env = Environment(Random(123)) // Fixed seed so that we get the same shuffle everytime for this test
        env.addMethod(ShuffleMethod())
        env.storeValue("ints", Value(listOf(1, 2, 3, 4, 5)))

        val evaluator = Evaluator()

        assertThat(evaluator.evaluate(env, "shuffle ints").wrapped as List<Int>)
            .containsExactly(3, 1, 4, 5, 2).inOrder()

        // Original ints are not affected
        assertThat(evaluator.evaluate(env, "ints").wrapped as List<Int>)
            .containsExactly(1, 2, 3, 4, 5).inOrder()
    }

    @Test
    fun testSortMethod() = runTest {
        val env = Environment()

        val evaluator = Evaluator()
        env.addMethod(SortMethod())
        env.addMethod(CompareMethod())
        env.addMethod(ToIntMethod())
        env.storeValue("ints", Value(listOf(3, 1, 4, 5, 2)))
        env.storeValue("str-numbers", Value(listOf("3", "2", "1", "30", "20", "10", "20")))

        assertThat(evaluator.evaluate(env, "sort ints").wrapped as List<Int>)
            .containsExactly(1, 2, 3, 4, 5).inOrder()

        assertThat(evaluator.evaluate(env, "sort --order 'descending ints").wrapped as List<Int>)
            .containsExactly(5, 4, 3, 2, 1).inOrder()

        assertThat(evaluator.evaluate(env, "sort str-numbers").wrapped as List<String>)
            .containsExactly("1", "10", "2", "20", "20", "3", "30").inOrder()

        assertThat(evaluator.evaluate(env, "sort --with '(compare to-int \$l to-int \$r) str-numbers").wrapped as List<String>)
            .containsExactly("1", "2", "3", "10", "20", "20", "30").inOrder()

        assertThat(evaluator.evaluate(env, "sort --order 'descending --with '(compare to-int \$l to-int \$r) str-numbers").wrapped as List<String>)
            .containsExactly("30", "20", "20", "10", "3", "2", "1").inOrder()

        // Original lists are not affected
        assertThat(evaluator.evaluate(env, "ints").wrapped as List<Int>).containsExactly(3, 1, 4, 5, 2).inOrder()
        assertThat(evaluator.evaluate(env, "str-numbers").wrapped as List<String>)
            .containsExactly("3", "2", "1", "30", "20", "10", "20").inOrder()
    }

    @Test
    fun testUnionMethod() = runTest {
        val env = Environment()
        env.addMethod(UnionMethod())
        env.storeValue("ints1", Value(listOf(1, 2, 3, 4, 5)))
        env.storeValue("ints2", Value(listOf(6, 7, 8, 9, 10)))
        env.storeValue("ints3", Value(listOf(11, 12, 13, 14, 15)))

        val evaluator = Evaluator()

        assertThat(evaluator.evaluate(env, "union ints1 ints2 ints3").wrapped as List<Int>)
            .containsExactly(1 .. 15).inOrder()

        assertThat(evaluator.evaluate(env, "union").wrapped as List<Int>).isEmpty()
    }
}
package dev.bitspittle.limp.methods

import com.varabyte.truthish.assertThat
import com.varabyte.truthish.assertThrows
import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.exceptions.EvaluationException
import dev.bitspittle.limp.methods.collection.*
import dev.bitspittle.limp.methods.compare.CompareMethod
import dev.bitspittle.limp.methods.compare.EqualsMethod
import dev.bitspittle.limp.methods.compare.GreaterThanMethod
import dev.bitspittle.limp.methods.compare.LessThanMethod
import dev.bitspittle.limp.methods.convert.ToIntMethod
import dev.bitspittle.limp.methods.convert.ToStringMethod
import dev.bitspittle.limp.methods.math.PowMethod
import dev.bitspittle.limp.methods.math.RemainderMethod
import dev.bitspittle.limp.methods.system.SetMethod
import dev.bitspittle.limp.types.ConsoleLogger
import dev.bitspittle.limp.types.Placeholder
import kotlinx.coroutines.test.runTest
import kotlin.random.Random
import kotlin.test.Test

@Suppress("UNCHECKED_CAST")
class CollectionMethodsTest {
    @Test
    fun testInMethod() = runTest {
        val env = Environment()
        env.addMethod(InMethod())
        env.addMethod(SetMethod(ConsoleLogger()))
        env.addMethod(GreaterThanMethod())
        env.addMethod(LessThanMethod())
        env.addMethod(ListMethod())
        env.addMethod(UnionMethod())

        val evaluator = Evaluator()
        evaluator.evaluate(env, "set 'ints (list 1 2 3 4 5)")
        evaluator.evaluate(env, "set 'fruits (list \"Apple\" \"Banana\" \"Cantaloupe\")")
        evaluator.evaluate(env, "set 'empty (list)")
        evaluator.evaluate(env, "set 'mixed (union ints fruits)")

        assertThat(evaluator.evaluate(env, "in? ints 3") as Boolean).isTrue()
        assertThat(evaluator.evaluate(env, "in? ints 6") as Boolean).isFalse()
        assertThat(evaluator.evaluate(env, "in? ints '(> \$it 4)") as Boolean).isTrue()
        assertThat(evaluator.evaluate(env, "in? ints '(< \$it 0)") as Boolean).isFalse()
        assertThat(evaluator.evaluate(env, "in? ints \"6\"") as Boolean).isFalse()

        assertThat(evaluator.evaluate(env, "in? fruits \"Banana\"") as Boolean).isTrue()
        assertThat(evaluator.evaluate(env, "in? fruits \"Cherry\"") as Boolean).isFalse()
        assertThat(evaluator.evaluate(env, "in? fruits \"banana\"") as Boolean).isFalse()

        assertThat(evaluator.evaluate(env, "in? empty 3") as Boolean).isFalse()
        assertThat(evaluator.evaluate(env, "in? empty \"Apple\"") as Boolean).isFalse()

        assertThat(evaluator.evaluate(env, "in? mixed 3") as Boolean).isTrue()
        assertThat(evaluator.evaluate(env, "in? mixed \"Apple\"") as Boolean).isTrue()
    }

    @Test
    fun testIndexOfMethod() = runTest {
        val env = Environment()
        env.addMethod(IndexOfMethod())
        env.addMethod(SetMethod(ConsoleLogger()))
        env.addMethod(GreaterThanMethod())
        env.addMethod(LessThanMethod())
        env.addMethod(ListMethod())

        val evaluator = Evaluator()
        evaluator.evaluate(env, "set 'ints (list 1 2 3 4 5)")
        evaluator.evaluate(env, "set 'empty (list)")

        assertThat(evaluator.evaluate(env, "index-of ints 3")).isEqualTo(2)
        assertThat(evaluator.evaluate(env, "index-of ints 6")).isEqualTo(-1)
        assertThat(evaluator.evaluate(env, "index-of ints '(> \$it 4)")).isEqualTo(4)
        assertThat(evaluator.evaluate(env, "index-of ints '(< \$it 0)")).isEqualTo(-1)

        // When there are dupes, first match returned
        assertThat(evaluator.evaluate(env, "index-of (list 1 2 2 2 2 2) 2")).isEqualTo(1)
    }

    @Test
    fun testEmptyMethod() = runTest {
        val env = Environment()
        env.addMethod(EmptyMethod())
        env.addMethod(SetMethod(ConsoleLogger()))
        env.addMethod(ListMethod())

        val evaluator = Evaluator()
        evaluator.evaluate(env, "set 'ints (list 1 2 3 4 5)")
        evaluator.evaluate(env, "set 'empty (list)")

        assertThat(evaluator.evaluate(env, "empty? ints") as Boolean).isFalse()
        assertThat(evaluator.evaluate(env, "empty? empty") as Boolean).isTrue()
    }

    @Test
    fun testAnyMethod() = runTest {
        val env = Environment()
        env.addMethod(AnyMethod())
        env.addMethod(SetMethod(ConsoleLogger()))
        env.addMethod(GreaterThanMethod())
        env.addMethod(ListMethod())
        env.storeValue("true", true)

        val evaluator = Evaluator()
        evaluator.evaluate(env, "set '\$ints (list 1 2 3 4 5)")
        evaluator.evaluate(env, "set '\$empty (list)")

        assertThat(evaluator.evaluate(env, "any? \$ints '(> \$it 3)") as Boolean).isTrue()
        assertThat(evaluator.evaluate(env, "any? \$ints '(> \$it 8)") as Boolean).isFalse()
        // Any must have at least one match
        assertThat(evaluator.evaluate(env, "any? \$empty 'true") as Boolean).isFalse()

        assertThrows<EvaluationException> {
            evaluator.evaluate(env, "any? \$ints 3") // Second argument must be an expression
        }
    }

    @Test
    fun testAllMethod() = runTest {
        val env = Environment()
        env.addMethod(AllMethod())
        env.addMethod(SetMethod(ConsoleLogger()))
        env.addMethod(GreaterThanMethod())
        env.addMethod(ListMethod())
        env.storeValue("true", true)

        val evaluator = Evaluator()
        evaluator.evaluate(env, "set '\$ints (list 1 2 3 4 5)")
        evaluator.evaluate(env, "set '\$empty (list)")

        assertThat(evaluator.evaluate(env, "all? \$ints '(> \$it 0)") as Boolean).isTrue()
        assertThat(evaluator.evaluate(env, "all? \$ints '(> \$it 3)") as Boolean).isFalse()
        // Always true for empty lists
        assertThat(evaluator.evaluate(env, "all? \$empty 'false") as Boolean).isTrue()

        assertThrows<EvaluationException> {
            evaluator.evaluate(env, "all? \$ints 3") // Second argument must be an expression
        }
    }

    @Test
    fun testNoneMethod() = runTest {
        val env = Environment()
        env.addMethod(NoneMethod())
        env.addMethod(SetMethod(ConsoleLogger()))
        env.addMethod(GreaterThanMethod())
        env.addMethod(ListMethod())
        env.storeValue("true", true)

        val evaluator = Evaluator()
        evaluator.evaluate(env, "set '\$ints (list 1 2 3 4 5)")
        evaluator.evaluate(env, "set '\$empty (list)")

        assertThat(evaluator.evaluate(env, "none? \$ints '(> \$it 5)") as Boolean).isTrue()
        assertThat(evaluator.evaluate(env, "none? \$ints '(> \$it 3)") as Boolean).isFalse()
        // Always true for empty lists
        assertThat(evaluator.evaluate(env, "none? \$empty 'true") as Boolean).isTrue()

        assertThrows<EvaluationException> {
            evaluator.evaluate(env, "none? \$ints 3") // Second argument must be an expression
        }
    }

    @Test
    fun testTakeMethod() = runTest {
        val env = Environment()
        val random = Random(123)
        env.addMethod(TakeMethod { random })
        env.storeValue("_", Placeholder)

        val evaluator = Evaluator()

        env.storeValue("ints", listOf(1, 2, 3, 4, 5))
        env.storeValue("strs", listOf("Aa", "Bb", "Cc", "Dd"))

        assertThat(evaluator.evaluate(env, "take ints 2") as List<Int>).containsExactly(1, 2).inOrder()
        assertThat(evaluator.evaluate(env, "take ints 8") as List<Int>).containsExactly(1, 2, 3, 4, 5).inOrder()
        assertThat(evaluator.evaluate(env, "take ints 0") as List<Int>).isEmpty()
        assertThat(evaluator.evaluate(env, "take ints _") as List<Int>).containsExactly(1, 2, 3, 4, 5).inOrder()

        assertThat(evaluator.evaluate(env, "take --from 'back ints 2") as List<Int>).containsExactly(4, 5).inOrder()
        assertThat(evaluator.evaluate(env, "take --from 'back ints _") as List<Int>).containsExactly(1, 2, 3, 4, 5).inOrder()

        assertThat(evaluator.evaluate(env, "take --from 'random ints 3") as List<Int>).containsExactly(3, 1, 4).inOrder()

        assertThat(evaluator.evaluate(env, "take strs 2") as List<String>).containsExactly("Aa", "Bb").inOrder()
        assertThat(evaluator.evaluate(env, "take --from 'back strs 2") as List<String>).containsExactly("Cc", "Dd").inOrder()

        assertThrows<EvaluationException> {
            // Negative numbers are not allowed
            evaluator.evaluate(env, "take ints -5")
        }
    }

    @Test
    fun testDropMethod() = runTest {
        val env = Environment()
        val random = Random(123)
        env.addMethod(DropMethod { random })

        val evaluator = Evaluator()

        env.storeValue("ints", listOf(1, 2, 3, 4, 5))
        env.storeValue("strs", listOf("Aa", "Bb", "Cc", "Dd"))

        assertThat(evaluator.evaluate(env, "drop ints 2") as List<Int>).containsExactly(3, 4, 5).inOrder()
        assertThat(evaluator.evaluate(env, "drop ints 8") as List<Int>).isEmpty()
        assertThat(evaluator.evaluate(env, "drop ints 0") as List<Int>).containsExactly(1, 2, 3, 4, 5).inOrder()

        assertThat(evaluator.evaluate(env, "drop --from 'back ints 2") as List<Int>).containsExactly(1, 2, 3).inOrder()
        assertThat(evaluator.evaluate(env, "drop --from 'random ints 3") as List<Int>).containsExactly(2, 5).inOrder()

        assertThat(evaluator.evaluate(env, "drop strs 2") as List<String>).containsExactly("Cc", "Dd").inOrder()
        assertThat(evaluator.evaluate(env, "drop --from 'back strs 2") as List<String>).containsExactly("Aa", "Bb").inOrder()

        assertThrows<EvaluationException> {
            // Negative numbers are not allowed
            evaluator.evaluate(env, "drop ints -5")
        }
    }

    @Test
    fun testFilterMethod() = runTest {
        val env = Environment()
        env.addMethod(FilterMethod())
        env.addMethod(GreaterThanMethod())
        env.addMethod(RemainderMethod())
        env.addMethod(EqualsMethod())
        env.storeValue("false", false)

        val evaluator = Evaluator()

        env.storeValue("ints", (1 .. 10).toList())

        assertThat(evaluator.evaluate(env, "filter ints '(> \$it 5)") as List<Int>).containsExactly(6, 7, 8, 9, 10).inOrder()
        assertThat(evaluator.evaluate(env, "filter ints '(= % \$it 2 0)") as List<Int>).containsExactly(2, 4, 6, 8, 10).inOrder()
        assertThat(evaluator.evaluate(env, "filter ints 'false") as List<Int>).isEmpty()
    }

    @Test
    fun testMapMethod() = runTest {
        val env = Environment()
        env.addMethod(MapMethod())
        env.addMethod(PowMethod())
        env.addMethod(ToStringMethod())
        env.storeValue("ints", (1..5).toList())

        val evaluator = Evaluator()

        assertThat(evaluator.evaluate(env, "map ints '(^ \$it 2)") as List<Int>).containsExactly(1, 4, 9, 16, 25).inOrder()
        assertThat(evaluator.evaluate(env, "map ints '(to-string \$it)") as List<String>).containsExactly("1", "2", "3", "4", "5").inOrder()
        assertThat(evaluator.evaluate(env, "map ints '\$it") as List<Int>).containsExactly(1, 2, 3, 4, 5).inOrder()
    }

    @Test
    fun testFirstMethod() = runTest {
        val env = Environment()
        env.storeValue("_", Placeholder)
        env.addMethod(FirstMethod())
        env.addMethod(GreaterThanMethod())
        env.addMethod(RemainderMethod())
        env.addMethod(EqualsMethod())
        env.storeValue("false", false)

        val evaluator = Evaluator()

        env.storeValue("\$ints", (1 .. 10).toList())

        assertThat(evaluator.evaluate(env, "first --matching '(> \$it 5) \$ints")).isEqualTo(6)
        assertThat(evaluator.evaluate(env, "first --matching '(= % \$it 2 0) \$ints")).isEqualTo(2)

        // No "matching" just means get the very first item in the list (although you can use list-get 0 as well)
        assertThat(evaluator.evaluate(env, "first \$ints")).isEqualTo(1)

        assertThrows<EvaluationException> {
            evaluator.evaluate(env, "first --matching 'false \$ints")
        }
    }

    @Test
    fun testSingleMethod() = runTest {
        val env = Environment()
        env.addMethod(SingleMethod())
        env.addMethod(GreaterThanMethod())
        env.addMethod(RemainderMethod())
        env.addMethod(EqualsMethod())
        env.storeValue("false", false)

        val evaluator = Evaluator()

        env.storeValue("\$int", listOf(6))
        env.storeValue("\$ints", (1 .. 10).toList())

        assertThat(evaluator.evaluate(env, "single \$int")).isEqualTo(6)

        assertThat(evaluator.evaluate(env, "single --matching '(= \$it 8) \$ints")).isEqualTo(8)
        assertThat(evaluator.evaluate(env, "single --matching '(= % \$it 7 0) \$ints")).isEqualTo(7)

        assertThat(evaluator.evaluate(env, "single --matching '(= % \$it 7 0) \$ints")).isEqualTo(7)

        assertThrows<EvaluationException> {
            // Single must not return an empty list
            evaluator.evaluate(env, "single --matching 'false \$ints")
        }

        assertThrows<EvaluationException> {
            // Single must not return a list with 2+ elements
            evaluator.evaluate(env, "single \$ints")
        }
    }

    @Test
    fun testShuffledMethod() = runTest {
        val env = Environment()
        val random = Random(123)
        env.addMethod(ShuffledMethod { random })
        env.storeValue("ints", listOf(1, 2, 3, 4, 5))

        val evaluator = Evaluator()

        assertThat(evaluator.evaluate(env, "shuffled ints") as List<Int>)
            .containsExactly(3, 1, 4, 5, 2).inOrder()

        // Original ints are not affected
        assertThat(evaluator.evaluate(env, "ints") as List<Int>)
            .containsExactly(1, 2, 3, 4, 5).inOrder()
    }

    @Test
    fun testShuffleMethod() = runTest {
        val env = Environment()
        val random = Random(123)
        env.addMethod(ShuffleMethod { random })
        // Limp does not (at the moment) support creating mutable lists, but they can be passed in via code
        env.storeValue("ints", mutableListOf(1, 2, 3, 4, 5))

        val evaluator = Evaluator()

        evaluator.evaluate(env, "shuffle! ints")
        assertThat(evaluator.evaluate(env, "ints") as List<Int>)
            .containsExactly(3, 1, 4, 5, 2).inOrder()
    }

    @Test
    fun testRandomMethod() = runTest {
        val env = Environment()
        val random = Random(123)
        env.addMethod(RandomMethod { random })
        env.storeValue("\$ints", listOf(1, 2, 3, 4, 5))
        env.storeValue("\$strs", listOf("A", "B", "C"))

        val evaluator = Evaluator()

        assertThat(evaluator.evaluate(env, "random \$ints")).isEqualTo(2)
        assertThat(evaluator.evaluate(env, "random \$ints")).isEqualTo(1)
        assertThat(evaluator.evaluate(env, "random \$ints")).isEqualTo(4)

        assertThat(evaluator.evaluate(env, "random \$strs")).isEqualTo("A")
        assertThat(evaluator.evaluate(env, "random \$strs")).isEqualTo("A")
        assertThat(evaluator.evaluate(env, "random \$strs")).isEqualTo("C")
    }

    @Test
    fun testSortedMethod() = runTest {
        val env = Environment()

        val evaluator = Evaluator()
        env.addMethod(SortedMethod())
        env.addMethod(CompareMethod())
        env.addMethod(ToIntMethod())
        env.storeValue("empty", listOf<Int>())
        env.storeValue("ints", listOf(3, 1, 4, 5, 2))
        env.storeValue("str-numbers", listOf("3", "2", "1", "30", "20", "10", "20"))

        assertThat(evaluator.evaluate(env, "sorted empty") as List<*>).isEmpty()
        assertThat(evaluator.evaluate(env, "sorted --with '(compare \$a \$b) empty") as List<*>).isEmpty()

        assertThat(evaluator.evaluate(env, "sorted ints") as List<Int>)
            .containsExactly(1, 2, 3, 4, 5).inOrder()

        assertThat(evaluator.evaluate(env, "sorted --order 'descending ints") as List<Int>)
            .containsExactly(5, 4, 3, 2, 1).inOrder()

        assertThat(evaluator.evaluate(env, "sorted str-numbers") as List<String>)
            .containsExactly("1", "10", "2", "20", "20", "3", "30").inOrder()

        assertThat(evaluator.evaluate(env, "sorted --with '(compare to-int \$a to-int \$b) str-numbers") as List<String>)
            .containsExactly("1", "2", "3", "10", "20", "20", "30").inOrder()

        assertThat(evaluator.evaluate(env, "sorted --order 'descending --with '(compare to-int \$a to-int \$b) str-numbers") as List<String>)
            .containsExactly("30", "20", "20", "10", "3", "2", "1").inOrder()

        // Original lists are not affected
        assertThat(evaluator.evaluate(env, "ints") as List<Int>).containsExactly(3, 1, 4, 5, 2).inOrder()
        assertThat(evaluator.evaluate(env, "str-numbers") as List<String>)
            .containsExactly("3", "2", "1", "30", "20", "10", "20").inOrder()
    }

    @Test
    fun testReversedMethod() = runTest {
        val env = Environment()

        val evaluator = Evaluator()
        env.addMethod(ReversedMethod())

        env.storeValue("\$ints", listOf(1, 2, 3, 4, 5))
        env.storeValue("\$strs", listOf("A", "B", "C"))

        assertThat(evaluator.evaluate(env, "reversed \$ints") as List<Int>)
            .containsExactly(5, 4, 3, 2, 1).inOrder()

        assertThat(evaluator.evaluate(env, "reversed \$strs") as List<String>)
            .containsExactly("C", "B", "A").inOrder()
    }

    @Test
    fun testDistinctMethod() = runTest {
        val env = Environment()

        val evaluator = Evaluator()
        val random = Random(123)
        env.addMethod(DistinctMethod())
        env.addMethod(ShuffledMethod { random })

        env.storeValue("\$ints", listOf(1, 2, 2, 3, 3, 3, 4, 4, 4, 4, 5, 5, 5, 5, 5))
        env.storeValue("\$strs", listOf("A", "B", "B", "C", "C", "C", "D", "D", "D", "D"))

        assertThat(evaluator.evaluate(env, "distinct \$ints") as List<Int>)
            .containsExactly(1, 2, 3, 4, 5).inOrder()

        assertThat(evaluator.evaluate(env, "distinct \$strs") as List<String>)
            .containsExactly("A", "B", "C", "D").inOrder()

        // Even if we shuffle the list it will still be distinct-ified
        run {
            assertThat(evaluator.evaluate(env, "distinct shuffled \$ints") as List<Int>)
                .containsExactly(1, 2, 3, 4, 5)

            assertThat(evaluator.evaluate(env, "distinct shuffled \$strs") as List<String>)
                .containsExactly("A", "B", "C", "D")
        }
    }

    @Test
    fun testUnionMethod() = runTest {
        val env = Environment()
        env.addMethod(UnionMethod())
        env.storeValue("ints1", listOf(1, 2, 3, 4, 5))
        env.storeValue("ints2", listOf(6, 7, 8, 9, 10))
        env.storeValue("ints3", listOf(11, 12, 13, 14, 15))

        val evaluator = Evaluator()

        assertThat(evaluator.evaluate(env, "union ints1 ints2 ints3") as List<Int>)
            .containsExactly(1 .. 15).inOrder()

        assertThat(evaluator.evaluate(env, "union") as List<Int>).isEmpty()
    }

    @Test
    fun testRepeatMethod() = runTest {
        val env = Environment()
        env.addMethod(RepeatMethod())

        val evaluator = Evaluator()

        assertThat(evaluator.evaluate(env, "repeat 1 5")).isEqualTo(listOf(1, 1, 1, 1, 1))
        assertThat(evaluator.evaluate(env, "repeat \"A\" 3")).isEqualTo(listOf("A", "A", "A"))
        assertThat(evaluator.evaluate(env, "repeat 12345 0") as List<Int>).isEmpty()

        // Negative counts are not supported
        assertThrows<EvaluationException> {
            evaluator.evaluate(env, "repeat 12345 -1")
        }.also { ex ->
            assertThat(ex.cause).isInstanceOf<IllegalArgumentException>()
        }
    }
}
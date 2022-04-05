package dev.bitspittle.limp.methods

import com.varabyte.truthish.assertThat
import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.methods.compare.*
import dev.bitspittle.limp.methods.math.*
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class CompareMethodsTest {
    @Test
    fun testComparisonMethods() = runTest {
        val env = Environment()
        val methods = listOf(
            LessThanMethod(),
            LessThanEqualsMethod(),
            EqualsMethod(),
            NotEqualsMethod(),
            GreaterThanMethod(),
            GreaterThanEqualsMethod(),
        )
        methods.forEach { env.addMethod(it) }

        val evaluator = Evaluator()

        // Works on numbers of course...
        assertThat(evaluator.evaluate(env, "< 1 2") as Boolean).isTrue()
        assertThat(evaluator.evaluate(env, "<= 1 2") as Boolean).isTrue()
        assertThat(evaluator.evaluate(env, "= 1 2") as Boolean).isFalse()
        assertThat(evaluator.evaluate(env, "!= 1 2") as Boolean).isTrue()
        assertThat(evaluator.evaluate(env, ">= 1 2") as Boolean).isFalse()
        assertThat(evaluator.evaluate(env, "> 1 2") as Boolean).isFalse()

        assertThat(evaluator.evaluate(env, "< 2 2") as Boolean).isFalse()
        assertThat(evaluator.evaluate(env, "<= 2 2") as Boolean).isTrue()
        assertThat(evaluator.evaluate(env, "= 2 2") as Boolean).isTrue()
        assertThat(evaluator.evaluate(env, "!= 2 2") as Boolean).isFalse()
        assertThat(evaluator.evaluate(env, ">= 2 2") as Boolean).isTrue()
        assertThat(evaluator.evaluate(env, "> 2 2") as Boolean).isFalse()

        assertThat(evaluator.evaluate(env, "< 3 2") as Boolean).isFalse()
        assertThat(evaluator.evaluate(env, "<= 3 2") as Boolean).isFalse()
        assertThat(evaluator.evaluate(env, "= 3 2") as Boolean).isFalse()
        assertThat(evaluator.evaluate(env, "!= 3 2") as Boolean).isTrue()
        assertThat(evaluator.evaluate(env, ">= 3 2") as Boolean).isTrue()
        assertThat(evaluator.evaluate(env, "> 3 2") as Boolean).isTrue()

        // But also works on strings!
        assertThat(evaluator.evaluate(env, "< \"a\" \"b\"") as Boolean).isTrue()
        assertThat(evaluator.evaluate(env, "<= \"a\" \"b\"") as Boolean).isTrue()
        assertThat(evaluator.evaluate(env, "= \"a\" \"b\"") as Boolean).isFalse()
        assertThat(evaluator.evaluate(env, "!= \"a\" \"b\"") as Boolean).isTrue()
        assertThat(evaluator.evaluate(env, ">= \"a\" \"b\"") as Boolean).isFalse()
        assertThat(evaluator.evaluate(env, "> \"a\" \"b\"") as Boolean).isFalse()

        assertThat(evaluator.evaluate(env, "< \"b\" \"b\"") as Boolean).isFalse()
        assertThat(evaluator.evaluate(env, "<= \"b\" \"b\"") as Boolean).isTrue()
        assertThat(evaluator.evaluate(env, "= \"b\" \"b\"") as Boolean).isTrue()
        assertThat(evaluator.evaluate(env, "!= \"b\" \"b\"") as Boolean).isFalse()
        assertThat(evaluator.evaluate(env, ">= \"b\" \"b\"") as Boolean).isTrue()
        assertThat(evaluator.evaluate(env, "> \"b\" \"b\"") as Boolean).isFalse()

        assertThat(evaluator.evaluate(env, "< \"c\" \"b\"") as Boolean).isFalse()
        assertThat(evaluator.evaluate(env, "<= \"c\" \"b\"") as Boolean).isFalse()
        assertThat(evaluator.evaluate(env, "= \"c\" \"b\"") as Boolean).isFalse()
        assertThat(evaluator.evaluate(env, "!= \"c\" \"b\"") as Boolean).isTrue()
        assertThat(evaluator.evaluate(env, ">= \"c\" \"b\"") as Boolean).isTrue()
        assertThat(evaluator.evaluate(env, "> \"c\" \"b\"") as Boolean).isTrue()
    }

    @Test
    fun testCompareMethod() = runTest {
        val env = Environment()
        env.addMethod(CompareMethod())

        val evaluator = Evaluator()

        assertThat(evaluator.evaluate(env, "compare 1 2") as Int).isLessThan(0)
        assertThat(evaluator.evaluate(env, "compare 2 2") as Int).isEqualTo(0)
        assertThat(evaluator.evaluate(env, "compare 3 2") as Int).isGreaterThan(0)
    }
}
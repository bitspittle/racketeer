package dev.bitspittle.limp.methods

import com.varabyte.truthish.assertThat
import com.varabyte.truthish.assertThrows
import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.exceptions.EvaluationException
import dev.bitspittle.limp.methods.collection.ListMethod
import dev.bitspittle.limp.methods.text.ConcatMethod
import dev.bitspittle.limp.methods.text.JoinToStringMethod
import dev.bitspittle.limp.methods.text.LowerMethod
import dev.bitspittle.limp.methods.text.UpperMethod
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class TextMethodsTest {
    @Test
    fun testConcatMethod() = runTest {
        val env = Environment()
        env.addMethod(ConcatMethod())

        val evaluator = Evaluator()

        assertThat(evaluator.evaluate(env, "concat \"Hello \" \"World\"")).isEqualTo("Hello World")

        assertThrows<EvaluationException> {
            // Only strings!
            evaluator.evaluate(env, "concat \"123\" 456")
        }
    }

    @Test
    fun testUpperMethod() = runTest {
        val env = Environment()
        env.addMethod(UpperMethod())

        val evaluator = Evaluator()

        assertThat(evaluator.evaluate(env, "upper \"Hello There!? 123\"")).isEqualTo("HELLO THERE!? 123")
        assertThrows<EvaluationException> {
            // Only strings
            evaluator.evaluate(env, "upper 123")
        }
    }

    @Test
    fun testLowerMethod() = runTest {
        val env = Environment()
        env.addMethod(LowerMethod())

        val evaluator = Evaluator()

        assertThat(evaluator.evaluate(env, "lower \"Hello There!? 123\"")).isEqualTo("hello there!? 123")
        assertThrows<EvaluationException> {
            // Only strings
            evaluator.evaluate(env, "lower 123")
        }
    }

    @Test
    fun testJoinToStringMethod() = runTest {
        val env = Environment()
        env.addMethod(JoinToStringMethod())
        env.addMethod(ConcatMethod())
        env.addMethod(UpperMethod())
        env.addMethod(ListMethod())

        val evaluator = Evaluator()
        assertThat(
            evaluator.evaluate(env, "join-to-string (list \"a\" \"b\" \"c\" \"d\")")
        ).isEqualTo("a, b, c, d")

        assertThat(
            evaluator.evaluate(env, "join-to-string (list 1 2 3 4)")
        ).isEqualTo("1, 2, 3, 4")

        assertThat(
            evaluator.evaluate(env, "join-to-string --separator \" - \" (list \"a\" \"b\" \"c\" \"d\")")
        ).isEqualTo("a - b - c - d")

        assertThat(
            evaluator.evaluate(env, "join-to-string --format '(upper \$it) (list \"a\" \"b\" \"c\" \"d\")")
        ).isEqualTo("A, B, C, D")

        assertThat(
            evaluator.evaluate(env, "join-to-string --separator \" - \" --format '(concat \$it \$it) (list \"a\" \"b\" \"c\" \"d\")")
        ).isEqualTo("aa - bb - cc - dd")
    }
}
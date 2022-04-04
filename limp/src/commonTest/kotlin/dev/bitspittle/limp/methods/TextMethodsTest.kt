package dev.bitspittle.limp.methods

import com.varabyte.truthish.assertThat
import com.varabyte.truthish.assertThrows
import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.Value
import dev.bitspittle.limp.methods.collection.ListMethod
import dev.bitspittle.limp.methods.text.ConcatMethod
import dev.bitspittle.limp.methods.text.JoinToStringMethod
import dev.bitspittle.limp.methods.text.LowerMethod
import dev.bitspittle.limp.methods.text.UpperMethod
import kotlin.test.Test

class TextMethodsTest {
    @Test
    fun testConcatMethod() {
        val env = Environment()
        val method = ConcatMethod()

        assertThat(method.invoke(env, listOf(Value("Hello "), Value("World"))).wrapped).isEqualTo("Hello World")
        assertThat(method.invoke(env, listOf(Value('a'), Value('z'))).wrapped).isEqualTo("az")

        assertThrows<IllegalArgumentException> {
            // Only strings!
            method.invoke(env, listOf(Value(123), Value("456"))).wrapped
        }
    }

    @Test
    fun testUpperMethod() {
        val env = Environment()
        val method = UpperMethod()

        assertThat(method.invoke(env, listOf(Value("Hello There!? 123"))).wrapped).isEqualTo("HELLO THERE!? 123")
        assertThrows<IllegalArgumentException> {
            // Only strings
            method.invoke(env, listOf(Value(123))).wrapped
        }
    }

    @Test
    fun toLowerMethod() {
        val env = Environment()
        val method = LowerMethod()

        assertThat(method.invoke(env, listOf(Value("Hello There!? 123"))).wrapped).isEqualTo("hello there!? 123")
        assertThrows<IllegalArgumentException> {
            // Only strings
            method.invoke(env, listOf(Value(123))).wrapped
        }
    }

    @Test
    fun testJoinToStringMethod() {
        val env = Environment()
        env.addMethod(JoinToStringMethod())
        env.addMethod(ConcatMethod())
        env.addMethod(UpperMethod())
        env.addMethod(ListMethod())

        val evaluator = Evaluator()
        assertThat(
            evaluator.evaluate(env, "join-to-string (list \"a\" \"b\" \"c\" \"d\")").wrapped
        ).isEqualTo("a, b, c, d")

        assertThat(
            evaluator.evaluate(env, "join-to-string (list 1 2 3 4)").wrapped
        ).isEqualTo("1, 2, 3, 4")

        assertThat(
            evaluator.evaluate(env, "join-to-string --separator \" - \" (list \"a\" \"b\" \"c\" \"d\")").wrapped
        ).isEqualTo("a - b - c - d")

        assertThat(
            evaluator.evaluate(env, "join-to-string --format '(upper \$it) (list \"a\" \"b\" \"c\" \"d\")").wrapped
        ).isEqualTo("A, B, C, D")

        assertThat(
            evaluator.evaluate(env, "join-to-string --separator \" - \" --format '(concat \$it \$it) (list \"a\" \"b\" \"c\" \"d\")").wrapped
        ).isEqualTo("aa - bb - cc - dd")
    }
}
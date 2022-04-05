package dev.bitspittle.limp.methods

import com.varabyte.truthish.assertThat
import com.varabyte.truthish.assertThrows
import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.exceptions.EvaluationException
import dev.bitspittle.limp.methods.logic.AndMethod
import dev.bitspittle.limp.methods.logic.IfMethod
import dev.bitspittle.limp.methods.logic.NotMethod
import dev.bitspittle.limp.methods.logic.OrMethod
import dev.bitspittle.limp.methods.math.AddMethod
import dev.bitspittle.limp.types.Placeholder
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class LogicMethodsTest {
    @Test
    fun testNotMethod() = runTest {
        val env = Environment()
        env.addMethod(NotMethod())
        env.storeValue("true", true)
        env.storeValue("false", false)

        val evaluator = Evaluator()

        assertThat(evaluator.evaluate(env, "! true") as Boolean).isFalse()
        assertThat(evaluator.evaluate(env, "! false") as Boolean).isTrue()
    }

    @Test
    fun testAndMethod() = runTest {
        val env = Environment()
        env.addMethod(AndMethod())
        env.storeValue("true", true)
        env.storeValue("false", false)

        val evaluator = Evaluator()

        assertThat(evaluator.evaluate(env, "&& true true") as Boolean).isTrue()
        assertThat(evaluator.evaluate(env, "&& true false") as Boolean).isFalse()
        assertThat(evaluator.evaluate(env, "&& false true") as Boolean).isFalse()
        assertThat(evaluator.evaluate(env, "&& false false") as Boolean).isFalse()
    }

    @Test
    fun testOrMethod() = runTest {
        val env = Environment()
        env.addMethod(OrMethod())
        env.storeValue("true", true)
        env.storeValue("false", false)

        val evaluator = Evaluator()

        assertThat(evaluator.evaluate(env, "|| true true") as Boolean).isTrue()
        assertThat(evaluator.evaluate(env, "|| true false") as Boolean).isTrue()
        assertThat(evaluator.evaluate(env, "|| false true") as Boolean).isTrue()
        assertThat(evaluator.evaluate(env, "|| false false") as Boolean).isFalse()
    }

    @Test
    fun testIfBranching() = runTest {
        val env = Environment()
        env.addMethod(IfMethod())
        env.addMethod(AddMethod())
        env.storeValue("true", true)
        env.storeValue("false", false)
        env.storeValue("_", Placeholder)

        val evaluator = Evaluator()
        assertThat(evaluator.evaluate(env, "if true '3 '4")).isEqualTo(3)
        assertThat(evaluator.evaluate(env, "if false '3 '4")).isEqualTo(4)

        assertThat(evaluator.evaluate(env, "if true _ '4")).isEqualTo(Unit)
        assertThat(evaluator.evaluate(env, "if false '3 _")).isEqualTo(Unit)

        assertThat(evaluator.evaluate(env, "if true '(+ 8 7) '(this would crash if run)")).isEqualTo(15)

        assertThrows<EvaluationException> {
            evaluator.evaluate(env, "if false '(+ 8 7) '(this would crash if run)")
        }
    }
}
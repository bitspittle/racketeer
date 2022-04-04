package dev.bitspittle.limp.methods

import com.varabyte.truthish.assertThat
import com.varabyte.truthish.assertThrows
import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.Value
import dev.bitspittle.limp.exceptions.EvaluationException
import dev.bitspittle.limp.methods.logic.AndMethod
import dev.bitspittle.limp.methods.logic.IfMethod
import dev.bitspittle.limp.methods.logic.NotMethod
import dev.bitspittle.limp.methods.logic.OrMethod
import dev.bitspittle.limp.methods.math.AddMethod
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class LogicMethodsTest {
    @Test
    fun testNotMethod() = runTest {
        val env = Environment()
        env.addMethod(NotMethod())
        env.storeValue("true", Value.True)
        env.storeValue("false", Value.False)

        val evaluator = Evaluator()

        assertThat(evaluator.evaluate(env, "! true").wrapped as Boolean).isFalse()
        assertThat(evaluator.evaluate(env, "! false").wrapped as Boolean).isTrue()
    }

    @Test
    fun testAndMethod() = runTest {
        val env = Environment()
        env.addMethod(AndMethod())
        env.storeValue("true", Value.True)
        env.storeValue("false", Value.False)

        val evaluator = Evaluator()

        assertThat(evaluator.evaluate(env, "&& true true").wrapped as Boolean).isTrue()
        assertThat(evaluator.evaluate(env, "&& true false").wrapped as Boolean).isFalse()
        assertThat(evaluator.evaluate(env, "&& false true").wrapped as Boolean).isFalse()
        assertThat(evaluator.evaluate(env, "&& false false").wrapped as Boolean).isFalse()
    }

    @Test
    fun testOrMethod() = runTest {
        val env = Environment()
        env.addMethod(OrMethod())
        env.storeValue("true", Value.True)
        env.storeValue("false", Value.False)

        val evaluator = Evaluator()

        assertThat(evaluator.evaluate(env, "|| true true").wrapped as Boolean).isTrue()
        assertThat(evaluator.evaluate(env, "|| true false").wrapped as Boolean).isTrue()
        assertThat(evaluator.evaluate(env, "|| false true").wrapped as Boolean).isTrue()
        assertThat(evaluator.evaluate(env, "|| false false").wrapped as Boolean).isFalse()
    }

    @Test
    fun testIfBranching() = runTest {
        val env = Environment()
        env.addMethod(IfMethod())
        env.addMethod(AddMethod())
        env.storeValue("true", Value.True)
        env.storeValue("false", Value.False)
        env.storeValue("_", Value.Placeholder)

        val evaluator = Evaluator()
        assertThat(evaluator.evaluate(env, "if true '3 '4").wrapped).isEqualTo(3)
        assertThat(evaluator.evaluate(env, "if false '3 '4").wrapped).isEqualTo(4)

        assertThat(evaluator.evaluate(env, "if true _ '4")).isEqualTo(Value.Empty)
        assertThat(evaluator.evaluate(env, "if false '3 _")).isEqualTo(Value.Empty)

        assertThat(evaluator.evaluate(env, "if true '(+ 8 7) '(this would crash if run)").wrapped).isEqualTo(15)

        assertThrows<EvaluationException> {
            evaluator.evaluate(env, "if false '(+ 8 7) '(this would crash if run)")
        }
    }
}
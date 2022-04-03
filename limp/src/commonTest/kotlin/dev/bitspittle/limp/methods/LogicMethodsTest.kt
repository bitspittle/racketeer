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
import dev.bitspittle.limp.methods.math.EqualsMethod
import dev.bitspittle.limp.methods.math.NotEqualsMethod
import dev.bitspittle.limp.methods.system.SetMethod
import kotlin.test.Test

class LogicMethodsTest {
    @Test
    fun testNotMethod() {
        val env = Environment()
        val method = NotMethod()

        assertThat(method.invoke(env, listOf(Value(true))).wrapped as Boolean).isFalse()
        assertThat(method.invoke(env, listOf(Value(false))).wrapped as Boolean).isTrue()
    }

    @Test
    fun testAndMethod() {
        val env = Environment()
        val method = AndMethod()

        assertThat(method.invoke(env, listOf(Value(true), Value(true))).wrapped).isEqualTo(true)
        assertThat(method.invoke(env, listOf(Value(true), Value(false))).wrapped).isEqualTo(false)
        assertThat(method.invoke(env, listOf(Value(false), Value(true))).wrapped).isEqualTo(false)
        assertThat(method.invoke(env, listOf(Value(false), Value(false))).wrapped).isEqualTo(false)
    }

    @Test
    fun testOrMethod() {
        val env = Environment()
        val method = OrMethod()

        assertThat(method.invoke(env, listOf(Value(true), Value(true))).wrapped).isEqualTo(true)
        assertThat(method.invoke(env, listOf(Value(true), Value(false))).wrapped).isEqualTo(true)
        assertThat(method.invoke(env, listOf(Value(false), Value(true))).wrapped).isEqualTo(true)
        assertThat(method.invoke(env, listOf(Value(false), Value(false))).wrapped).isEqualTo(false)
    }

    @Test
    fun testIfBranching() {
        val env = Environment()
        env.addMethod(IfMethod())
        env.addMethod(AddMethod())
        env.storeValue("true", Value(true))
        env.storeValue("false", Value(false))
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
package dev.bitspittle.limp.methods

import com.varabyte.truthish.assertThat
import com.varabyte.truthish.assertThrows
import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.exceptions.EvaluationException
import dev.bitspittle.limp.methods.compare.EqualsMethod
import dev.bitspittle.limp.methods.compare.NotEqualsMethod
import dev.bitspittle.limp.methods.math.*
import dev.bitspittle.limp.methods.system.DefMethod
import dev.bitspittle.limp.methods.system.SetMethod
import dev.bitspittle.limp.types.Placeholder
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class SystemMethodsTest {
    @Test
    fun testSetVariables() = runTest {
        val env = Environment()
        env.addMethod(SetMethod())
        env.addMethod(AddMethod())
        env.addMethod(EqualsMethod())
        env.addMethod(NotEqualsMethod())
        env.storeValue("_", Placeholder)

        val evaluator = Evaluator()

        env.pushScope()
        assertThat(env.loadValue("int1")).isNull()
        assertThat(env.loadValue("int2")).isNull()
        assertThat(env.loadValue("str")).isNull()

        assertThat(evaluator.evaluate(env, "set 'int1 12")).isEqualTo(Unit)
        assertThat(evaluator.evaluate(env, "set 'int2 34")).isEqualTo(Unit)
        assertThat(env.loadValue("int1")!!).isEqualTo(12)
        assertThat(env.loadValue("int2")!!).isEqualTo(34)
        assertThat(env.loadValue("str")).isNull()

        assertThat(evaluator.evaluate(env, "= int1 12")).isEqualTo(true)
        assertThat(evaluator.evaluate(env, "!= int2 int1")).isEqualTo(true)
        assertThat(evaluator.evaluate(env, "+ int1 int2")).isEqualTo(46)

        assertThat(evaluator.evaluate(env, "set 'str \"Dummy text\"")).isEqualTo(Unit)
        assertThat(env.loadValue("str")!!).isEqualTo("Dummy text")

        assertThat(evaluator.evaluate(env, "= str \"Dummy text\"")).isEqualTo(true)
        assertThat(evaluator.evaluate(env, "= str \"Smart text\"")).isEqualTo(false)

        env.popScope()
        assertThat(env.loadValue("int1")).isNull()
        assertThat(env.loadValue("int2")).isNull()
        assertThat(env.loadValue("str")).isNull()

        assertThrows<EvaluationException> {
            assertThat(evaluator.evaluate(env, "set '(invalid variable name) 12")).isEqualTo(Unit)
        }

        // By default, overwriting is not allowed! But you can specify an option
        evaluator.evaluate(env, "set 'set-multiple-times 123")
        assertThrows<EvaluationException> {
            assertThat(evaluator.evaluate(env, "set 'set-multiple-times 456")).isEqualTo(Unit)
        }
        assertThat(env.loadValue("set-multiple-times")).isEqualTo(123)
        assertThat(evaluator.evaluate(env, "set --overwrite _ 'set-multiple-times 456")).isEqualTo(Unit)
        assertThat(env.loadValue("set-multiple-times")).isEqualTo(456)
    }

    @Test
    fun testDefineMethods() = runTest {
        val env = Environment()
        env.addMethod(MinMethod())
        env.addMethod(MaxMethod())
        env.addMethod(DefMethod())

        val evaluator = Evaluator()

        // Define clamp
        env.scoped {
            assertThat(env.loadValue("val")).isNull()
            assertThat(env.loadValue("low")).isNull()
            assertThat(env.loadValue("hi")).isNull()
            assertThat(env.getMethod("clamp")).isNull()

            evaluator.evaluate(env, "def 'clamp 'val 'low 'hi '(min (max low val) hi)")
            env.getMethod("clamp")!!.let { result ->
                assertThat(result.name).isEqualTo("clamp")
                assertThat(result.numArgs).isEqualTo(3)
                assertThat(result.consumeRest).isEqualTo(false)
            }

            assertThat(evaluator.evaluate(env, "clamp 1 2 4")).isEqualTo(2)
            assertThat(evaluator.evaluate(env, "clamp 2 2 4")).isEqualTo(2)
            assertThat(evaluator.evaluate(env, "clamp 3 2 4")).isEqualTo(3)
            assertThat(evaluator.evaluate(env, "clamp 4 2 4")).isEqualTo(4)
            assertThat(evaluator.evaluate(env, "clamp 5 2 4")).isEqualTo(4)

            assertThat(env.loadValue("val")).isNull()
            assertThat(env.loadValue("low")).isNull()
            assertThat(env.loadValue("hi")).isNull()
        }

        // Check the ability to define environment values AFTER defining a deferred lambda
        env.scoped {
            evaluator.evaluate(env, "def 'add3 'a 'b 'c '(+ a + b c)")
            assertThat(env.getMethod("add3")).isNotNull()
            assertThat(env.getMethod("+")).isNull()

            assertThrows<EvaluationException> {
                evaluator.evaluate(env, "add3 1 2 3")
            }

            env.addMethod(AddMethod())
            assertThat(evaluator.evaluate(env, "add3 1 2 3")).isEqualTo(6)
        }

        // Misc. error checking

        assertThrows<EvaluationException> {
            evaluator.evaluate(env, "def 'not-enough-params")
        }

        assertThrows<EvaluationException> {
            evaluator.evaluate(env, "def '(bad name) 'arg1 'arg2 '(+ arg1 +arg2)")
        }

        assertThrows<EvaluationException> {
            evaluator.evaluate(env, "def 'name 'arg1 '(bad name) '(+ arg1 +arg2)")
        }
    }
}
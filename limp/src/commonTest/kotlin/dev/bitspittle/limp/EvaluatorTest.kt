package dev.bitspittle.limp

import com.varabyte.truthish.assertThat
import com.varabyte.truthish.assertThrows
import dev.bitspittle.limp.exceptions.EvaluationException
import dev.bitspittle.limp.methods.collection.ListMethod
import dev.bitspittle.limp.methods.logic.IfMethod
import dev.bitspittle.limp.methods.math.*
import dev.bitspittle.limp.methods.range.IntRangeMethod
import dev.bitspittle.limp.methods.system.DefMethod
import dev.bitspittle.limp.methods.system.SetMethod
import kotlin.test.Test

class EvaluatorTest {
    @Test
    fun testSimpleEvaluation() {
        val env = Environment()
        env.addMethod(AddMethod())
        env.addMethod(MulMethod())
        env.addMethod(SubMethod())

        val evaluator = Evaluator()
        assertThat(evaluator.evaluate(env, "+ 1 2").wrapped).isEqualTo(3)
        assertThat(evaluator.evaluate(env, "+ 1 * 3 2").wrapped).isEqualTo(7)
        assertThat(evaluator.evaluate(env, "+ 1 * 3 - 8 2").wrapped).isEqualTo(19)
        assertThat(evaluator.evaluate(env, "(+ 1 (* 3 (- 8 2)))").wrapped).isEqualTo(19)

        env.storeValue("\$a", Value(5))
        env.storeValue("\$b", Value(90))
        assertThat(evaluator.evaluate(env, "+ \$a (* 2 \$b)").wrapped).isEqualTo(185)
    }

    @Test
    fun testEvaluationWithPlaceholder() {
        val env = Environment()
        env.addMethod(IntRangeMethod())
        env.storeValue("_", Value.Placeholder)

        val evaluator = Evaluator()
        assertThat(evaluator.evaluate(env, ".. _ 10").wrapped).isEqualTo(0 .. 10)
        assertThat(evaluator.evaluate(env, ".. 1 _").wrapped).isEqualTo(1 .. Int.MAX_VALUE)
        assertThat(evaluator.evaluate(env, ".. _ _").wrapped).isEqualTo(0 .. Int.MAX_VALUE)
    }

    @Test
    fun testEvaluationWithOptionalParameters() {
        val env = Environment()
        env.addMethod(IntRangeMethod())

        val evaluator = Evaluator()
        assertThat(evaluator.evaluate(env, ".. 1 10").wrapped).isEqualTo(1 .. 10)
        assertThat(evaluator.evaluate(env, ".. --step 4 1 20").wrapped).isEqualTo(1 .. 20 step 4)

        assertThrows<EvaluationException> {
            // Whoops, typo!
            assertThat(evaluator.evaluate(env, ".. --stop 4 1 20").wrapped).isEqualTo(1..20 step 4)
        }
    }

    @Test
    fun testEvaluationWithRestParameters() {
        val env = Environment()
        env.addMethod(ListMethod())
        env.addMethod(AddMethod())
        env.addMethod(AddListMethod())
        env.addMethod(MulListMethod())

        val evaluator = Evaluator()
        assertThat(evaluator.evaluate(env, "list 1 2 3 4 5").wrapped as List<Int>).containsExactly(1, 2, 3, 4, 5)
            .inOrder()

        assertThat(evaluator.evaluate(env, "sum list 1 2 3 4 5").wrapped).isEqualTo(15)
        assertThat(evaluator.evaluate(env, "+ (mul list 1 2 3) (sum list 4 5 6)").wrapped).isEqualTo(21)
    }

    @Test
    fun testSetVariables() {
        val env = Environment()
        env.addMethod(SetMethod())
        env.addMethod(AddMethod())
        env.addMethod(EqualsMethod())
        env.addMethod(NotEqualsMethod())

        val evaluator = Evaluator()

        env.pushScope()
        assertThat(env.loadValue("int1")).isNull()
        assertThat(env.loadValue("int2")).isNull()
        assertThat(env.loadValue("str")).isNull()

        assertThat(evaluator.evaluate(env, "set 'int1 12")).isEqualTo(Value.Empty)
        assertThat(evaluator.evaluate(env, "set 'int2 34")).isEqualTo(Value.Empty)
        assertThat(env.loadValue("int1")!!.wrapped).isEqualTo(12)
        assertThat(env.loadValue("int2")!!.wrapped).isEqualTo(34)
        assertThat(env.loadValue("str")).isNull()

        assertThat(evaluator.evaluate(env, "= int1 12").wrapped).isEqualTo(true)
        assertThat(evaluator.evaluate(env, "!= int2 int1").wrapped).isEqualTo(true)
        assertThat(evaluator.evaluate(env, "+ int1 int2").wrapped).isEqualTo(46)

        assertThat(evaluator.evaluate(env, "set 'str \"Dummy text\"")).isEqualTo(Value.Empty)
        assertThat(env.loadValue("str")!!.wrapped).isEqualTo("Dummy text")

        assertThat(evaluator.evaluate(env, "= str \"Dummy text\"").wrapped).isEqualTo(true)
        assertThat(evaluator.evaluate(env, "= str \"Smart text\"").wrapped).isEqualTo(false)

        env.popScope()
        assertThat(env.loadValue("int1")).isNull()
        assertThat(env.loadValue("int2")).isNull()
        assertThat(env.loadValue("str")).isNull()

        assertThrows<EvaluationException> {
            assertThat(evaluator.evaluate(env, "set '(invalid variable name) 12")).isEqualTo(Value.Empty)
        }
    }

    @Test
    fun testDefineMethods() {
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

            assertThat(evaluator.evaluate(env, "clamp 1 2 4").wrapped).isEqualTo(2)
            assertThat(evaluator.evaluate(env, "clamp 2 2 4").wrapped).isEqualTo(2)
            assertThat(evaluator.evaluate(env, "clamp 3 2 4").wrapped).isEqualTo(3)
            assertThat(evaluator.evaluate(env, "clamp 4 2 4").wrapped).isEqualTo(4)
            assertThat(evaluator.evaluate(env, "clamp 5 2 4").wrapped).isEqualTo(4)

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
            assertThat(evaluator.evaluate(env, "add3 1 2 3").wrapped).isEqualTo(6)
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

    @Test
    fun methodExceptionsWillGetRethrownAsEvaluationExceptions() {
        val env = Environment()
        env.addMethod(DivMethod())

        val evaluator = Evaluator()
        assertThrows<EvaluationException> {
            evaluator.evaluate(env, "/ 10 0")
        }
    }
}
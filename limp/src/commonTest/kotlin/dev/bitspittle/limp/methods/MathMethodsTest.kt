package dev.bitspittle.limp.methods

import com.varabyte.truthish.assertThat
import com.varabyte.truthish.assertThrows
import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.Value
import dev.bitspittle.limp.exceptions.EvaluationException
import dev.bitspittle.limp.methods.collection.ListMethod
import dev.bitspittle.limp.methods.compare.*
import dev.bitspittle.limp.methods.math.*
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class MathMethodsTest {
    @Test
    fun testAddMethod() = runTest {
        val env = Environment()
        env.addMethod(AddMethod())

        val evaluator = Evaluator()
        assertThat(evaluator.evaluate(env, "+ 1 2").wrapped).isEqualTo(3)
    }

    @Test
    fun testAddListMethod() = runTest {
        val env = Environment()
        env.addMethod(AddListMethod())
        env.addMethod(ListMethod())

        val evaluator = Evaluator()
        assertThat(evaluator.evaluate(env, "sum list 1 2 3").wrapped).isEqualTo(6)
        assertThat(evaluator.evaluate(env, "sum list").wrapped).isEqualTo(0)
    }

    @Test
    fun testSubMethod() = runTest {
        val env = Environment()
        env.addMethod(SubMethod())

        val evaluator = Evaluator()
        assertThat(evaluator.evaluate(env, "- 5 2").wrapped).isEqualTo(3)
    }

    @Test
    fun testMulMethod() = runTest {
        val env = Environment()
        env.addMethod(MulMethod())

        val evaluator = Evaluator()
        assertThat(evaluator.evaluate(env, "* 3 2").wrapped).isEqualTo(6)
    }

    @Test
    fun testMulListMethod() = runTest {
        val env = Environment()
        env.addMethod(MulListMethod())
        env.addMethod(ListMethod())

        val evaluator = Evaluator()
        assertThat(evaluator.evaluate(env, "mul list 1 2 3").wrapped).isEqualTo(6)
        assertThat(evaluator.evaluate(env, "mul list").wrapped).isEqualTo(1)
    }

    @Test
    fun testPowMethod() = runTest {
        val env = Environment()
        env.addMethod(PowMethod())

        val evaluator = Evaluator()

        assertThat(evaluator.evaluate(env, "^ 2 3").wrapped).isEqualTo(8)
        assertThat(evaluator.evaluate(env, "^ 3 2").wrapped).isEqualTo(9)
        assertThat(evaluator.evaluate(env, "^ 1000 0").wrapped).isEqualTo(1)
        assertThat(evaluator.evaluate(env, "^ 20 1").wrapped).isEqualTo(20)

        assertThrows<EvaluationException> {
            evaluator.evaluate(env, "^ 5 -1")
        }
    }

    @Test
    fun testDivMethod() = runTest {
        val env = Environment()
        env.addMethod(DivMethod())

        val evaluator = Evaluator()

        assertThat(evaluator.evaluate(env, "/ 9 3").wrapped).isEqualTo(3)
        assertThat(evaluator.evaluate(env, "/ 9 4").wrapped).isEqualTo(2)
        assertThat(evaluator.evaluate(env, "/ 9 -1").wrapped).isEqualTo(-9)

        assertThrows<EvaluationException> {
            evaluator.evaluate(env, "/ 9 0")
        }
    }

    @Test
    fun testRemainderMethod() = runTest {
        val env = Environment()
        env.addMethod(RemainderMethod())

        val evaluator = Evaluator()

        assertThat(evaluator.evaluate(env, "% 9 3").wrapped).isEqualTo(0)
        assertThat(evaluator.evaluate(env, "% 9 4").wrapped).isEqualTo(1)

        assertThrows<EvaluationException> {
            evaluator.evaluate(env, "% 9 0")
        }
    }

    @Test
    fun testMinMethod() = runTest {
        val env = Environment()
        env.addMethod(MinMethod())

        val evaluator = Evaluator()
        assertThat(evaluator.evaluate(env, "min 5 2").wrapped).isEqualTo(2)
        assertThat(evaluator.evaluate(env, "min -5 2").wrapped).isEqualTo(-5)
    }

    @Test
    fun testMaxMethod() = runTest {
        val env = Environment()
        env.addMethod(MaxMethod())

        val evaluator = Evaluator()
        assertThat(evaluator.evaluate(env, "max 5 2").wrapped).isEqualTo(5)
        assertThat(evaluator.evaluate(env, "max -5 2").wrapped).isEqualTo(2)
    }

    @Test
    fun testClampMethod() = runTest {
        val env = Environment()
        env.addMethod(ClampMethod())

        val evaluator = Evaluator()

        assertThat(evaluator.evaluate(env, "clamp 0 2 5").wrapped).isEqualTo(2)
        assertThat(evaluator.evaluate(env, "clamp 2 2 5").wrapped).isEqualTo(2)
        assertThat(evaluator.evaluate(env, "clamp 4 2 5").wrapped).isEqualTo(4)
        assertThat(evaluator.evaluate(env, "clamp 5 2 5").wrapped).isEqualTo(5)
        assertThat(evaluator.evaluate(env, "clamp 9 2 5").wrapped).isEqualTo(5)
    }
}
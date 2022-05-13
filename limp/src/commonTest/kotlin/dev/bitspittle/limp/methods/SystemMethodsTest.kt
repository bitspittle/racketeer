package dev.bitspittle.limp.methods

import com.varabyte.truthish.assertThat
import com.varabyte.truthish.assertThrows
import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.TestLangService
import dev.bitspittle.limp.exceptions.EvaluationException
import dev.bitspittle.limp.methods.collection.ListGetMethod
import dev.bitspittle.limp.methods.collection.ListMethod
import dev.bitspittle.limp.methods.compare.EqualsMethod
import dev.bitspittle.limp.methods.compare.NotEqualsMethod
import dev.bitspittle.limp.methods.math.AddMethod
import dev.bitspittle.limp.methods.math.MaxMethod
import dev.bitspittle.limp.methods.math.MinMethod
import dev.bitspittle.limp.methods.system.*
import dev.bitspittle.limp.types.ConsoleLogger
import dev.bitspittle.limp.types.Placeholder
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

@Suppress("UNCHECKED_CAST")
class SystemMethodsTest {
    @Test
    fun testSetVariables() = runTest {
        val env = Environment()
        val service = TestLangService()
        env.addMethod(SetMethod(service.logger))
        env.addMethod(AddMethod())
        env.addMethod(EqualsMethod())
        env.addMethod(NotEqualsMethod())
        env.storeValue("_", Placeholder)

        val evaluator = Evaluator()

        env.pushScope()
        assertThat(env.loadValue("\$int1")).isNull()
        assertThat(env.loadValue("\$int2")).isNull()
        assertThat(env.loadValue("\$str")).isNull()

        evaluator.evaluate(env, "set '\$int1 12")
        evaluator.evaluate(env, "set '\$int2 34")
        assertThat(env.expectValue("\$int1")).isEqualTo(12)
        assertThat(env.expectValue("\$int2")).isEqualTo(34)
        assertThat(env.loadValue("\$str")).isNull()

        assertThat(evaluator.evaluate(env, "= \$int1 12")).isEqualTo(true)
        assertThat(evaluator.evaluate(env, "!= \$int2 \$int1")).isEqualTo(true)
        assertThat(evaluator.evaluate(env, "+ \$int1 \$int2")).isEqualTo(46)

        evaluator.evaluate(env, "set '\$str \"Dummy text\"")
        assertThat(env.expectValue("\$str")).isEqualTo("Dummy text")

        assertThat(evaluator.evaluate(env, "= \$str \"Dummy text\"")).isEqualTo(true)
        assertThat(evaluator.evaluate(env, "= \$str \"Smart text\"")).isEqualTo(false)

        env.popScope()
        assertThat(env.loadValue("\$int1")).isNull()
        assertThat(env.loadValue("\$int2")).isNull()
        assertThat(env.loadValue("\$str")).isNull()

        assertThrows<EvaluationException> {
            evaluator.evaluate(env, "set '(invalid variable name) 12")
        }

        // By default, overwriting is not allowed! But you can specify an option
        evaluator.evaluate(env, "set '\$set-multiple-times 123")
        assertThrows<EvaluationException> {
            evaluator.evaluate(env, "set '\$set-multiple-times 456")
        }
        assertThat(env.loadValue("\$set-multiple-times")).isEqualTo(123)
        evaluator.evaluate(env, "set --overwrite _ '\$set-multiple-times 456")
        assertThat(env.loadValue("\$set-multiple-times")).isEqualTo(456)

        assertThat(service.logs.isEmpty())
        assertThat(evaluator.evaluate(env, "set 'no-leading-dollar 123"))
        assertThat(service.logs.count { it.startsWith("[W]") }).isEqualTo(1)
    }

    @Test
    fun testDefineMethods() = runTest {
        val env = Environment()
        env.addMethod(DefMethod())
        env.storeValue("_", Placeholder)

        val evaluator = Evaluator()

        // Define clamp
        env.scoped {
            env.addMethod(MinMethod())
            env.addMethod(MaxMethod())

            assertThat(env.loadValue("val")).isNull()
            assertThat(env.loadValue("low")).isNull()
            assertThat(env.loadValue("hi")).isNull()
            assertThat(env.getMethod("clamp")).isNull()

            evaluator.evaluate(env, "def 'clamp 'val 'low 'hi '(min (max low val) hi)")
            env.expectMethod("clamp").let { result ->
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

        // Verify inline behavior
        env.scoped {
            env.addMethod(SetMethod(ConsoleLogger()))
            env.addMethod(AddMethod())

            evaluator.evaluate(env, "def 'set-add3-normal '\$a '\$b '\$c '(set '\$sum + \$a + \$b \$c)")
            assertThat(env.getMethod("set-add3-normal")).isNotNull()

            evaluator.evaluate(env, "def 'set-add3-inline --inline _ '\$a '\$b '\$c '(set '\$sum + \$a + \$b \$c)")
            assertThat(env.getMethod("set-add3-inline")).isNotNull()

            // We'll want to make sure lambda parameters don't leak in inline mode
            assertThat(env.loadValue("\$a")).isNull()
            assertThat(env.loadValue("\$b")).isNull()
            assertThat(env.loadValue("\$c")).isNull()
            assertThat(env.loadValue("\$sum")).isNull()

            evaluator.evaluate(env, "set-add3-normal 1 2 3")
            // Trust me, it was called! But the values it calculated were lost when the scope got torn down
            assertThat(env.loadValue("\$a")).isNull()
            assertThat(env.loadValue("\$b")).isNull()
            assertThat(env.loadValue("\$c")).isNull()
            assertThat(env.loadValue("\$sum")).isNull()

            evaluator.evaluate(env, "set-add3-inline 1 2 3")
            // With inline, the method was called in our current scope! So sum will stuck. But lambda parameters
            // shouldn't leak.
            assertThat(env.loadValue("\$a")).isNull()
            assertThat(env.loadValue("\$b")).isNull()
            assertThat(env.loadValue("\$c")).isNull()
            assertThat(env.loadValue("\$sum")).isEqualTo(6)
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

        // Overwrite flag required to allow overwriting a function with the same name
        evaluator.evaluate(env, "def 'onetwothree '122")
        assertThrows<EvaluationException> {
            evaluator.evaluate(env, "def 'onetwothree '123")
        }
        evaluator.evaluate(env, "def --overwrite _ 'onetwothree '124")
        assertThat(evaluator.evaluate(env, "onetwothree")).isEqualTo(124)
    }

    @Test
    fun testAddAliases() = runTest {
        val env = Environment()
        env.addMethod(AliasMethod())
        env.storeValue("_", Placeholder)

        val evaluator = Evaluator()

        // You can define aliases before the methods / variables exist
        env.scoped {
            evaluator.evaluate(env, "alias 'lget 'list-get")
            evaluator.evaluate(env, "alias '\$ints '\$these-are-some-ints")

            env.addMethod(ListMethod())
            env.addMethod(ListGetMethod())
            env.addMethod(SetMethod(ConsoleLogger()))

            evaluator.evaluate(env, "set '\$these-are-some-ints (list 1 2 3 4 5)")
            assertThat(evaluator.evaluate(env, "lget \$ints 1")).isEqualTo(2)
        }

        env.addMethod(ListMethod())
        assertThrows<EvaluationException> {
            // lget alias scope was dropped
            evaluator.evaluate(env, "lget (list 1 2 3 4 5) 0")
        }

        // Two aliases to the same name are OK but...
        evaluator.evaluate(env, "alias 'lget1 'list-get")
        evaluator.evaluate(env, "alias 'lget2 'list-get")

        assertThrows<EvaluationException> {
            // Re-using an existing alias is not allowed
            evaluator.evaluate(env, "alias 'lget2 'list")
        }

        // ... unless you overwrite it
        evaluator.evaluate(env, "alias --overwrite _ 'lget2 'list")

        // Aliases will always lose in precedence to existing methods and variables
        evaluator.evaluate(env, "alias --overwrite _ 'list 'dummy")
        assertThat(evaluator.evaluate(env, "list 1 2 3 4 5") as List<Int>).containsExactly(1, 2, 3, 4, 5).inOrder()

        assertThrows<EvaluationException> {
            // Invalid type
            assertThat(evaluator.evaluate(env, "alias 123 456"))
        }
    }

    @Test
    fun testRunMethod() = runTest {
        val env = Environment()
        env.addMethod(RunMethod())
        env.addMethod(SetMethod(ConsoleLogger()))
        env.addMethod(AddMethod())

        val evaluator = Evaluator()
        assertThat(evaluator.evaluate(env, "run '(set '\$x 10) '(set '\$y 20) '(+ \$x \$y)")).isEqualTo(30)
        assertThrows<EvaluationException> {
            evaluator.evaluate(env, "\$x") // Variables are unscoped after run
        }
        assertThat(evaluator.evaluate(env, "run")).isEqualTo(Unit)
    }

    @Test
    fun testDbgMethod() = runTest {
        val env = Environment()
        val service = TestLangService()
        env.addMethod(DbgMethod(service.logger))
        env.addMethod(AddMethod())

        assertThat(service.logs.isEmpty())

        val evaluator = Evaluator()

        evaluator.evaluate(env, "dbg 123")
        assertThat(service.logs).containsExactly("[D] 123 # Int")

        service.clearLogs()
        evaluator.evaluate(env, "dbg --msg \"Debug value\" 456")
        assertThat(service.logs).containsExactly("[D] Debug value: 456 # Int")

        // You can chain debug statements
        service.clearLogs()
        evaluator.evaluate(env, "dbg + dbg 10 dbg 20")
        assertThat(service.logs)
            .containsExactly("[D] 20 # Int", "[D] 10 # Int", "[D] 30 # Int")
            .inOrder()
    }

    @Test
    fun testInfoMethod() = runTest {
        val env = Environment()
        val service = TestLangService()
        env.addMethod(InfoMethod(service.logger))

        assertThat(service.logs.isEmpty())

        val evaluator = Evaluator()

        evaluator.evaluate(env, "info 123")
        assertThat(service.logs).containsExactly("[I] 123")

        service.clearLogs()
        evaluator.evaluate(env, "info \"Hello\"")
        evaluator.evaluate(env, "info \"World\"")
        assertThat(service.logs).containsExactly("[I] Hello", "[I] World").inOrder()
    }
}
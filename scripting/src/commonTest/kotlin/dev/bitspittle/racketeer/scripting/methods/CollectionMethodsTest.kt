package dev.bitspittle.racketeer.scripting.methods

import com.varabyte.truthish.assertThat
import com.varabyte.truthish.assertThrows
import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Evaluator
import dev.bitspittle.limp.exceptions.EvaluationException
import dev.bitspittle.limp.methods.collection.ListMethod
import dev.bitspittle.limp.methods.range.IntRangeMethod
import dev.bitspittle.limp.methods.system.SetMethod
import dev.bitspittle.limp.types.Placeholder
import dev.bitspittle.racketeer.scripting.methods.collection.ChooseHandler
import dev.bitspittle.racketeer.scripting.methods.collection.ChooseMethod
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

@Suppress("UNCHECKED_CAST")
class CollectionMethodsTest {
    @Suppress("JoinDeclarationAndAssignment") // Intentional style for readability
    @Test
    fun testChooseMethod() = runTest {
        val env = Environment()

        lateinit var chooseResponse: (List<Any>) -> List<Any>
        val chooseHandler = object : ChooseHandler {
            override suspend fun query(prompt: String?, list: List<Any>, range: IntRange): List<Any> {
                return chooseResponse(list)
            }
        }
        env.addMethod(ChooseMethod(chooseHandler))
        env.addMethod(SetMethod())
        env.addMethod(ListMethod())
        env.addMethod(IntRangeMethod())
        env.storeValue("_", Placeholder)

        val evaluator = Evaluator()

        evaluator.evaluate(env, "set '\$ints list 1 2 3 4 5")
        evaluator.evaluate(env, "set '\$empty list")

        chooseResponse = { list -> list.drop(1).take(2) }
        assertThat(evaluator.evaluate(env, "choose \$ints _") as List<Int>).containsExactly(2, 3).inOrder()

        // The chooser isn't asked about the specific case when the list is empty
        assertThat(evaluator.evaluate(env, "choose \$empty .. 0 _") as List<Int>).isEmpty()

        // Can't request taking more items than what exists in the list
        assertThrows<EvaluationException> {
            evaluator.evaluate(env, "choose \$empty .. 1 _") as List<Int>
        }
        assertThrows<EvaluationException> {
            evaluator.evaluate(env, "choose \$ints .. 99 100")
        }

        // Yikes! It's a developer bug if the choosehandler returns a list whose size isn't in the requested range
        assertThrows<EvaluationException> {
            evaluator.evaluate(env, "choose \$ints .. 3 _")
        }
        // There, we fixed it
        chooseResponse = { list -> list.drop(1).dropLast(1) }
        assertThat(evaluator.evaluate(env, "choose \$ints .. 3 _") as List<Int>).containsExactly(2, 3, 4).inOrder()

        chooseResponse = { throw IllegalStateException("I don't like these choices") }

        assertThrows<EvaluationException> {
            assertThat(evaluator.evaluate(env, "choose \$ints _") as List<Int>).containsExactly(2, 3).inOrder()
        }.also { ex ->
            assertThat(ex.cause is IllegalStateException)
        }

    }
}
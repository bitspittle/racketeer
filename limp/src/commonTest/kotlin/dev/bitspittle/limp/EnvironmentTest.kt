package dev.bitspittle.limp

import com.varabyte.truthish.assertThat
import com.varabyte.truthish.assertThrows
import kotlin.test.Test

class EnvironmentTest {
    @Test
    fun canRegisterMethodsAndVariables() {
        val env = Environment()
        env.addMethod(object : Method("fun1", 0) {
            override suspend fun invoke(env: Environment, eval: Evaluator, params: List<Any>, options: Map<String, Any>, rest: List<Any>) = Unit
        })
        env.addMethod(object : Method("fun2", 0) {
            override suspend fun invoke(env: Environment, eval: Evaluator, params: List<Any>, options: Map<String, Any>, rest: List<Any>) = Unit
        })

        assertThrows<IllegalArgumentException> {
            env.addMethod(object : Method("fun2", 2) {
                override suspend fun invoke(
                    env: Environment,
                    eval: Evaluator,
                    params: List<Any>,
                    options: Map<String, Any>,
                    rest: List<Any>
                ) = Unit
            })
        }

        env.storeValue("var1", 10)
        env.storeValue("var2", 20)

        assertThrows<IllegalArgumentException> {
            env.storeValue("var2", 30)
        }

        assertThat(env.getMethod("does-not-exist")).isNull()
        env.expectMethod("fun1").let { m ->
            assertThat(m.name).isEqualTo("fun1")
            assertThat(m.numArgs).isEqualTo(0)
        }
        env.expectMethod("fun2").let { m ->
            assertThat(m.name).isEqualTo("fun2")
            assertThat(m.numArgs).isEqualTo(0)
        }
        assertThat(env.loadValue("does-not-exist")).isNull()
        assertThat(env.expectValue("var1")).isEqualTo(10)
        assertThat(env.expectValue("var2")).isEqualTo(20)
    }

    @Test
    fun canPushAndPopScopes() {
        val env = Environment()

        env.addMethod(object : Method("fun", 0) {
            override suspend fun invoke(env: Environment, eval: Evaluator, params: List<Any>, options: Map<String, Any>, rest: List<Any>) = Unit
        })
        env.storeValue("var", 0)

        env.pushScope()
        env.addMethod(object : Method("fun", 1) {
            override suspend fun invoke(env: Environment, eval: Evaluator, params: List<Any>, options: Map<String, Any>, rest: List<Any>) = Unit
        })
        env.storeValue("var", 10)

        env.pushScope()
        env.addMethod(object : Method("fun", 2) {
            override suspend fun invoke(env: Environment, eval: Evaluator, params: List<Any>, options: Map<String, Any>, rest: List<Any>) = Unit
        })
        env.storeValue("var", 20)

        assertThat(env.expectMethod("fun").numArgs).isEqualTo(2)
        assertThat(env.expectValue("var")).isEqualTo(20)

        env.popScope()
        assertThat(env.expectMethod("fun").numArgs).isEqualTo(1)
        assertThat(env.expectValue("var")).isEqualTo(10)

        env.popScope()
        assertThat(env.expectMethod("fun").numArgs).isEqualTo(0)
        assertThat(env.expectValue("var")).isEqualTo(0)

        assertThrows<IllegalStateException> { env.popScope() }
    }
}
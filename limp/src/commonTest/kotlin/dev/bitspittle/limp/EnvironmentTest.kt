package dev.bitspittle.limp

import com.varabyte.truthish.assertThat
import com.varabyte.truthish.assertThrows
import kotlin.test.Test

class EnvironmentTest {
    @Test
    fun canRegisterMethodsAndVariables() {
        val env = Environment()
        env.addMethod(object : Method("fun1", 0) {
            override fun invoke(env: Environment, params: List<Value>, options: Map<String, Value>, rest: List<Value>) = Value.Empty
        })
        env.addMethod(object : Method("fun2", 0) {
            override fun invoke(env: Environment, params: List<Value>, options: Map<String, Value>, rest: List<Value>) = Value.Empty
        })

        assertThrows<IllegalArgumentException> {
            env.addMethod(object : Method("fun2", 2) {
                override fun invoke(
                    env: Environment,
                    params: List<Value>,
                    options: Map<String, Value>,
                    rest: List<Value>
                ) = Value.Empty
            })
        }

        env.storeValue("var1", Value(10))
        env.storeValue("var2", Value(20))

        assertThrows<IllegalArgumentException> {
            env.storeValue("var2", Value(30))
        }

        assertThat(env.getMethod("does-not-exist")).isNull()
        env.getMethod("fun1")!!.let { m ->
            assertThat(m.name).isEqualTo("fun1")
            assertThat(m.numArgs).isEqualTo(0)
        }
        env.getMethod("fun2")!!.let { m ->
            assertThat(m.name).isEqualTo("fun2")
            assertThat(m.numArgs).isEqualTo(0)
        }
        assertThat(env.loadValue("does-not-exist")).isNull()
        assertThat(env.loadValue("var1")!!.wrapped).isEqualTo(10)
        assertThat(env.loadValue("var2")!!.wrapped).isEqualTo(20)
    }

    @Test
    fun canPushAndPopScopes() {
        val env = Environment()

        env.addMethod(object : Method("fun", 0) {
            override fun invoke(env: Environment, params: List<Value>, options: Map<String, Value>, rest: List<Value>) = Value.Empty
        })
        env.storeValue("var", Value(0))

        env.pushScope()
        env.addMethod(object : Method("fun", 1) {
            override fun invoke(env: Environment, params: List<Value>, options: Map<String, Value>, rest: List<Value>) = Value.Empty
        })
        env.storeValue("var", Value(10))

        env.pushScope()
        env.addMethod(object : Method("fun", 2) {
            override fun invoke(env: Environment, params: List<Value>, options: Map<String, Value>, rest: List<Value>) = Value.Empty
        })
        env.storeValue("var", Value(20))

        assertThat(env.getMethod("fun")!!.numArgs).isEqualTo(2)
        assertThat(env.loadValue("var")!!.wrapped).isEqualTo(20)

        env.popScope()
        assertThat(env.getMethod("fun")!!.numArgs).isEqualTo(1)
        assertThat(env.loadValue("var")!!.wrapped).isEqualTo(10)

        env.popScope()
        assertThat(env.getMethod("fun")!!.numArgs).isEqualTo(0)
        assertThat(env.loadValue("var")!!.wrapped).isEqualTo(0)

        assertThrows<IllegalStateException> { env.popScope() }
    }
}
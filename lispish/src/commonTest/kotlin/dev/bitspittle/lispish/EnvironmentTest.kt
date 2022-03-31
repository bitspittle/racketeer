package dev.bitspittle.lispish

import com.varabyte.truthish.assertThat
import com.varabyte.truthish.assertThrows
import kotlin.test.Test

class EnvironmentTest {
    @Test
    fun canRegisterMethodsAndVariables() {
        val env = Environment()
        env.add(object : Method("fun1", 0) {
            override fun invoke(params: List<Value>) = Value.Empty
        })
        env.add(object : Method("fun2", 0) {
            override fun invoke(params: List<Value>) = Value.Empty
        })
        env.add(object : Method("fun2", 2) {
            override fun invoke(params: List<Value>) = Value.Empty
        })

        env.set("var1", Value(10))
        env.set("var1", Value(20))

        assertThat(env.getMethod("does-not-exist")).isNull()
        env.getMethod("fun1")!!.let { m ->
            assertThat(m.name).isEqualTo("fun1")
            assertThat(m.numArgs).isEqualTo(0)
        }
        env.getMethod("fun2")!!.let { m ->
            assertThat(m.name).isEqualTo("fun2")
            assertThat(m.numArgs).isEqualTo(2)
        }
        assertThat(env.getValue("does-not-exist")).isNull()
        assertThat(env.getValue("var1")!!.into(Converters(), Int::class)).isEqualTo(20)
    }

    @Test
    fun canPushAndPopScopes() {
        val env = Environment()

        env.add(object : Method("fun", 0) {
            override fun invoke(params: List<Value>) = Value.Empty
        })
        env.set("var", Value(0))

        env.pushScope()
        env.add(object : Method("fun", 1) {
            override fun invoke(params: List<Value>) = Value.Empty
        })
        env.set("var", Value(10))

        env.pushScope()
        env.add(object : Method("fun", 2) {
            override fun invoke(params: List<Value>) = Value.Empty
        })
        env.set("var", Value(20))

        assertThat(env.getMethod("fun")!!.numArgs).isEqualTo(2)
        assertThat(env.getValue("var")!!.into(Converters(), Int::class)).isEqualTo(20)

        env.popScope()
        assertThat(env.getMethod("fun")!!.numArgs).isEqualTo(1)
        assertThat(env.getValue("var")!!.into(Converters(), Int::class)).isEqualTo(10)

        env.popScope()
        assertThat(env.getMethod("fun")!!.numArgs).isEqualTo(0)
        assertThat(env.getValue("var")!!.into(Converters(), Int::class)).isEqualTo(0)

        assertThrows<IllegalStateException> { env.popScope() }
    }
}
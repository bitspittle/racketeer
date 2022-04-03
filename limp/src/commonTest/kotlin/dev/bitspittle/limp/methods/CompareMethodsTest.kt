package dev.bitspittle.limp.methods

import com.varabyte.truthish.assertThat
import dev.bitspittle.limp.Environment
import dev.bitspittle.limp.Value
import dev.bitspittle.limp.methods.compare.*
import dev.bitspittle.limp.methods.math.*
import kotlin.test.Test

class CompareMethodsTest {
    @Test
    fun testComparisonMethods() {
        val env = Environment()

        val lt = LessThanMethod()
        val lte = LessThanEqualsMethod()
        val eq = EqualsMethod()
        val neq = NotEqualsMethod()
        val gt = GreaterThanMethod()
        val gte = GreaterThanEqualsMethod()

        listOf(Value(1), Value(2)).let { nums ->
            assertThat(lt.invoke(env, nums).wrapped).isEqualTo(true)
            assertThat(lte.invoke(env, nums).wrapped).isEqualTo(true)
            assertThat(eq.invoke(env, nums).wrapped).isEqualTo(false)
            assertThat(neq.invoke(env, nums).wrapped).isEqualTo(true)
            assertThat(gte.invoke(env, nums).wrapped).isEqualTo(false)
            assertThat(gt.invoke(env, nums).wrapped).isEqualTo(false)
        }

        listOf(Value(2), Value(2)).let { nums ->
            assertThat(lt.invoke(env, nums).wrapped).isEqualTo(false)
            assertThat(lte.invoke(env, nums).wrapped).isEqualTo(true)
            assertThat(eq.invoke(env, nums).wrapped).isEqualTo(true)
            assertThat(neq.invoke(env, nums).wrapped).isEqualTo(false)
            assertThat(gte.invoke(env, nums).wrapped).isEqualTo(true)
            assertThat(gt.invoke(env, nums).wrapped).isEqualTo(false)
        }

        listOf(Value(3), Value(2)).let { nums ->
            assertThat(lt.invoke(env, nums).wrapped).isEqualTo(false)
            assertThat(lte.invoke(env, nums).wrapped).isEqualTo(false)
            assertThat(eq.invoke(env, nums).wrapped).isEqualTo(false)
            assertThat(neq.invoke(env, nums).wrapped).isEqualTo(true)
            assertThat(gte.invoke(env, nums).wrapped).isEqualTo(true)
            assertThat(gt.invoke(env, nums).wrapped).isEqualTo(true)
        }
    }
}
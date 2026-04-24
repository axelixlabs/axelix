package cache

import com.axelixlabs.axelix.sbs.spring.core.cache.DefaultEnhancedCache
import com.axelixlabs.axelix.sbs.spring.core.cache.EnhancedCache
import org.jetbrains.lincheck.datastructures.ModelCheckingOptions
import org.jetbrains.lincheck.datastructures.Operation
import org.jetbrains.lincheck.datastructures.Param
import org.jetbrains.lincheck.datastructures.StressOptions
import org.jetbrains.lincheck.datastructures.StringGen
import org.junit.jupiter.api.Test
import org.springframework.cache.concurrent.ConcurrentMapCache
import java.util.concurrent.Callable

class DefaultEnhancedCacheJavaTest {

    val delegate = DefaultEnhancedCache(ConcurrentMapCache("test-cache"))
    val subject =

        @Param(name = "value", gen = StringGen::class)
        @Param(name = "key", gen = StringGen::class, conf = "1:3")
        object : EnhancedCache by delegate {

        @Operation
        fun getValueWrapper(@Param(name = "key") key: Any) = delegate.get(key)

        @Operation
        fun getWithType(@Param(name = "key") key: Any) = delegate.get(key, String::class.java)

        @Operation
        fun getWithValueLoader(@Param(name = "key") key: Any) = delegate.get(key, Callable { "loaded value" })

        @Operation
        override fun put(@Param(name = "key") key: Any, @Param(name = "value") value: Any?) = delegate.put(key, value)

        @Operation
        override fun evict(@Param(name = "key") key: Any) = delegate.evict(key)

        @Operation
        override fun clear() = delegate.clear()

        @Operation
        override fun disable() = delegate.disable()

        @Operation
        override fun enable() = delegate.enable()

        @Operation
        override fun isEnabled() = delegate.isEnabled()
    }

    @Test
    fun test_StressTesting() {
        StressOptions().check(subject::class)
    }

    @Test
    fun test_ModelChecking() {
        ModelCheckingOptions().check(subject::class)
    }
}
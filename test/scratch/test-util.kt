package scratch

import org.mockito.internal.matchers.Equals
import org.mockito.internal.matchers.InstanceOf
import org.mockito.internal.matchers.Same
import org.mockito.internal.progress.ThreadSafeMockingProgress
import scratch.ScratchConfig.AppendType
import scratch.ScratchConfig.AppendType.APPEND
import kotlin.reflect.KClass

fun <T> eq(value: T): T {
    ThreadSafeMockingProgress.mockingProgress().argumentMatcherStorage.reportMatcher(Equals(value))
    return value
}

fun <T> same(value: T): T {
    ThreadSafeMockingProgress.mockingProgress().argumentMatcherStorage.reportMatcher(Same(value))
    return value
}

@Suppress("UNCHECKED_CAST")
fun <T : Any> some(type: KClass<T>): T {
    val matcher = InstanceOf.VarArgAware(type.java, "<any " + type.java.canonicalName + ">")
    ThreadSafeMockingProgress.mockingProgress().argumentMatcherStorage.reportMatcher(matcher)
    if (type == AppendType::class) return APPEND as T
    return type.constructors.find { it.parameters.isEmpty() }!!.call()
}
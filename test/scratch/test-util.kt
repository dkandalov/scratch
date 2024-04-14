package scratch

import org.mockito.internal.matchers.Equals
import org.mockito.internal.matchers.InstanceOf
import org.mockito.internal.matchers.Same
import org.mockito.internal.progress.ThreadSafeMockingProgress
import kotlin.reflect.KClass

fun <T> eq(value: T): T {
    ThreadSafeMockingProgress.mockingProgress().argumentMatcherStorage.reportMatcher(Equals(value))
    return value
}

fun <T> same(value: T): T {
    ThreadSafeMockingProgress.mockingProgress().argumentMatcherStorage.reportMatcher(Same(value))
    return value
}

fun <T : Any> some(kClass: KClass<T>): T {
    ThreadSafeMockingProgress.mockingProgress().argumentMatcherStorage
        .reportMatcher(InstanceOf(kClass.java, "<any " + kClass.java.canonicalName + ">"))
    return when {
        kClass.java.isEnum -> kClass.java.enumConstants.first()
        else -> kClass.constructors.find { it.parameters.isEmpty() }!!.call()
    }
}
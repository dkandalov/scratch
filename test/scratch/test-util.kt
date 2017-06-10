/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package scratch

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.mockito.internal.matchers.Equals
import org.mockito.internal.matchers.InstanceOf
import org.mockito.internal.matchers.Same
import org.mockito.internal.progress.ThreadSafeMockingProgress
import kotlin.reflect.KClass

infix fun <T> T.shouldEqual(that: T) {
    assertThat(this, equalTo(that))
}

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
    if (type == ScratchConfig.AppendType::class) return ScratchConfig.AppendType.APPEND as T
    return type.constructors.find { it.parameters.isEmpty() }!!.call()
}
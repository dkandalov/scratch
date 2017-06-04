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

import org.hamcrest.core.IsEqual.equalTo
import org.junit.Assert.assertThat
import org.junit.Test
import scratch.ScratchConfig.Companion.DEFAULT_CONFIG
import scratch.ScratchConfig.Companion.DOWN
import scratch.ScratchConfig.Companion.UP

class ScratchConfigTests {

    private val scratch1 = Scratch.create("scratch1.txt")
    private val scratch2 = Scratch.create("scratch2.txt")
    private val scratch3 = Scratch.create("scratch3.txt")
    private val config = DEFAULT_CONFIG.with(listOf(scratch1, scratch2, scratch3))

    @Test fun `moving top scratch to bottom`() {
        assertThat(config.move(scratch1, UP), equalTo(DEFAULT_CONFIG.with(listOf(scratch2, scratch3, scratch1))))
    }

    @Test fun `moving bottom scratch to top`() {
        assertThat(config.move(scratch3, DOWN), equalTo(DEFAULT_CONFIG.with(listOf(scratch3, scratch1, scratch2))))
    }

    @Test fun `moving scratch up`() {
        assertThat(config.move(scratch2, UP), equalTo(DEFAULT_CONFIG.with(listOf(scratch2, scratch1, scratch3))))
    }
}

package scratch

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual.equalTo
import org.junit.Test
import scratch.ScratchConfig.Companion.defaultConfig
import scratch.ScratchConfig.Companion.down
import scratch.ScratchConfig.Companion.up

class ScratchConfigTests {
    private val scratch1 = Scratch("scratch1.txt")
    private val scratch2 = Scratch("scratch2.txt")
    private val scratch3 = Scratch("scratch3.txt")
    private val config = defaultConfig.with(listOf(scratch1, scratch2, scratch3))

    @Test fun `moving top scratch to bottom`() {
        assertThat(config.move(scratch1, up), equalTo(defaultConfig.with(listOf(scratch2, scratch3, scratch1))))
    }

    @Test fun `moving bottom scratch to top`() {
        assertThat(config.move(scratch3, down), equalTo(defaultConfig.with(listOf(scratch3, scratch1, scratch2))))
    }

    @Test fun `moving scratch up`() {
        assertThat(config.move(scratch2, up), equalTo(defaultConfig.with(listOf(scratch2, scratch1, scratch3))))
    }
}

package scratch

import org.hamcrest.core.IsEqual.equalTo
import org.junit.Assert.assertThat
import org.junit.Test

class ScratchTests {
    @Test fun `creating scratches`() {
        Scratch("scratch.txt").apply {
            assertThat(name, equalTo("scratch"))
            assertThat(extension, equalTo("txt"))
        }

        Scratch("&scratch.txt").apply {
            assertThat(name, equalTo("scratch"))
            assertThat(extension, equalTo("txt"))
        }

        Scratch("scratch.t&xt").apply {
            assertThat(name, equalTo("scratch"))
            assertThat(extension, equalTo("txt"))
        }

        Scratch("scratch").apply {
            assertThat(name, equalTo("scratch"))
            assertThat(extension, equalTo(""))
        }
    }
}

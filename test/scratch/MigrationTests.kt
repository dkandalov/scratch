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

import org.junit.Test
import scratch.ide.MoveResult.Failure
import scratch.ide.MoveResult.Success
import scratch.ide.moveScratches
import java.io.File
import java.nio.file.Files



class MigrationTests {

    @Test fun `successfully migrate to IDE scratches location`() {
        val fromFolder = createTempFolder {
            createFile("scratch.txt")
            createFile("scratch2.txt")
            createFile("scratch3.txt")
        }
        val toFolder = createTempFolder {}

        val moveResult = moveScratches(fromFolder.list().toList(), fromFolder.absolutePath, toFolder.absolutePath)

        moveResult shouldEqual Success
        toFolder.list() shouldEqual arrayOf(
            "scratch.txt",
            "scratch2.txt",
            "scratch3.txt"
        )
    }

    @Test fun `renamed files in target directory when names clash`() {
        val fromFolder = createTempFolder {
            createFile("scratch.txt")
            createFile("scratch2.txt")
            createFile("scratch3.txt")
        }
        val toFolder = createTempFolder {
            createFile("_scratch.txt")
            createFile("scratch.txt")
            createFile("scratch5.txt")
        }

        val moveResult = moveScratches(fromFolder.list().toList(), fromFolder.absolutePath, toFolder.absolutePath)

        moveResult shouldEqual Success
        toFolder.list() shouldEqual arrayOf(
            "__scratch.txt",
            "_scratch.txt",
            "scratch.txt",
            "scratch2.txt",
            "scratch3.txt",
            "scratch5.txt"
        )
    }

    @Test fun `fail and report error when target folder doesn't exist (or on any other exception)`() {
        val fromFolder = createTempFolder {
            createFile("scratch.txt")
            createFile("scratch2.txt")
            createFile("scratch3.txt")
        }
        val toFolder = File("/non-existing-folder")

        val moveResult = moveScratches(fromFolder.list().toList(), fromFolder.absolutePath, toFolder.absolutePath)

        moveResult shouldEqual Failure(reason = "Target folder doesn't exist: /non-existing-folder")
    }

    private fun File.createFile(childFileName: String) {
        File("$absolutePath/$childFileName").createNewFile()
    }

    private fun createTempFolder(f: File.() -> Unit) = Files.createTempDirectory("").toFile().apply {
        this.f()
        deleteOnExit()
    }
}
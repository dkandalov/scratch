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

        val moveResult = moveScratches(fromFolder.list()!!.toList(), fromFolder.absolutePath, toFolder.absolutePath)

        moveResult shouldEqual Success
        toFolder.list()!!.toSet() shouldEqual setOf(
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

        val moveResult = moveScratches(fromFolder.list()!!.toList(), fromFolder.absolutePath, toFolder.absolutePath)

        moveResult shouldEqual Success
        toFolder.list()!!.toSet() shouldEqual setOf(
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

        val moveResult = moveScratches(fromFolder.list()!!.toList(), fromFolder.absolutePath, toFolder.absolutePath)

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
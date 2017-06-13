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

package scratch.filesystem

import com.intellij.CommonBundle
import com.intellij.openapi.application.Application
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.Computable
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import scratch.Answer
import java.io.File
import java.io.File.separator
import java.io.IOException


class FileSystem(
    scratchesFolderPath: String?,
    private val localFileSystem: LocalFileSystem = LocalFileSystem.getInstance(),
    private val application: Application = ApplicationManager.getApplication()
) {

    val scratchesPath: String =
        if (scratchesFolderPath == null || scratchesFolderPath.isEmpty()) {
            PathManager.getPluginsPath() + "/scratches/"
        } else {
            scratchesFolderPath + "/" // add trailing "/" in case it's not specified in config
        }

    fun listScratchFiles() = scratchFiles.map { it.name }

    fun scratchFileExists(fileName: String) = virtualFileBy(fileName).isValidScratch

    fun isValidScratchName(fileName: String): Answer {
        val hasPathChars = fileName.contains("/") || fileName.contains("\\")
        val hasWildcards = fileName.contains("*") || fileName.contains("?")
        return if (hasPathChars || hasWildcards || isHidden(fileName) || !localFileSystem.isValidName(fileName)) {
            Answer.no("Not a valid file name")
        } else if (File(scratchesPath + fileName).exists()) {
            Answer.no("There is existing file with this name")
        } else {
            Answer.yes()
        }
    }

    fun renameFile(oldFileName: String, newFileName: String): Boolean {
        val virtualFile = virtualFileBy(oldFileName) ?: return false
        return application.runWriteAction(Computable {
            try {
                virtualFile.rename(this, newFileName)
                true
            } catch (e: IOException) {
                log.warn(e)
                false
            }
        })
    }

    fun createEmptyFile(fileName: String): Boolean = createFile(fileName, text = "")

    fun createFile(fileName: String, text: String): Boolean =
        application.runWriteAction(Computable {
            try {
                doCreateFile(fileName, text)
            } catch (e: IOException) {
                log.warn(e)
                false
            }
        })

    fun removeFile(fileName: String): Boolean =
        application.runWriteAction(Computable {
            try {
                doRemoveFile(fileName)
            } catch (e: IOException) {
                log.warn(e)
                false
            }
        })

    private fun doRemoveFile(fileName: String): Boolean {
        val virtualFile = virtualFileBy(fileName) ?: return false
        virtualFile.delete(this)
        return true
    }

    private fun doCreateFile(fileName: String, text: String): Boolean {
        ensureExists(File(scratchesPath))
        val scratchesFolder = virtualFileBy("") ?: return false

        val scratchFile = scratchesFolder.createChildData(this, fileName)
        scratchFile.setBinaryContent(text.toByteArray(charset))
        return true
    }

    fun virtualFileBy(fileName: String): VirtualFile? =
        localFileSystem.refreshAndFindFileByPath(scratchesPath + fileName)

    fun isScratch(virtualFile: VirtualFile) = scratchFiles.contains(virtualFile)

    private val scratchFiles: List<VirtualFile> get() {
        val scratchesFolder = virtualFileBy("")
        if (scratchesFolder == null || !scratchesFolder.exists()) return emptyList()
        return scratchesFolder.children.filter{ it.isValidScratch }
    }

    private val VirtualFile?.isValidScratch get() =
        this != null && exists() && !isDirectory && !isHidden(name)

    companion object {
        private val log = Logger.getInstance(FileSystem::class.java)

        /**
         * Use UTF-8 to be compatible with old version of plugin.
         */
        private val charset = Charsets.UTF_8

        private fun isHidden(fileName: String) = fileName.startsWith(".")

        private fun ensureExists(dir: File) {
            if (!dir.exists() && !dir.mkdirs()) {
                throw IOException(CommonBundle.message("exception.directory.can.not.create", dir.path))
            }
        }
    }
}


fun moveScratches(scratchFilePaths: List<String>, fromFolder: String, toFolder: String): MoveResult {
    fun File.renamedIfExists(prefix: String = "_"): File {
        return if (!exists()) this
        else File(parent + separator + prefix + name).renamedIfExists()
    }

    val folder = File(toFolder)
    if (!folder.exists()) {
        return MoveResult.Failure("Target folder doesn't exist: ${folder.path}")
    }
    return try {
        val failedToMove = scratchFilePaths.map{ File(fromFolder + separator + it) }
            .filter { file ->
                val targetFile = File(folder.absolutePath + separator + file.name).renamedIfExists()
                val wasRenamed = file.renameTo(targetFile)
                !wasRenamed
            }
        if (failedToMove.isEmpty()) MoveResult.Success
        else MoveResult.Failure("Failed to move files: ${failedToMove.joinToString { it.name }}")
    } catch(e: Exception) {
        MoveResult.Failure(e.message ?: "")
    }
}

sealed class MoveResult {
    object Success: MoveResult()
    data class Failure(val reason: String): MoveResult()
}

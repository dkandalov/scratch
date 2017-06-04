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
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.Computable
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import scratch.Answer
import java.io.File
import java.io.IOException
import java.nio.charset.Charset


class FileSystem(scratchesFolderPath: String?) {

    private val fileManager = VirtualFileManager.getInstance()
    private val scratchesFolderPath: String

    init {
        if (scratchesFolderPath == null || scratchesFolderPath.isEmpty()) {
            this.scratchesFolderPath = PathManager.getPluginsPath() + "/scratches/"
        } else {
            this.scratchesFolderPath = scratchesFolderPath + "/" // add trailing "/" in case it's not specified in config
        }
    }

    fun listScratchFiles(): List<String> {
        val virtualFile = virtualFileBy(scratchFolder)
        if (virtualFile == null || !virtualFile.exists()) {
            return emptyList()
        }
        return virtualFile.children.filter{ it.canBeScratch() }.map { it.name }
    }

    fun scratchFileExists(fileName: String): Boolean {
        return virtualFileBy(fileName).canBeScratch()
    }

    fun isValidScratchName(fileName: String): Answer {
        val hasPathChars = fileName.contains("/") || fileName.contains("\\")
        val hasWildcards = fileName.contains("*") || fileName.contains("?")
        if (hasPathChars || hasWildcards || isHidden(fileName) || !LocalFileSystem.getInstance().isValidName(fileName)) {
            return Answer.no("Not a valid file name")
        } else if (File(scratchesFolderPath + fileName).exists()) {
            return Answer.no("There is existing file with this name")
        } else {
            return Answer.yes()
        }
    }

    fun renameFile(oldFileName: String, newFileName: String): Boolean {
        val virtualFile = virtualFileBy(oldFileName) ?: return false
        return ApplicationManager.getApplication().runWriteAction(Computable {
            doRenameFile(virtualFile, newFileName)
        })
    }

    fun createEmptyFile(fileName: String): Boolean {
        return createFile(fileName, "")
    }

    fun createFile(fileName: String, text: String): Boolean {
        val computable = Computable<Boolean> {
            try {
                doCreateFile(fileName, text)
            } catch (e: IOException) {
                log.warn(e)
                false
            }
        }
        return ApplicationManager.getApplication().runWriteAction(computable)
    }

    fun removeFile(fileName: String): Boolean {
        val computable = Computable<Boolean> {
            try {
                doRemoveFile(fileName)
            } catch (e: IOException) {
                log.warn(e)
                false
            }
        }
        return ApplicationManager.getApplication().runWriteAction(computable)
    }

    private fun doRemoveFile(fileName: String): Boolean {
        val virtualFile = virtualFileBy(fileName) ?: return false
        virtualFile.delete(this)
        return true
    }

    private fun doCreateFile(fileName: String, text: String): Boolean {
        ensureExists(File(scratchesFolderPath))
        val scratchesFolder = virtualFileBy(scratchFolder) ?: return false

        val scratchFile = scratchesFolder.createChildData(this@FileSystem, fileName)
        scratchFile.setBinaryContent(text.toByteArray(charset))
        return true
    }

    private fun doRenameFile(virtualFile: VirtualFile, newFileName: String): Boolean {
        return try {
            virtualFile.rename(this, newFileName)
            true
        } catch (e: IOException) {
            log.warn(e)
            false
        }
    }

    fun virtualFileBy(fileName: String): VirtualFile? {
        return fileManager.refreshAndFindFileByUrl("file://" + scratchesFolderPath + fileName)
    }

    fun isScratch(virtualFile: VirtualFile): Boolean {
        val scratchFolder = virtualFileBy(FileSystem.scratchFolder)
        return scratchFolder != null && scratchFolder.children.any { it == virtualFile }
    }

    private fun VirtualFile?.canBeScratch() =
        this != null && exists() && !isDirectory && !isHidden(name)

    companion object {
        private val log = Logger.getInstance(FileSystem::class.java)

        /**
         * Use UTF-8 to be compatible with old version of plugin.
         */
        private val charset = Charset.forName("UTF8")
        private val scratchFolder = ""

        private fun isHidden(fileName: String) = fileName.startsWith(".")

        private fun ensureExists(dir: File) {
            if (!dir.exists() && !dir.mkdirs()) {
                throw IOException(CommonBundle.message("exception.directory.can.not.create", dir.path))
            }
        }
    }
}

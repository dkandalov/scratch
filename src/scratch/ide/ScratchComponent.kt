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

package scratch.ide

import com.intellij.ide.scratch.ScratchFileService
import com.intellij.ide.scratch.ScratchRootType
import com.intellij.openapi.application.ApplicationManager.getApplication
import com.intellij.openapi.components.ApplicationComponent
import com.intellij.openapi.fileEditor.impl.NonProjectFileWritingAccessExtension
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import scratch.MrScratchManager
import scratch.filesystem.FileSystem
import scratch.filesystem.MoveResult
import scratch.filesystem.moveScratches
import java.io.File

class ScratchComponent: ApplicationComponent {

    private lateinit var mrScratchManager: MrScratchManager
    private lateinit var fileSystem: FileSystem

    override fun initComponent() {
        val log = ScratchLog()
        val configPersistence = ScratchConfigPersistence.instance
        val config = configPersistence.asConfig()

        fileSystem = FileSystem(configPersistence.scratchesFolderPath)

        val ideScratchesPath = ScratchFileService.getInstance().getRootPath(ScratchRootType.getInstance())
        FileUtil.ensureExists(File(ideScratchesPath))

        val needsMigration = ideScratchesPath != configPersistence.scratchesFolderPath
        if (needsMigration) {
            val moveResult = moveScratches(fileSystem.listScratchFiles(), fileSystem.scratchesPath, ideScratchesPath)
            when (moveResult) {
                is MoveResult.Success -> {
                    fileSystem = FileSystem(ideScratchesPath)
                    configPersistence.scratchesFolderPath = ideScratchesPath
                    VirtualFileManager.getInstance().refreshAndFindFileByUrl("file://$ideScratchesPath")
                    log.migratedToIdeScratches()
                }
                is MoveResult.Failure -> {
                    log.failedToMigrateScratchesToIdeLocation(moveResult.reason)
                }
            }
        }

        val ide = Ide(fileSystem, log)
        mrScratchManager = MrScratchManager(ide, fileSystem, config, log)
        mrScratchManager.syncScratchesWithFileSystem()

        ClipboardListener(mrScratchManager).startListening()
        OpenEditorTracker(mrScratchManager, fileSystem).startTracking()

        if (config.listenToClipboard) log.listeningToClipboard(true)
    }

    override fun getComponentName() = ScratchComponent::class.java.simpleName!!

    override fun disposeComponent() {}

    class FileWritingAccessExtension: NonProjectFileWritingAccessExtension {
        override fun isWritable(virtualFile: VirtualFile) = fileSystem().isScratch(virtualFile)
    }

    companion object {
        fun mrScratchManager() = getApplication().getComponent(ScratchComponent::class.java).mrScratchManager
        fun fileSystem() = getApplication().getComponent(ScratchComponent::class.java).fileSystem
    }
}

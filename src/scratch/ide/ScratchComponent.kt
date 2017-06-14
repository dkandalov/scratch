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
import com.intellij.notification.NotificationType.INFORMATION
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager.getApplication
import com.intellij.openapi.components.ApplicationComponent
import com.intellij.openapi.fileEditor.impl.NonProjectFileWritingAccessExtension
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.Disposer.newDisposable
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import scratch.MrScratchManager
import scratch.ide.Util.showNotification
import java.io.File

class ScratchComponent: ApplicationComponent {

    private lateinit var mrScratchManager: MrScratchManager
    private lateinit var fileSystem: FileSystem
    private lateinit var log: ScratchLog
    private val configPersistence = ScratchConfigPersistence.instance
    private lateinit var wiringDisposable: Disposable

    override fun initComponent() {
        wireComponents()
        checkIfScratchesNeedMigration()
    }

    private fun wireComponents() {
        wiringDisposable = newDisposable()
        log = ScratchLog()
        val config = configPersistence.asConfig()

        fileSystem = FileSystem(configPersistence.scratchesFolderPath)

        val ide = Ide(fileSystem, log)
        mrScratchManager = MrScratchManager(ide, fileSystem, config, log)
        mrScratchManager.syncScratchesWithFileSystem()

        OpenEditorTracker(mrScratchManager, fileSystem).startTracking(wiringDisposable)
        ClipboardListener(mrScratchManager).startListening(wiringDisposable)

        if (configPersistence.listenToClipboard) log.listeningToClipboard(true)
    }

    private fun checkIfScratchesNeedMigration() {
        val ideScratchesPath = ScratchFileService.getInstance().getRootPath(ScratchRootType.getInstance())
        FileUtil.ensureExists(File(ideScratchesPath))

        val needsMigration = ideScratchesPath != configPersistence.scratchesFolderPath
        if (needsMigration) {
            val message =
                "<a href=''>Click here</a> to migrate scratches to folder with IDE built-in scratches. " +
                "Both plugin and IDE scratches will be kept. In case of conflicting names, files will be prefixed with '_'."

            showNotification(message, INFORMATION) {
                val moveResult = moveScratches(fileSystem.listScratchFiles(), fileSystem.scratchesPath, ideScratchesPath)
                when (moveResult) {
                    is MoveResult.Success -> {
                        VirtualFileManager.getInstance().refreshAndFindFileByUrl("file://$ideScratchesPath")
                        configPersistence.scratchesFolderPath = ideScratchesPath

                        Disposer.dispose(wiringDisposable)
                        wireComponents()

                        log.migratedToIdeScratches()
                    }
                    is MoveResult.Failure -> {
                        log.failedToMigrateScratchesToIdeLocation(moveResult.reason)
                    }
                }
            }
        }
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

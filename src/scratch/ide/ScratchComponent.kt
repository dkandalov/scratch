package scratch.ide

import com.intellij.ide.scratch.ScratchFileService
import com.intellij.ide.scratch.ScratchRootType
import com.intellij.notification.NotificationType.INFORMATION
import com.intellij.openapi.application.ApplicationManager.getApplication
import com.intellij.openapi.components.ApplicationComponent
import com.intellij.openapi.fileEditor.impl.NonProjectFileWritingAccessExtension
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import scratch.MrScratchManager
import java.io.File

class ScratchComponent: ApplicationComponent {

    private lateinit var mrScratchManager: MrScratchManager
    private lateinit var fileSystem: FileSystem
    private lateinit var log: ScratchLog
    private val configPersistence = ScratchConfigPersistence.instance

    override fun initComponent() {
        wireComponents()
        checkIfScratchesNeedMigration()
    }

    private fun wireComponents() {
        log = ScratchLog()
        val config = configPersistence.asConfig()

        fileSystem = FileSystem(configPersistence.scratchesFolderPath)

        val ide = Ide(fileSystem, log)
        mrScratchManager = MrScratchManager(ide, fileSystem, config, log)
        mrScratchManager.syncScratchesWithFileSystem()

        OpenEditorTracker(mrScratchManager, fileSystem).startTracking()
        ClipboardListener(mrScratchManager).startListening()

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
                        configPersistence.scratchesFolderPath = ideScratchesPath
                        wireComponents()
                        VirtualFileManager.getInstance().refreshWithoutFileWatcher(true)
                        log.migratedToIdeScratches()
                    }
                    is MoveResult.Failure -> {
                        log.failedToMigrateScratchesToIdeLocation(moveResult.reason)
                    }
                }
            }
        }
    }

    companion object {
        fun mrScratchManager() = getApplication().getComponent(ScratchComponent::class.java).mrScratchManager
        fun fileSystem() = getApplication().getComponent(ScratchComponent::class.java).fileSystem
    }
}

class FileWritingAccessExtension: NonProjectFileWritingAccessExtension {
    override fun isWritable(virtualFile: VirtualFile) = ScratchComponent.fileSystem().isScratch(virtualFile)
}

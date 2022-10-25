package scratch.ide

import com.intellij.ide.scratch.ScratchFileService
import com.intellij.ide.scratch.ScratchRootType
import com.intellij.notification.NotificationType.INFORMATION
import com.intellij.openapi.application.ApplicationManager.getApplication
import com.intellij.openapi.fileEditor.impl.NonProjectFileWritingAccessExtension
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import scratch.MrScratchManager
import java.io.File

class ScratchComponent {
    private val log = ScratchLog()
    private val configPersistence = ScratchConfigPersistence.instance
    private val fileSystem = FileSystem(configPersistence.scratchesFolderPath)
    private val mrScratchManager = MrScratchManager(Ide(fileSystem, log), fileSystem, configPersistence.toScratchConfig(), log)

    init {
        mrScratchManager.syncScratchesWithFileSystem()

        OpenEditorTracker(mrScratchManager, fileSystem).startTracking()
        ClipboardListener(mrScratchManager).startListening()

        if (configPersistence.listenToClipboard) log.listeningToClipboard(true)
    }

    companion object {
        fun mrScratchManager() = getApplication().getComponent(ScratchComponent::class.java).mrScratchManager
        fun fileSystem() = getApplication().getComponent(ScratchComponent::class.java).fileSystem
    }
}

class FileWritingAccessExtension: NonProjectFileWritingAccessExtension {
    override fun isWritable(virtualFile: VirtualFile) = ScratchComponent.fileSystem().isScratch(virtualFile)
}

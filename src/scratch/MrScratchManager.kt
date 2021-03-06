package scratch

import com.intellij.openapi.util.UserDataHolder
import scratch.ScratchConfig.DefaultScratchMeaning
import scratch.ScratchConfig.DefaultScratchMeaning.LAST_OPENED
import scratch.ScratchConfig.DefaultScratchMeaning.TOPMOST
import scratch.ide.FileSystem
import scratch.ide.Ide
import scratch.ide.ScratchLog


class MrScratchManager(
    private val ide: Ide,
    private val fileSystem: FileSystem,
    private var config: ScratchConfig,
    private val log: ScratchLog
) {
    fun userWantsToSeeScratchesList(userDataHolder: UserDataHolder) {
        syncScratchesWithFileSystem()
        ide.displayScratchesListPopup(config.scratches, userDataHolder)
    }

    fun userWantsToOpenScratch(scratch: Scratch, userDataHolder: UserDataHolder) {
        if (fileSystem.scratchFileExists(scratch.fileName)) {
            ide.openScratch(scratch, userDataHolder)
        } else {
            log.failedToOpen(scratch)
        }
    }

    fun userWantsToOpenDefaultScratch(userDataHolder: UserDataHolder) {
        syncScratchesWithFileSystem()

        if (config.scratches.isEmpty()) {
            userWantsToEnterNewScratchName(userDataHolder)
            return
        }

        ide.openScratch(defaultScratch(), userDataHolder)
    }

    fun userWantsToChangeMeaningOfDefaultScratch(value: DefaultScratchMeaning) {
        updateConfig(config.withDefaultScratchMeaning(value))
    }

    fun defaultScratchMeaning() = config.defaultScratchMeaning

    fun userOpenedScratch(scratchFileName: String) {
        val scratch = findByFileName(scratchFileName) ?: return
        updateConfig(config.withLastOpenedScratch(scratch))
    }


    fun userWantsToEditScratchName(scratchFileName: String) {
        val scratch = findByFileName(scratchFileName) ?: return
        userWantsToEditScratchName(scratch)
    }

    fun userWantsToEditScratchName(scratch: Scratch) {
        ide.showRenameDialogFor(scratch)
    }

    fun checkIfUserCanRename(scratch: Scratch, fullNameWithMnemonics: String): Answer {
        if (fullNameWithMnemonics.isEmpty()) return Answer.no("Name cannot be empty")

        val renamedScratch = Scratch(fullNameWithMnemonics)
        if (scratch.fileName == renamedScratch.fileName) return Answer.yes()

        val haveScratchWithSameName = config.scratches.any {
            it != scratch &&
                it.name == renamedScratch.name &&
                it.extension == renamedScratch.extension
        }
        if (haveScratchWithSameName) return Answer.no("There is already a scratch with this name")

        return fileSystem.isValidScratchName(renamedScratch.fileName)
    }

    fun userWantsToRename(scratch: Scratch, fullNameWithMnemonics: String) {
        if (scratch.fullNameWithMnemonics == fullNameWithMnemonics) return

        val renamedScratch = Scratch(fullNameWithMnemonics)
        val wasRenamed = fileSystem.renameFile(scratch.fileName, renamedScratch.fileName)
        if (wasRenamed) {
            updateConfig(config.replace(scratch, renamedScratch))
        } else {
            log.failedToRename(scratch)
        }
    }


    fun userMovedScratch(scratch: Scratch, shift: Int) {
        updateConfig(config.move(scratch, shift))
    }


    fun userWantsToListenToClipboard(value: Boolean) {
        updateConfig(config.listenToClipboard(value))
        log.listeningToClipboard(value)
    }

    fun clipboardListenerWantsToPasteTextToScratch(clipboardText: String) {
        if (config.scratches.isEmpty()) {
            log.failedToOpenDefaultScratch()
            return
        }
        val scratch = defaultScratch()
        if (fileSystem.scratchFileExists(scratch.fileName)) {
            ide.addTextTo(scratch, clipboardText, config.clipboardAppendType)
        } else {
            log.failedToOpenDefaultScratch()
        }
    }

    fun shouldListenToClipboard(): Boolean {
        return config.listenToClipboard
    }

    fun userWantsToEnterNewScratchName(userDataHolder: UserDataHolder) {
        val defaultName = "scratch"
        val defaultExtension = "txt"
        if (isUniqueScratchName(defaultName)) {
            ide.openNewScratchDialog("$defaultName.$defaultExtension", userDataHolder)
        } else {
            (1..99).find { index -> isUniqueScratchName(defaultName + index) }
                ?.let { index -> ide.openNewScratchDialog("$defaultName$index.$defaultExtension", userDataHolder) }
        }
    }

    fun checkIfUserCanCreateScratchWithName(fullNameWithMnemonics: String): Answer {
        if (fullNameWithMnemonics.isEmpty()) return Answer.no("Name cannot be empty")

        val scratch = Scratch(fullNameWithMnemonics)

        if (!isUniqueScratchName(scratch.name, scratch.extension))
            return Answer.no("There is already a scratch with this name")

        return fileSystem.isValidScratchName(scratch.fileName)
    }

    fun userWantsToAddNewScratch(fullNameWithMnemonics: String, userDataHolder: UserDataHolder) {
        val scratch = Scratch(fullNameWithMnemonics)
        val wasCreated = fileSystem.createEmptyFile(scratch.fileName)
        if (wasCreated) {
            updateConfig(config.add(scratch))
            ide.openScratch(scratch, userDataHolder)
        } else {
            log.failedToCreate(scratch)
        }
    }


    fun userAttemptedToDeleteScratch(scratchFileName: String, userDataHolder: UserDataHolder) {
        val scratch = findByFileName(scratchFileName) ?: return
        userAttemptedToDeleteScratch(scratch, userDataHolder)
    }

    fun userAttemptedToDeleteScratch(scratch: Scratch, userDataHolder: UserDataHolder) {
        ide.showDeleteDialogFor(scratch, userDataHolder)
    }

    fun userWantsToDeleteScratch(scratch: Scratch) {
        val wasDeleted = fileSystem.deleteFile(scratch.fileName)
        if (wasDeleted) {
            updateConfig(config.without(scratch))
        } else {
            log.failedToDelete(scratch)
        }
    }


    fun syncScratchesWithFileSystem() {
        val fileNames = fileSystem.listScratchFiles()

        val oldScratches = config.scratches.filter { fileNames.contains(it.fileName) }
        val newFileNames = fileNames.filter { fileName -> oldScratches.none { scratch -> fileName == scratch.fileName } }
        val newScratches = newFileNames.map { Scratch(it) }

        val scratches = oldScratches + newScratches
        if (newScratches.isNotEmpty() || oldScratches.size != config.scratches.size) {
            var newConfig = config.with(scratches)
            if (config.lastOpenedScratch !in scratches) {
                newConfig = newConfig.withLastOpenedScratch(null)
            }
            updateConfig(newConfig)
        }
    }

    private fun defaultScratch() =
        when (config.defaultScratchMeaning) {
            TOPMOST     -> config.scratches[0]
            LAST_OPENED -> config.lastOpenedScratch ?: config.scratches[0]
        }

    private fun isUniqueScratchName(name: String) = config.scratches.none { it.name == name }

    private fun isUniqueScratchName(name: String, extension: String) =
        config.scratches.none { it.name == name && it.extension == extension }

    private fun updateConfig(newConfig: ScratchConfig) {
        if (config == newConfig) return
        config = newConfig
        ide.persistConfig(config)
    }

    private fun findByFileName(scratchFileName: String) =
        config.scratches.find { it.fileName == scratchFileName }
}

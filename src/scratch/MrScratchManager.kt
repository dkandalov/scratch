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

import com.intellij.openapi.util.UserDataHolder
import scratch.ScratchConfig.DefaultScratchMeaning
import scratch.ScratchConfig.DefaultScratchMeaning.LAST_OPENED
import scratch.ScratchConfig.DefaultScratchMeaning.TOPMOST
import scratch.filesystem.FileSystem
import scratch.ide.Ide
import scratch.ide.ScratchLog
import java.util.*


class MrScratchManager(
    private val ide: Ide,
    private val fileSystem: FileSystem,
    private var config: ScratchConfig,
    private val log: ScratchLog
) {

    fun migrate(scratchTexts: List<String>) {
        if (!fileSystem.listScratchFiles().isEmpty()) {
            log.willNotMigrateBecauseTargetFolderIsNotEmpty()
            return
        }
        val allEmpty = scratchTexts.none { it.isNotEmpty() }
        if (allEmpty) {
            val scratches = Arrays.asList<Scratch>(
                Scratch.create("&scratch.txt"),
                Scratch.create("scratch&2.txt"),
                Scratch.create("scratch&3.xml"),
                Scratch.create("scratch&4.xml")
            )
            for (scratch in scratches) {
                fileSystem.createEmptyFile(scratch.fileName)
            }
            updateConfig(config.with(scratches).needsMigration(false))
            return
        }

        val indexes = ArrayList<Int>()
        val scratches = ArrayList<Scratch>()

        (1..scratchTexts.size).forEach { i ->
            val scratchName = if (i == 1) "&scratch" else "scratch&" + i
            val scratch = Scratch.create(scratchName + ".txt")

            val wasCreated = fileSystem.createFile(scratch.fileName, scratchTexts[i - 1])
            if (wasCreated) {
                scratches.add(scratch)
            } else {
                indexes.add(i)
            }
        }

        if (indexes.isEmpty()) {
            log.migratedScratchesToFiles()
        } else {
            log.failedToMigrateScratchesToFiles(indexes)
        }
        updateConfig(config.with(scratches).needsMigration(false))
    }


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
        val scratch = findByFileName(scratchFileName)
        if (scratch != null) {
            updateConfig(config.withLastOpenedScratch(scratch))
        }
    }


    fun userWantsToEditScratchName(scratchFileName: String) {
        val scratch = findByFileName(scratchFileName)
        if (scratch != null) {
            userWantsToEditScratchName(scratch)
        }
    }

    fun userWantsToEditScratchName(scratch: Scratch) {
        ide.showRenameDialogFor(scratch)
    }

    fun checkIfUserCanRename(scratch: Scratch, fullNameWithMnemonics: String): Answer {
        if (fullNameWithMnemonics.isEmpty()) return Answer.no("Name cannot be empty")

        val renamedScratch = Scratch.create(fullNameWithMnemonics)
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

        val renamedScratch = Scratch.create(fullNameWithMnemonics)
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

    fun clipboardListenerWantsToAddTextToScratch(clipboardText: String) {
        if (config.scratches.isEmpty()) {
            log.failedToOpenDefaultScratch()
        } else {
            val scratch = defaultScratch()
            if (fileSystem.scratchFileExists(scratch.fileName)) {
                ide.addTextTo(scratch, clipboardText, config.clipboardAppendType)
            } else {
                log.failedToOpenDefaultScratch()
            }
        }
    }

    fun shouldListenToClipboard(): Boolean {
        return config.listenToClipboard
    }

    fun userWantsToEnterNewScratchName(userDataHolder: UserDataHolder) {
        val defaultName = "scratch"
        val defaultExtension = "txt"
        if (isUniqueScratchName(defaultName)) {
            ide.openNewScratchDialog(defaultName + "." + defaultExtension, userDataHolder)
            return
        }
        for (i in 1..99) {
            if (isUniqueScratchName(defaultName + i)) {
                ide.openNewScratchDialog(defaultName + i + "." + defaultExtension, userDataHolder)
                return
            }
        }
    }

    fun checkIfUserCanCreateScratchWithName(fullNameWithMnemonics: String): Answer {
        if (fullNameWithMnemonics.isEmpty()) return Answer.no("Name cannot be empty")

        val scratch = Scratch.create(fullNameWithMnemonics)

        if (!isUniqueScratchName(scratch.name, scratch.extension))
            return Answer.no("There is already a scratch with this name")

        return fileSystem.isValidScratchName(scratch.fileName)
    }

    fun userWantsToAddNewScratch(fullNameWithMnemonics: String, userDataHolder: UserDataHolder) {
        val scratch = Scratch.create(fullNameWithMnemonics)
        val wasCreated = fileSystem.createEmptyFile(scratch.fileName)
        if (wasCreated) {
            updateConfig(config.add(scratch))
            ide.openScratch(scratch, userDataHolder)
        } else {
            log.failedToCreate(scratch)
        }
    }


    fun userAttemptedToDeleteScratch(scratchFileName: String, userDataHolder: UserDataHolder) {
        val scratch = findByFileName(scratchFileName)
        if (scratch != null) userAttemptedToDeleteScratch(scratch, userDataHolder)
    }

    fun userAttemptedToDeleteScratch(scratch: Scratch, userDataHolder: UserDataHolder) {
        ide.showDeleteDialogFor(scratch, userDataHolder)
    }

    fun userWantsToDeleteScratch(scratch: Scratch) {
        val wasRemoved = fileSystem.removeFile(scratch.fileName)
        if (wasRemoved) {
            updateConfig(config.without(scratch))
        } else {
            log.failedToDelete(scratch)
        }
    }


    private fun syncScratchesWithFileSystem() {
        val fileNames = fileSystem.listScratchFiles()

        val oldScratches = config.scratches.filter { fileNames.contains(it.fileName) }
        val newFileNames = fileNames.filter { fileName -> !oldScratches.any { scratch -> fileName == scratch.fileName } }
        val newScratches = newFileNames.map { Scratch.create(it) }

        val scratches = oldScratches + newScratches
        if (!newScratches.isEmpty() || oldScratches.size != config.scratches.size) {
            var newConfig = config.with(scratches)
            if (!scratches.contains(config.lastOpenedScratch)) {
                newConfig = newConfig.withLastOpenedScratch(null)
            }
            updateConfig(newConfig)
        }
    }

    private fun defaultScratch() =
        when (config.defaultScratchMeaning) {
            TOPMOST -> config.scratches[0]
            LAST_OPENED -> config.lastOpenedScratch ?: config.scratches[0]
            else -> throw IllegalStateException()
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

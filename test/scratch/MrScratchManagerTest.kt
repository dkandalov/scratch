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

import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.util.containers.ContainerUtil.list
import org.hamcrest.core.IsEqual.equalTo
import org.junit.Assert.assertThat
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Matchers
import org.mockito.Mockito.*
import scratch.Answer.Companion.no
import scratch.Answer.Companion.yes
import scratch.ScratchConfig.AppendType
import scratch.ScratchConfig.DefaultScratchMeaning.LAST_OPENED
import scratch.ScratchConfig.DefaultScratchMeaning.TOPMOST
import scratch.filesystem.FileSystem
import scratch.ide.Ide
import scratch.ide.ScratchLog
import java.util.*


class MrScratchManagerTest {

    private val log = mock(ScratchLog::class.java)
    private val ide = mock(Ide::class.java)
    private val fileSystem = mock(FileSystem::class.java)
    private val defaultConfig = ScratchConfig.DEFAULT_CONFIG
    private var mrScratchManager: MrScratchManager? = null


    @Test fun shouldLog_when_successfullyMigratedScratchesToFiles() {
        mrScratchManager = scratchManagerWith(defaultConfig.needsMigration(true))
        `when`(fileSystem.createFile(Matchers.anyString(), Matchers.anyString())).thenReturn(true)

        val scratchTexts = list("text1", "text2", "text3", "text4", "text5")
        mrScratchManager!!.migrate(scratchTexts)

        verify(fileSystem).createFile("scratch.txt", "text1")
        verify(fileSystem).createFile("scratch2.txt", "text2")
        verify(fileSystem).createFile("scratch3.txt", "text3")
        verify(fileSystem).createFile("scratch4.txt", "text4")
        verify(fileSystem).createFile("scratch5.txt", "text5")
        verify(log).migratedScratchesToFiles()
    }

    @Test fun shouldNotMigrate_when_thereAreFilesInScratchFolder() {
        mrScratchManager = scratchManagerWith(defaultConfig.needsMigration(true))
        `when`(fileSystem.listScratchFiles()).thenReturn(list("scratch.txt"))

        val scratchTexts = list("text1", "text2", "text3", "text4", "text5")
        mrScratchManager!!.migrate(scratchTexts)

        verify(fileSystem).listScratchFiles()
        verifyNoMoreInteractions(fileSystem)
        verify(log).willNotMigrateBecauseTargetFolderIsNotEmpty()
    }

    @Test fun shouldSendNewConfigToIde_when_successfullyMigratedScratchesToFiles() {
        mrScratchManager = scratchManagerWith(defaultConfig.needsMigration(true))
        `when`(fileSystem.createFile(Matchers.anyString(), Matchers.anyString())).thenReturn(true)

        val scratches = list("text1", "text2", "text3", "text4", "text5")
        mrScratchManager!!.migrate(scratches)

        verify(ide).persistConfig(Matchers.eq(defaultConfig
                                                  .with(list(
                                                      scratch("&scratch.txt"),
                                                      scratch("scratch&2.txt"),
                                                      scratch("scratch&3.txt"),
                                                      scratch("scratch&4.txt"),
                                                      scratch("scratch&5.txt")))
                                                  .needsMigration(false)
        ))
    }

    @Test fun shouldLog_when_failedToMigrateSomeOfTheFiles() {
        mrScratchManager = scratchManagerWith(defaultConfig.needsMigration(true))
        `when`(fileSystem.createFile(Matchers.anyString(), Matchers.anyString())).thenReturn(true)
        `when`(fileSystem.createFile(Matchers.eq("scratch3.txt"), Matchers.anyString())).thenReturn(false)
        `when`(fileSystem.createFile(Matchers.eq("scratch4.txt"), Matchers.anyString())).thenReturn(false)

        val scratches = list("text1", "text2", "text3", "text4", "text5")
        mrScratchManager!!.migrate(scratches)

        verify(fileSystem).createFile("scratch.txt", "text1")
        verify(fileSystem).createFile("scratch2.txt", "text2")
        verify(fileSystem).createFile("scratch3.txt", "text3")
        verify(fileSystem).createFile("scratch4.txt", "text4")
        verify(fileSystem).createFile("scratch5.txt", "text5")
        verify(log).failedToMigrateScratchesToFiles(list(3, 4))
    }

    @Test fun shouldSendNewConfigToIde_when_failedToMigrateSomeOfTheFiles() {
        mrScratchManager = scratchManagerWith(defaultConfig.needsMigration(true))
        `when`(fileSystem.createFile(Matchers.anyString(), Matchers.anyString())).thenReturn(true)
        `when`(fileSystem.createFile(Matchers.eq("scratch3.txt"), Matchers.anyString())).thenReturn(false)
        `when`(fileSystem.createFile(Matchers.eq("scratch4.txt"), Matchers.anyString())).thenReturn(false)

        val scratches = list("text1", "text2", "text3", "text4", "text5")
        mrScratchManager!!.migrate(scratches)

        verify(ide).persistConfig(Matchers.eq(defaultConfig
                                                  .with(list(
                                                      scratch("&scratch.txt"),
                                                      scratch("scratch&2.txt"),
                                                      scratch("scratch&5.txt")
                                                  ))
                                                  .needsMigration(false)
        ))
    }


    @Test fun displayingScratchesList_when_configAndFiles_MatchExactly() {
        mrScratchManager = scratchManagerWith(defaultConfig.with(list(
            scratch("scratch.txt"),
            scratch("scratch2.java"),
            scratch("scratch3.html")
        )))
        `when`(fileSystem.listScratchFiles()).thenReturn(list("scratch.txt", "scratch2.java", "scratch3.html"))

        mrScratchManager!!.userWantsToSeeScratchesList(someUserData)

        verify(fileSystem).listScratchFiles()
        verify(ide).displayScratchesListPopup(Matchers.eq(list(
            scratch("scratch.txt"),
            scratch("scratch2.java"),
            scratch("scratch3.html")
        )), Matchers.same(someUserData))
        verifyNoMoreInteractions(fileSystem, ide)
    }

    @Test fun displayingScratchesList_when_configAndFiles_Match_ButHaveDifferentOrder() {
        mrScratchManager = scratchManagerWith(defaultConfig.with(list(
            scratch("scratch.txt"),
            scratch("scratch2.java"),
            scratch("scratch3.html")
        )))
        `when`(fileSystem.listScratchFiles()).thenReturn(list("scratch2.java", "scratch3.html", "scratch.txt"))

        mrScratchManager!!.userWantsToSeeScratchesList(someUserData)

        verify(fileSystem).listScratchFiles()
        verify(ide).displayScratchesListPopup(Matchers.eq(list(
            scratch("scratch.txt"),
            scratch("scratch2.java"),
            scratch("scratch3.html")
        )), Matchers.same(someUserData))
        verifyNoMoreInteractions(fileSystem, ide)
    }

    @Test fun displayingScratchesList_when_fileSystemHasNewFiles() {
        mrScratchManager = scratchManagerWith(defaultConfig.with(list(
            scratch("scratch.txt")
        )))
        `when`(fileSystem.listScratchFiles()).thenReturn(list("scratch2.java", "scratch.txt"))

        mrScratchManager!!.userWantsToSeeScratchesList(someUserData)

        verify(fileSystem).listScratchFiles()
        verify(ide).persistConfig(Matchers.eq(defaultConfig.with(list(
            scratch("scratch.txt"),
            scratch("scratch2.java")
        ))))
        verify(ide).displayScratchesListPopup(Matchers.eq(list(
            scratch("scratch.txt"),
            scratch("scratch2.java")
        )), Matchers.same(someUserData))
        verifyNoMoreInteractions(fileSystem, ide)
    }


    @Test fun openingScratch_when_scratchFileExists() {
        val scratch = scratch("scratch.txt")
        val config = defaultConfig.with(list(scratch))
        mrScratchManager = scratchManagerWith(config)
        `when`(fileSystem.scratchFileExists("scratch.txt")).thenReturn(true)

        mrScratchManager!!.userWantsToOpenScratch(scratch, someUserData)

        verify(fileSystem).scratchFileExists(Matchers.eq("scratch.txt"))
        verify(ide).openScratch(Matchers.eq(scratch), Matchers.same(someUserData))
    }

    @Test fun openingScratch_when_scratchFileDoesNotExist() {
        val scratch = scratch("scratch.txt")
        mrScratchManager = scratchManagerWith(defaultConfig.with(list(scratch)))
        `when`(fileSystem.scratchFileExists("scratch.txt")).thenReturn(false)

        mrScratchManager!!.userWantsToOpenScratch(scratch, someUserData)

        verify(fileSystem).scratchFileExists(Matchers.eq("scratch.txt"))
        verify(log).failedToOpen(Matchers.eq(scratch))
        verifyNoMoreInteractions(fileSystem, ide, log)
    }

    @Test fun openingDefaultScratch_when_itCanBeOpened_and_configuredAs_topmost() {
        val scratch1 = scratch("scratch1.txt")
        val scratch2 = scratch("scratch2.txt")
        val config = defaultConfig
            .with(list(scratch1, scratch2))
            .withDefaultScratchMeaning(TOPMOST)
            .withLastOpenedScratch(scratch2)
        mrScratchManager = scratchManagerWith(config)
        `when`(fileSystem.listScratchFiles()).thenReturn(list(scratch1.asFileName(), scratch2.asFileName()))
        `when`(fileSystem.scratchFileExists(Matchers.anyString())).thenReturn(true)

        mrScratchManager!!.userWantsToOpenDefaultScratch(someUserData)

        verify(ide).openScratch(Matchers.eq(scratch1), Matchers.same(someUserData))
    }

    @Test fun openingDefaultScratch_when_itCanBeOpened_and_configured_asLastOpened() {
        val scratch1 = scratch("scratch1.txt")
        val scratch2 = scratch("scratch2.txt")
        val config = defaultConfig
            .with(list(scratch1, scratch2))
            .withDefaultScratchMeaning(LAST_OPENED)
            .withLastOpenedScratch(scratch2)
        mrScratchManager = scratchManagerWith(config)
        `when`(fileSystem.listScratchFiles()).thenReturn(list(scratch1.asFileName(), scratch2.asFileName()))
        `when`(fileSystem.scratchFileExists(Matchers.anyString())).thenReturn(true)

        mrScratchManager!!.userWantsToOpenDefaultScratch(someUserData)

        verify(ide).openScratch(Matchers.eq(scratch2), Matchers.same(someUserData))
        verifyNoMoreInteractions(ide)
    }

    @Test fun openingDefaultScratch_when_itIsLastOpened_but_doesNotExist() {
        val scratch1 = scratch("scratch1.txt")
        val scratch2 = scratch("scratch2.txt")
        val config = defaultConfig
            .with(list(scratch1, scratch2))
            .withDefaultScratchMeaning(LAST_OPENED)
            .withLastOpenedScratch(scratch2)
        mrScratchManager = scratchManagerWith(config)
        `when`(fileSystem.listScratchFiles()).thenReturn(list(scratch1.asFileName()))
        `when`(fileSystem.scratchFileExists(scratch1.asFileName())).thenReturn(true)

        mrScratchManager!!.userWantsToOpenDefaultScratch(someUserData)

        verify(ide).openScratch(Matchers.eq(scratch1), Matchers.same(someUserData))
    }

    @Test fun openingDefaultScratch_when_scratchFileDoesNotExist() {
        mrScratchManager = scratchManagerWith(defaultConfig.with(list(scratch("scratch.txt"))))
        `when`(fileSystem.listScratchFiles()).thenReturn(ArrayList<String>())

        mrScratchManager!!.userWantsToOpenDefaultScratch(someUserData)

        verify(ide).persistConfig(defaultConfig)
        verify(ide).openNewScratchDialog(Matchers.anyString(), Matchers.same(someUserData))
        verifyNoMoreInteractions(ide, log)
    }

    @Test fun openingDefaultScratch_when_scratchListIsEmpty() {
        mrScratchManager = scratchManagerWith(defaultConfig)
        `when`(fileSystem.listScratchFiles()).thenReturn(ArrayList<String>())

        mrScratchManager!!.userWantsToOpenDefaultScratch(someUserData)

        verify(ide).openNewScratchDialog(Matchers.anyString(), Matchers.same(someUserData))
        verifyNoMoreInteractions(ide, log)
    }

    @Test fun shouldUpdateConfig_when_scratchIsOpened() {
        val config = defaultConfig.with(list(scratch("scratch&1.txt"), scratch("scratch&2.txt")))
        mrScratchManager = scratchManagerWith(config)

        mrScratchManager!!.userOpenedScratch("scratch2.txt")

        verify(ide).persistConfig(config.withLastOpenedScratch(scratch("scratch&2.txt")))
    }


    @Test fun appendingClipboardTextTo_TopmostScratch() {
        val scratch1 = scratch("scratch1.txt")
        val scratch2 = scratch("scratch2.txt")
        mrScratchManager = scratchManagerWith(defaultConfig
                                                  .with(list(scratch1, scratch2))
                                                  .withDefaultScratchMeaning(TOPMOST))
        `when`(fileSystem.scratchFileExists(Matchers.anyString())).thenReturn(true)

        mrScratchManager!!.clipboardListenerWantsToAddTextToScratch("clipboard text")

        verify(ide).addTextTo(Matchers.eq(scratch1), Matchers.eq("clipboard text"), Matchers.any(AppendType::class.java))
    }

    @Test fun appendingClipboardTextTo_LastOpenedScratch() {
        val scratch1 = scratch("scratch1.txt")
        val scratch2 = scratch("scratch2.txt")
        mrScratchManager = scratchManagerWith(defaultConfig
                                                  .with(list(scratch1, scratch2))
                                                  .withDefaultScratchMeaning(LAST_OPENED)
                                                  .withLastOpenedScratch(scratch2))
        `when`(fileSystem.scratchFileExists(Matchers.anyString())).thenReturn(true)

        mrScratchManager!!.clipboardListenerWantsToAddTextToScratch("clipboard text")

        verify(ide).addTextTo(Matchers.eq(scratch2), Matchers.eq("clipboard text"), Matchers.any(AppendType::class.java))
    }

    @Test fun appendingClipboardTextToDefaultScratch_when_scratchListIsEmpty() {
        mrScratchManager = scratchManagerWith(defaultConfig)

        mrScratchManager!!.clipboardListenerWantsToAddTextToScratch("clipboard text")

        verify(log).failedToOpenDefaultScratch()
    }

    @Test fun shouldSendNewConfigToIde_when_userTurnsOnListeningToClipboard() {
        mrScratchManager = scratchManagerWith(defaultConfig.listenToClipboard(false))

        mrScratchManager!!.userWantsToListenToClipboard(true)

        verify(ide).persistConfig(defaultConfig.listenToClipboard(true))
        verify(log).listeningToClipboard(true)
    }


    @Test fun canRenameScratch_when_nameIsUnique() {
        mrScratchManager = scratchManagerWith(defaultConfig.with(list(scratch("scratch.txt"))))
        `when`(fileSystem.isValidScratchName(Matchers.anyString())).thenReturn(yes())

        val answer = mrScratchManager!!.checkIfUserCanRename(scratch("scratch.txt"), "renamed&Scratch.txt")
        assertTrue(answer.isYes)

        verify(fileSystem).isValidScratchName("renamedScratch.txt")
        verifyZeroInteractions(ide, fileSystem)
    }

    @Test fun canRenameScratch_when_thereIsScratchWithSameName() {
        mrScratchManager = scratchManagerWith(defaultConfig.with(list(
            scratch("scratch.txt"),
            scratch("&renamedScratch.txt")
        )))

        val answer = mrScratchManager!!.checkIfUserCanRename(scratch("scratch.txt"), "renamed&Scratch.txt")
        assertTrue(answer.isNo)
        verifyZeroInteractions(ide, fileSystem)
    }

    @Test fun canRenameScratch_when_fileNameIsIncorrect() {
        val scratch = scratch("scratch.txt")
        mrScratchManager = scratchManagerWith(defaultConfig.with(list(scratch)))
        `when`(fileSystem.isValidScratchName(Matchers.anyString())).thenReturn(no("for a reason"))

        assertThat(mrScratchManager!!.checkIfUserCanRename(scratch, "renamedScratch.txt"), equalTo(no("for a reason")))

        verify(fileSystem).isValidScratchName(Matchers.eq("renamedScratch.txt"))
        verifyNoMoreInteractions(fileSystem, ide)
    }

    @Test fun renamingScratch_when_fileRenameIsSuccessful() {
        val scratch = scratch("scratch.txt")
        mrScratchManager = scratchManagerWith(defaultConfig.with(list(scratch)))
        `when`(fileSystem.renameFile(Matchers.anyString(), Matchers.anyString())).thenReturn(true)

        mrScratchManager!!.userWantsToRename(scratch, "&renamedScratch.txt")

        verify(fileSystem).renameFile("scratch.txt", "renamedScratch.txt")
        verify(ide).persistConfig(defaultConfig.with(list(
            scratch("&renamedScratch.txt")
        )))
        verifyNoMoreInteractions(ide, fileSystem)
    }

    @Test fun renamingScratch_when_fileRenameFails() {
        val scratch = scratch("scratch.txt")
        mrScratchManager = scratchManagerWith(defaultConfig.with(list(scratch)))
        `when`(fileSystem.renameFile(Matchers.anyString(), Matchers.anyString())).thenReturn(false)

        mrScratchManager!!.userWantsToRename(scratch, "renamedScratch.txt")

        verify(fileSystem).renameFile("scratch.txt", "renamedScratch.txt")
        verify(log).failedToRename(scratch)
        verifyNoMoreInteractions(ide, log, fileSystem)
    }

    @Test fun renamingScratch_when_newNameIsSameAsTheOldOne() {
        val scratch = scratch("scratch.txt")
        mrScratchManager = scratchManagerWith(defaultConfig.with(list(scratch)))
        `when`(fileSystem.renameFile(Matchers.anyString(), Matchers.anyString())).thenReturn(false)

        mrScratchManager!!.userWantsToRename(scratch, "scratch.txt")

        verifyZeroInteractions(ide, log, fileSystem)
    }


    @Test fun enteringNewScratchName_when_noScratchesExist() {
        mrScratchManager = scratchManagerWith(defaultConfig)

        mrScratchManager!!.userWantsToEnterNewScratchName(someUserData)

        verify(ide).openNewScratchDialog(Matchers.eq("scratch.txt"), Matchers.eq(someUserData))
    }

    @Test fun enteringNewScratchName_when_thereAreExistingScratches() {
        mrScratchManager = scratchManagerWith(defaultConfig.with(list(
            scratch("scratch1.txt"),
            scratch("scratch.txt")
        )))

        mrScratchManager!!.userWantsToEnterNewScratchName(someUserData)

        verify(ide).openNewScratchDialog(Matchers.eq("scratch2.txt"), Matchers.eq(someUserData))
    }

    @Test fun enteringNewScratchName_when_thereAreExistingNonTxtScratches() {
        mrScratchManager = scratchManagerWith(defaultConfig.with(list(
            scratch("scratch3.js"),
            scratch("scratch2.xml"),
            scratch("scratch1.xml"),
            scratch("scratch1.java"),
            scratch("scratch.java")
        )))

        mrScratchManager!!.userWantsToEnterNewScratchName(someUserData)

        verify(ide).openNewScratchDialog(Matchers.eq("scratch4.txt"), Matchers.eq(someUserData))
    }

    @Test fun canCreateNewScratch_when_nameIsUnique() {
        mrScratchManager = scratchManagerWith(defaultConfig)
        `when`(fileSystem.isValidScratchName(Matchers.anyString())).thenReturn(yes())

        val answer = mrScratchManager!!.checkIfUserCanCreateScratchWithName("&scratch.txt")
        assertTrue(answer.isYes)

        verify(fileSystem).isValidScratchName("scratch.txt")
        verifyNoMoreInteractions(ide, fileSystem)
    }

    @Test fun canCreateNewScratch_when_thereIsScratchWithSameNameAndExtension() {
        mrScratchManager = scratchManagerWith(defaultConfig.with(list(scratch("scratch.txt"))))

        val answer = mrScratchManager!!.checkIfUserCanCreateScratchWithName("&scratch.txt")
        assertTrue(answer.isNo)

        verifyZeroInteractions(ide, fileSystem)
    }

    @Test fun canCreateNewScratch_when_thereIsScratchWithSameNameButDifferentExtension() {
        mrScratchManager = scratchManagerWith(defaultConfig.with(list(scratch("scratch.txt"))))
        `when`(fileSystem.isValidScratchName(Matchers.anyString())).thenReturn(yes())

        val answer = mrScratchManager!!.checkIfUserCanCreateScratchWithName("&scratch.js")
        assertTrue(answer.isYes)

        verify(fileSystem).isValidScratchName("scratch.js")
        verifyNoMoreInteractions(ide, fileSystem)
    }

    @Test fun canCreateNewScratch_when_fileNameIsIncorrect() {
        mrScratchManager = scratchManagerWith(defaultConfig)
        `when`(fileSystem.isValidScratchName(Matchers.anyString())).thenReturn(no("for a reason"))

        val answer = mrScratchManager!!.checkIfUserCanCreateScratchWithName("&scratch.txt")
        assertTrue(answer.isNo)

        verify(fileSystem).isValidScratchName("scratch.txt")
        verifyNoMoreInteractions(ide, fileSystem)
    }

    @Test fun creatingNewScratch_when_scratchIsCreatedSuccessfully() {
        mrScratchManager = scratchManagerWith(defaultConfig.with(list(
            scratch("scratch0.txt")
        )))
        `when`(fileSystem.createEmptyFile(Matchers.anyString())).thenReturn(true)

        mrScratchManager!!.userWantsToAddNewScratch("&scratch.txt", someUserData)

        verify(fileSystem).createEmptyFile("scratch.txt")
        verify(ide).persistConfig(Matchers.eq(defaultConfig.with(list(
            scratch("scratch0.txt"),
            scratch("&scratch.txt")
        ))))
        verify(ide).openScratch(Matchers.eq(scratch("&scratch.txt")), Matchers.eq(someUserData))
        verifyNoMoreInteractions(ide, fileSystem)
    }

    @Test fun creatingNewScratch_when_scratchIsCreatedSuccessfully_andShouldBePrependedToListOfScratches() {
        val config = defaultConfig.with(list(
            scratch("scratch0.txt")
        )).withNewScratch(AppendType.PREPEND)
        mrScratchManager = scratchManagerWith(config)
        `when`(fileSystem.createEmptyFile(Matchers.anyString())).thenReturn(true)

        mrScratchManager!!.userWantsToAddNewScratch("&scratch.txt", someUserData)

        verify(fileSystem).createEmptyFile("scratch.txt")
        verify(ide).persistConfig(Matchers.eq(config.with(list(
            scratch("&scratch.txt"),
            scratch("scratch0.txt")
        ))))
        verify(ide).openScratch(Matchers.eq(scratch("&scratch.txt")), Matchers.eq(someUserData))
        verifyNoMoreInteractions(ide, fileSystem)
    }

    @Test fun creatingNewScratch_when_fileCreationFails() {
        mrScratchManager = scratchManagerWith(defaultConfig.with(list(scratch("&scratch.txt"))))
        `when`(fileSystem.createEmptyFile(Matchers.anyString())).thenReturn(false)

        mrScratchManager!!.userWantsToAddNewScratch("&scratch.txt", someUserData)

        verify(fileSystem).createEmptyFile("scratch.txt")
        verify(log).failedToCreate(scratch("&scratch.txt"))
    }


    @Test fun deleteScratch_whenFileCanBeRemoved() {
        val scratch = scratch("&scratch.txt")
        mrScratchManager = scratchManagerWith(defaultConfig.with(list(scratch)))
        `when`(fileSystem.removeFile(Matchers.anyString())).thenReturn(true)

        mrScratchManager!!.userWantsToDeleteScratch(scratch)

        verify(fileSystem).removeFile("scratch.txt")
        verify(ide).persistConfig(defaultConfig)
        verifyNoMoreInteractions(ide, fileSystem)
    }

    @Test fun deleteScratch_whenFileCouldNotBeRemoved() {
        val scratch = scratch("&scratch.txt")
        mrScratchManager = scratchManagerWith(defaultConfig.with(list(scratch)))
        `when`(fileSystem.removeFile(Matchers.anyString())).thenReturn(false)

        mrScratchManager!!.userWantsToDeleteScratch(scratch)

        verify(fileSystem).removeFile("scratch.txt")
        verify(log).failedToDelete(scratch)
        verifyNoMoreInteractions(ide, fileSystem)
    }


    @Test fun movingScratchUpInScratchesList() {
        mrScratchManager = scratchManagerWith(defaultConfig.with(list(
            scratch("scratch1.txt"),
            scratch("scratch2.txt"),
            scratch("scratch3.txt")
        )))

        val shiftUp = -1
        mrScratchManager!!.userMovedScratch(scratch("scratch2.txt"), shiftUp)

        verify(ide).persistConfig(defaultConfig.with(list(
            scratch("scratch2.txt"),
            scratch("scratch1.txt"),
            scratch("scratch3.txt")
        )))
    }

    private fun scratchManagerWith(config: ScratchConfig): MrScratchManager {
        return MrScratchManager(ide, fileSystem, config, log)
    }

    companion object {
        private val someUserData = UserDataHolderBase()

        private fun scratch(fullNameWithMnemonics: String): Scratch {
            return Scratch.create(fullNameWithMnemonics)
        }
    }
}

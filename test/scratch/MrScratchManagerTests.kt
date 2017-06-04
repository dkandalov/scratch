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
import org.hamcrest.core.IsEqual.equalTo
import org.junit.Assert.assertThat
import org.junit.Assert.assertTrue
import org.junit.Test
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
import org.mockito.Mockito.`when` as whenInvoked


class MrScratchManagerTests {

    private val log = mock(ScratchLog::class.java)
    private val ide = mock(Ide::class.java)
    private val fileSystem = mock(FileSystem::class.java)
    private val defaultConfig = ScratchConfig.DEFAULT_CONFIG
    private lateinit var mrScratchManager: MrScratchManager


    @Test fun `should log when successfully migrated scratches to files`() {
        mrScratchManager = scratchManagerWith(defaultConfig.needsMigration(true))
        whenInvoked(fileSystem.createFile(anyString(), anyString())).thenReturn(true)

        val scratchTexts = listOf("text1", "text2", "text3", "text4", "text5")
        mrScratchManager.migrate(scratchTexts)

        verify(fileSystem).createFile("scratch.txt", "text1")
        verify(fileSystem).createFile("scratch2.txt", "text2")
        verify(fileSystem).createFile("scratch3.txt", "text3")
        verify(fileSystem).createFile("scratch4.txt", "text4")
        verify(fileSystem).createFile("scratch5.txt", "text5")
        verify(log).migratedScratchesToFiles()
    }

    @Test fun `should not migrate when there are files in scratch folder`() {
        mrScratchManager = scratchManagerWith(defaultConfig.needsMigration(true))
        whenInvoked(fileSystem.listScratchFiles()).thenReturn(listOf("scratch.txt"))

        val scratchTexts = listOf("text1", "text2", "text3", "text4", "text5")
        mrScratchManager.migrate(scratchTexts)

        verify(fileSystem).listScratchFiles()
        verifyNoMoreInteractions(fileSystem)
        verify(log).willNotMigrateBecauseTargetFolderIsNotEmpty()
    }

    @Test fun `should send new config to ide when successfully migrated scratches to files`() {
        mrScratchManager = scratchManagerWith(defaultConfig.needsMigration(true))
        whenInvoked(fileSystem.createFile(anyString(), anyString())).thenReturn(true)

        val scratches = listOf("text1", "text2", "text3", "text4", "text5")
        mrScratchManager.migrate(scratches)

        verify(ide).persistConfig(eq(defaultConfig.with(listOf(
            scratch("&scratch.txt"),
            scratch("scratch&2.txt"),
            scratch("scratch&3.txt"),
            scratch("scratch&4.txt"),
            scratch("scratch&5.txt")
        )).needsMigration(false)))
    }

    @Test fun `should log when failed to migrate some of the files`() {
        mrScratchManager = scratchManagerWith(defaultConfig.needsMigration(true))
        whenInvoked(fileSystem.createFile(anyString(), anyString())).thenReturn(true)
        whenInvoked(fileSystem.createFile(eq("scratch3.txt"), anyString())).thenReturn(false)
        whenInvoked(fileSystem.createFile(eq("scratch4.txt"), anyString())).thenReturn(false)

        val scratches = listOf("text1", "text2", "text3", "text4", "text5")
        mrScratchManager.migrate(scratches)

        verify(fileSystem).createFile("scratch.txt", "text1")
        verify(fileSystem).createFile("scratch2.txt", "text2")
        verify(fileSystem).createFile("scratch3.txt", "text3")
        verify(fileSystem).createFile("scratch4.txt", "text4")
        verify(fileSystem).createFile("scratch5.txt", "text5")
        verify(log).failedToMigrateScratchesToFiles(listOf(3, 4))
    }

    @Test fun `should send new config to ide when failed to migrate some of the files`() {
        mrScratchManager = scratchManagerWith(defaultConfig.needsMigration(true))
        whenInvoked(fileSystem.createFile(anyString(), anyString())).thenReturn(true)
        whenInvoked(fileSystem.createFile(eq("scratch3.txt"), anyString())).thenReturn(false)
        whenInvoked(fileSystem.createFile(eq("scratch4.txt"), anyString())).thenReturn(false)

        val scratches = listOf("text1", "text2", "text3", "text4", "text5")
        mrScratchManager.migrate(scratches)

        verify(ide).persistConfig(eq(defaultConfig.with(listOf(
            scratch("&scratch.txt"),
            scratch("scratch&2.txt"),
            scratch("scratch&5.txt")
        )).needsMigration(false)))
    }


    @Test fun `displaying scratches list when config and files  match exactly`() {
        mrScratchManager = scratchManagerWith(defaultConfig.with(listOf(
            scratch("scratch.txt"),
            scratch("scratch2.java"),
            scratch("scratch3.html")
        )))
        whenInvoked(fileSystem.listScratchFiles()).thenReturn(listOf("scratch.txt", "scratch2.java", "scratch3.html"))

        mrScratchManager.userWantsToSeeScratchesList(someUserData)

        verify(fileSystem).listScratchFiles()
        verify(ide).displayScratchesListPopup(eq(listOf(
            scratch("scratch.txt"),
            scratch("scratch2.java"),
            scratch("scratch3.html")
        )), same(someUserData))
        verifyNoMoreInteractions(fileSystem, ide)
    }

    @Test fun `displaying scratches list when config and files  match  but have different order`() {
        mrScratchManager = scratchManagerWith(defaultConfig.with(listOf(
            scratch("scratch.txt"),
            scratch("scratch2.java"),
            scratch("scratch3.html")
        )))
        whenInvoked(fileSystem.listScratchFiles()).thenReturn(listOf("scratch2.java", "scratch3.html", "scratch.txt"))

        mrScratchManager.userWantsToSeeScratchesList(someUserData)

        verify(fileSystem).listScratchFiles()
        verify(ide).displayScratchesListPopup(eq(listOf(
            scratch("scratch.txt"),
            scratch("scratch2.java"),
            scratch("scratch3.html")
        )), same(someUserData))
        verifyNoMoreInteractions(fileSystem, ide)
    }

    @Test fun `displaying scratches list when file system has new files`() {
        mrScratchManager = scratchManagerWith(defaultConfig.with(listOf(
            scratch("scratch.txt")
        )))
        whenInvoked(fileSystem.listScratchFiles()).thenReturn(listOf("scratch2.java", "scratch.txt"))

        mrScratchManager.userWantsToSeeScratchesList(someUserData)

        verify(fileSystem).listScratchFiles()
        verify(ide).persistConfig(eq(defaultConfig.with(listOf(
            scratch("scratch.txt"),
            scratch("scratch2.java")
        ))))
        verify(ide).displayScratchesListPopup(eq(listOf(
            scratch("scratch.txt"),
            scratch("scratch2.java")
        )), same(someUserData))
        verifyNoMoreInteractions(fileSystem, ide)
    }


    @Test fun `opening scratch when scratch file exists`() {
        val scratch = scratch("scratch.txt")
        val config = defaultConfig.with(listOf(scratch))
        mrScratchManager = scratchManagerWith(config)
        whenInvoked(fileSystem.scratchFileExists("scratch.txt")).thenReturn(true)

        mrScratchManager.userWantsToOpenScratch(scratch, someUserData)

        verify(fileSystem).scratchFileExists(eq("scratch.txt"))
        verify(ide).openScratch(eq(scratch), same(someUserData))
    }

    @Test fun `opening scratch when scratch file does not exist`() {
        val scratch = scratch("scratch.txt")
        mrScratchManager = scratchManagerWith(defaultConfig.with(listOf(scratch)))
        whenInvoked(fileSystem.scratchFileExists("scratch.txt")).thenReturn(false)

        mrScratchManager.userWantsToOpenScratch(scratch, someUserData)

        verify(fileSystem).scratchFileExists(eq("scratch.txt"))
        verify(log).failedToOpen(eq(scratch))
        verifyNoMoreInteractions(fileSystem, ide, log)
    }

    @Test fun `opening default scratch when it can be opened and configured as topmost`() {
        val scratch1 = scratch("scratch1.txt")
        val scratch2 = scratch("scratch2.txt")
        val config = defaultConfig
            .with(listOf(scratch1, scratch2))
            .withDefaultScratchMeaning(TOPMOST)
            .withLastOpenedScratch(scratch2)
        mrScratchManager = scratchManagerWith(config)
        whenInvoked(fileSystem.listScratchFiles()).thenReturn(listOf(scratch1.fileName, scratch2.fileName))
        whenInvoked(fileSystem.scratchFileExists(anyString())).thenReturn(true)

        mrScratchManager.userWantsToOpenDefaultScratch(someUserData)

        verify(ide).openScratch(eq(scratch1), same(someUserData))
    }

    @Test fun `opening default scratch when it can be opened and configured as last opened`() {
        val scratch1 = scratch("scratch1.txt")
        val scratch2 = scratch("scratch2.txt")
        val config = defaultConfig
            .with(listOf(scratch1, scratch2))
            .withDefaultScratchMeaning(LAST_OPENED)
            .withLastOpenedScratch(scratch2)
        mrScratchManager = scratchManagerWith(config)
        whenInvoked(fileSystem.listScratchFiles()).thenReturn(listOf(scratch1.fileName, scratch2.fileName))
        whenInvoked(fileSystem.scratchFileExists(anyString())).thenReturn(true)

        mrScratchManager.userWantsToOpenDefaultScratch(someUserData)

        verify(ide).openScratch(eq(scratch2), same(someUserData))
        verifyNoMoreInteractions(ide)
    }

    @Test fun `opening default scratch when it is last opened but does not exist`() {
        val scratch1 = scratch("scratch1.txt")
        val scratch2 = scratch("scratch2.txt")
        val config = defaultConfig
            .with(listOf(scratch1, scratch2))
            .withDefaultScratchMeaning(LAST_OPENED)
            .withLastOpenedScratch(scratch2)
        mrScratchManager = scratchManagerWith(config)
        whenInvoked(fileSystem.listScratchFiles()).thenReturn(listOf(scratch1.fileName))
        whenInvoked(fileSystem.scratchFileExists(scratch1.fileName)).thenReturn(true)

        mrScratchManager.userWantsToOpenDefaultScratch(someUserData)

        verify(ide).openScratch(eq(scratch1), same(someUserData))
    }

    @Test fun `opening default scratch when scratch file does not exist`() {
        mrScratchManager = scratchManagerWith(defaultConfig.with(listOf(scratch("scratch.txt"))))
        whenInvoked(fileSystem.listScratchFiles()).thenReturn(ArrayList<String>())

        mrScratchManager.userWantsToOpenDefaultScratch(someUserData)

        verify(ide).persistConfig(defaultConfig)
        verify(ide).openNewScratchDialog(anyString(), same(someUserData))
        verifyNoMoreInteractions(ide, log)
    }

    @Test fun `opening default scratch when scratch list is empty`() {
        mrScratchManager = scratchManagerWith(defaultConfig)
        whenInvoked(fileSystem.listScratchFiles()).thenReturn(ArrayList<String>())

        mrScratchManager.userWantsToOpenDefaultScratch(someUserData)

        verify(ide).openNewScratchDialog(anyString(), same(someUserData))
        verifyNoMoreInteractions(ide, log)
    }

    @Test fun `should update config when scratch is opened`() {
        val config = defaultConfig.with(listOf(scratch("scratch&1.txt"), scratch("scratch&2.txt")))
        mrScratchManager = scratchManagerWith(config)

        mrScratchManager.userOpenedScratch("scratch2.txt")

        verify(ide).persistConfig(config.withLastOpenedScratch(scratch("scratch&2.txt")))
    }


    @Test fun `appending clipboard text to  topmost scratch`() {
        val scratch1 = scratch("scratch1.txt")
        val scratch2 = scratch("scratch2.txt")
        mrScratchManager = scratchManagerWith(defaultConfig
                                                  .with(listOf(scratch1, scratch2))
                                                  .withDefaultScratchMeaning(TOPMOST))
        whenInvoked(fileSystem.scratchFileExists(anyString())).thenReturn(true)

        mrScratchManager.clipboardListenerWantsToAddTextToScratch("clipboard text")

        verify(ide).addTextTo(eq(scratch1), eq("clipboard text"), some(AppendType::class))
    }

    @Test fun `appending clipboard text to  last opened scratch`() {
        val scratch1 = scratch("scratch1.txt")
        val scratch2 = scratch("scratch2.txt")
        mrScratchManager = scratchManagerWith(
            defaultConfig.with(listOf(scratch1, scratch2))
                .withDefaultScratchMeaning(LAST_OPENED)
                .withLastOpenedScratch(scratch2)
        )
        whenInvoked(fileSystem.scratchFileExists(anyString())).thenReturn(true)

        mrScratchManager.clipboardListenerWantsToAddTextToScratch("clipboard text")

        verify(ide).addTextTo(eq(scratch2), eq("clipboard text"), some(AppendType::class))
    }

    @Test fun `appending clipboard text to default scratch when scratch list is empty`() {
        mrScratchManager = scratchManagerWith(defaultConfig)

        mrScratchManager.clipboardListenerWantsToAddTextToScratch("clipboard text")

        verify(log).failedToOpenDefaultScratch()
    }

    @Test fun `should send new config to ide when user turns on listening to clipboard`() {
        mrScratchManager = scratchManagerWith(defaultConfig.listenToClipboard(false))

        mrScratchManager.userWantsToListenToClipboard(true)

        verify(ide).persistConfig(defaultConfig.listenToClipboard(true))
        verify(log).listeningToClipboard(true)
    }


    @Test fun `can rename scratch when name is unique`() {
        mrScratchManager = scratchManagerWith(defaultConfig.with(listOf(scratch("scratch.txt"))))
        whenInvoked(fileSystem.isValidScratchName(anyString())).thenReturn(yes())

        val answer = mrScratchManager.checkIfUserCanRename(scratch("scratch.txt"), "renamed&Scratch.txt")
        assertTrue(answer.isYes)

        verify(fileSystem).isValidScratchName("renamedScratch.txt")
        verifyZeroInteractions(ide, fileSystem)
    }

    @Test fun `can rename scratch when there is scratch with same name`() {
        mrScratchManager = scratchManagerWith(defaultConfig.with(listOf(
            scratch("scratch.txt"),
            scratch("&renamedScratch.txt")
        )))

        val answer = mrScratchManager.checkIfUserCanRename(scratch("scratch.txt"), "renamed&Scratch.txt")
        assertTrue(answer.isNo)
        verifyZeroInteractions(ide, fileSystem)
    }

    @Test fun `can rename scratch when file name is incorrect`() {
        val scratch = scratch("scratch.txt")
        mrScratchManager = scratchManagerWith(defaultConfig.with(listOf(scratch)))
        whenInvoked(fileSystem.isValidScratchName(anyString())).thenReturn(no("for a reason"))

        assertThat(mrScratchManager.checkIfUserCanRename(scratch, "renamedScratch.txt"), equalTo(no("for a reason")))

        verify(fileSystem).isValidScratchName(eq("renamedScratch.txt"))
        verifyNoMoreInteractions(fileSystem, ide)
    }

    @Test fun `renaming scratch when file rename is successful`() {
        val scratch = scratch("scratch.txt")
        mrScratchManager = scratchManagerWith(defaultConfig.with(listOf(scratch)))
        whenInvoked(fileSystem.renameFile(anyString(), anyString())).thenReturn(true)

        mrScratchManager.userWantsToRename(scratch, "&renamedScratch.txt")

        verify(fileSystem).renameFile("scratch.txt", "renamedScratch.txt")
        verify(ide).persistConfig(defaultConfig.with(listOf(
            scratch("&renamedScratch.txt")
        )))
        verifyNoMoreInteractions(ide, fileSystem)
    }

    @Test fun `renaming scratch when file rename fails`() {
        val scratch = scratch("scratch.txt")
        mrScratchManager = scratchManagerWith(defaultConfig.with(listOf(scratch)))
        whenInvoked(fileSystem.renameFile(anyString(), anyString())).thenReturn(false)

        mrScratchManager.userWantsToRename(scratch, "renamedScratch.txt")

        verify(fileSystem).renameFile("scratch.txt", "renamedScratch.txt")
        verify(log).failedToRename(scratch)
        verifyNoMoreInteractions(ide, log, fileSystem)
    }

    @Test fun `renaming scratch when new name is same as the old one`() {
        val scratch = scratch("scratch.txt")
        mrScratchManager = scratchManagerWith(defaultConfig.with(listOf(scratch)))
        whenInvoked(fileSystem.renameFile(anyString(), anyString())).thenReturn(false)

        mrScratchManager.userWantsToRename(scratch, "scratch.txt")

        verifyZeroInteractions(ide, log, fileSystem)
    }


    @Test fun `entering new scratch name when no scratches exist`() {
        mrScratchManager = scratchManagerWith(defaultConfig)

        mrScratchManager.userWantsToEnterNewScratchName(someUserData)

        verify(ide).openNewScratchDialog(eq("scratch.txt"), eq(someUserData))
    }

    @Test fun `entering new scratch name when there are existing scratches`() {
        mrScratchManager = scratchManagerWith(defaultConfig.with(listOf(
            scratch("scratch1.txt"),
            scratch("scratch.txt")
        )))

        mrScratchManager.userWantsToEnterNewScratchName(someUserData)

        verify(ide).openNewScratchDialog(eq("scratch2.txt"), eq(someUserData))
    }

    @Test fun `entering new scratch name when there are existing non txt scratches`() {
        mrScratchManager = scratchManagerWith(defaultConfig.with(listOf(
            scratch("scratch3.js"),
            scratch("scratch2.xml"),
            scratch("scratch1.xml"),
            scratch("scratch1.java"),
            scratch("scratch.java")
        )))

        mrScratchManager.userWantsToEnterNewScratchName(someUserData)

        verify(ide).openNewScratchDialog(eq("scratch4.txt"), eq(someUserData))
    }

    @Test fun `can create new scratch when name is unique`() {
        mrScratchManager = scratchManagerWith(defaultConfig)
        whenInvoked(fileSystem.isValidScratchName(anyString())).thenReturn(yes())

        val answer = mrScratchManager.checkIfUserCanCreateScratchWithName("&scratch.txt")
        assertTrue(answer.isYes)

        verify(fileSystem).isValidScratchName("scratch.txt")
        verifyNoMoreInteractions(ide, fileSystem)
    }

    @Test fun `can create new scratch when there is scratch with same name and extension`() {
        mrScratchManager = scratchManagerWith(defaultConfig.with(listOf(scratch("scratch.txt"))))

        val answer = mrScratchManager.checkIfUserCanCreateScratchWithName("&scratch.txt")
        assertTrue(answer.isNo)

        verifyZeroInteractions(ide, fileSystem)
    }

    @Test fun `can create new scratch when there is scratch with same name but different extension`() {
        mrScratchManager = scratchManagerWith(defaultConfig.with(listOf(scratch("scratch.txt"))))
        whenInvoked(fileSystem.isValidScratchName(anyString())).thenReturn(yes())

        val answer = mrScratchManager.checkIfUserCanCreateScratchWithName("&scratch.js")
        assertTrue(answer.isYes)

        verify(fileSystem).isValidScratchName("scratch.js")
        verifyNoMoreInteractions(ide, fileSystem)
    }

    @Test fun `can create new scratch when file name is incorrect`() {
        mrScratchManager = scratchManagerWith(defaultConfig)
        whenInvoked(fileSystem.isValidScratchName(anyString())).thenReturn(no("for a reason"))

        val answer = mrScratchManager.checkIfUserCanCreateScratchWithName("&scratch.txt")
        assertTrue(answer.isNo)

        verify(fileSystem).isValidScratchName("scratch.txt")
        verifyNoMoreInteractions(ide, fileSystem)
    }

    @Test fun `creating new scratch when scratch is created successfully`() {
        mrScratchManager = scratchManagerWith(defaultConfig.with(listOf(
            scratch("scratch0.txt")
        )))
        whenInvoked(fileSystem.createEmptyFile(anyString())).thenReturn(true)

        mrScratchManager.userWantsToAddNewScratch("&scratch.txt", someUserData)

        verify(fileSystem).createEmptyFile("scratch.txt")
        verify(ide).persistConfig(eq(defaultConfig.with(listOf(
            scratch("scratch0.txt"),
            scratch("&scratch.txt")
        ))))
        verify(ide).openScratch(eq(scratch("&scratch.txt")), eq(someUserData))
        verifyNoMoreInteractions(ide, fileSystem)
    }

    @Test fun `creating new scratch when scratch is created successfully and should be prepended to list of scratches`() {
        val config = defaultConfig.with(listOf(
            scratch("scratch0.txt")
        )).withNewScratch(AppendType.PREPEND)
        mrScratchManager = scratchManagerWith(config)
        whenInvoked(fileSystem.createEmptyFile(anyString())).thenReturn(true)

        mrScratchManager.userWantsToAddNewScratch("&scratch.txt", someUserData)

        verify(fileSystem).createEmptyFile("scratch.txt")
        verify(ide).persistConfig(eq(config.with(listOf(
            scratch("&scratch.txt"),
            scratch("scratch0.txt")
        ))))
        verify(ide).openScratch(eq(scratch("&scratch.txt")), eq(someUserData))
        verifyNoMoreInteractions(ide, fileSystem)
    }

    @Test fun `creating new scratch when file creation fails`() {
        mrScratchManager = scratchManagerWith(defaultConfig.with(listOf(scratch("&scratch.txt"))))
        whenInvoked(fileSystem.createEmptyFile(anyString())).thenReturn(false)

        mrScratchManager.userWantsToAddNewScratch("&scratch.txt", someUserData)

        verify(fileSystem).createEmptyFile("scratch.txt")
        verify(log).failedToCreate(scratch("&scratch.txt"))
    }


    @Test fun `delete scratch when file can be removed`() {
        val scratch = scratch("&scratch.txt")
        mrScratchManager = scratchManagerWith(defaultConfig.with(listOf(scratch)))
        whenInvoked(fileSystem.removeFile(anyString())).thenReturn(true)

        mrScratchManager.userWantsToDeleteScratch(scratch)

        verify(fileSystem).removeFile("scratch.txt")
        verify(ide).persistConfig(defaultConfig)
        verifyNoMoreInteractions(ide, fileSystem)
    }

    @Test fun `delete scratch when file could not be removed`() {
        val scratch = scratch("&scratch.txt")
        mrScratchManager = scratchManagerWith(defaultConfig.with(listOf(scratch)))
        whenInvoked(fileSystem.removeFile(anyString())).thenReturn(false)

        mrScratchManager.userWantsToDeleteScratch(scratch)

        verify(fileSystem).removeFile("scratch.txt")
        verify(log).failedToDelete(scratch)
        verifyNoMoreInteractions(ide, fileSystem)
    }


    @Test fun `moving scratch up in scratches list`() {
        mrScratchManager = scratchManagerWith(defaultConfig.with(listOf(
            scratch("scratch1.txt"),
            scratch("scratch2.txt"),
            scratch("scratch3.txt")
        )))

        val shiftUp = -1
        mrScratchManager.userMovedScratch(scratch("scratch2.txt"), shiftUp)

        verify(ide).persistConfig(defaultConfig.with(listOf(
            scratch("scratch2.txt"),
            scratch("scratch1.txt"),
            scratch("scratch3.txt")
        )))
    }

    private fun scratchManagerWith(config: ScratchConfig) = MrScratchManager(ide, fileSystem, config, log)

    companion object {
        private val someUserData = UserDataHolderBase()

        private fun scratch(fullNameWithMnemonics: String) = Scratch.create(fullNameWithMnemonics)
    }
}

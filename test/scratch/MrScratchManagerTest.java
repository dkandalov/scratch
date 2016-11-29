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

package scratch;

import com.intellij.openapi.util.UserDataHolder;
import com.intellij.openapi.util.UserDataHolderBase;
import org.junit.Test;
import scratch.filesystem.FileSystem;
import scratch.ide.Ide;
import scratch.ide.ScratchLog;

import java.util.ArrayList;
import java.util.List;

import static com.intellij.util.containers.ContainerUtil.list;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import static scratch.Answer.no;
import static scratch.Answer.yes;
import static scratch.ScratchConfig.AppendType;
import static scratch.ScratchConfig.DefaultScratchMeaning.LAST_OPENED;
import static scratch.ScratchConfig.DefaultScratchMeaning.TOPMOST;


@SuppressWarnings("Duplicates")
public class MrScratchManagerTest {
	private static final UserDataHolder someUserData = new UserDataHolderBase();

	private final ScratchLog log = mock(ScratchLog.class);
	private final Ide ide = mock(Ide.class);
	private final FileSystem fileSystem = mock(FileSystem.class);
	private final ScratchConfig defaultConfig = ScratchConfig.DEFAULT_CONFIG;
	private MrScratchManager mrScratchManager;


	@Test public void shouldLog_when_successfullyMigratedScratchesToFiles() {
		mrScratchManager = scratchManagerWith(defaultConfig.needsMigration(true));
		when(fileSystem.createFile(anyString(), anyString())).thenReturn(true);

		List<String> scratchTexts = list("text1", "text2", "text3", "text4", "text5");
		mrScratchManager.migrate(scratchTexts);

		verify(fileSystem).createFile("scratch.txt", "text1");
		verify(fileSystem).createFile("scratch2.txt", "text2");
		verify(fileSystem).createFile("scratch3.txt", "text3");
		verify(fileSystem).createFile("scratch4.txt", "text4");
		verify(fileSystem).createFile("scratch5.txt", "text5");
		verify(log).migratedScratchesToFiles();
	}

	@Test public void shouldNotMigrate_when_thereAreFilesInScratchFolder() {
		mrScratchManager = scratchManagerWith(defaultConfig.needsMigration(true));
		when(fileSystem.listScratchFiles()).thenReturn(list("scratch.txt"));

		List<String> scratchTexts = list("text1", "text2", "text3", "text4", "text5");
		mrScratchManager.migrate(scratchTexts);

		verify(fileSystem).listScratchFiles();
		verifyNoMoreInteractions(fileSystem);
		verify(log).willNotMigrateBecauseTargetFolderIsNotEmpty();
	}

	@Test public void shouldSendNewConfigToIde_when_successfullyMigratedScratchesToFiles() {
		mrScratchManager = scratchManagerWith(defaultConfig.needsMigration(true));
		when(fileSystem.createFile(anyString(), anyString())).thenReturn(true);

		List<String> scratches = list("text1", "text2", "text3", "text4", "text5");
		mrScratchManager.migrate(scratches);

		verify(ide).persistConfig(eq(defaultConfig
						.with(list(
								scratch("&scratch.txt"),
								scratch("scratch&2.txt"),
								scratch("scratch&3.txt"),
								scratch("scratch&4.txt"),
								scratch("scratch&5.txt")))
						.needsMigration(false)
		));
	}

	@Test public void shouldLog_when_failedToMigrateSomeOfTheFiles() {
		mrScratchManager = scratchManagerWith(defaultConfig.needsMigration(true));
		when(fileSystem.createFile(anyString(), anyString())).thenReturn(true);
		when(fileSystem.createFile(eq("scratch3.txt"), anyString())).thenReturn(false);
		when(fileSystem.createFile(eq("scratch4.txt"), anyString())).thenReturn(false);

		List<String> scratches = list("text1", "text2", "text3", "text4", "text5");
		mrScratchManager.migrate(scratches);

		verify(fileSystem).createFile("scratch.txt", "text1");
		verify(fileSystem).createFile("scratch2.txt", "text2");
		verify(fileSystem).createFile("scratch3.txt", "text3");
		verify(fileSystem).createFile("scratch4.txt", "text4");
		verify(fileSystem).createFile("scratch5.txt", "text5");
		verify(log).failedToMigrateScratchesToFiles(list(3, 4));
	}

	@Test public void shouldSendNewConfigToIde_when_failedToMigrateSomeOfTheFiles() {
		mrScratchManager = scratchManagerWith(defaultConfig.needsMigration(true));
		when(fileSystem.createFile(anyString(), anyString())).thenReturn(true);
		when(fileSystem.createFile(eq("scratch3.txt"), anyString())).thenReturn(false);
		when(fileSystem.createFile(eq("scratch4.txt"), anyString())).thenReturn(false);

		List<String> scratches = list("text1", "text2", "text3", "text4", "text5");
		mrScratchManager.migrate(scratches);

		verify(ide).persistConfig(eq(defaultConfig
						.with(list(
								scratch("&scratch.txt"),
								scratch("scratch&2.txt"),
								scratch("scratch&5.txt")
						))
						.needsMigration(false)
		));
	}


	@Test public void displayingScratchesList_when_configAndFiles_MatchExactly() {
		mrScratchManager = scratchManagerWith(defaultConfig.with(list(
				scratch("scratch.txt"),
				scratch("scratch2.java"),
				scratch("scratch3.html")
		)));
		when(fileSystem.listScratchFiles()).thenReturn(list("scratch.txt", "scratch2.java", "scratch3.html"));

		mrScratchManager.userWantsToSeeScratchesList(someUserData);

		verify(fileSystem).listScratchFiles();
		verify(ide).displayScratchesListPopup(eq(list(
				scratch("scratch.txt"),
				scratch("scratch2.java"),
				scratch("scratch3.html")
		)), same(someUserData));
		verifyNoMoreInteractions(fileSystem, ide);
	}

	@Test public void displayingScratchesList_when_configAndFiles_Match_ButHaveDifferentOrder() {
		mrScratchManager = scratchManagerWith(defaultConfig.with(list(
				scratch("scratch.txt"),
				scratch("scratch2.java"),
				scratch("scratch3.html")
		)));
		when(fileSystem.listScratchFiles()).thenReturn(list("scratch2.java", "scratch3.html", "scratch.txt"));

		mrScratchManager.userWantsToSeeScratchesList(someUserData);

		verify(fileSystem).listScratchFiles();
		verify(ide).displayScratchesListPopup(eq(list(
				scratch("scratch.txt"),
				scratch("scratch2.java"),
				scratch("scratch3.html")
		)), same(someUserData));
		verifyNoMoreInteractions(fileSystem, ide);
	}

	@Test public void displayingScratchesList_when_fileSystemHasNewFiles() {
		mrScratchManager = scratchManagerWith(defaultConfig.with(list(
				scratch("scratch.txt")
		)));
		when(fileSystem.listScratchFiles()).thenReturn(list("scratch2.java", "scratch.txt"));

		mrScratchManager.userWantsToSeeScratchesList(someUserData);

		verify(fileSystem).listScratchFiles();
		verify(ide).persistConfig(eq(defaultConfig.with(list(
				scratch("scratch.txt"),
				scratch("scratch2.java")
		))));
		verify(ide).displayScratchesListPopup(eq(list(
				scratch("scratch.txt"),
				scratch("scratch2.java")
		)), same(someUserData));
		verifyNoMoreInteractions(fileSystem, ide);
	}


	@Test public void openingScratch_when_scratchFileExists() {
		Scratch scratch = scratch("scratch.txt");
		ScratchConfig config = defaultConfig.with(list(scratch));
		mrScratchManager = scratchManagerWith(config);
		when(fileSystem.scratchFileExists("scratch.txt")).thenReturn(true);

		mrScratchManager.userWantsToOpenScratch(scratch, someUserData);

		verify(fileSystem).scratchFileExists(eq("scratch.txt"));
		verify(ide).openScratch(eq(scratch), same(someUserData));
	}

	@Test public void openingScratch_when_scratchFileDoesNotExist() {
		Scratch scratch = scratch("scratch.txt");
		mrScratchManager = scratchManagerWith(defaultConfig.with(list(scratch)));
		when(fileSystem.scratchFileExists("scratch.txt")).thenReturn(false);

		mrScratchManager.userWantsToOpenScratch(scratch, someUserData);

		verify(fileSystem).scratchFileExists(eq("scratch.txt"));
		verify(log).failedToOpen(eq(scratch));
		verifyNoMoreInteractions(fileSystem, ide, log);
	}

	@Test public void openingDefaultScratch_when_itCanBeOpened_and_configuredAs_topmost() {
		Scratch scratch1 = scratch("scratch1.txt");
		Scratch scratch2 = scratch("scratch2.txt");
		ScratchConfig config = defaultConfig
				.with(list(scratch1, scratch2))
				.withDefaultScratchMeaning(TOPMOST)
				.withLastOpenedScratch(scratch2);
		mrScratchManager = scratchManagerWith(config);
		when(fileSystem.listScratchFiles()).thenReturn(list(scratch1.asFileName(), scratch2.asFileName()));
		when(fileSystem.scratchFileExists(anyString())).thenReturn(true);

		mrScratchManager.userWantsToOpenDefaultScratch(someUserData);

		verify(ide).openScratch(eq(scratch1), same(someUserData));
	}

	@Test public void openingDefaultScratch_when_itCanBeOpened_and_configured_asLastOpened() {
		Scratch scratch1 = scratch("scratch1.txt");
		Scratch scratch2 = scratch("scratch2.txt");
		ScratchConfig config = defaultConfig
				.with(list(scratch1, scratch2))
				.withDefaultScratchMeaning(LAST_OPENED)
				.withLastOpenedScratch(scratch2);
		mrScratchManager = scratchManagerWith(config);
		when(fileSystem.listScratchFiles()).thenReturn(list(scratch1.asFileName(), scratch2.asFileName()));
		when(fileSystem.scratchFileExists(anyString())).thenReturn(true);

		mrScratchManager.userWantsToOpenDefaultScratch(someUserData);

		verify(ide).openScratch(eq(scratch2), same(someUserData));
		verifyNoMoreInteractions(ide);
	}

	@Test public void openingDefaultScratch_when_itIsLastOpened_but_doesNotExist() {
		Scratch scratch1 = scratch("scratch1.txt");
		Scratch scratch2 = scratch("scratch2.txt");
		ScratchConfig config = defaultConfig
				.with(list(scratch1, scratch2))
				.withDefaultScratchMeaning(LAST_OPENED)
				.withLastOpenedScratch(scratch2);
		mrScratchManager = scratchManagerWith(config);
		when(fileSystem.listScratchFiles()).thenReturn(list(scratch1.asFileName()));
		when(fileSystem.scratchFileExists(scratch1.asFileName())).thenReturn(true);

		mrScratchManager.userWantsToOpenDefaultScratch(someUserData);

		verify(ide).openScratch(eq(scratch1), same(someUserData));
	}

	@Test public void openingDefaultScratch_when_scratchFileDoesNotExist() {
		mrScratchManager = scratchManagerWith(defaultConfig.with(list(scratch("scratch.txt"))));
		when(fileSystem.listScratchFiles()).thenReturn(new ArrayList<>());

		mrScratchManager.userWantsToOpenDefaultScratch(someUserData);

		verify(ide).persistConfig(defaultConfig);
		verify(ide).openNewScratchDialog(anyString(), same(someUserData));
		verifyNoMoreInteractions(ide, log);
	}

	@Test public void openingDefaultScratch_when_scratchListIsEmpty() {
		mrScratchManager = scratchManagerWith(defaultConfig);
		when(fileSystem.listScratchFiles()).thenReturn(new ArrayList<>());

		mrScratchManager.userWantsToOpenDefaultScratch(someUserData);

		verify(ide).openNewScratchDialog(anyString(), same(someUserData));
		verifyNoMoreInteractions(ide, log);
	}

	@Test public void shouldUpdateConfig_when_scratchIsOpened() {
		ScratchConfig config = defaultConfig.with(list(scratch("scratch&1.txt"), scratch("scratch&2.txt")));
		mrScratchManager = scratchManagerWith(config);

		mrScratchManager.userOpenedScratch("scratch2.txt");

		verify(ide).persistConfig(config.withLastOpenedScratch(scratch("scratch&2.txt")));
	}


	@Test public void appendingClipboardTextTo_TopmostScratch() {
		Scratch scratch1 = scratch("scratch1.txt");
		Scratch scratch2 = scratch("scratch2.txt");
		mrScratchManager = scratchManagerWith(defaultConfig
				.with(list(scratch1, scratch2))
				.withDefaultScratchMeaning(TOPMOST));
		when(fileSystem.scratchFileExists(anyString())).thenReturn(true);

		mrScratchManager.clipboardListenerWantsToAddTextToScratch("clipboard text");

		verify(ide).addTextTo(eq(scratch1), eq("clipboard text"), any(AppendType.class));
	}

	@Test public void appendingClipboardTextTo_LastOpenedScratch() {
		Scratch scratch1 = scratch("scratch1.txt");
		Scratch scratch2 = scratch("scratch2.txt");
		mrScratchManager = scratchManagerWith(defaultConfig
				.with(list(scratch1, scratch2))
				.withDefaultScratchMeaning(LAST_OPENED)
				.withLastOpenedScratch(scratch2));
		when(fileSystem.scratchFileExists(anyString())).thenReturn(true);

		mrScratchManager.clipboardListenerWantsToAddTextToScratch("clipboard text");

		verify(ide).addTextTo(eq(scratch2), eq("clipboard text"), any(AppendType.class));
	}

	@Test public void appendingClipboardTextToDefaultScratch_when_scratchListIsEmpty() {
		mrScratchManager = scratchManagerWith(defaultConfig);

		mrScratchManager.clipboardListenerWantsToAddTextToScratch("clipboard text");

		verify(log).failedToOpenDefaultScratch();
	}

	@Test public void shouldSendNewConfigToIde_when_userTurnsOnListeningToClipboard() {
		mrScratchManager = scratchManagerWith(defaultConfig.listenToClipboard(false));

		mrScratchManager.userWantsToListenToClipboard(true);

		verify(ide).persistConfig(defaultConfig.listenToClipboard(true));
		verify(log).listeningToClipboard(true);
	}


	@Test public void canRenameScratch_when_nameIsUnique() {
		mrScratchManager = scratchManagerWith(defaultConfig.with(list(scratch("scratch.txt"))));
		when(fileSystem.isValidScratchName(anyString())).thenReturn(yes());

		Answer answer = mrScratchManager.checkIfUserCanRename(scratch("scratch.txt"), "renamed&Scratch.txt");
		assertTrue(answer.isYes);

		verify(fileSystem).isValidScratchName("renamedScratch.txt");
		verifyZeroInteractions(ide, fileSystem);
	}

	@Test public void canRenameScratch_when_thereIsScratchWithSameName() {
		mrScratchManager = scratchManagerWith(defaultConfig.with(list(
				scratch("scratch.txt"),
				scratch("&renamedScratch.txt")
		)));

		Answer answer = mrScratchManager.checkIfUserCanRename(scratch("scratch.txt"), "renamed&Scratch.txt");
		assertTrue(answer.isNo);
		verifyZeroInteractions(ide, fileSystem);
	}

	@Test public void canRenameScratch_when_fileNameIsIncorrect() {
		Scratch scratch = scratch("scratch.txt");
		mrScratchManager = scratchManagerWith(defaultConfig.with(list(scratch)));
		when(fileSystem.isValidScratchName(anyString())).thenReturn(no("for a reason"));

		assertThat(mrScratchManager.checkIfUserCanRename(scratch, "renamedScratch.txt"), equalTo(no("for a reason")));

		verify(fileSystem).isValidScratchName(eq("renamedScratch.txt"));
		verifyNoMoreInteractions(fileSystem, ide);
	}

	@Test public void renamingScratch_when_fileRenameIsSuccessful() {
		Scratch scratch = scratch("scratch.txt");
		mrScratchManager = scratchManagerWith(defaultConfig.with(list(scratch)));
		when(fileSystem.renameFile(anyString(), anyString())).thenReturn(true);

		mrScratchManager.userWantsToRename(scratch, "&renamedScratch.txt");

		verify(fileSystem).renameFile("scratch.txt", "renamedScratch.txt");
		verify(ide).persistConfig(defaultConfig.with(list(
				scratch("&renamedScratch.txt")
		)));
		verifyNoMoreInteractions(ide, fileSystem);
	}

	@Test public void renamingScratch_when_fileRenameFails() {
		Scratch scratch = scratch("scratch.txt");
		mrScratchManager = scratchManagerWith(defaultConfig.with(list(scratch)));
		when(fileSystem.renameFile(anyString(), anyString())).thenReturn(false);

		mrScratchManager.userWantsToRename(scratch, "renamedScratch.txt");

		verify(fileSystem).renameFile("scratch.txt", "renamedScratch.txt");
		verify(log).failedToRename(scratch);
		verifyNoMoreInteractions(ide, log, fileSystem);
	}

	@Test public void renamingScratch_when_newNameIsSameAsTheOldOne() {
		Scratch scratch = scratch("scratch.txt");
		mrScratchManager = scratchManagerWith(defaultConfig.with(list(scratch)));
		when(fileSystem.renameFile(anyString(), anyString())).thenReturn(false);

		mrScratchManager.userWantsToRename(scratch, "scratch.txt");

		verifyZeroInteractions(ide, log, fileSystem);
	}


	@Test public void enteringNewScratchName_when_noScratchesExist() {
		mrScratchManager = scratchManagerWith(defaultConfig);

		mrScratchManager.userWantsToEnterNewScratchName(someUserData);

		verify(ide).openNewScratchDialog(eq("scratch.txt"), eq(someUserData));
	}

	@Test public void enteringNewScratchName_when_thereAreExistingScratches() {
		mrScratchManager = scratchManagerWith(defaultConfig.with(list(
				scratch("scratch1.txt"),
				scratch("scratch.txt")
		)));

		mrScratchManager.userWantsToEnterNewScratchName(someUserData);

		verify(ide).openNewScratchDialog(eq("scratch2.txt"), eq(someUserData));
	}

	@Test public void enteringNewScratchName_when_thereAreExistingNonTxtScratches() {
		mrScratchManager = scratchManagerWith(defaultConfig.with(list(
				scratch("scratch3.js"),
				scratch("scratch2.xml"),
				scratch("scratch1.xml"),
				scratch("scratch1.java"),
				scratch("scratch.java")
		)));

		mrScratchManager.userWantsToEnterNewScratchName(someUserData);

		verify(ide).openNewScratchDialog(eq("scratch4.txt"), eq(someUserData));
	}

	@Test public void canCreateNewScratch_when_nameIsUnique() {
		mrScratchManager = scratchManagerWith(defaultConfig);
		when(fileSystem.isValidScratchName(anyString())).thenReturn(yes());

		Answer answer = mrScratchManager.checkIfUserCanCreateScratchWithName("&scratch.txt");
		assertTrue(answer.isYes);

		verify(fileSystem).isValidScratchName("scratch.txt");
		verifyNoMoreInteractions(ide, fileSystem);
	}

	@Test public void canCreateNewScratch_when_thereIsScratchWithSameNameAndExtension() {
		mrScratchManager = scratchManagerWith(defaultConfig.with(list(scratch("scratch.txt"))));

		Answer answer = mrScratchManager.checkIfUserCanCreateScratchWithName("&scratch.txt");
		assertTrue(answer.isNo);

		verifyZeroInteractions(ide, fileSystem);
	}

	@Test public void canCreateNewScratch_when_thereIsScratchWithSameNameButDifferentExtension() {
		mrScratchManager = scratchManagerWith(defaultConfig.with(list(scratch("scratch.txt"))));
		when(fileSystem.isValidScratchName(anyString())).thenReturn(yes());

		Answer answer = mrScratchManager.checkIfUserCanCreateScratchWithName("&scratch.js");
		assertTrue(answer.isYes);

		verify(fileSystem).isValidScratchName("scratch.js");
		verifyNoMoreInteractions(ide, fileSystem);
	}

	@Test public void canCreateNewScratch_when_fileNameIsIncorrect() {
		mrScratchManager = scratchManagerWith(defaultConfig);
		when(fileSystem.isValidScratchName(anyString())).thenReturn(no("for a reason"));

		Answer answer = mrScratchManager.checkIfUserCanCreateScratchWithName("&scratch.txt");
		assertTrue(answer.isNo);

		verify(fileSystem).isValidScratchName("scratch.txt");
		verifyNoMoreInteractions(ide, fileSystem);
	}

	@Test public void creatingNewScratch_when_scratchIsCreatedSuccessfully() {
		mrScratchManager = scratchManagerWith(defaultConfig.with(list(
				scratch("scratch0.txt")
		)));
		when(fileSystem.createEmptyFile(anyString())).thenReturn(true);

		mrScratchManager.userWantsToAddNewScratch("&scratch.txt", someUserData);

		verify(fileSystem).createEmptyFile("scratch.txt");
		verify(ide).persistConfig(eq(defaultConfig.with(list(
				scratch("scratch0.txt"),
				scratch("&scratch.txt")
		))));
		verify(ide).openScratch(eq(scratch("&scratch.txt")), eq(someUserData));
		verifyNoMoreInteractions(ide, fileSystem);
	}

	@Test public void creatingNewScratch_when_scratchIsCreatedSuccessfully_andShouldBePrependedToListOfScratches() {
		ScratchConfig config = defaultConfig.with(list(
				scratch("scratch0.txt")
		)).withNewScratch(AppendType.PREPEND);
		mrScratchManager = scratchManagerWith(config);
		when(fileSystem.createEmptyFile(anyString())).thenReturn(true);

		mrScratchManager.userWantsToAddNewScratch("&scratch.txt", someUserData);

		verify(fileSystem).createEmptyFile("scratch.txt");
		verify(ide).persistConfig(eq(config.with(list(
				scratch("&scratch.txt"),
				scratch("scratch0.txt")
		))));
		verify(ide).openScratch(eq(scratch("&scratch.txt")), eq(someUserData));
		verifyNoMoreInteractions(ide, fileSystem);
	}

	@Test public void creatingNewScratch_when_fileCreationFails() {
		mrScratchManager = scratchManagerWith(defaultConfig.with(list(scratch("&scratch.txt"))));
		when(fileSystem.createEmptyFile(anyString())).thenReturn(false);

		mrScratchManager.userWantsToAddNewScratch("&scratch.txt", someUserData);

		verify(fileSystem).createEmptyFile("scratch.txt");
		verify(log).failedToCreate(scratch("&scratch.txt"));
	}


	@Test public void deleteScratch_whenFileCanBeRemoved() {
		Scratch scratch = scratch("&scratch.txt");
		mrScratchManager = scratchManagerWith(defaultConfig.with(list(scratch)));
		when(fileSystem.removeFile(anyString())).thenReturn(true);

		mrScratchManager.userWantsToDeleteScratch(scratch);

		verify(fileSystem).removeFile("scratch.txt");
		verify(ide).persistConfig(defaultConfig);
		verifyNoMoreInteractions(ide, fileSystem);
	}

	@Test public void deleteScratch_whenFileCouldNotBeRemoved() {
		Scratch scratch = scratch("&scratch.txt");
		mrScratchManager = scratchManagerWith(defaultConfig.with(list(scratch)));
		when(fileSystem.removeFile(anyString())).thenReturn(false);

		mrScratchManager.userWantsToDeleteScratch(scratch);

		verify(fileSystem).removeFile("scratch.txt");
		verify(log).failedToDelete(scratch);
		verifyNoMoreInteractions(ide, fileSystem);
	}


	@Test public void movingScratchUpInScratchesList() {
		mrScratchManager = scratchManagerWith(defaultConfig.with(list(
				scratch("scratch1.txt"),
				scratch("scratch2.txt"),
				scratch("scratch3.txt")
		)));

		int shiftUp = -1;
		mrScratchManager.userMovedScratch(scratch("scratch2.txt"), shiftUp);

		verify(ide).persistConfig(defaultConfig.with(list(
				scratch("scratch2.txt"),
				scratch("scratch1.txt"),
				scratch("scratch3.txt")
		)));
	}

	private static Scratch scratch(String fullNameWithMnemonics) {
		return Scratch.create(fullNameWithMnemonics);
	}

	private MrScratchManager scratchManagerWith(ScratchConfig config) {
		return new MrScratchManager(ide, fileSystem, config, log);
	}
}

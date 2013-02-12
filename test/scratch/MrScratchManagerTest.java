package scratch;

import com.intellij.openapi.util.UserDataHolder;
import com.intellij.openapi.util.UserDataHolderBase;
import org.junit.Test;
import scratch.filesystem.FileSystem;
import scratch.ide.Ide;

import java.util.List;

import static com.intellij.util.containers.ContainerUtil.list;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import static scratch.MrScratchManager.Answer;
import static scratch.MrScratchManager.Answer.yes;
import static scratch.ScratchConfig.AppendType;


/**
 * User: dima
 * Date: 09/02/2013
 */
public class MrScratchManagerTest {

	private static final UserDataHolder USER_DATA = new UserDataHolderBase();

	private final Ide ide = mock(Ide.class);
	private final FileSystem fileSystem = mock(FileSystem.class);
	private final ScratchConfig defaultConfig = ScratchConfig.DEFAULT_CONFIG;
	private MrScratchManager mrScratchManager;

	@Test public void shouldNotifyIde_when_successfullyMigratedScratchesToFiles() {
		mrScratchManager = createWith(defaultConfig.needsMigration(true));
		when(fileSystem.createFile(anyString(), anyString())).thenReturn(true);

		List<String> scratches = list("text1", "text2", "text3", "text4", "text5");
		mrScratchManager.migrate(scratches);

		verify(fileSystem).createFile("scratch.txt", "text1");
		verify(fileSystem).createFile("scratch2.txt", "text2");
		verify(fileSystem).createFile("scratch3.txt", "text3");
		verify(fileSystem).createFile("scratch4.txt", "text4");
		verify(fileSystem).createFile("scratch5.txt", "text5");
		verify(ide).migratedScratchesToFiles();
	}

	@Test public void shouldSendNewConfigToIde_when_successfullyMigratedScratchesToFiles() {
		mrScratchManager = createWith(defaultConfig.needsMigration(true));
		when(fileSystem.createFile(anyString(), anyString())).thenReturn(true);

		List<String> scratches = list("text1", "text2", "text3", "text4", "text5");
		mrScratchManager.migrate(scratches);

		verify(ide).updateConfig(eq(defaultConfig
				.with(list(
						new Scratch("&scratch", "txt"),
						new Scratch("scratch&2", "txt"),
						new Scratch("scratch&3", "txt"),
						new Scratch("scratch&4", "txt"),
						new Scratch("scratch&5", "txt")))
				.needsMigration(false)
		));
	}

	@Test public void shouldNotifyIde_when_failedToMigrateSomeOfTheFiles() {
		mrScratchManager = createWith(defaultConfig.needsMigration(true));
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
		verify(ide).failedToMigrateScratchesToFiles(list(3, 4));
	}

	@Test public void shouldSendNewConfigToIde_when_failedToMigrateSomeOfTheFiles() {
		mrScratchManager = createWith(defaultConfig.needsMigration(true));
		when(fileSystem.createFile(anyString(), anyString())).thenReturn(true);
		when(fileSystem.createFile(eq("scratch3.txt"), anyString())).thenReturn(false);
		when(fileSystem.createFile(eq("scratch4.txt"), anyString())).thenReturn(false);

		List<String> scratches = list("text1", "text2", "text3", "text4", "text5");
		mrScratchManager.migrate(scratches);

		verify(ide).updateConfig(eq(defaultConfig
				.with(list(
						new Scratch("&scratch", "txt"),
						new Scratch("scratch&2", "txt"),
						new Scratch("scratch&5", "txt")
				))
				.needsMigration(false)
		));
	}

	@Test public void displayingScratchesList_when_configAndFiles_MatchExactly() {
		mrScratchManager = createWith(defaultConfig.with(list(
				new Scratch("scratch", "txt"),
				new Scratch("scratch2", "java"),
				new Scratch("scratch3", "html")
		)));
		when(fileSystem.listScratchFiles()).thenReturn(list("scratch.txt", "scratch2.java", "scratch3.html"));

		mrScratchManager.userWantsToSeeScratchesList(USER_DATA);

		verify(fileSystem).listScratchFiles();
		verify(ide).displayScratchesListPopup(eq(list(
				new Scratch("scratch", "txt"),
				new Scratch("scratch2", "java"),
				new Scratch("scratch3", "html")
		)), same(USER_DATA));
		verifyNoMoreInteractions(fileSystem, ide);
	}

	@Test public void displayingScratchesList_when_configAndFiles_Match_ButHaveDifferentOrder() {
		mrScratchManager = createWith(defaultConfig.with(list(
				new Scratch("scratch", "txt"),
				new Scratch("scratch2", "java"),
				new Scratch("scratch3", "html")
		)));
		when(fileSystem.listScratchFiles()).thenReturn(list("scratch2.java", "scratch3.html", "scratch.txt"));

		mrScratchManager.userWantsToSeeScratchesList(USER_DATA);

		verify(fileSystem).listScratchFiles();
		verify(ide).displayScratchesListPopup(eq(list(
				new Scratch("scratch", "txt"),
				new Scratch("scratch2", "java"),
				new Scratch("scratch3", "html")
		)), same(USER_DATA));
		verifyNoMoreInteractions(fileSystem, ide);
	}

	@Test public void displayingScratchesList_when_fileSystemHasNewFiles() {
		mrScratchManager = createWith(defaultConfig.with(list(
				new Scratch("scratch", "txt")
		)));
		when(fileSystem.listScratchFiles()).thenReturn(list("scratch2.java", "scratch.txt"));

		mrScratchManager.userWantsToSeeScratchesList(USER_DATA);

		verify(fileSystem).listScratchFiles();
		verify(ide).updateConfig(eq(defaultConfig.with(list(
				new Scratch("scratch", "txt"),
				new Scratch("scratch2", "java")
		))));
		verify(ide).displayScratchesListPopup(eq(list(
				new Scratch("scratch", "txt"),
				new Scratch("scratch2", "java")
		)), same(USER_DATA));
		verifyNoMoreInteractions(fileSystem, ide);
	}

	@Test public void openingScratch_when_scratchFileExists() {
		Scratch scratch = new Scratch("scratch", "txt");
		mrScratchManager = createWith(defaultConfig.with(list(scratch)));
		when(fileSystem.scratchFileExists("scratch.txt")).thenReturn(true);

		mrScratchManager.userWantsToOpenScratch(scratch, USER_DATA);

		verify(fileSystem).scratchFileExists(eq("scratch.txt"));
		verify(ide).openScratch(eq(scratch), same(USER_DATA));
	}

	@Test public void openingScratch_when_scratchFileDoesNotExist() {
		Scratch scratch = new Scratch("scratch", "txt");
		mrScratchManager = createWith(defaultConfig.with(list(scratch)));
		when(fileSystem.scratchFileExists("scratch.txt")).thenReturn(false);

		mrScratchManager.userWantsToOpenScratch(scratch, USER_DATA);

		verify(fileSystem).scratchFileExists(eq("scratch.txt"));
		verify(ide).failedToOpen(eq(scratch));
	}

	@Test public void openingDefaultScratch_when_scratchListIsNotEmpty_and_scratchFileExists() {
		Scratch scratch = new Scratch("scratch", "txt");
		mrScratchManager = createWith(defaultConfig.with(list(scratch)));
		when(fileSystem.scratchFileExists("scratch.txt")).thenReturn(true);

		mrScratchManager.userWantsToOpenDefaultScratch(USER_DATA);

		verify(ide).openScratch(eq(scratch), same(USER_DATA));
	}

	@Test public void openingDefaultScratch_when_scratchFileDoesNotExist() {
		mrScratchManager = createWith(defaultConfig.with(list(new Scratch("scratch", "txt"))));
		when(fileSystem.scratchFileExists("scratch.txt")).thenReturn(false);

		mrScratchManager.userWantsToOpenDefaultScratch(USER_DATA);
		verify(fileSystem).scratchFileExists(eq("scratch.txt"));

		verify(ide).failedToOpenDefaultScratch();
	}

	// TODO should create new scratch in this case
	@Test public void openingDefaultScratch_when_scratchListIsEmpty() {
		mrScratchManager = createWith(defaultConfig);

		mrScratchManager.userWantsToOpenDefaultScratch(USER_DATA);

		verify(ide).failedToOpenDefaultScratch();
	}

	@Test public void appendingClipboardTextToDefaultScratch() {
		Scratch scratch = new Scratch("scratch", "txt");
		mrScratchManager = createWith(defaultConfig.with(list(scratch)));
		when(fileSystem.scratchFileExists("scratch.txt")).thenReturn(true);

		mrScratchManager.clipboardListenerWantsToAddTextToScratch("clipboard text");

		verify(fileSystem).scratchFileExists(eq("scratch.txt"));
		verify(ide).addTextTo(eq(scratch), eq("clipboard text"), any(AppendType.class));
	}

	// TODO should create new scratch in this case?
	@Test public void appendingClipboardTextToDefaultScratch_when_scratchListIsEmpty() {
		mrScratchManager = createWith(defaultConfig);

		mrScratchManager.clipboardListenerWantsToAddTextToScratch("clipboard text");

		verify(ide).failedToOpenDefaultScratch();
	}

	@Test public void renamingScratch_when_newNameIsUnique_and_fileRenameWasSuccessful() {
		Scratch scratch = new Scratch("scratch", "txt");
		mrScratchManager = createWith(defaultConfig.with(list(scratch)));
		when(fileSystem.renameFile(anyString(), anyString())).thenReturn(true);

		mrScratchManager.userWantsToRename(scratch);
		assertThat(mrScratchManager.checkIfUserCanRename(scratch, "&renamedScratch.txt"), equalTo(yes()));
		mrScratchManager.userRenamed(scratch, "&renamedScratch.txt");

		verify(ide).showRenameDialogFor(scratch);
		verify(fileSystem).renameFile("scratch.txt", "renamedScratch.txt");
		verify(ide).updateConfig(defaultConfig.with(list(
				new Scratch("&renamedScratch", "txt")
		)));
	}

	@Test public void renamingScratch_when_fileRenameFailed() {
		Scratch scratch = new Scratch("scratch", "txt");
		mrScratchManager = createWith(defaultConfig.with(list(scratch)));
		when(fileSystem.renameFile(anyString(), anyString())).thenReturn(false);

		mrScratchManager.userWantsToRename(scratch);
		assertThat(mrScratchManager.checkIfUserCanRename(scratch, "renamedScratch.txt"), equalTo(yes()));
		mrScratchManager.userRenamed(scratch, "renamedScratch.txt");

		verify(ide).showRenameDialogFor(scratch);
		verify(fileSystem).renameFile("scratch.txt", "renamedScratch.txt");
		verify(ide).failedToRename(scratch);
	}

	@Test public void ifCanRenameScratch_when_newNameIsNotUnique() {
		mrScratchManager = createWith(defaultConfig.with(list(
				new Scratch("scratch", "txt"),
				new Scratch("&renamedScratch", "txt")
		)));

		Answer answer = mrScratchManager.checkIfUserCanRename(new Scratch("scratch", "txt"), "renamed&Scratch.txt");
		assertTrue(answer.isNo);
		verifyZeroInteractions(ide, fileSystem);
	}

	@Test public void ifCanRenameScratch_toNameWithoutExtension() {
		Scratch scratch = new Scratch("scratch", "txt");
		mrScratchManager = createWith(defaultConfig.with(list(scratch)));

		assertThat(mrScratchManager.checkIfUserCanRename(scratch, "renamedScratch"), equalTo(yes()));
	}

	@Test public void movingScratchUpInScratchesList() {
		mrScratchManager = createWith(defaultConfig.with(list(
				new Scratch("scratch1", "txt"),
				new Scratch("scratch2", "txt"),
				new Scratch("scratch3", "txt")
		)));

		int shiftUp = -1;
		mrScratchManager.userMovedScratch(new Scratch("scratch2", "txt"), shiftUp);

		verify(ide).updateConfig(defaultConfig.with(list(
				new Scratch("scratch2", "txt"),
				new Scratch("scratch1", "txt"),
				new Scratch("scratch3", "txt")
		)));
	}

	@Test public void shouldNewConfigToIde_when_userTurnsOnListeningToClipboard() {
		mrScratchManager = createWith(defaultConfig.listenToClipboard(false));

		mrScratchManager.userWantsToListenToClipboard(true);

		verify(ide).updateConfig(defaultConfig.listenToClipboard(true));
	}

	private MrScratchManager createWith(ScratchConfig config) {
		return new MrScratchManager(ide, fileSystem, config);
	}
}

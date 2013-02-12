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
import static org.mockito.Mockito.*;
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
						new ScratchInfo("&scratch", "txt"),
						new ScratchInfo("scratch&2", "txt"),
						new ScratchInfo("scratch&3", "txt"),
						new ScratchInfo("scratch&4", "txt"),
						new ScratchInfo("scratch&5", "txt")))
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
						new ScratchInfo("&scratch", "txt"),
						new ScratchInfo("scratch&2", "txt"),
						new ScratchInfo("scratch&5", "txt")
				))
				.needsMigration(false)
		));
	}

	@Test public void displayingScratchesList_when_configAndFiles_MatchExactly() {
		mrScratchManager = createWith(defaultConfig.with(list(
				new ScratchInfo("scratch", "txt"),
				new ScratchInfo("scratch2", "java"),
				new ScratchInfo("scratch3", "html")
		)));
		when(fileSystem.listScratchFiles()).thenReturn(list("scratch.txt", "scratch2.java", "scratch3.html"));

		mrScratchManager.userWantsToSeeScratchesList(USER_DATA);

		verify(fileSystem).listScratchFiles();
		verify(ide).displayScratchesListPopup(eq(list(
				new ScratchInfo("scratch", "txt"),
				new ScratchInfo("scratch2", "java"),
				new ScratchInfo("scratch3", "html")
		)), same(USER_DATA));
		verifyNoMoreInteractions(fileSystem, ide);
	}

	@Test public void displayingScratchesList_when_configAndFiles_Match_ButHaveDifferentOrder() {
		mrScratchManager = createWith(defaultConfig.with(list(
				new ScratchInfo("scratch", "txt"),
				new ScratchInfo("scratch2", "java"),
				new ScratchInfo("scratch3", "html")
		)));
		when(fileSystem.listScratchFiles()).thenReturn(list("scratch2.java", "scratch3.html", "scratch.txt"));

		mrScratchManager.userWantsToSeeScratchesList(USER_DATA);

		verify(fileSystem).listScratchFiles();
		verify(ide).displayScratchesListPopup(eq(list(
				new ScratchInfo("scratch", "txt"),
				new ScratchInfo("scratch2", "java"),
				new ScratchInfo("scratch3", "html")
		)), same(USER_DATA));
		verifyNoMoreInteractions(fileSystem, ide);
	}

	@Test public void displayingScratchesList_when_fileSystemHasNewFiles() {
		mrScratchManager = createWith(defaultConfig.with(list(
				new ScratchInfo("scratch", "txt")
		)));
		when(fileSystem.listScratchFiles()).thenReturn(list("scratch2.java", "scratch.txt"));

		mrScratchManager.userWantsToSeeScratchesList(USER_DATA);

		verify(fileSystem).listScratchFiles();
		verify(ide).updateConfig(eq(defaultConfig.with(list(
				new ScratchInfo("scratch", "txt"),
				new ScratchInfo("scratch2", "java")
		))));
		verify(ide).displayScratchesListPopup(eq(list(
				new ScratchInfo("scratch", "txt"),
				new ScratchInfo("scratch2", "java")
		)), same(USER_DATA));
		verifyNoMoreInteractions(fileSystem, ide);
	}

	@Test public void openingScratch_when_scratchFileExists() {
		ScratchInfo scratchInfo = new ScratchInfo("scratch", "txt");
		mrScratchManager = createWith(defaultConfig.with(list(scratchInfo)));
		when(fileSystem.scratchFileExists("scratch.txt")).thenReturn(true);

		mrScratchManager.userWantsToOpenScratch(scratchInfo, USER_DATA);

		verify(fileSystem).scratchFileExists(eq("scratch.txt"));
		verify(ide).openScratch(eq(scratchInfo), same(USER_DATA));
	}

	@Test public void openingScratch_when_scratchFileDoesNotExist() {
		ScratchInfo scratchInfo = new ScratchInfo("scratch", "txt");
		mrScratchManager = createWith(defaultConfig.with(list(scratchInfo)));
		when(fileSystem.scratchFileExists("scratch.txt")).thenReturn(false);

		mrScratchManager.userWantsToOpenScratch(scratchInfo, USER_DATA);

		verify(fileSystem).scratchFileExists(eq("scratch.txt"));
		verify(ide).failedToOpen(eq(scratchInfo));
	}

	@Test public void openingDefaultScratch_when_scratchListIsNotEmpty_and_scratchFileExists() {
		ScratchInfo scratchInfo = new ScratchInfo("scratch", "txt");
		mrScratchManager = createWith(defaultConfig.with(list(scratchInfo)));
		when(fileSystem.scratchFileExists("scratch.txt")).thenReturn(true);

		mrScratchManager.userWantsToOpenDefaultScratch(USER_DATA);

		verify(ide).openScratch(eq(scratchInfo), same(USER_DATA));
	}

	@Test public void openingDefaultScratch_when_scratchFileDoesNotExist() {
		mrScratchManager = createWith(defaultConfig.with(list(new ScratchInfo("scratch", "txt"))));
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
		ScratchInfo scratchInfo = new ScratchInfo("scratch", "txt");
		mrScratchManager = createWith(defaultConfig.with(list(scratchInfo)));
		when(fileSystem.scratchFileExists("scratch.txt")).thenReturn(true);

		mrScratchManager.clipboardListenerWantsToAddTextToScratch("clipboard text");

		verify(fileSystem).scratchFileExists(eq("scratch.txt"));
		verify(ide).addTextTo(eq(scratchInfo), eq("clipboard text"), any(AppendType.class));
	}

	// TODO should create new scratch in this case?
	@Test public void appendingClipboardTextToDefaultScratch_when_scratchListIsEmpty() {
		mrScratchManager = createWith(defaultConfig);

		mrScratchManager.clipboardListenerWantsToAddTextToScratch("clipboard text");

		verify(ide).failedToOpenDefaultScratch();
	}

	@Test public void renamingScratch_when_newNameIsUnique_and_fileRenameWasSuccessful() {
		mrScratchManager = createWith(defaultConfig.with(list(
				new ScratchInfo("scratch", "txt")
		)));
		when(fileSystem.renameFile(anyString(), anyString())).thenReturn(true);

		assertThat(mrScratchManager.canUserRename(new ScratchInfo("scratch", "txt"), "&renamedScratch.txt"), equalTo(true));
		mrScratchManager.userWantsToRename(new ScratchInfo("scratch", "txt"), "&renamedScratch.txt");

		verify(fileSystem).renameFile("scratch.txt", "renamedScratch.txt");
		verify(ide).updateConfig(defaultConfig.with(list(
				new ScratchInfo("&renamedScratch", "txt")
		)));
	}

	@Test public void renamingScratch_when_newNameIsNotUnique() {
		mrScratchManager = createWith(defaultConfig.with(list(
				new ScratchInfo("scratch", "txt"),
				new ScratchInfo("&renamedScratch", "txt")
		)));

		assertThat(mrScratchManager.canUserRename(new ScratchInfo("scratch", "txt"), "renamed&Scratch.txt"), equalTo(false));
		verifyZeroInteractions(ide, fileSystem);
	}

	@Test public void renamingScratch_toNameWithoutExtension() {
		ScratchInfo scratchInfo = new ScratchInfo("scratch", "txt");
		mrScratchManager = createWith(defaultConfig.with(list(scratchInfo)));

		assertThat(mrScratchManager.canUserRename(scratchInfo, "renamedScratch"), equalTo(true));
	}

	@Test public void renamingScratch_when_fileRenameFailed() {
		ScratchInfo scratchInfo = new ScratchInfo("scratch", "txt");
		mrScratchManager = createWith(defaultConfig.with(list(scratchInfo)));
		when(fileSystem.renameFile(anyString(), anyString())).thenReturn(false);

		assertThat(mrScratchManager.canUserRename(scratchInfo, "renamedScratch.txt"), equalTo(true));
		mrScratchManager.userWantsToRename(scratchInfo, "renamedScratch.txt");

		verify(fileSystem).renameFile("scratch.txt", "renamedScratch.txt");
		verify(ide).failedToRename(scratchInfo);
	}

	@Test public void movingScratchUpInScratchesList() {
		mrScratchManager = createWith(defaultConfig.with(list(
				new ScratchInfo("scratch1", "txt"),
				new ScratchInfo("scratch2", "txt"),
				new ScratchInfo("scratch3", "txt")
		)));

		int shiftUp = -1;
		mrScratchManager.userMovedScratch(new ScratchInfo("scratch2", "txt"), shiftUp);

		verify(ide).updateConfig(defaultConfig.with(list(
				new ScratchInfo("scratch2", "txt"),
				new ScratchInfo("scratch1", "txt"),
				new ScratchInfo("scratch3", "txt")
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

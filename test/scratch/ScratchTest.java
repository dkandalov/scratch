package scratch;

import org.junit.Test;
import scratch.filesystem.FileSystem;
import scratch.ide.Ide;

import java.util.List;

import static com.intellij.util.containers.ContainerUtil.list;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;


/**
 * User: dima
 * Date: 09/02/2013
 */
public class ScratchTest {

	private final Ide ide = mock(Ide.class);
	private final FileSystem fileSystem = mock(FileSystem.class);
	private Scratch scratch;

	@Test public void shouldNotifyIde_when_successfullyMigratedScratchesToFiles() {
		scratch = createScratchWith(defaultConfig());
		when(fileSystem.createFile(anyString(), anyString())).thenReturn(true);

		List<String> scratches = list("text1", "text2", "text3", "text4", "text5");
		scratch.migrate(scratches);

		verify(fileSystem).createFile("scratch.txt", "text1");
		verify(fileSystem).createFile("scratch2.txt", "text2");
		verify(fileSystem).createFile("scratch3.txt", "text3");
		verify(fileSystem).createFile("scratch4.txt", "text4");
		verify(fileSystem).createFile("scratch5.txt", "text5");
		verify(ide).migratedScratchesToFiles();
	}

	@Test public void shouldSendNewConfigToIde_when_successfullyMigratedScratchesToFiles() {
		scratch = createScratchWith(defaultConfig());
		when(fileSystem.createFile(anyString(), anyString())).thenReturn(true);

		List<String> scratches = list("text1", "text2", "text3", "text4", "text5");
		scratch.migrate(scratches);

		verify(ide).updateConfig(eq(defaultConfig()
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
		scratch = createScratchWith(defaultConfig());
		when(fileSystem.createFile(anyString(), anyString())).thenReturn(true);
		when(fileSystem.createFile(eq("scratch3.txt"), anyString())).thenReturn(false);
		when(fileSystem.createFile(eq("scratch4.txt"), anyString())).thenReturn(false);

		List<String> scratches = list("text1", "text2", "text3", "text4", "text5");
		scratch.migrate(scratches);

		verify(fileSystem).createFile("scratch.txt", "text1");
		verify(fileSystem).createFile("scratch2.txt", "text2");
		verify(fileSystem).createFile("scratch3.txt", "text3");
		verify(fileSystem).createFile("scratch4.txt", "text4");
		verify(fileSystem).createFile("scratch5.txt", "text5");
		verify(ide).failedToMigrateScratchesToFiles(list(3, 4));
	}

	@Test public void shouldSendNewConfigToIde_when_failedToMigrateSomeOfTheFiles() {
		scratch = createScratchWith(defaultConfig());
		when(fileSystem.createFile(anyString(), anyString())).thenReturn(true);
		when(fileSystem.createFile(eq("scratch3.txt"), anyString())).thenReturn(false);
		when(fileSystem.createFile(eq("scratch4.txt"), anyString())).thenReturn(false);

		List<String> scratches = list("text1", "text2", "text3", "text4", "text5");
		scratch.migrate(scratches);

		verify(ide).updateConfig(eq(defaultConfig()
				.with(list(
						new ScratchInfo("&scratch", "txt"),
						new ScratchInfo("scratch&2", "txt"),
						new ScratchInfo("scratch&5", "txt")
				))
				.needsMigration(false)
		));
	}

	@Test public void displayingScratchesList_when_configAndFiles_Match() {
		scratch = createScratchWith(defaultConfig().with(list(
				new ScratchInfo("scratch", "txt"),
				new ScratchInfo("scratch2", "java"),
				new ScratchInfo("scratch3", "html")
		)).needsMigration(false));
		when(fileSystem.listOfScratchFiles()).thenReturn(list("scratch.txt", "scratch2.java", "scratch3.html"));

		scratch.userWantsToSeeScratchesList();

		verify(fileSystem).listOfScratchFiles();
		verify(ide).displayScratchesListPopup(eq(list(
				new ScratchInfo("scratch", "txt"),
				new ScratchInfo("scratch2", "java"),
				new ScratchInfo("scratch3", "html")
		)));
		verifyNoMoreInteractions(fileSystem, ide);
	}

	@Test public void openingScratch_when_scratchFileExists() {
		ScratchInfo scratchInfo = new ScratchInfo("scratch", "txt");
		scratch = createScratchWith(defaultConfig().with(list(scratchInfo)));
		when(fileSystem.fileExists("scratch.txt")).thenReturn(true);

		scratch.userWantsToOpenScratch(scratchInfo);

		verify(fileSystem).fileExists(eq("scratch.txt"));
		verify(ide).openScratch(eq(scratchInfo));
	}

	@Test public void openingScratch_when_scratchFileDoesNotExist() {
		ScratchInfo scratchInfo = new ScratchInfo("scratch", "txt");
		scratch = createScratchWith(defaultConfig().with(list(scratchInfo)));
		when(fileSystem.fileExists("scratch.txt")).thenReturn(false);

		scratch.userWantsToOpenScratch(scratchInfo);

		verify(fileSystem).fileExists(eq("scratch.txt"));
		verify(ide).failedToOpen(eq(scratchInfo));
	}

	@Test public void openingDefaultScratch_when_scratchesListIsNotEmpty_and_scratchFileExists() {
		ScratchInfo scratchInfo = new ScratchInfo("scratch", "txt");
		scratch = createScratchWith(defaultConfig().with(list(scratchInfo)));
		when(fileSystem.fileExists("scratch.txt")).thenReturn(true);

		scratch.userWantsToOpenDefaultScratch();

		verify(ide).openScratch(scratchInfo);
	}

	@Test public void openingDefaultScratch_when_scratchFileDoesNotExist() {
		scratch = createScratchWith(defaultConfig().with(list(new ScratchInfo("scratch", "txt"))));
		when(fileSystem.fileExists("scratch.txt")).thenReturn(false);

		scratch.userWantsToOpenDefaultScratch();
		verify(fileSystem).fileExists(eq("scratch.txt"));

		verify(ide).failedToOpenDefaultScratch();
	}

	@Test public void openingDefaultScratch_when_scratchesListIsEmpty() {
		scratch = createScratchWith(defaultConfig());

		scratch.userWantsToOpenDefaultScratch();

		verify(ide).failedToOpenDefaultScratch();
	}

	@Test public void renamingScratch_when_newNameIsUnique_and_fileRenameWasSuccessful() {
		scratch = createScratchWith(defaultConfig().with(list(
				new ScratchInfo("scratch", "txt")
		)));
		when(fileSystem.renameFile(anyString(), anyString())).thenReturn(true);

		assertThat(scratch.canUserRename(new ScratchInfo("scratch", "txt"), "&renamedScratch.txt"), equalTo(true));
		scratch.userWantsToRename(new ScratchInfo("scratch", "txt"), "&renamedScratch.txt");

		verify(fileSystem).renameFile("scratch.txt", "renamedScratch.txt");
		verify(ide).updateConfig(defaultConfig().with(list(
				new ScratchInfo("&renamedScratch", "txt")
		)));
	}

	@Test public void renamingScratch_when_newNameIsNotUnique() {
		scratch = createScratchWith(defaultConfig().with(list(
				new ScratchInfo("scratch", "txt"),
				new ScratchInfo("&renamedScratch", "txt")
		)));

		assertThat(scratch.canUserRename(new ScratchInfo("scratch", "txt"), "renamed&Scratch.txt"), equalTo(false));
		verifyZeroInteractions(ide, fileSystem);
	}

	@Test public void renamingScratch_toNameWithoutExtension() {
		ScratchInfo scratchInfo = new ScratchInfo("scratch", "txt");
		scratch = createScratchWith(defaultConfig().with(list(scratchInfo)));

		assertThat(scratch.canUserRename(scratchInfo, "renamedScratch"), equalTo(true));
	}

	@Test public void renamingScratch_when_fileRenameFailed() {
		ScratchInfo scratchInfo = new ScratchInfo("scratch", "txt");
		scratch = createScratchWith(defaultConfig().with(list(scratchInfo)));
		when(fileSystem.renameFile(anyString(), anyString())).thenReturn(false);

		assertThat(scratch.canUserRename(scratchInfo, "renamedScratch.txt"), equalTo(true));
		scratch.userWantsToRename(scratchInfo, "renamedScratch.txt");

		verify(fileSystem).renameFile("scratch.txt", "renamedScratch.txt");
		verify(ide).failedToRename(scratchInfo);
	}

	@Test public void movingScratchUpInScratchesList() {
		scratch = createScratchWith(defaultConfig().with(list(
				new ScratchInfo("scratch1", "txt"),
				new ScratchInfo("scratch2", "txt"),
				new ScratchInfo("scratch3", "txt")
		)));

		int shiftUp = -1;
		scratch.userMovedScratch(new ScratchInfo("scratch2", "txt"), shiftUp);

		verify(ide).updateConfig(defaultConfig().with(list(
				new ScratchInfo("scratch2", "txt"),
				new ScratchInfo("scratch1", "txt"),
				new ScratchInfo("scratch3", "txt")
		)));
	}

	@Test public void shouldNewConfigToIde_when_userTurnsOnListeningToClipboard() {
		scratch = createScratchWith(defaultConfig().listenToClipboard(false));

		scratch.userWantsToListenToClipboard(true);

		verify(ide).updateConfig(defaultConfig().listenToClipboard(true));
	}

	private Scratch createScratchWith(ScratchConfig config) {
		return new Scratch(ide, fileSystem, config);
	}

	private static ScratchConfig defaultConfig() {
		return ScratchConfig.DEFAULT_CONFIG;
	}

}

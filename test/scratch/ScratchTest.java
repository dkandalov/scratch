package scratch;

import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static com.intellij.util.containers.ContainerUtil.list;
import static com.intellij.util.containers.ContainerUtil.newArrayList;
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


	private Scratch createScratchWith(ScratchConfig config) {
		return new Scratch(ide, fileSystem, config);
	}


	private static ScratchConfig defaultConfig() {
		return ScratchConfig.DEFAULT_CONFIG;
	}

	private static class ScratchConfig {
		public static final ScratchConfig DEFAULT_CONFIG = new ScratchConfig(Collections.<ScratchInfo>emptyList(), false, true);

		public final List<ScratchInfo> scratchInfos;
		public final boolean needsMigration;
		public final boolean listenToClipboard;

		private ScratchConfig(List<ScratchInfo> scratchInfos, boolean listenToClipboard, boolean needsMigration) {
			this.scratchInfos = scratchInfos;
			this.listenToClipboard = listenToClipboard;
			this.needsMigration = needsMigration;
		}

		public ScratchConfig with(List<ScratchInfo> newScratchInfos) {
			return new ScratchConfig(newScratchInfos, listenToClipboard, needsMigration);
		}

		public ScratchConfig needsMigration(boolean value) {
			return new ScratchConfig(scratchInfos, listenToClipboard, value);
		}

		public ScratchConfig replace(final ScratchInfo scratchInfo, final ScratchInfo newScratchInfo) {
			return new ScratchConfig(ContainerUtil.map(scratchInfos, new Function<ScratchInfo, ScratchInfo>() {
				@Override public ScratchInfo fun(ScratchInfo it) {
					return it.equals(scratchInfo) ? newScratchInfo : it;
				}
			}),listenToClipboard, needsMigration);
		}

		public ScratchConfig move(final ScratchInfo scratchInfo, int shift) {
			final ScratchInfo prevScratchInfo = scratchInfos.get(scratchInfos.indexOf(scratchInfo) + shift);
			return this.with(ContainerUtil.map(scratchInfos, new Function<ScratchInfo, ScratchInfo>() {
				@Override public ScratchInfo fun(ScratchInfo it) {
					if (it.equals(prevScratchInfo)) return scratchInfo;
					else if (it.equals(scratchInfo)) return prevScratchInfo;
					else return it;
				}
			}));
		}

		@Override public String toString() {
			return "ScratchConfig{" +
					"listenToClipboard=" + listenToClipboard + ", " +
					"needsMigration=" + needsMigration + ", " +
					"scratchInfos=\n" + StringUtil.join(scratchInfos, ",\n") +
					'}';
		}

		@SuppressWarnings("RedundantIfStatement")
		@Override public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			ScratchConfig that = (ScratchConfig) o;

			if (listenToClipboard != that.listenToClipboard) return false;
			if (needsMigration != that.needsMigration) return false;
			if (scratchInfos != null ? !scratchInfos.equals(that.scratchInfos) : that.scratchInfos != null)
				return false;

			return true;
		}

		@Override public int hashCode() {
			int result = scratchInfos != null ? scratchInfos.hashCode() : 0;
			result = 31 * result + (needsMigration ? 1 : 0);
			result = 31 * result + (listenToClipboard ? 1 : 0);
			return result;
		}
	}

	private static class Scratch {
		private final Ide ide;
		private final FileSystem fileSystem;
		private ScratchConfig config;

		public Scratch(Ide ide, FileSystem fileSystem, ScratchConfig config) {
			this.ide = ide;
			this.fileSystem = fileSystem;
			this.config = config;
		}

		public void migrate(List<String> scratches) {
			List<Integer> indexes = newArrayList();
			List<ScratchInfo> scratchesInfo = newArrayList();

			for (int i = 1; i <= scratches.size(); i++) {
				String scratchName = (i == 1 ? "&scratch" : "scratch&" + i);
				ScratchInfo scratchInfo = new ScratchInfo(scratchName, "txt");

				boolean wasCreated = fileSystem.createFile(scratchInfo.asFileName(), scratches.get(i - 1));
				if (wasCreated) {
					scratchesInfo.add(scratchInfo);
				} else {
					indexes.add(i);
				}
			}

			if (indexes.isEmpty()) {
				ide.migratedScratchesToFiles();
			} else {
				ide.failedToMigrateScratchesToFiles(indexes);
			}
			config = config.with(scratchesInfo).needsMigration(false);
			ide.updateConfig(config);
		}

		public void userWantsToSeeScratchesList() {
			List<String> fileNames = fileSystem.listOfScratchFiles();

			List<ScratchInfo> scratchesInfo = newArrayList();
			for (String fileName : fileNames) {
				scratchesInfo.add(ScratchInfo.createFrom(fileName));
			}
			ide.displayScratchesListPopup(scratchesInfo);
		}

		public void userWantsToOpenScratch(ScratchInfo scratchInfo) {
			if (fileSystem.fileExists(scratchInfo.asFileName()))
				ide.openScratch(scratchInfo);
			else
				ide.failedToOpen(scratchInfo);
		}

		public void userWantsToOpenDefaultScratch() {
			if (config.scratchInfos.isEmpty()) {
				ide.failedToOpenDefaultScratch();
			} else {
				ScratchInfo scratchInfo = config.scratchInfos.get(0);
				if (fileSystem.fileExists(scratchInfo.asFileName())) {
					ide.openScratch(scratchInfo);
				} else {
					ide.failedToOpenDefaultScratch();
				}
			}
		}

		public boolean canUserRename(final ScratchInfo scratchInfo, String fullNameWithMnemonics) {
			final ScratchInfo renamedScratchInfo = ScratchInfo.createFrom(fullNameWithMnemonics);

			return !ContainerUtil.exists(config.scratchInfos, new Condition<ScratchInfo>() {
				@Override public boolean value(ScratchInfo it) {
					return !it.equals(scratchInfo) && it.name.equals(renamedScratchInfo.name);
				}
			});
		}

		public void userWantsToRename(ScratchInfo scratchInfo, String fullNameWithMnemonics) {
			ScratchInfo renamedScratchInfo = ScratchInfo.createFrom(fullNameWithMnemonics);

			boolean wasRenamed = fileSystem.renameFile(scratchInfo.asFileName(), renamedScratchInfo.asFileName());
			if (wasRenamed) {
				config = config.replace(scratchInfo, renamedScratchInfo);
				ide.updateConfig(config);
			} else {
				ide.failedToRename(scratchInfo);
			}
		}

		public void userMovedScratch(final ScratchInfo scratchInfo, int shift) {
			config = config.move(scratchInfo, shift);
			ide.updateConfig(config);
		}
	}

	private static class FileSystem {
		public boolean createFile(String fileName, String text) {
			// TODO implement
			return false;
		}

		public List<String> listOfScratchFiles() {
			// TODO implement
			return null;
		}

		public boolean fileExists(String s) {
			// TODO implement
			return false;
		}

		public boolean renameFile(String oldFileName, String newFileName) {
			// TODO implement
			return false;
		}
	}

	private static class Ide {
		public void migratedScratchesToFiles() {
			// TODO implement

		}

		public void failedToMigrateScratchesToFiles(List<Integer> integers) {
			// TODO implement

		}

		public void displayScratchesListPopup(List<ScratchInfo> scratchInfos) {
			// TODO implement

		}

		public void updateConfig(ScratchConfig config) {
			// TODO implement

		}

		public void openScratch(ScratchInfo scratchInfo) {
			// TODO implement

		}

		public void failedToOpen(ScratchInfo scratchInfo) {
			// TODO implement

		}

		public void failedToOpenDefaultScratch() {
			// TODO implement

		}

		public void failedToRename(ScratchInfo scratchInfo) {
			// TODO implement

		}
	}

	private static class ScratchInfo {
		public final String nameWithMnemonics;
		public final String name;
		public final String extension;

		public static ScratchInfo createFrom(String fullNameWithMnemonics) {
			return new ScratchInfo(
					extractNameFrom(fullNameWithMnemonics),
					extractExtensionFrom(fullNameWithMnemonics)
			);
		}

		private ScratchInfo(String nameWithMnemonics, String extension) {
			this.nameWithMnemonics = nameWithMnemonics;
			this.name = nameWithMnemonics.replace("&", "");
			this.extension = extension;
		}

		public String asFileName() {
			return name + "." + extension;
		}

		@Override public String toString() {
			return "{nameWithMnemonics='" + nameWithMnemonics + "'" + ", extension='" + extension + "'}";
		}

		@SuppressWarnings("RedundantIfStatement")
		@Override public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			ScratchInfo that = (ScratchInfo) o;

			if (extension != null ? !extension.equals(that.extension) : that.extension != null) return false;
			if (nameWithMnemonics != null ? !nameWithMnemonics.equals(that.nameWithMnemonics) : that.nameWithMnemonics != null) return false;

			return true;
		}

		@Override public int hashCode() {
			int result = nameWithMnemonics != null ? nameWithMnemonics.hashCode() : 0;
			result = 31 * result + (extension != null ? extension.hashCode() : 0);
			return result;
		}

		private static String extractExtensionFrom(String fileName) {
			int index = fileName.lastIndexOf(".");
			return index == -1 ? "" : fileName.substring(index + 1);
		}

		private static String extractNameFrom(String fileName) {
			int index = fileName.lastIndexOf(".");
			if (index == -1) index = fileName.length();
			return fileName.substring(0, index);
		}
	}
}

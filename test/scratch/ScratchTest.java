package scratch;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
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
		scratch = createScratchWith(ScratchConfig.DEFAULT_CONFIG);
		when(fileSystem.createFile(anyString(), anyString())).thenReturn(true);

		List<String> scratches = asList("text1", "text2", "text3", "text4", "text5");
		scratch.migrate(scratches);

		verify(fileSystem).createFile("scratch1.txt", "text1");
		verify(fileSystem).createFile("scratch2.txt", "text2");
		verify(fileSystem).createFile("scratch3.txt", "text3");
		verify(fileSystem).createFile("scratch4.txt", "text4");
		verify(fileSystem).createFile("scratch5.txt", "text5");
		verify(ide).migratedScratchesToFiles();
	}

	@Test public void shouldSendNewConfigToIde_when_successfullyMigratedScratchesToFiles() {
		scratch = createScratchWith(ScratchConfig.DEFAULT_CONFIG);
		when(fileSystem.createFile(anyString(), anyString())).thenReturn(true);

		List<String> scratches = asList("text1", "text2", "text3", "text4", "text5");
		scratch.migrate(scratches);

		verify(ide).updateConfig(eq(ScratchConfig.DEFAULT_CONFIG
				.withScratches(asList(
						new ScratchInfo("scratch1", "txt"),
						new ScratchInfo("scratch2", "txt"),
						new ScratchInfo("scratch3", "txt"),
						new ScratchInfo("scratch4", "txt"),
						new ScratchInfo("scratch5", "txt")))
				.needsMigration(false)
		));
	}

	@Test public void shouldNotifyIde_when_notAbleToMigrateSomeOfTheFiles() {
		scratch = createScratchWith(ScratchConfig.DEFAULT_CONFIG);
		when(fileSystem.createFile(anyString(), anyString())).thenReturn(true);
		when(fileSystem.createFile(eq("scratch3.txt"), anyString())).thenReturn(false);
		when(fileSystem.createFile(eq("scratch4.txt"), anyString())).thenReturn(false);
		List<String> scratches = asList("text1", "text2", "text3", "text4", "text5");

		scratch.migrate(scratches);

		verify(fileSystem).createFile("scratch1.txt", "text1");
		verify(fileSystem).createFile("scratch2.txt", "text2");
		verify(fileSystem).createFile("scratch3.txt", "text3");
		verify(fileSystem).createFile("scratch4.txt", "text4");
		verify(fileSystem).createFile("scratch5.txt", "text5");
		verify(ide).failedToMigrateScratchesToFiles(asList(3, 4));
	}

	@Test public void displayingScratchesList_WhenConfigAndFiles_Match() {
		// TODO non-empty config
		scratch = createScratchWith(ScratchConfig.DEFAULT_CONFIG);
		when(fileSystem.listOfScratchFiles()).thenReturn(asList("scratch1.txt", "scratch2.java", "scratch3.html"));

		scratch.userWantsToSeeScratchesList();

		verify(fileSystem).listOfScratchFiles();
		verify(ide).displayScratchesListPopup(asList(
				new ScratchInfo("scratch1", "txt"),
				new ScratchInfo("scratch2", "java"),
				new ScratchInfo("scratch3", "html")
		));
		verifyNoMoreInteractions(fileSystem, ide);
	}

	private Scratch createScratchWith(ScratchConfig config) {
		return new Scratch(ide, fileSystem, config);
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

		public ScratchConfig withScratches(List<ScratchInfo> newScratchInfos) {
			return new ScratchConfig(newScratchInfos, listenToClipboard, needsMigration);
		}

		public ScratchConfig needsMigration(boolean value) {
			return new ScratchConfig(scratchInfos, listenToClipboard, value);
		}

		@Override public String toString() {
			return "ScratchConfig{" +
					"listenToClipboard=" + listenToClipboard +
					", scratchInfos=" + scratchInfos +
					", needsMigration=" + needsMigration +
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
			List<Integer> indexes = new ArrayList<Integer>();
			List<ScratchInfo> scratchesInfo = new ArrayList<ScratchInfo>();

			for (int i = 1; i <= scratches.size(); i++) {
				boolean wasCreated = fileSystem.createFile("scratch" + i + ".txt", scratches.get(i - 1));
				if (wasCreated) {
					scratchesInfo.add(new ScratchInfo("scratch" + i, "txt"));
				} else {
					indexes.add(i);
				}
			}
			config = config.withScratches(scratchesInfo);

			if (indexes.isEmpty()) {
				config = config.needsMigration(false);
				ide.migratedScratchesToFiles();
			} else {
				ide.failedToMigrateScratchesToFiles(indexes);
			}
			ide.updateConfig(config);
		}

		public void userWantsToSeeScratchesList() {
			// TODO implement

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
	}

	private static class ScratchInfo {
		public final String name;
		public final String extension;

		private ScratchInfo(String name, String extension) {
			this.name = name;
			this.extension = extension;
		}

		@Override public String toString() {
			return "ScratchInfo{" +
					"extension='" + extension + '\'' +
					", name='" + name + '\'' +
					'}';
		}

		@SuppressWarnings("RedundantIfStatement")
		@Override public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			ScratchInfo that = (ScratchInfo) o;

			if (extension != null ? !extension.equals(that.extension) : that.extension != null) return false;
			if (name != null ? !name.equals(that.name) : that.name != null) return false;

			return true;
		}

		@Override public int hashCode() {
			int result = name != null ? name.hashCode() : 0;
			result = 31 * result + (extension != null ? extension.hashCode() : 0);
			return result;
		}
	}
}

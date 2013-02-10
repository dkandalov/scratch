package scratch;

import org.junit.Test;

import java.util.ArrayList;
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
	private final Scratch scratch = new Scratch(ide, fileSystem);

	@Test public void successfulMigrationOfScratchesToPhysicalFiles() {
		when(fileSystem.createFile(anyString(), anyString())).thenReturn(true);
		List<String> scratches = asList("text1", "text2", "text3", "text4", "text5");

		scratch.migrate(scratches);

		verify(fileSystem).createFile("scratch1.txt", "text1");
		verify(fileSystem).createFile("scratch2.txt", "text2");
		verify(fileSystem).createFile("scratch3.txt", "text3");
		verify(fileSystem).createFile("scratch4.txt", "text4");
		verify(fileSystem).createFile("scratch5.txt", "text5");
		verify(ide).migratedScratchesToFiles();
		verifyNoMoreInteractions(fileSystem, ide);
	}

	@Test public void failedMigrationOfScratchesToPhysicalFiles() {
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
		verifyNoMoreInteractions(fileSystem, ide);
	}

	private class Scratch {
		private final Ide ide;
		private final FileSystem fileSystem;

		public Scratch(Ide ide, FileSystem fileSystem) {
			this.ide = ide;
			this.fileSystem = fileSystem;
		}

		public void migrate(List<String> scratches) {
			List<Integer> indexes = new ArrayList<Integer>();
			for (int i = 1; i <= scratches.size(); i++) {
				boolean wasCreated = fileSystem.createFile("scratch" + i + ".txt", scratches.get(i - 1));
				if (!wasCreated) indexes.add(i);
			}

			if (indexes.isEmpty()) ide.migratedScratchesToFiles();
			else ide.failedToMigrateScratchesToFiles(indexes);
		}
	}

	private class FileSystem {
		public boolean createFile(String fileName, String text) {
			// TODO implement
			return false;
		}
	}

	private class Ide {
		public void migratedScratchesToFiles() {
			// TODO implement

		}

		public void failedToMigrateScratchesToFiles(List<Integer> integers) {
			// TODO implement

		}
	}
}

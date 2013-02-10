package scratch.ide;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.text.StringUtil;
import scratch.ScratchConfig;
import scratch.ScratchInfo;

import java.util.List;

/**
* User: dima
* Date: 10/02/2013
*/
public class Ide {
	private static final Logger LOG = Logger.getInstance(Ide.class);

	public void migratedScratchesToFiles() {
		LOG.info("Migrated scratches to physical files");
	}

	public void failedToMigrateScratchesToFiles(List<Integer> scratchIndexes) {
		LOG.error("Failed to migrated scratches to physical files. " +
				"Failed scratches: " + StringUtil.join(scratchIndexes, ", "));
	}

	public void updateConfig(ScratchConfig config) {
		ScratchConfigPersistence.getInstance().updateFrom(config);
	}

	public void displayScratchesListPopup(List<ScratchInfo> scratchInfos) {
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

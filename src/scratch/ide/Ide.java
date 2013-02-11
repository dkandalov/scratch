package scratch.ide;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.ListPopupStep;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.UserDataHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import scratch.ScratchConfig;
import scratch.ScratchInfo;
import scratch.filesystem.FileSystem;
import scratch.ide.popup.ScratchListPopup;
import scratch.ide.popup.ScratchListPopupStep;

import java.awt.*;
import java.util.List;

import static com.intellij.notification.NotificationType.WARNING;
import static scratch.ide.Util.*;

/**
 * User: dima
 * Date: 10/02/2013
 */
public class Ide {
	private static final Logger LOG = Logger.getInstance(Ide.class);

	private final FileSystem fileSystem;


	public Ide(FileSystem fileSystem) {
		this.fileSystem = fileSystem;
	}

	public void updateConfig(ScratchConfig config) {
		ScratchConfigPersistence.getInstance().updateFrom(config);
	}

	public void displayScratchesListPopup(List<ScratchInfo> scratchInfos, UserDataHolder userDataHolder) {
		Project project = takeProjectFrom(userDataHolder);
		Ref<Component> componentRef = Ref.create();

		ListPopupStep popupStep = new ScratchListPopupStep(scratchInfos, project, componentRef);
		ScratchListPopup popup = new ScratchListPopup(popupStep);
		componentRef.set(popup.getComponent()); // this kind of a hack was copied from com.intellij.tasks.actions.SwitchTaskAction#createPopup
		popup.showCenteredInCurrentWindow(project);
	}

	public void openScratch(ScratchInfo scratchInfo, UserDataHolder userDataHolder) {
		Project project = takeProjectFrom(userDataHolder);

		VirtualFile file = fileSystem.findVirtualFileFor(scratchInfo);
		if (file != null) {
			new OpenFileDescriptor(project, file).navigate(true);
		} else {
			failedToFindVirtualFileFor(scratchInfo);
		}
	}

	public void failedToRename(ScratchInfo scratchInfo) {
		// TODO implement

	}

	public void migratedScratchesToFiles() {
		LOG.info("Migrated scratches to physical files");
	}

	public void failedToMigrateScratchesToFiles(List<Integer> scratchIndexes) {
		String title = "Failed to migrated scratches to physical files. ";
		String message = "Failed scratches: " + StringUtil.join(scratchIndexes, ", ");
		notifyUser(title, message, WARNING);
	}

	public void failedToOpenDefaultScratch() {
		notifyUser("", "Failed to open default scratch", WARNING);
	}

	public void failedToOpen(ScratchInfo scratchInfo) {
		notifyUser("", "Failed to open scratch: '" + scratchInfo.name + "'", WARNING);
	}

	private static void failedToFindVirtualFileFor(ScratchInfo scratchInfo) {
		LOG.warn("Failed to find virtual file for '" + scratchInfo.asFileName() + "'");
	}

}

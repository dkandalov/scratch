package scratch.ide;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.UserDataHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import scratch.ScratchConfig;
import scratch.ScratchInfo;
import scratch.filesystem.FileSystem;

import javax.swing.*;
import java.util.List;

import static com.intellij.notification.NotificationType.WARNING;
import static com.intellij.openapi.ui.popup.JBPopupFactory.ActionSelectionAid.NUMBERING;
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
		// TODO use this
//		ListPopupStep popupStep = new BaseListPopupStep<String>("strings", asList("aaa", "bbb", "ccc"));
//		ScratchListPopup popup = new ScratchListPopup(popupStep);
//		popup.showCenteredInCurrentWindow(project);

		AnActionEvent event = eventFrom(userDataHolder);
		Project project = event.getProject();
		DefaultActionGroup actionGroup = createActionGroup(scratchInfos);

		JBPopupFactory factory = JBPopupFactory.getInstance();
		ListPopup listPopup = factory.createActionGroupPopup("List of Scratches", actionGroup, event.getDataContext(), NUMBERING, true);
		listPopup.showCenteredInCurrentWindow(project);
	}

	public void openScratch(ScratchInfo scratchInfo, UserDataHolder userDataHolder) {
		AnActionEvent event = eventFrom(userDataHolder);
		Project project = event.getProject();

		VirtualFile file = fileSystem.findVirtualFileFor(scratchInfo);
		if (file != null) {
			new OpenFileDescriptor(project, file).navigate(true);
		} else {
			failedToFindVirtualFileFor(scratchInfo);
		}
	}

	public void failedToOpenDefaultScratch() {
		// TODO implement

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

	public void failedToOpen(ScratchInfo scratchInfo) {
		notifyUser("", "Failed to open scratch: '" + scratchInfo.name + "'", WARNING);
	}

	private static DefaultActionGroup createActionGroup(List<ScratchInfo> scratchInfos) {
		DefaultActionGroup actionGroup = new DefaultActionGroup();
		for (final ScratchInfo scratchInfo : scratchInfos) {
			String name = scratchInfo.fullNameWithMnemonics();
			actionGroup.add(new AnAction(name, "Open " + scratchInfo.name, getIcon(name)) {
				@Override public void actionPerformed(AnActionEvent event) {
					ScratchComponent.instance().userWantsToOpenScratch(scratchInfo, holdingTo(event));
				}
			});
		}
		return actionGroup;
	}

	private static Icon getIcon(String scratchName) {
		String[] split = scratchName.split("\\.");
		if (split.length > 1) {
			String fileType = split[split.length - 1];
			return IconLoader.findIcon("/fileTypes/" + fileType + ".png"); // TODO use FileType class
		} else {
			return AllIcons.FileTypes.Text;
		}
	}

	private static void failedToFindVirtualFileFor(ScratchInfo scratchInfo) {
		LOG.warn("Failed to find virtual file for '" + scratchInfo.asFileName() + "'");
	}
}

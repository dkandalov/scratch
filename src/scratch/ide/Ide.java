package scratch.ide;

import com.intellij.icons.AllIcons;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopupStep;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.UserDataHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import scratch.ScratchConfig;
import scratch.ScratchInfo;
import scratch.filesystem.FileSystem;
import scratch.ide.popup.ScratchListPopup;

import javax.swing.*;
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
		final Project project = takeProjectFrom(userDataHolder);
		final Ref<Component> componentRef = Ref.create();

		ListPopupStep popupStep = new BaseListPopupStep<ScratchInfo>("List of Scratches", scratchInfos) {
			private final FileTypeManager fileTypeManager = FileTypeManager.getInstance();

			@Override public PopupStep onChosen(ScratchInfo scratchInfo, boolean finalChoice) {
				if (finalChoice) {
					ScratchComponent.instance().userWantsToOpenScratch(scratchInfo, holdingOnTo(project));
					return FINAL_CHOICE;
				}
				return createActionsPopupFor(scratchInfo);
			}

			private PopupStep createActionsPopupFor(final ScratchInfo scratchInfo) {
				AnAction openAction = new DumbAwareAction("Open") {
					@Override public void actionPerformed(AnActionEvent event) {
						ScratchComponent.instance().userWantsToOpenScratch(scratchInfo, holdingOnTo(project));
					}
				};
				AnAction renameAction = new DumbAwareAction("Rename") {
					@Override public void actionPerformed(AnActionEvent event) {
						// TODO
					}
				};
				AnAction deleteAction = new DumbAwareAction("Delete") {
					@Override public void actionPerformed(AnActionEvent event) {
						// TODO
					}
				};
				ActionGroup actionGroup = new DefaultActionGroup(openAction, renameAction, deleteAction);
				return JBPopupFactory.getInstance().createActionsStep(
						actionGroup,
						DataManager.getInstance().getDataContext(componentRef.get()),
						false, true, "", componentRef.get(), false
				);
			}

			@NotNull @Override public String getTextFor(ScratchInfo scratchInfo) {
				return scratchInfo.nameWithMnemonics;
			}

			@Override public Icon getIconFor(ScratchInfo scratchInfo) {
				FileType fileType = fileTypeManager.getFileTypeByExtension(scratchInfo.extension);
				return fileType.getIcon();
			}

			@Override public boolean hasSubstep(ScratchInfo selectedValue) {
				return true;
			}
		};
		ScratchListPopup popup = new ScratchListPopup(popupStep);
		componentRef.set(popup.getComponent());
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

	private static DefaultActionGroup createActionGroup(List<ScratchInfo> scratchInfos) {
		DefaultActionGroup actionGroup = new DefaultActionGroup();
		for (final ScratchInfo scratchInfo : scratchInfos) {
			String name = scratchInfo.fullNameWithMnemonics();
			actionGroup.add(new AnAction(name, "Open " + scratchInfo.name, getIcon(name)) {
				@Override public void actionPerformed(AnActionEvent event) {
					ScratchComponent.instance().userWantsToOpenScratch(scratchInfo, holdingOnTo(event.getProject()));
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

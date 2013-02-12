package scratch.ide;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.ListPopupStep;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.UserDataHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.openapi.wm.IdeFrame;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nullable;
import scratch.MrScratchManager;
import scratch.ScratchConfig;
import scratch.ScratchInfo;
import scratch.filesystem.FileSystem;
import scratch.ide.popup.ScratchListPopup;
import scratch.ide.popup.ScratchListPopupStep;

import java.awt.*;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.List;

import static com.intellij.notification.NotificationType.WARNING;
import static java.awt.datatransfer.DataFlavor.stringFlavor;
import static scratch.ScratchConfig.AppendType;
import static scratch.ScratchConfig.AppendType.APPEND;
import static scratch.ScratchConfig.AppendType.PREPEND;
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

	public void addTextTo(ScratchInfo scratchInfo, final String clipboardText, final AppendType appendType) {
		VirtualFile virtualFile = fileSystem.findVirtualFileFor(scratchInfo);
		if (virtualFile == null) {
			failedToFindVirtualFileFor(scratchInfo);
			return;
		}
		final Document document = FileDocumentManager.getInstance().getDocument(virtualFile);
		if (document == null) return;
		if (hasFocusInEditor(document)) return;

		ApplicationManager.getApplication().runWriteAction(new Runnable() {
			@Override public void run() {
				String text = document.getText();
				String newText;
				if (appendType == APPEND) {
					if (text.endsWith("\n")) {
						newText = text + clipboardText;
					} else {
						newText = text + "\n" + clipboardText;
					}
				} else if (appendType == PREPEND) {
					newText = clipboardText + "\n" + text;
				} else {
					throw new IllegalStateException();
				}

				document.setText(newText);
				FileDocumentManager.getInstance().saveDocument(document);
			}
		});
	}

	private static boolean hasFocusInEditor(Document document) {
		Editor selectedTextEditor = getSelectedEditor();
		return selectedTextEditor != null && selectedTextEditor.getDocument().equals(document);
	}

	private static Editor getSelectedEditor() {
		IdeFrame frame = IdeFocusManager.findInstance().getLastFocusedFrame();
		if (frame == null) return null;

		FileEditorManager instance = FileEditorManager.getInstance(frame.getProject());
		return instance.getSelectedTextEditor();
	}


	// TODO what if this is ON and clipboard content is appended to scratch forever?
	public static class ClipboardListener {
		private static final Logger LOG = Logger.getInstance(ClipboardListener.class);

		private final MrScratchManager mrScratchManager;

		public ClipboardListener(MrScratchManager mrScratchManager) {
			this.mrScratchManager = mrScratchManager;
		}

		public void startListening() {
			CopyPasteManager.getInstance().addContentChangedListener(new CopyPasteManager.ContentChangedListener() {
				@Override
				public void contentChanged(@Nullable Transferable oldTransferable, Transferable newTransferable) {
					if (!mrScratchManager.shouldListenToClipboard()) return;

					try {
						String oldClipboard = null;
						if (oldTransferable != null) {
							Object transferData = oldTransferable.getTransferData(stringFlavor);
							oldClipboard = (transferData == null ? null : transferData.toString());
						}
						String clipboard = null;
						if (newTransferable != null) {
							Object transferData = newTransferable.getTransferData(stringFlavor);
							clipboard = (transferData == null ? null : transferData.toString());
						}
						if (clipboard == null || StringUtils.equals(oldClipboard, clipboard)) return;

						mrScratchManager.clipboardListenerWantsToAddTextToScratch(clipboard);

					} catch (UnsupportedFlavorException e) {
						LOG.info(e);
					} catch (IOException e) {
						LOG.info(e);
					}
				}
			});
		}
	}

}

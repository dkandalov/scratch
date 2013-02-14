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
import com.intellij.openapi.ui.InputValidatorEx;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.popup.ListPopupStep;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.UserDataHolder;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.openapi.wm.IdeFrame;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nullable;
import scratch.Answer;
import scratch.MrScratchManager;
import scratch.Scratch;
import scratch.ScratchConfig;
import scratch.filesystem.FileSystem;
import scratch.ide.popup.ScratchListPopup;
import scratch.ide.popup.ScratchListPopupStep;

import java.awt.*;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.List;

import static java.awt.datatransfer.DataFlavor.stringFlavor;
import static scratch.ScratchConfig.AppendType.APPEND;
import static scratch.ScratchConfig.AppendType.PREPEND;
import static scratch.ide.Util.*;

/**
 * User: dima
 * Date: 10/02/2013
 */
public class Ide {
	private final FileSystem fileSystem;
	private final ScratchLog log;


	public Ide(FileSystem fileSystem, ScratchLog log) {
		this.fileSystem = fileSystem;
		this.log = log;
	}

	public void persistConfig(ScratchConfig config) {
		ScratchConfigPersistence.getInstance().updateFrom(config);
	}

	public void displayScratchesListPopup(List<Scratch> scratches, UserDataHolder userDataHolder) {
		Project project = takeProjectFrom(userDataHolder);
		Ref<Component> componentRef = Ref.create();

		ListPopupStep popupStep = new ScratchListPopupStep(scratches, project, componentRef);
		ScratchListPopup popup = new ScratchListPopup(popupStep);
		componentRef.set(popup.getComponent()); // this "hack" was copied from com.intellij.tasks.actions.SwitchTaskAction#createPopup
		popup.showCenteredInCurrentWindow(project);
	}

	public void openScratch(Scratch scratch, UserDataHolder userDataHolder) {
		Project project = takeProjectFrom(userDataHolder);

		VirtualFile file = fileSystem.virtualFileFor(scratch.asFileName());
		if (file != null) {
			new OpenFileDescriptor(project, file).navigate(true);
		} else {
			log.failedToFindVirtualFileFor(scratch);
		}
	}

	public void openNewScratchDialog(String suggestedScratchName) {
		String message = "Scratch name (you can use '&' for mnemonics):";
		String scratchName = Messages.showInputDialog(message, "New Scratch", NO_ICON, suggestedScratchName, new InputValidatorEx() {
			@Override public boolean checkInput(String scratchName) {
				return ScratchComponent.mrScratchManager().checkIfUserCanCreateScratchWithName(scratchName).isYes;
			}

			@Nullable @Override public String getErrorText(String scratchName) {
				Answer answer = ScratchComponent.mrScratchManager().checkIfUserCanCreateScratchWithName(scratchName);
				return answer.explanation;
			}

			@Override public boolean canClose(String inputString) {
				return true;
			}
		});
		if (scratchName == null) return;

		ScratchComponent.mrScratchManager().userWantsToAddNewScratch(scratchName);
	}

	public void addTextTo(Scratch scratch, final String clipboardText, final ScratchConfig.AppendType appendType) {
		VirtualFile virtualFile = fileSystem.virtualFileFor(scratch.asFileName());
		if (virtualFile == null) {
			log.failedToFindVirtualFileFor(scratch);
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

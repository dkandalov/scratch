package ru.scratch;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.openapi.wm.IdeFrame;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

public class ScratchListenClipboardAction extends ToggleAction implements CopyPasteManager.ContentChangedListener {
	private static final Logger LOG = Logger.getInstance(ScratchListenClipboardAction.class.getName());

	private static final Icon IS_ON_ICON = AllIcons.Actions.Menu_paste;
	private static final Icon IS_OFF_ICON = IconLoader.getDisabledIcon(AllIcons.Actions.Menu_paste);

	public ScratchListenClipboardAction() {
		CopyPasteManager.getInstance().addContentChangedListener(this);
	}

	@Override
	public void setSelected(AnActionEvent event, boolean enabled) {
		ScratchData.getInstance().setAppendContentFromClipboard(enabled);
		updateIcon(event.getPresentation(), enabled);
	}

	@Override
	public boolean isSelected(AnActionEvent event) {
		return ScratchData.getInstance().isAppendContentFromClipboard();
	}

	@Override
	public void contentChanged(@Nullable Transferable oldTransferable, Transferable newTransferable) {
		if (!ScratchData.getInstance().isAppendContentFromClipboard()) return;

		try {
			String oldClipboard = null;
			if (oldTransferable != null && oldTransferable.getTransferData(DataFlavor.stringFlavor) != null) {
				oldClipboard = oldTransferable.getTransferData(DataFlavor.stringFlavor).toString();
			}
			String clipboard = null;
			if (newTransferable != null && newTransferable.getTransferData(DataFlavor.stringFlavor) != null) {
				clipboard = newTransferable.getTransferData(DataFlavor.stringFlavor).toString();
			}
			if (clipboard != null && !StringUtils.equals(oldClipboard, clipboard)) {
				writeToScratch(clipboard);
			}
		} catch (UnsupportedFlavorException e) {
			LOG.info(e);
		} catch (IOException e) {
			LOG.info(e);
		}
	}

	private static void writeToScratch(final String clipboard) {
		ApplicationManager.getApplication().runWriteAction(new Runnable() {
			@Override
			public void run() {
				Document document = getScratchDocument();

				if (hasFocusInEditor(document))
					return;

				String text = document.getText();
				if (text.endsWith("\n")) {
					document.setText(text + clipboard);
				} else {
					document.setText(text + "\n" + clipboard);
				}

				FileDocumentManager.getInstance().saveDocument(document);
			}
		});
	}

	private static boolean hasFocusInEditor(Document document) {
		Editor selectedTextEditor = getSelectedEditor();
		if (selectedTextEditor != null) {
			if (selectedTextEditor.getDocument().equals(document)) {
				return true;
			}
		}
		return false;
	}

	private static Document getScratchDocument() {
		ScratchComponent scratchComponent = ApplicationManager.getApplication().getComponent(ScratchComponent.class);
		VirtualFile file = scratchComponent.getDefaultScratch();
		FileDocumentManager fileDocumentManager = FileDocumentManager.getInstance();
		return fileDocumentManager.getDocument(file);
	}

	private static Editor getSelectedEditor() {
		IdeFrame frame = IdeFocusManager.findInstance().getLastFocusedFrame();
		if (frame == null) return null;

		FileEditorManager instance = FileEditorManager.getInstance(frame.getProject());
		return instance.getSelectedTextEditor();
	}

	@Override
	public void update(AnActionEvent e) {
		super.update(e);
		updateIcon(e.getPresentation(), isSelected(e));
	}

	private void updateIcon(Presentation presentation, boolean enabled) {
		if (enabled) {
			presentation.setIcon(IS_ON_ICON);
		} else {
			presentation.setIcon(IS_OFF_ICON);
		}
	}

}

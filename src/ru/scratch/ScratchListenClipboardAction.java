package ru.scratch;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javax.swing.*;

import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nullable;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.IdeFocusManager;

public class ScratchListenClipboardAction extends AnAction implements CopyPasteManager.ContentChangedListener {
	private static final Logger LOG = Logger.getInstance(ScratchListenClipboardAction.class.getName());
	boolean enabled = false;

	public static final Icon ICON = IconLoader.getIcon("/clipboardOff.gif");
	public static final Icon ICON1 = IconLoader.getIcon("/clipboardOn.gif");

	public void actionPerformed(AnActionEvent e) {
		if (!enabled) {
			CopyPasteManager.getInstance().addContentChangedListener(this);
		} else {
			CopyPasteManager.getInstance().removeContentChangedListener(this);
		}
		enabled = !enabled;
		updateIcon(e.getPresentation());
	}

	@Override
	public void contentChanged(@Nullable Transferable oldTransferable, Transferable newTransferable) {
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
				write(clipboard);
			}
		} catch (UnsupportedFlavorException e) {
			LOG.info(e);
		} catch (IOException e) {
			LOG.info(e);
		}
	}

	private void write(final String clipboard) {
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

	private boolean hasFocusInEditor(Document document) {
		Editor selectedTextEditor = getSelectedEditor();
		if (selectedTextEditor != null) {
			if (selectedTextEditor.getDocument().equals(document)) {
				return true;
			}
		}
		return false;
	}

	private Document getScratchDocument() {
		ScratchComponent scratchComponent = ApplicationManager.getApplication().getComponent(ScratchComponent.class);
		VirtualFile file = scratchComponent.getDefaultScratch();
		FileDocumentManager fileDocumentManager = FileDocumentManager.getInstance();
		return fileDocumentManager.getDocument(file);
	}

	private Editor getSelectedEditor() {
		Project project = IdeFocusManager.findInstance().getLastFocusedFrame().getProject();
		FileEditorManager instance = FileEditorManager.getInstance(project);
		return instance.getSelectedTextEditor();
	}

	@Override
	public void update(AnActionEvent e) {
		super.update(e);
		updateIcon(e.getPresentation());
	}

	private void updateIcon(Presentation presentation) {
		if (enabled) {
			presentation.setIcon(ICON1);
		} else {
			presentation.setIcon(ICON);
		}
	}

}

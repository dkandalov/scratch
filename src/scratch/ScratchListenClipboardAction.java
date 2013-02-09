/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package scratch;

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
import com.intellij.openapi.project.DumbAware;
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

public class ScratchListenClipboardAction extends ToggleAction implements DumbAware, CopyPasteManager.ContentChangedListener {
	private static final Logger LOG = Logger.getInstance(ScratchListenClipboardAction.class.getName());

	private static final Icon IS_ON_ICON = IconLoader.getIcon("/actions/menu-paste.png");
	private static final Icon IS_OFF_ICON = IconLoader.getDisabledIcon(IconLoader.getIcon("/actions/menu-paste.png"));

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
				if (document == null) {
					return;
				}
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

	@Nullable
	private static Document getScratchDocument() {
		VirtualFile file = ScratchComponent.getDefaultScratch();
		if (file == null) {
			return null;
		}
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
		presentation.setIcon(enabled ? IS_ON_ICON : IS_OFF_ICON);
	}

}

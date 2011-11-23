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
package ru.scratch;

import static com.intellij.openapi.ui.popup.JBPopupFactory.ActionSelectionAid.MNEMONICS;
import static ru.scratch.OpenScratchAction.projectFor;

import javax.swing.*;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.util.IconLoader;

/**
 * @author Dmitry Kandalov
 */
public class OpenScratchListAction extends AnAction {
	private static final String POPUP_TITLE = "List of Scratches";
	private static final Icon ICON = IconLoader.getIcon("/fileTypes/text.png");

	@Override
	public void actionPerformed(AnActionEvent event) {
		DefaultActionGroup actionGroup = new DefaultActionGroup();

		final Project project = projectFor(event);
		for (final ScratchVirtualFile scratchFile : scratchFiles()) {
			actionGroup.add(new AnAction(addMnemonics(scratchFile.getName()), "Open " + scratchFile.getName(), ICON) {
				@Override
				public void actionPerformed(AnActionEvent e) {
					new OpenFileDescriptor(project, scratchFile).navigate(true);
				}
			});
		}

		JBPopupFactory factory = JBPopupFactory.getInstance();
		ListPopup listPopup = factory.createActionGroupPopup(POPUP_TITLE, actionGroup, event.getDataContext(), MNEMONICS, true);
		listPopup.showCenteredInCurrentWindow(project);
	}

	@Override
	public void update(AnActionEvent e) {
		e.getPresentation().setEnabled(projectFor(e) != null);
	}

	private String addMnemonics(String scratchName) {
		// this is workaround to add mnemonic for "scratch.txt"
		// other scratches are given mnemonics by IntelliJ according to their numbers (like "scratch&2.txt")
		if (scratchName.equals("scratch.txt")) {
			return "&scratch.txt";
		} else {
			return scratchName;
		}
	}

	private ScratchVirtualFile[] scratchFiles() {
		ScratchComponent scratchComponent = ApplicationManager.getApplication().getComponent(ScratchComponent.class);
		return scratchComponent.getScratchFiles();
	}
}

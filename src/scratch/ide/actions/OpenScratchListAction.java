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
package scratch.ide.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.util.IconLoader;
import scratch.ScratchComponent;

import javax.swing.*;
import java.io.File;

import static com.intellij.openapi.ui.popup.JBPopupFactory.ActionSelectionAid.NUMBERING;
import static scratch.ide.actions.OpenScratchAction.projectFor;

/**
 * @author Dmitry Kandalov
 */
public class OpenScratchListAction extends DumbAwareAction {
	private static final String POPUP_TITLE = "List of Scratches";
	private static final Icon ICON = IconLoader.getIcon("/fileTypes/text.png");

	@Override
	public void actionPerformed(AnActionEvent event) {
		Project project = projectFor(event);

//		ListPopupStep popupStep = new BaseListPopupStep<String>("strings", asList("aaa", "bbb", "ccc"));
//		ScratchListPopup popup = new ScratchListPopup(popupStep);
//		popup.showCenteredInCurrentWindow(project);

		DefaultActionGroup actionGroup = createActionGroup(project);
		showListPopup(event, actionGroup, project);
	}

	private DefaultActionGroup createActionGroup(final Project project) {
		DefaultActionGroup actionGroup = new DefaultActionGroup();
		for (final File scratchFile : ScratchComponent.filesAsList()) {
			final String scratchName = scratchFile.getName();
			actionGroup.add(new AnAction(scratchName, "Open " + scratchName, getIcon(scratchName)) {
				@Override
				public void actionPerformed(AnActionEvent e) {
					ScratchComponent.openInEditor(project, scratchFile);
				}
			});
		}
		return actionGroup;
	}

	private static void showListPopup(AnActionEvent event, DefaultActionGroup actionGroup, Project project) {
		JBPopupFactory factory = JBPopupFactory.getInstance();
		ListPopup listPopup = factory.createActionGroupPopup(POPUP_TITLE, actionGroup, event.getDataContext(), NUMBERING, true);
		listPopup.showCenteredInCurrentWindow(project);
	}

	@Override
	public void update(AnActionEvent e) {
		e.getPresentation().setEnabled(projectFor(e) != null);
	}

	private static Icon getIcon(String scratchName) {
		String[] split = scratchName.split("\\.");
		Icon icon;
		if (split.length > 1) {
			String fileType = split[split.length - 1];
			icon = IconLoader.findIcon("/fileTypes/" + fileType + ".png");
		} else {
			icon = ICON;
		}
		if (icon == null || icon.getIconHeight() == 0) {
			icon = ICON;
		}
		return icon;
	}

}

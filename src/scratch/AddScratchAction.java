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
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.NonEmptyInputValidator;

import java.io.File;

/**
 * @author Vojtech Krasa
 */
public class AddScratchAction extends DumbAwareAction {
	private static final Logger LOG = Logger.getInstance(AddScratchAction.class);

	@Override
	public void actionPerformed(AnActionEvent event) {
		Project project = projectFor(event);

		String fileName = showInputDialog();
		if (fileName != null) {
			File file = ScratchComponent.createFile(fileName);
			ScratchComponent.openInEditor(project, file);
		}
	}

	private static String showInputDialog() {
		return Messages.showInputDialog(
				"File name:", "Create New Scratch", Messages.getQuestionIcon(),
				defaultScratchName(), new NonEmptyInputValidator());
	}

	private static String defaultScratchName() {
		String s = "scratch.txt";
		File file = new File(ScratchComponent.scratchesRootPath(), s);
		int i = 0;
		while (file.exists()) {
			s = "scratch" + ++i + ".txt";
			file = new File(ScratchComponent.scratchesRootPath(), s);
		}
		return s;
	}

	@Override
	public void update(AnActionEvent e) {
		e.getPresentation().setEnabled(projectFor(e) != null);
	}

	private static Project projectFor(AnActionEvent event) {
		return event.getData(PlatformDataKeys.PROJECT);
	}
}

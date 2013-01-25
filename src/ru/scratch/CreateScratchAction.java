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

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.NonEmptyInputValidator;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.File;

/**
 * @author Vojtech Krasa
 */
public class CreateScratchAction extends AnAction {
	private static final Logger LOG = Logger.getInstance(CreateScratchAction.class);

	@Override
	public void actionPerformed(AnActionEvent event) {
		Project project = projectFor(event);

		String fileName;
		fileName = Messages.showInputDialog("File name:", "Create New Scratch", Messages.getQuestionIcon(),
				"scratch.txt", new NonEmptyInputValidator());
		if (fileName != null) {
			File file = new File(ScratchComponent.pluginsRootPath(), fileName);
			FileUtil.createIfNotExists(file);
			VirtualFile fileByUrl = Util.getVirtualFile(file.getAbsolutePath());
			if (fileByUrl == null) {
				LOG.warn("Failed to open scratch file: " + file.getAbsolutePath());
				return;
			}

			OpenFileDescriptor fileDescriptor = new OpenFileDescriptor(project, fileByUrl);
			fileDescriptor.navigate(true);
			ScratchData.getInstance().setLastOpenedFileName(fileName);
		}
	}

	@Override
	public void update(AnActionEvent e) {
		e.getPresentation().setEnabled(projectFor(e) != null);
	}

	private static Project projectFor(AnActionEvent event) {
		return event.getData(PlatformDataKeys.PROJECT);
	}
}

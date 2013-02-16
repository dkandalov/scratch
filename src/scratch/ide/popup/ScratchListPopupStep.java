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

package scratch.ide.popup;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import org.jetbrains.annotations.NotNull;
import scratch.Scratch;

import javax.swing.*;
import java.util.List;

import static scratch.ide.ScratchComponent.mrScratchManager;
import static scratch.ide.Util.holdingOnTo;


public class ScratchListPopupStep extends BaseListPopupStep<Scratch> {
	private final FileTypeManager fileTypeManager;
	private final Project project;

	public ScratchListPopupStep(List<Scratch> scratches, Project project) {
		super("List of Scratches", scratches);
		this.project = project;
		this.fileTypeManager = FileTypeManager.getInstance();
	}

	@Override public PopupStep onChosen(Scratch scratch, boolean finalChoice) {
		if (!finalChoice) return null;

		mrScratchManager().userWantsToOpenScratch(scratch, holdingOnTo(project));
		return FINAL_CHOICE;
	}

	@NotNull @Override public String getTextFor(Scratch scratch) {
		return scratch.fullNameWithMnemonics;
	}

	@Override public Icon getIconFor(Scratch scratch) {
		FileType fileType = fileTypeManager.getFileTypeByExtension(scratch.extension);
		return fileType.getIcon();
	}

	@Override public boolean isMnemonicsNavigationEnabled() {
		return true;
	}

	@Override public boolean isSpeedSearchEnabled() {
		return true;
	}

	@Override public boolean isAutoSelectionEnabled() {
		return false;
	}

}

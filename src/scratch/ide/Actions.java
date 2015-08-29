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

package scratch.ide;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

import static scratch.ScratchConfig.DefaultScratchMeaning;
import static scratch.ScratchConfig.DefaultScratchMeaning.*;
import static scratch.ide.ScratchComponent.fileSystem;
import static scratch.ide.ScratchComponent.mrScratchManager;
import static scratch.ide.Util.currentFileIn;
import static scratch.ide.Util.holdingOnTo;

@SuppressWarnings("ComponentNotRegistered")
public class Actions {

	public static class DeleteScratch extends DumbAwareAction {
		@Override public void actionPerformed(AnActionEvent event) {
			VirtualFile scratchFile = getCurrentScratchFile(event);
			if (scratchFile == null) return;

			mrScratchManager().userAttemptedToDeleteScratch(scratchFile.getName());
		}

		@Override public void update(AnActionEvent event) {
			event.getPresentation().setEnabled(getCurrentScratchFile(event) != null);
		}
	}


	public static class NewScratch extends DumbAwareAction {
		@Override public void actionPerformed(AnActionEvent event) {
			mrScratchManager().userWantsToEnterNewScratchName(holdingOnTo(event.getProject()));
		}

		@Override public void update(AnActionEvent event) {
			event.getPresentation().setEnabled(event.getProject() != null);
		}
	}


	public static class OpenDefaultScratch extends DumbAwareAction {
		@Override public void actionPerformed(AnActionEvent event) {
			mrScratchManager().userWantsToOpenDefaultScratch(holdingOnTo(event.getProject()));
		}

		@Override public void update(AnActionEvent event) {
			event.getPresentation().setEnabled(event.getProject() != null);
		}
	}


	public static class OpenScratchList extends DumbAwareAction {
		@Override public void actionPerformed(AnActionEvent event) {
			mrScratchManager().userWantsToSeeScratchesList(holdingOnTo(event.getProject()));
		}

		@Override public void update(AnActionEvent event) {
			event.getPresentation().setEnabled(event.getProject() != null);
		}
	}


	public static class RenameScratch extends DumbAwareAction {
		@Override public void actionPerformed(AnActionEvent event) {
			VirtualFile scratchFile = getCurrentScratchFile(event);
			if (scratchFile == null) return;

			mrScratchManager().userWantsToEditScratchName(scratchFile.getName());
		}

		@Override public void update(AnActionEvent event) {
			event.getPresentation().setEnabled(getCurrentScratchFile(event) != null);
		}
	}


	public static class ListenToClipboard extends ToggleAction implements DumbAware {
		private static final Icon IS_ON_ICON = IconLoader.getIcon("/actions/menu-paste.png");
		private static final Icon IS_OFF_ICON = IconLoader.getDisabledIcon(IS_ON_ICON);

		@Override public void setSelected(AnActionEvent event, boolean enabled) {
			mrScratchManager().userWantsToListenToClipboard(enabled);
			event.getPresentation().setIcon(enabled ? IS_ON_ICON : IS_OFF_ICON);
		}

		@Override public boolean isSelected(AnActionEvent event) {
			return mrScratchManager().shouldListenToClipboard();
		}

		@Override public void update(@NotNull AnActionEvent event) {
			super.update(event);
			event.getPresentation().setIcon(isSelected(event) ? IS_ON_ICON : IS_OFF_ICON);
		}
	}

	public static class MakeDefaultScratchBeTopmost extends ToggleAction implements DumbAware {

		@Override public void setSelected(AnActionEvent event, boolean enabled) {
			DefaultScratchMeaning meaning = (enabled ? TOPMOST : LAST_OPENED);
			mrScratchManager().userWantsToChangeMeaningOfDefaultScratch(meaning);
		}

		@Override public boolean isSelected(AnActionEvent event) {
			return mrScratchManager().defaultScratchMeaning() == TOPMOST;
		}
	}

	public static class MakeDefaultScratchBeLastOpened extends ToggleAction implements DumbAware {

		@Override public void setSelected(AnActionEvent event, boolean enabled) {
			DefaultScratchMeaning meaning = (enabled ? LAST_OPENED : TOPMOST);
			mrScratchManager().userWantsToChangeMeaningOfDefaultScratch(meaning);
		}

		@Override public boolean isSelected(AnActionEvent event) {
			return mrScratchManager().defaultScratchMeaning() == LAST_OPENED;
		}
	}


	private static VirtualFile getCurrentScratchFile(AnActionEvent event) {
		VirtualFile currentFile = currentFileIn(event.getProject());
		if (currentFile == null || !fileSystem().isScratch(currentFile)) return null;
		return currentFile;
	}
}

package scratch.ide;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.VirtualFile;

import javax.swing.*;

import static scratch.ide.ScratchComponent.fileSystem;
import static scratch.ide.ScratchComponent.mrScratchManager;
import static scratch.ide.Util.currentFileIn;
import static scratch.ide.Util.holdingOnTo;

public class Actions {
	public static class DeleteScratchAction extends DumbAwareAction {
		@Override public void actionPerformed(AnActionEvent event) {
			VirtualFile scratchFile = getCurrentScratchFile(event);
			if (scratchFile == null) return;

			mrScratchManager().userAttemptedToDeleteScratch(scratchFile.getName());
		}

		@Override public void update(AnActionEvent event) {
			event.getPresentation().setEnabled(getCurrentScratchFile(event) != null);
		}
	}

	public static class NewScratchAction extends DumbAwareAction {
		@Override public void actionPerformed(AnActionEvent event) {
			mrScratchManager().userWantsToEnterNewScratchName();
		}

		@Override public void update(AnActionEvent event) {
			event.getPresentation().setEnabled(event.getProject() != null);
		}
	}

	public static class OpenDefaultScratchAction extends DumbAwareAction {
		@Override public void actionPerformed(AnActionEvent event) {
			mrScratchManager().userWantsToOpenDefaultScratch(holdingOnTo(event.getProject()));
		}

		@Override public void update(AnActionEvent event) {
			event.getPresentation().setEnabled(event.getProject() != null);
		}
	}

	public static class OpenScratchListAction extends DumbAwareAction {
		@Override public void actionPerformed(AnActionEvent event) {
			mrScratchManager().userWantsToSeeScratchesList(holdingOnTo(event.getProject()));
		}

		@Override public void update(AnActionEvent event) {
			event.getPresentation().setEnabled(event.getProject() != null);
		}
	}

	public static class RenameScratchAction extends DumbAwareAction {
		@Override public void actionPerformed(AnActionEvent event) {
			VirtualFile scratchFile = getCurrentScratchFile(event);
			if (scratchFile == null) return;

			mrScratchManager().userWantsToEditScratchName(scratchFile.getName());
		}

		@Override public void update(AnActionEvent event) {
			event.getPresentation().setEnabled(getCurrentScratchFile(event) != null);
		}
	}

	public static class ScratchListenToClipboardAction extends ToggleAction implements DumbAware {
		private static final Icon IS_ON_ICON = AllIcons.Actions.Menu_paste;
		private static final Icon IS_OFF_ICON = IconLoader.getDisabledIcon(IS_ON_ICON);

		@Override public void setSelected(AnActionEvent event, boolean enabled) {
			mrScratchManager().userWantsToListenToClipboard(enabled);
			event.getPresentation().setIcon(enabled ? IS_ON_ICON : IS_OFF_ICON);
		}

		@Override public boolean isSelected(AnActionEvent event) {
			return mrScratchManager().shouldListenToClipboard();
		}

		@Override public void update(AnActionEvent event) {
			super.update(event);
			event.getPresentation().setIcon(isSelected(event) ? IS_ON_ICON : IS_OFF_ICON);
		}
	}

	private static VirtualFile getCurrentScratchFile(AnActionEvent event) {
		VirtualFile currentFile = currentFileIn(event.getProject());
		if (currentFile == null || !fileSystem().isScratch(currentFile)) return null;
		return currentFile;
	}
}

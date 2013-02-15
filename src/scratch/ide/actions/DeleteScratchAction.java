package scratch.ide.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.vfs.VirtualFile;

import static scratch.ide.ScratchComponent.mrScratchManager;
import static scratch.ide.actions.RenameScratchAction.getCurrentScratchFile;

/**
 * User: dima
 * Date: 14/02/2013
 */
public class DeleteScratchAction extends DumbAwareAction {
	@Override public void actionPerformed(AnActionEvent event) {
		VirtualFile scratchFile = getCurrentScratchFile(event);
		if (scratchFile == null) return;

		mrScratchManager().userAttemptedToDeleteScratch(scratchFile.getName());
	}

	@Override public void update(AnActionEvent event) {
		event.getPresentation().setEnabled(getCurrentScratchFile(event) != null);
	}
}

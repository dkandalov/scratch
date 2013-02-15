package scratch.ide.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.vfs.VirtualFile;

import static scratch.ide.ScratchComponent.fileSystem;
import static scratch.ide.ScratchComponent.mrScratchManager;
import static scratch.ide.Util.currentFileIn;

/**
 * User: dima
 * Date: 14/02/2013
 */
public class RenameScratchAction extends DumbAwareAction {
	@Override public void actionPerformed(AnActionEvent event) {
		VirtualFile scratchFile = getCurrentScratchFile(event);
		if (scratchFile == null) return;

		mrScratchManager().userWantsToEditScratchName(scratchFile.getName());
	}

	@Override public void update(AnActionEvent event) {
		event.getPresentation().setEnabled(getCurrentScratchFile(event) != null);
	}

	public static VirtualFile getCurrentScratchFile(AnActionEvent event) {
		VirtualFile currentFile = currentFileIn(event.getProject());
		if (currentFile == null || !fileSystem().isScratch(currentFile)) return null;
		return currentFile;
	}
}

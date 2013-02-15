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
		VirtualFile currentFile = currentScratchFile(event);
		if (currentFile == null) return;

		String scratchFileName = currentFile.getName();
		mrScratchManager().userWantsToEditScratchName(scratchFileName);
	}

	@Override public void update(AnActionEvent event) {
		event.getPresentation().setEnabled(currentScratchFile(event) != null);
	}

	private static VirtualFile currentScratchFile(AnActionEvent event) {
		VirtualFile currentFile = currentFileIn(event.getProject());
		if (currentFile == null || !fileSystem().isScratch(currentFile)) return null;
		return currentFile;
	}
}

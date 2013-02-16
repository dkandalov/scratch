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

/**
 * User: dima
 * Date: 11/02/2013
 */
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

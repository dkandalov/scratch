package scratch.ide.popup;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import org.jetbrains.annotations.NotNull;
import scratch.Scratch;

import javax.swing.*;
import java.util.List;

/**
 * User: dima
 * Date: 11/02/2013
 */
public class ScratchListPopupStep extends BaseListPopupStep<Scratch> {
	private final FileTypeManager fileTypeManager;

	public ScratchListPopupStep(List<Scratch> scratches) {
		super("List of Scratches", scratches);
		this.fileTypeManager = FileTypeManager.getInstance();
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
}

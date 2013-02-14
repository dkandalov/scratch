package scratch.ide.popup;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.openapi.util.Ref;
import org.jetbrains.annotations.NotNull;
import scratch.MrScratchManager;
import scratch.Scratch;
import scratch.ide.ScratchComponent;

import javax.swing.*;
import java.awt.*;
import java.util.List;

import static scratch.ide.Util.holdingOnTo;

/**
 * User: dima
 * Date: 11/02/2013
 */
public class ScratchListPopupStep extends BaseListPopupStep<Scratch> {
	private final FileTypeManager fileTypeManager;
	private final MrScratchManager mrScratchManager;
	private final Project project;
	private final Ref<Component> componentRef;

	public ScratchListPopupStep(List<Scratch> scratches, Project project, Ref<Component> componentRef) {
		super("List of Scratches", scratches);

		this.project = project;
		this.componentRef = componentRef;
		this.mrScratchManager = ScratchComponent.mrScratchManager();
		this.fileTypeManager = FileTypeManager.getInstance();
	}

	@Override public PopupStep onChosen(Scratch scratch, boolean finalChoice) {
		if (finalChoice) {
			mrScratchManager.userWantsToOpenScratch(scratch, holdingOnTo(project));
			return FINAL_CHOICE;
		}
		return createActionsPopupFor(scratch);
	}

	private PopupStep createActionsPopupFor(final Scratch scratch) {
		AnAction renameAction = new DumbAwareAction("Rename") {
			@Override public void actionPerformed(AnActionEvent event) {
				// TODO
			}
		};
		AnAction deleteAction = new DumbAwareAction("Delete") {
			@Override public void actionPerformed(AnActionEvent event) {
				// TODO
			}
		};
		ActionGroup actionGroup = new DefaultActionGroup(renameAction, deleteAction);
		return JBPopupFactory.getInstance().createActionsStep(
				actionGroup,
				DataManager.getInstance().getDataContext(componentRef.get()),
				false, true, "", componentRef.get(), false
		);
	}

	@NotNull @Override public String getTextFor(Scratch scratch) {
		return scratch.fullNameWithMnemonics;
	}

	@Override public Icon getIconFor(Scratch scratch) {
		FileType fileType = fileTypeManager.getFileTypeByExtension(scratch.extension);
		return fileType.getIcon();
	}

	@Override public boolean hasSubstep(Scratch selectedValue) {
		return false; // TODO make it true
	}

	@Override public boolean isMnemonicsNavigationEnabled() {
		return true;
	}

	@Override public boolean isSpeedSearchEnabled() {
		return true;
	}
}

package scratch.popup;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.ui.popup.ListItemDescriptor;
import com.intellij.openapi.ui.popup.ListPopupStep;
import com.intellij.ui.ColorUtil;
import com.intellij.ui.popup.list.GroupedItemsListRenderer;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;

class ScratchListElementRenderer extends GroupedItemsListRenderer {
	private final ScratchListPopup myPopup;

	public ScratchListElementRenderer(final ScratchListPopup aPopup) {
		super(new ListItemDescriptor() {
			@Override
			public String getTextFor(Object value) {
				return aPopup.getListStep().getTextFor(value);
			}

			@Override
			public String getTooltipFor(Object value) {
				return null;
			}

			@Override
			public Icon getIconFor(Object value) {
				return aPopup.getListStep().getIconFor(value);
			}

			@Override
			public boolean hasSeparatorAboveOf(Object value) {
				return aPopup.getListModel().isSeparatorAboveOf(value);
			}

			@Override
			public String getCaptionAboveOf(Object value) {
				return aPopup.getListModel().getCaptionAboveOf(value);
			}
		});
		myPopup = aPopup;
	}

	@Override
	protected void customizeComponent(JList list, Object value, boolean isSelected) {
		ListPopupStep<Object> step = myPopup.getListStep();
		boolean isSelectable = step.isSelectable(value);
		myTextLabel.setEnabled(isSelectable);

		if (step.isMnemonicsNavigationEnabled()) {
			final int pos = step.getMnemonicNavigationFilter().getMnemonicPos(value);
			if (pos != -1) {
				String text = myTextLabel.getText();
				text = text.substring(0, pos) + text.substring(pos + 1);
				myTextLabel.setText(text);
				myTextLabel.setDisplayedMnemonicIndex(pos);
			}
		}
		else {
			myTextLabel.setDisplayedMnemonicIndex(-1);
		}

		if (step.hasSubstep(value) && isSelectable) {
			myNextStepLabel.setVisible(true);
			final boolean isDark = ColorUtil.isDark(UIUtil.getListSelectionBackground());
			myNextStepLabel.setIcon(isSelected ? isDark ? AllIcons.Icons.Ide.NextStepInverted
					: AllIcons.Icons.Ide.NextStep
					: AllIcons.Icons.Ide.NextStepGrayed);
		}
		else {
			myNextStepLabel.setVisible(false);
			//myNextStepLabel.setIcon(PopupIcons.EMPTY_ICON);
		}

		if (isSelected) {
			setSelected(myNextStepLabel);
		}
		else {
			setDeselected(myNextStepLabel);
		}
	}


}
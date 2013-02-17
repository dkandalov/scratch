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

import com.intellij.ide.IdeEventQueue;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.actionSystem.KeyboardShortcut;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.actionSystem.Shortcut;
import com.intellij.openapi.keymap.KeymapManager;
import com.intellij.openapi.ui.InputValidatorEx;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.ui.popup.ListPopupStep;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.statistics.StatisticsInfo;
import com.intellij.psi.statistics.StatisticsManager;
import com.intellij.ui.JBListWithHintProvider;
import com.intellij.ui.ListScrollingUtil;
import com.intellij.ui.SeparatorWithText;
import com.intellij.ui.popup.ClosableByLeftArrow;
import com.intellij.ui.popup.WizardPopup;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import scratch.Answer;
import scratch.Scratch;
import scratch.ScratchConfig;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

import static scratch.ide.ScratchComponent.mrScratchManager;
import static scratch.ide.popup.ScratchListElementRenderer.NextStep;

/**
 * Originally was a copy of {@link com.intellij.ui.popup.list.ListPopupImpl}.
 * The main reason for copying was to use {@link PopupModelWithMovableItems}
 * instead of {@link com.intellij.ui.popup.list.ListPopupModel}.
 */
@SuppressWarnings("unchecked")
public abstract class ScratchListPopup extends WizardPopup implements ListPopup {
	private static final String DELETE_ACTION_ID = "$Delete";
	private static final String GENERATE_ACTION_ID = "Generate";
	private static final String RENAME_ACTION_ID = "RenameElement";
	private static final int MY_MAX_ROW_COUNT = 20;

	private MyList myList;

	private MyMouseMotionListener myMouseMotionListener;
	private MyMouseListener myMouseListener;

	private PopupModelWithMovableItems myListModel;

	private int myIndexForShowingChild = -1;
	private boolean myAutoHandleBeforeShow;


	public ScratchListPopup(@NotNull ListPopupStep aStep) {
		super(aStep);
		registerActions();
	}

	private void registerActions() {
		List<KeyStroke> keyStrokes;

		keyStrokes = copyKeyStrokesFromAction(GENERATE_ACTION_ID, KeyStroke.getKeyStroke("ctrl N"));
		registerAction("addScratch", keyStrokes, new AbstractAction() {
			@Override public void actionPerformed(ActionEvent event) {
				ScratchListPopup.this.dispose();
				onNewScratch();
			}
		});

		keyStrokes = copyKeyStrokesFromAction(RENAME_ACTION_ID, KeyStroke.getKeyStroke("alt shift R"));
		registerAction("renameScratch", keyStrokes, new AbstractAction() {
			@Override public void actionPerformed(ActionEvent event) {
				final Scratch scratch = selectedScratch();
				if (scratch != null) {
					ScratchListPopup.this.dispose();
					onRenameScratch(scratch);
				}
			}
		});

		keyStrokes = copyKeyStrokesFromAction(DELETE_ACTION_ID, KeyStroke.getKeyStroke("DELETE"));
		registerAction("deleteScratch", keyStrokes, new AbstractAction() {
			@Override public void actionPerformed(ActionEvent event) {
				Scratch scratch = selectedScratch();
				if (scratch != null) {
					onScratchDelete(scratch);
				}
			}
		});

		registerAction("moveScratchUp", KeyStroke.getKeyStroke("alt UP"), new AbstractAction() {
			@Override public void actionPerformed(ActionEvent event) {
				Scratch scratch = selectedScratch();
				if (scratch != null) {
					move(scratch, ScratchConfig.UP);
					onScratchMoved(scratch, ScratchConfig.UP);
				}
			}
		});
		registerAction("moveScratchDown", KeyStroke.getKeyStroke("alt DOWN"), new AbstractAction() {
			@Override public void actionPerformed(ActionEvent event) {
				Scratch scratch = selectedScratch();
				if (scratch != null) {
					move(scratch, ScratchConfig.DOWN);
					onScratchMoved(scratch, ScratchConfig.DOWN);
				}
			}
		});
	}

	protected void onNewScratch() {}

	protected void onRenameScratch(Scratch scratch) {}

	protected void onScratchDelete(Scratch scratch) {}

	protected void onScratchMoved(Scratch scratch, int down) {}

	private void registerAction(@NonNls String aActionName, List<KeyStroke> keyStrokes, Action aAction) {
		for (int i = 0; i < keyStrokes.size(); i++) {
			KeyStroke keyStroke = keyStrokes.get(i);
			registerAction(aActionName + i, keyStroke, aAction);
		}
	}

	private static List<KeyStroke> copyKeyStrokesFromAction(String actionId, KeyStroke defaultKeyStroke) {
		List<KeyStroke> result = new ArrayList<KeyStroke>();
		Shortcut[] shortcuts = KeymapManager.getInstance().getActiveKeymap().getShortcuts(actionId);
		for (Shortcut shortcut : shortcuts) {
			if (!(shortcut instanceof KeyboardShortcut)) continue;

			KeyboardShortcut keyboardShortcut = (KeyboardShortcut) shortcut;
			if (keyboardShortcut.getSecondKeyStroke() == null && keyboardShortcut.getFirstKeyStroke() != null) {
				result.add(keyboardShortcut.getFirstKeyStroke());
			}
		}
		if (result.isEmpty()) result.add(defaultKeyStroke);
		return result;
	}

	@Nullable private Scratch selectedScratch() {
		int selectedIndex = getSelectedIndex();
		if (selectedIndex == -1) return null;
		return (Scratch) getListModel().get(selectedIndex);
	}

	private void move(Scratch scratch, int shift) {
		int newIndex = getListModel().moveItem(scratch, shift);
		myList.setSelectedIndex(newIndex);
	}

	protected PopupModelWithMovableItems getListModel() {
		return myListModel;
	}

	@Override
	protected boolean beforeShow() {
		myList.addMouseMotionListener(myMouseMotionListener);
		myList.addMouseListener(myMouseListener);

		myList.setVisibleRowCount(Math.min(MY_MAX_ROW_COUNT, myListModel.getSize()));

		boolean shouldShow = super.beforeShow();
		if (myAutoHandleBeforeShow) {
			final boolean toDispose = tryToAutoSelect(true);
			shouldShow &= !toDispose;
		}

		return shouldShow;
	}

	@Override
	protected void afterShow() {
		tryToAutoSelect(false);
	}

	private boolean tryToAutoSelect(boolean handleFinalChoices) {
		ListPopupStep<Object> listStep = getListStep();
		boolean selected = false;

		final int defaultIndex = listStep.getDefaultOptionIndex();
		if (defaultIndex >= 0 && defaultIndex < myList.getModel().getSize()) {
			ListScrollingUtil.selectItem(myList, defaultIndex);
			selected = true;
		}

		if (!selected) {
			selectFirstSelectableItem();
		}

		if (listStep.isAutoSelectionEnabled()) {
			if (!isVisible() && getSelectableCount() == 1) {
				return _handleSelect(handleFinalChoices, null);
			} else if (isVisible() && hasSingleSelectableItemWithSubmenu()) {
				return _handleSelect(handleFinalChoices, null);
			}
		}

		return false;
	}

	private boolean autoSelectUsingStatistics() {
		final String filter = getSpeedSearch().getFilter();
		if (!StringUtil.isEmpty(filter)) {
			int maxUseCount = -1;
			int mostUsedValue = -1;
			int elementsCount = myListModel.getSize();
			for (int i = 0; i < elementsCount; i++) {
				Object value = myListModel.getElementAt(i);
				final String text = getListStep().getTextFor(value);
				final int count =
						StatisticsManager.getInstance().getUseCount(new StatisticsInfo("#list_popup:" + myStep.getTitle() + "#" + filter, text));
				if (count > maxUseCount) {
					maxUseCount = count;
					mostUsedValue = i;
				}
			}

			if (mostUsedValue > 0) {
				ListScrollingUtil.selectItem(myList, mostUsedValue);
				return true;
			}
		}

		return false;
	}

	private void selectFirstSelectableItem() {
		for (int i = 0; i < myListModel.getSize(); i++) {
			if (getListStep().isSelectable(myListModel.getElementAt(i))) {
				myList.setSelectedIndex(i);
				break;
			}
		}
	}

	private boolean hasSingleSelectableItemWithSubmenu() {
		boolean oneSubmenuFound = false;
		int countSelectables = 0;
		for (int i = 0; i < myListModel.getSize(); i++) {
			Object elementAt = myListModel.getElementAt(i);
			if (getListStep().isSelectable(elementAt) ) {
				countSelectables ++;
				if (getStep().hasSubstep(elementAt)) {
					if (oneSubmenuFound) {
						return false;
					}
					oneSubmenuFound = true;
				}
			}
		}
		return oneSubmenuFound && countSelectables == 1;
	}

	private int getSelectableCount() {
		int count = 0;
		for (int i = 0; i < myListModel.getSize(); i++) {
			final Object each = myListModel.getElementAt(i);
			if (getListStep().isSelectable(each)) {
				count++;
			}
		}

		return count;
	}

	@Override
	protected JComponent createContent() {
		myMouseMotionListener = new MyMouseMotionListener();
		myMouseListener = new MyMouseListener();

		myListModel = new PopupModelWithMovableItems(this, getSpeedSearch(), getListStep());
		myList = new MyList();
		if (myStep.getTitle() != null) {
			myList.getAccessibleContext().setAccessibleName(myStep.getTitle());
		}
		myList.setSelectionMode(isMultiSelectionEnabled() ? ListSelectionModel.MULTIPLE_INTERVAL_SELECTION : ListSelectionModel.SINGLE_SELECTION);

		Insets padding = UIUtil.getListViewportPadding();
		myList.setBorder(new EmptyBorder(padding));

		ListScrollingUtil.installActions(myList);

		myList.setCellRenderer(getListElementRenderer());

		myList.getActionMap().get("selectNextColumn").setEnabled(false);
		myList.getActionMap().get("selectPreviousColumn").setEnabled(false);

		registerAction("handleSelection1", KeyEvent.VK_ENTER, 0, new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				handleSelect(true);
			}
		});

		registerAction("handleSelection2", KeyEvent.VK_RIGHT, 0, new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				handleSelect(false);
			}
		});

		registerAction("goBack2", KeyEvent.VK_LEFT, 0, new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (isClosableByLeftArrow()) {
					goBack();
				}
			}
		});


		myList.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

		return myList;
	}

	private boolean isMultiSelectionEnabled() {
		return false; // TODO remove
	}

	private boolean isClosableByLeftArrow() {
		return getParent() != null || myStep instanceof ClosableByLeftArrow;
	}

	@Override
	protected ActionMap getActionMap() {
		return myList.getActionMap();
	}

	@Override
	protected InputMap getInputMap() {
		return myList.getInputMap();
	}

	protected ListCellRenderer getListElementRenderer() {
		return new ScratchListElementRenderer(this);
	}

	@Override
	public ListPopupStep<Object> getListStep() {
		return (ListPopupStep<Object>) myStep;
	}

	@Override
	public void dispose() {
		myList.removeMouseMotionListener(myMouseMotionListener);
		myList.removeMouseListener(myMouseListener);
		super.dispose();
	}

	protected int getSelectedIndex() {
		return myList.getSelectedIndex();
	}

	@Override
	public void disposeChildren() {
		setIndexForShowingChild(-1);
		super.disposeChildren();
	}

	@Override
	protected void onAutoSelectionTimer() {
		if (myList.getModel().getSize() > 0 && !myList.isSelectionEmpty() ) {
			handleSelect(false);
		}
		else {
			disposeChildren();
			setIndexForShowingChild(-1);
		}
	}

	@Override
	public void handleSelect(boolean handleFinalChoices) {
		_handleSelect(handleFinalChoices, null);
	}

	@Override
	public void handleSelect(boolean handleFinalChoices, InputEvent e) {
		_handleSelect(handleFinalChoices, e);
	}

	private boolean _handleSelect(final boolean handleFinalChoices, InputEvent e) {
		if (myList.getSelectedIndex() == -1) return false;

		if (getSpeedSearch().isHoldingFilter() && myList.getModel().getSize() == 0) return false;

		if (myList.getSelectedIndex() == getIndexForShowingChild()) {
			if (myChild != null && !myChild.isVisible()) setIndexForShowingChild(-1);
			return false;
		}

		final Object[] selectedValues = myList.getSelectedValues();
		final ListPopupStep<Object> listStep = getListStep();
		if (!listStep.isSelectable(selectedValues[0])) return false;

		disposeChildren();

		if (myListModel.getSize() == 0) {
			setFinalRunnable(myStep.getFinalRunnable());
			setOk(true);
			disposeAllParents(e);
			setIndexForShowingChild(-1);
			return true;
		}

		valuesSelected(selectedValues);

		final PopupStep nextStep = listStep.onChosen(selectedValues[0], handleFinalChoices);
		return handleNextStep(nextStep, selectedValues.length == 1 ? selectedValues[0] : null, e);
	}

	private void valuesSelected(final Object[] values) {
		final String filter = getSpeedSearch().getFilter();
		if (!StringUtil.isEmpty(filter)) {
			for (Object value : values) {
				final String text = getListStep().getTextFor(value);
				StatisticsManager.getInstance().incUseCount(new StatisticsInfo("#list_popup:" + getListStep().getTitle() + "#" + filter, text));
			}
		}
	}

	private boolean handleNextStep(final PopupStep nextStep, Object parentValue, InputEvent e) {
		if (nextStep != PopupStep.FINAL_CHOICE) {
			final Point point = myList.indexToLocation(myList.getSelectedIndex());
			SwingUtilities.convertPointToScreen(point, myList);
			myChild = createPopup(this, nextStep, parentValue);
			if (myChild instanceof ScratchListPopup) {
				for (ListSelectionListener listener : myList.getListSelectionListeners()) {
					((ScratchListPopup)myChild).addListSelectionListener(listener);
				}
			}
			final JComponent container = getContent();
			assert container != null : "container == null";

			int y = point.y;
			if (parentValue != null && getListModel().isSeparatorAboveOf(parentValue)) {
				SeparatorWithText swt = new SeparatorWithText();
				swt.setCaption(getListModel().getCaptionAboveOf(parentValue));
				y += swt.getPreferredSize().height - 1;
			}

			myChild.show(container, point.x + container.getWidth() - STEP_X_PADDING, y, true);
			setIndexForShowingChild(myList.getSelectedIndex());
			return false;
		}
		else {
			setOk(true);
			setFinalRunnable(myStep.getFinalRunnable());
			disposeAllParents(e);
			setIndexForShowingChild(-1);
			return true;
		}
	}

	public void addListSelectionListener(ListSelectionListener listSelectionListener) {
		myList.addListSelectionListener(listSelectionListener);
	}

	public void delete(Scratch scratch) {
		myListModel.deleteItem(scratch);
	}

	public static class ScratchNameValidator implements InputValidatorEx {
		private final Scratch scratch;

		public ScratchNameValidator(Scratch scratch) {
			this.scratch = scratch;
		}

		@Override public boolean checkInput(String inputString) {
			Answer answer = mrScratchManager().checkIfUserCanRename(scratch, inputString);
			return answer.isYes;
		}

		@Nullable @Override public String getErrorText(String inputString) {
			Answer answer = mrScratchManager().checkIfUserCanRename(scratch, inputString);
			return answer.explanation;
		}

		@Override public boolean canClose(String inputString) {
			return true;
		}
	}

	private class MyMouseMotionListener extends MouseMotionAdapter {
		private int myLastSelectedIndex = -2;

		@Override
		public void mouseMoved(MouseEvent e) {
			Point point = e.getPoint();
			int index = myList.locationToIndex(point);

			if (index != myLastSelectedIndex) {
				if (!isMultiSelectionEnabled() || !UIUtil.isSelectionButtonDown(e) && myList.getSelectedIndices().length <= 1) {
					myList.setSelectedIndex(index);
				}
				restartTimer();
				myLastSelectedIndex = index;
			}

			notifyParentOnChildSelection();
		}
	}

	protected boolean isActionClick(MouseEvent e) {
		return UIUtil.isActionClick(e, MouseEvent.MOUSE_RELEASED, true);
	}

	private class MyMouseListener extends MouseAdapter {

		@Override
		public void mouseReleased(MouseEvent e) {
			if (!isActionClick(e) || isMultiSelectionEnabled() && UIUtil.isSelectionButtonDown(e)) return;
			IdeEventQueue.getInstance().blockNextEvents(e); // sometimes, after popup close, MOUSE_RELEASE event delivers to other components
			final Object selectedValue = myList.getSelectedValue();
			final ListPopupStep<Object> listStep = getListStep();
			handleSelect(handleFinalChoices(e, selectedValue, listStep), e);
			stopTimer();
		}
	}

	protected boolean handleFinalChoices(MouseEvent e, Object selectedValue, ListPopupStep<Object> listStep) {
		return selectedValue == null || !listStep.hasSubstep(selectedValue) || !listStep.isSelectable(selectedValue) || !isOnNextStepButton(e);
	}

	private boolean isOnNextStepButton(MouseEvent e) {
		final int index = myList.getSelectedIndex();
		final Rectangle bounds = myList.getCellBounds(index, index);
		final Point point = e.getPoint();
		return bounds != null && point.getX() > bounds.width + bounds.getX() - NextStep.getIconWidth();
	}

	@Override
	protected void process(KeyEvent aEvent) {
		myList.processKeyEvent(aEvent);
	}

	private int getIndexForShowingChild() {
		return myIndexForShowingChild;
	}

	private void setIndexForShowingChild(int aIndexForShowingChild) {
		myIndexForShowingChild = aIndexForShowingChild;
	}

	private class MyList extends JBListWithHintProvider implements DataProvider {
		public MyList() {
			super(myListModel);
		}

		@Override
		protected PsiElement getPsiElementForHint(Object selectedValue) {
			return selectedValue instanceof PsiElement ? (PsiElement)selectedValue : null;
		}

		@Override
		public Dimension getPreferredScrollableViewportSize() {
			return new Dimension(super.getPreferredScrollableViewportSize().width, getPreferredSize().height);
		}

		@Override
		public void processKeyEvent(KeyEvent e) {
			e.setSource(this);
			super.processKeyEvent(e);
		}

		@Override
		protected void processMouseEvent(MouseEvent e) {
			if (UIUtil.isActionClick(e, MouseEvent.MOUSE_PRESSED) && isOnNextStepButton(e)) {
				e.consume();
			}
			super.processMouseEvent(e);
		}

		@Override
		public Object getData(String dataId) {
			if (PlatformDataKeys.SELECTED_ITEM.is(dataId)){
				return myList.getSelectedValue();
			}
			if (PlatformDataKeys.SELECTED_ITEMS.is(dataId)){
				return myList.getSelectedValues();
			}
			return null;
		}
	}

	@Override
	protected void onSpeedSearchPatternChanged() {
		myListModel.refilter();
		if (myListModel.getSize() > 0) {
			if (!autoSelectUsingStatistics()) {
				int fullMatchIndex = myListModel.getClosestMatchIndex();
				if (fullMatchIndex != -1) {
					myList.setSelectedIndex(fullMatchIndex);
				}

				if (myListModel.getSize() <= myList.getSelectedIndex() || !myListModel.isVisible(myList.getSelectedValue())) {
					myList.setSelectedIndex(0);
				}
			}
		}
	}

	@Override
	protected void onSelectByMnemonic(Object value) {
		if (myListModel.isVisible(value)) {
			myList.setSelectedValue(value, true);
			myList.repaint();
			handleSelect(true);
		}
	}

	@Override
	protected JComponent getPreferredFocusableComponent() {
		return myList;
	}

	@Override
	protected void onChildSelectedFor(Object value) {
		if (myList.getSelectedValue() != value) {
			myList.setSelectedValue(value, false);
		}
	}

	@Override
	public void setHandleAutoSelectionBeforeShow(final boolean autoHandle) {
		myAutoHandleBeforeShow = autoHandle;
	}

	@Override
	public boolean isModalContext() {
		return true;
	}

}


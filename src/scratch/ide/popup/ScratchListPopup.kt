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

package scratch.ide.popup

import com.intellij.ide.IdeEventQueue
import com.intellij.openapi.actionSystem.DataProvider
import com.intellij.openapi.actionSystem.KeyboardShortcut
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.keymap.KeymapManager
import com.intellij.openapi.ui.InputValidatorEx
import com.intellij.openapi.ui.popup.ListPopup
import com.intellij.openapi.ui.popup.ListPopupStep
import com.intellij.openapi.ui.popup.PopupStep
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.statistics.StatisticsInfo
import com.intellij.psi.statistics.StatisticsManager
import com.intellij.ui.ScrollingUtil
import com.intellij.ui.SeparatorWithText
import com.intellij.ui.components.JBList
import com.intellij.ui.popup.ClosableByLeftArrow
import com.intellij.ui.popup.WizardPopup
import com.intellij.ui.speedSearch.ElementFilter
import com.intellij.util.ui.UIUtil
import org.jetbrains.annotations.NonNls
import scratch.Scratch
import scratch.ScratchConfig
import scratch.ide.ScratchComponent.Companion.mrScratchManager
import scratch.ide.popup.ScratchListElementRenderer.Companion.NextStep
import java.awt.Cursor
import java.awt.Dimension
import java.awt.event.*
import java.util.*
import javax.swing.*
import javax.swing.border.EmptyBorder
import javax.swing.event.ListSelectionListener

/**
 * Originally was a copy of [com.intellij.ui.popup.list.ListPopupImpl].
 * The main reason for copying was to use [PopupModelWithMovableItems]
 * instead of [com.intellij.ui.popup.list.ListPopupModel].
 */
@Suppress("UNCHECKED_CAST")
abstract class ScratchListPopup(aStep: ListPopupStep<Scratch>): WizardPopup(aStep as PopupStep<Any>), ListPopup {

    private lateinit var myList: MyList

    private var myMouseMotionListener: MyMouseMotionListener? = null
    private var myMouseListener: MyMouseListener? = null

    internal lateinit var listModel: PopupModelWithMovableItems<Scratch>

    private var indexForShowingChild = -1
    private var myAutoHandleBeforeShow: Boolean = false


    init {
        registerActions()
    }

    private fun registerActions() {
        var keyStrokes = copyKeyStrokesFromAction(GENERATE_ACTION_ID, KeyStroke.getKeyStroke("ctrl N"))

        registerAction("addScratch", keyStrokes, object: AbstractAction() {
            override fun actionPerformed(event: ActionEvent) {
                this@ScratchListPopup.dispose()
                onNewScratch()
            }
        })

        keyStrokes = copyKeyStrokesFromAction(RENAME_ACTION_ID, KeyStroke.getKeyStroke("alt shift R"))
        registerAction("renameScratch", keyStrokes, object: AbstractAction() {
            override fun actionPerformed(event: ActionEvent) {
                val scratch = selectedScratch()
                if (scratch != null) {
                    this@ScratchListPopup.dispose()
                    onRenameScratch(scratch)
                }
            }
        })

        keyStrokes = copyKeyStrokesFromAction(DELETE_ACTION_ID, KeyStroke.getKeyStroke("DELETE"))
        registerAction("deleteScratch", keyStrokes, object: AbstractAction() {
            override fun actionPerformed(event: ActionEvent) {
                val scratch = selectedScratch()
                if (scratch != null) {
                    this@ScratchListPopup.dispose()
                    onScratchDelete(scratch)
                }
            }
        })

        keyStrokes = copyKeyStrokesFromAction(DELETE_NO_PROMPT_ACTION_ID, KeyStroke.getKeyStroke("ctrl DELETE"))
        registerAction("deleteScratchWithoutPrompt", keyStrokes, object: AbstractAction() {
            override fun actionPerformed(event: ActionEvent) {
                val scratch = selectedScratch()
                if (scratch != null) {
                    onScratchDeleteWithoutPrompt(scratch)
                    delete(scratch)
                }
            }
        })

        registerAction("moveScratchUp", KeyStroke.getKeyStroke("alt UP"), object: AbstractAction() {
            override fun actionPerformed(event: ActionEvent) {
                val scratch = selectedScratch()
                if (scratch != null) {
                    move(scratch, ScratchConfig.UP)
                    onScratchMoved(scratch, ScratchConfig.UP)
                }
            }
        })
        registerAction("moveScratchDown", KeyStroke.getKeyStroke("alt DOWN"), object: AbstractAction() {
            override fun actionPerformed(event: ActionEvent) {
                val scratch = selectedScratch()
                if (scratch != null) {
                    move(scratch, ScratchConfig.DOWN)
                    onScratchMoved(scratch, ScratchConfig.DOWN)
                }
            }
        })
    }

    protected open fun onNewScratch() {}

    protected open fun onRenameScratch(scratch: Scratch) {}

    protected open fun onScratchDelete(scratch: Scratch) {}

    protected open fun onScratchDeleteWithoutPrompt(scratch: Scratch) {}

    protected open fun onScratchMoved(scratch: Scratch, shift: Int) {}

    private fun registerAction(@NonNls actionName: String, keyStrokes: List<KeyStroke>, action: Action) {
        for (i in keyStrokes.indices) {
            val keyStroke = keyStrokes[i]
            registerAction(actionName + i, keyStroke, action)
        }
    }

    private fun selectedScratch(): Scratch? {
        val selectedIndex = selectedIndex
        if (selectedIndex == -1) return null
        return listModel[selectedIndex] as Scratch?
    }

    private fun move(scratch: Scratch, shift: Int) {
        val newIndex = listModel.moveItem(scratch, shift)
        myList.selectedIndex = newIndex
    }

    override fun beforeShow(): Boolean {
        myList.addMouseMotionListener(myMouseMotionListener)
        myList.addMouseListener(myMouseListener)

        myList.visibleRowCount = Math.min(MY_MAX_ROW_COUNT, listModel.size)

        var shouldShow = super.beforeShow()
        if (myAutoHandleBeforeShow) {
            val toDispose = tryToAutoSelect(true)
            shouldShow = shouldShow and !toDispose
        }

        return shouldShow
    }

    override fun afterShow() {
        tryToAutoSelect(false)
    }

    private fun tryToAutoSelect(handleFinalChoices: Boolean): Boolean {
        val listStep = listStep
        var selected = false

        val defaultIndex = listStep.defaultOptionIndex
        if (defaultIndex >= 0 && defaultIndex < myList.model.size) {
            ScrollingUtil.selectItem(myList, defaultIndex)
            selected = true
        }

        if (!selected) {
            selectFirstSelectableItem()
        }

        if (listStep.isAutoSelectionEnabled) {
            if (!isVisible && selectableCount == 1) {
                return _handleSelect(handleFinalChoices, null)
            } else if (isVisible && hasSingleSelectableItemWithSubmenu()) {
                return _handleSelect(handleFinalChoices, null)
            }
        }

        return false
    }

    private fun autoSelectUsingStatistics(): Boolean {
        val filter = speedSearch.filter
        if (!StringUtil.isEmpty(filter)) {
            var maxUseCount = -1
            var mostUsedValue = -1
            val elementsCount = listModel.size
            for (i in 0..elementsCount - 1) {
                val value = listModel.getElementAt(i)
                val text = listStep.getTextFor(value)
                val count = StatisticsManager.getInstance().getUseCount(StatisticsInfo("#list_popup:" + myStep.title + "#" + filter, text))
                if (count > maxUseCount) {
                    maxUseCount = count
                    mostUsedValue = i
                }
            }

            if (mostUsedValue > 0) {
                ScrollingUtil.selectItem(myList, mostUsedValue)
                return true
            }
        }

        return false
    }

    private fun selectFirstSelectableItem() {
        for (i in 0..listModel.size - 1) {
            if (listStep.isSelectable(listModel.getElementAt(i))) {
                myList.selectedIndex = i
                break
            }
        }
    }

    private fun hasSingleSelectableItemWithSubmenu(): Boolean {
        var oneSubmenuFound = false
        var countSelectables = 0
        for (i in 0..listModel.size - 1) {
            val elementAt = listModel.getElementAt(i)
            if (listStep.isSelectable(elementAt)) {
                countSelectables++
                if (step.hasSubstep(elementAt)) {
                    if (oneSubmenuFound) {
                        return false
                    }
                    oneSubmenuFound = true
                }
            }
        }
        return oneSubmenuFound && countSelectables == 1
    }

    private val selectableCount: Int
        get() {
            val count = (0..listModel.size - 1)
                .map { listModel.getElementAt(it) }
                .count { listStep.isSelectable(it) }
            return count
        }

    override fun createContent(): JComponent {
        myMouseMotionListener = MyMouseMotionListener()
        myMouseListener = MyMouseListener()

        listModel = PopupModelWithMovableItems(this as ElementFilter<Scratch>, speedSearch, listStep)
        myList = MyList()
        if (myStep.title != null) {
            myList.accessibleContext.accessibleName = myStep.title
        }
        myList.selectionMode = ListSelectionModel.SINGLE_SELECTION

        val padding = UIUtil.getListViewportPadding()
        myList.border = EmptyBorder(padding)

        ScrollingUtil.installActions(myList)

        myList.cellRenderer = ScratchListElementRenderer(this)

        myList.actionMap.get("selectNextColumn").isEnabled = false
        myList.actionMap.get("selectPreviousColumn").isEnabled = false

        registerAction("handleSelection1", KeyEvent.VK_ENTER, 0, object: AbstractAction() {
            override fun actionPerformed(e: ActionEvent) {
                handleSelect(true)
            }
        })

        //		registerAction("handleSelection2", KeyEvent.VK_RIGHT, 0, new AbstractAction() {
        //			@Override
        //			public void actionPerformed(ActionEvent e) {
        //				handleSelect(false);
        //			}
        //		});

        registerAction("goBack2", KeyEvent.VK_LEFT, 0, object: AbstractAction() {
            override fun actionPerformed(e: ActionEvent) {
                if (isClosableByLeftArrow) {
                    goBack()
                }
            }
        })


        myList.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)

        return myList
    }

    private val isClosableByLeftArrow: Boolean
        get() = parent != null || myStep is ClosableByLeftArrow

    override fun getActionMap(): ActionMap {
        return myList.actionMap
    }

    override fun getInputMap(): InputMap {
        return myList.inputMap
    }

    override fun getListStep(): ListPopupStep<Scratch> {
        return myStep as ListPopupStep<Scratch>
    }

    override fun dispose() {
        myList.removeMouseMotionListener(myMouseMotionListener)
        myList.removeMouseListener(myMouseListener)
        super.dispose()
    }

    protected val selectedIndex: Int
        get() = myList.selectedIndex

    override fun disposeChildren() {
        indexForShowingChild = -1
        super.disposeChildren()
    }

    override fun onAutoSelectionTimer() {
        if (myList.model.size > 0 && !myList.isSelectionEmpty) {
            handleSelect(false)
        } else {
            disposeChildren()
            indexForShowingChild = -1
        }
    }

    override fun handleSelect(handleFinalChoices: Boolean) {
        _handleSelect(handleFinalChoices, null)
    }

    override fun handleSelect(handleFinalChoices: Boolean, e: InputEvent) {
        _handleSelect(handleFinalChoices, e)
    }

    private fun _handleSelect(handleFinalChoices: Boolean, e: InputEvent?): Boolean {
        if (myList.selectedIndex == -1) return false

        if (speedSearch.isHoldingFilter && myList.model.size == 0) return false

        if (myList.selectedIndex == indexForShowingChild) {
            if (myChild != null && !myChild.isVisible) indexForShowingChild = -1
            return false
        }

        val selectedValues = myList.selectedValuesList
        if (!listStep.isSelectable(selectedValues[0])) return false

        disposeChildren()

        if (listModel.size == 0) {
            setFinalRunnable(myStep.finalRunnable)
            setOk(true)
            disposeAllParents(e)
            indexForShowingChild = -1
            return true
        }

        valuesSelected(selectedValues)

        val nextStep = listStep.onChosen(selectedValues[0], handleFinalChoices)
        return handleNextStep(nextStep as PopupStep<*>?, if (selectedValues.size == 1) selectedValues[0] else null, e)
    }

    private fun valuesSelected(values: List<Scratch>) {
        val filter = speedSearch.filter
        if (!StringUtil.isEmpty(filter)) {
            values
                .map { listStep.getTextFor(it) }
                .forEach { StatisticsManager.getInstance().incUseCount(StatisticsInfo("#list_popup:" + listStep.title + "#" + filter, it)) }
        }
    }

    private fun handleNextStep(nextStep: PopupStep<*>?, parentValue: Scratch?, e: InputEvent?): Boolean {
        if (nextStep !== PopupStep.FINAL_CHOICE) {
            val point = myList.indexToLocation(myList.selectedIndex)
            SwingUtilities.convertPointToScreen(point, myList)
            myChild = createPopup(this, nextStep, parentValue)
            if (myChild is ScratchListPopup) {
                for (listener in myList.listSelectionListeners) {
                    (myChild as ScratchListPopup).addListSelectionListener(listener)
                }
            }
            val container = content ?: error("container == null")

            var y = point.y
            if (parentValue != null && listModel.isSeparatorAboveOf(parentValue)) {
                val swt = SeparatorWithText()
                swt.setCaption(listModel.getCaptionAboveOf(parentValue))
                y += swt.preferredSize.height - 1
            }

            myChild.show(container, point.x + container.width - WizardPopup.STEP_X_PADDING, y, true)
            indexForShowingChild = myList.selectedIndex
            return false
        } else {
            setOk(true)
            setFinalRunnable(myStep.finalRunnable)
            disposeAllParents(e)
            indexForShowingChild = -1
            return true
        }
    }

    override fun addListSelectionListener(listSelectionListener: ListSelectionListener) {
        myList.addListSelectionListener(listSelectionListener)
    }

    fun delete(scratch: Scratch) {
        listModel.deleteItem(scratch)
        if (listModel.size > 0) {
            ScrollingUtil.selectItem(myList, listModel.size - 1)
        }
    }

    class ScratchNameValidator(private val scratch: Scratch): InputValidatorEx {

        override fun checkInput(inputString: String): Boolean {
            val (isYes) = mrScratchManager().checkIfUserCanRename(scratch, inputString)
            return isYes
        }

        override fun getErrorText(inputString: String): String? {
            val (_, explanation) = mrScratchManager().checkIfUserCanRename(scratch, inputString)
            return explanation
        }

        override fun canClose(inputString: String): Boolean {
            return true
        }
    }

    private inner class MyMouseMotionListener: MouseMotionAdapter() {
        private var myLastSelectedIndex = -2

        override fun mouseMoved(e: MouseEvent?) {
            val point = e!!.point
            val index = myList.locationToIndex(point)

            if (index != myLastSelectedIndex) {
                myList.selectedIndex = index
                restartTimer()
                myLastSelectedIndex = index
            }

            notifyParentOnChildSelection()
        }
    }

    protected fun isActionClick(e: MouseEvent): Boolean {
        return UIUtil.isActionClick(e, MouseEvent.MOUSE_RELEASED, true)
    }

    private inner class MyMouseListener: MouseAdapter() {
        override fun mouseReleased(e: MouseEvent) {
            if (!isActionClick(e)) return
            IdeEventQueue.getInstance().blockNextEvents(e) // sometimes, after popup close, MOUSE_RELEASE event delivers to other components
            val selectedValue = myList.selectedValue
            val listStep = listStep
            handleSelect(handleFinalChoices(e, selectedValue, listStep), e)
            stopTimer()
        }
    }

    protected fun handleFinalChoices(e: MouseEvent, selectedValue: Scratch?, listStep: ListPopupStep<Scratch>): Boolean {
        return selectedValue == null || !listStep.hasSubstep(selectedValue) || !listStep.isSelectable(selectedValue) || !isOnNextStepButton(e)
    }

    private fun isOnNextStepButton(e: MouseEvent): Boolean {
        val index = myList.selectedIndex
        val bounds = myList.getCellBounds(index, index)
        val point = e.point
        return bounds != null && point.getX() > bounds.width + bounds.getX() - NextStep!!.iconWidth
    }

    override fun process(event: KeyEvent?) {
        myList.processKeyEvent(event!!)
    }

    private inner class MyList: JBList<Scratch>(listModel), DataProvider {

        override fun getPreferredScrollableViewportSize(): Dimension {
            return Dimension(super.getPreferredScrollableViewportSize().width, preferredSize.height)
        }

        public override fun processKeyEvent(e: KeyEvent) {
            e.source = this
            super.processKeyEvent(e)
        }

        override fun processMouseEvent(e: MouseEvent) {
            if (UIUtil.isActionClick(e, MouseEvent.MOUSE_PRESSED) && isOnNextStepButton(e)) {
                e.consume()
            }
            super.processMouseEvent(e)
        }

        override fun getData(dataId: String): Any? {
            if (PlatformDataKeys.SELECTED_ITEM.`is`(dataId)) {
                return myList.selectedValue
            }
            if (PlatformDataKeys.SELECTED_ITEMS.`is`(dataId)) {
                return myList.selectedValuesList
            }
            return null
        }
    }

    override fun onSpeedSearchPatternChanged() {
        listModel.refilter()
        if (listModel.size > 0) {
            if (!autoSelectUsingStatistics()) {
                val fullMatchIndex = listModel.closestMatchIndex
                if (fullMatchIndex != -1) {
                    myList.selectedIndex = fullMatchIndex
                }

                if (listModel.size <= myList.selectedIndex || !listModel.isVisible(myList.selectedValue as Scratch)) {
                    myList.selectedIndex = 0
                }
            }
        }
    }

    override fun onSelectByMnemonic(value: Any?) {
        if (listModel.isVisible(value as Scratch)) {
            myList.setSelectedValue(value, true)
            myList.repaint()
            handleSelect(true)
        }
    }

    override fun getPreferredFocusableComponent(): JComponent {
        return myList
    }

    override fun onChildSelectedFor(value: Any) {
        if (myList.selectedValue !== value) {
            myList.setSelectedValue(value, false)
        }
    }

    override fun setHandleAutoSelectionBeforeShow(autoHandle: Boolean) {
        myAutoHandleBeforeShow = autoHandle
    }

    override fun isModalContext(): Boolean {
        return true
    }

    companion object {
        private val DELETE_ACTION_ID = "\$Delete"
        private val DELETE_NO_PROMPT_ACTION_ID = "\$DeleteNoPrompt"
        private val GENERATE_ACTION_ID = "Generate"
        private val RENAME_ACTION_ID = "RenameElement"
        private val MY_MAX_ROW_COUNT = 20

        private fun copyKeyStrokesFromAction(actionId: String, defaultKeyStroke: KeyStroke): List<KeyStroke> {
            val result = ArrayList<KeyStroke>()
            val shortcuts = KeymapManager.getInstance().activeKeymap.getShortcuts(actionId)
            for (shortcut in shortcuts) {
                if (shortcut !is KeyboardShortcut) continue

                val keyboardShortcut = shortcut
                if (keyboardShortcut.secondKeyStroke == null) {
                    result.add(keyboardShortcut.firstKeyStroke)
                }
            }
            if (result.isEmpty()) result.add(defaultKeyStroke)
            return result
        }
    }

}


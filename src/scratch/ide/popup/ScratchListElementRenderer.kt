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

import com.intellij.openapi.ui.popup.ListItemDescriptor
import com.intellij.openapi.util.IconLoader
import com.intellij.ui.ColorUtil
import com.intellij.ui.popup.list.GroupedItemsListRenderer
import com.intellij.util.ui.UIUtil
import scratch.Scratch
import javax.swing.Icon
import javax.swing.JList

fun createListItemDescriptor(myPopup: ScratchListPopup) = object: ListItemDescriptor<Scratch> {
    override fun getTextFor(value: Scratch) = myPopup.listStep.getTextFor(value)
    override fun getTooltipFor(value: Scratch) = null
    override fun getIconFor(value: Scratch) = myPopup.listStep.getIconFor(value)
    override fun hasSeparatorAboveOf(value: Scratch) = myPopup.listModel.isSeparatorAboveOf(value)
    override fun getCaptionAboveOf(value: Scratch) = myPopup.listModel.getCaptionAboveOf(value)
}

internal class ScratchListElementRenderer(
    private val myPopup: ScratchListPopup
): GroupedItemsListRenderer<Scratch>(createListItemDescriptor(myPopup)) {

    override fun customizeComponent(list: JList<out Scratch>, value: Scratch, isSelected: Boolean) {
        val step = myPopup.listStep
        val isSelectable = step.isSelectable(value)
        myTextLabel.isEnabled = isSelectable

        if (step.isMnemonicsNavigationEnabled) {
            val navigationFilter = step.mnemonicNavigationFilter
            val pos = navigationFilter?.getMnemonicPos(value) ?: -1
            if (pos != -1) {
                var text = myTextLabel.text
                text = text.substring(0, pos) + text.substring(pos + 1)
                myTextLabel.text = text
                myTextLabel.displayedMnemonicIndex = pos
            }
        } else {
            myTextLabel.displayedMnemonicIndex = -1
        }

        if (step.hasSubstep(value) && isSelectable) {
            myNextStepLabel.isVisible = true
            myNextStepLabel.icon = if (isSelected) {
                val isDark = ColorUtil.isDark(UIUtil.getListSelectionBackground())
                if (isDark) NextStepInverted else NextStep
            } else {
                NextStepGrayed
            }
        } else {
            myNextStepLabel.isVisible = false
            //myNextStepLabel.setIcon(PopupIcons.EMPTY_ICON);
        }

        if (isSelected) {
            setSelected(myNextStepLabel)
        } else {
            setDeselected(myNextStepLabel)
        }
    }

    companion object {
        val NextStep: Icon? = IconLoader.findIcon("/icons/ide/nextStep.png")
        val NextStepGrayed = IconLoader.getIcon("/icons/ide/nextStepGrayed.png") // 12x12
        val NextStepInverted = IconLoader.getIcon("/icons/ide/nextStepInverted.png") // 12x12
    }
}
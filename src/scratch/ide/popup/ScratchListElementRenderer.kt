package scratch.ide.popup

import com.intellij.openapi.ui.popup.ListItemDescriptor
import com.intellij.openapi.util.IconLoader
import com.intellij.ui.ColorUtil
import com.intellij.ui.popup.list.GroupedItemsListRenderer
import com.intellij.util.ui.UIUtil
import scratch.Scratch
import javax.swing.JList

fun createListItemDescriptor(myPopup: ScratchListPopup) = object: ListItemDescriptor<Scratch> {
    override fun getTextFor(value: Scratch) = myPopup.listStep.getTextFor(value)
    override fun getTooltipFor(value: Scratch): String? = null
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
                val isDark = ColorUtil.isDark(UIUtil.getListSelectionBackground(true))
                if (isDark) nextStepInverted else nextStep
            } else {
                nextStepGrayed
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
        val nextStep = IconLoader.getIcon("/icons/ide/nextStep.svg", IconLoader::class.java)
        val nextStepGrayed = IconLoader.getIcon("/icons/ide/nextStep_dark.png", IconLoader::class.java)
        val nextStepInverted = IconLoader.getIcon("/icons/ide/nextStepInverted.svg", IconLoader::class.java)
    }
}
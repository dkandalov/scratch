package scratch.ide.popup

import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.PopupStep
import com.intellij.openapi.ui.popup.util.BaseListPopupStep
import scratch.Scratch
import scratch.ide.ScratchComponent.Companion.mrScratchManager
import scratch.ide.wrapAsDataHolder


class ScratchListPopupStep(scratches: List<Scratch>, private val project: Project): BaseListPopupStep<Scratch>("List of Scratches", scratches) {
    private val fileTypeManager = FileTypeManager.getInstance()

    override fun onChosen(scratch: Scratch, finalChoice: Boolean): PopupStep<*>? {
        if (!finalChoice) return null
        mrScratchManager().userWantsToOpenScratch(scratch, project.wrapAsDataHolder())
        return PopupStep.FINAL_CHOICE
    }

    override fun getTextFor(scratch: Scratch) = scratch.fullNameWithMnemonics

    override fun getIconFor(scratch: Scratch) = fileTypeManager.getFileTypeByExtension(scratch.extension).icon

    override fun isMnemonicsNavigationEnabled() = true

    override fun isSpeedSearchEnabled() = true

    override fun isAutoSelectionEnabled() = false
}

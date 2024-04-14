package scratch.ide

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionUpdateThread.EDT
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ToggleAction
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import scratch.ScratchConfig.DefaultScratchMeaning.LAST_OPENED
import scratch.ScratchConfig.DefaultScratchMeaning.TOPMOST
import scratch.ide.ScratchComponent.Companion.fileSystem
import scratch.ide.ScratchComponent.Companion.mrScratchManager

class DeleteScratch : DumbAwareAction() {
    override fun actionPerformed(event: AnActionEvent) {
        val scratchFile = event.currentScratchFile() ?: return
        mrScratchManager().userAttemptedToDeleteScratch(scratchFile.name, event.project.wrapAsDataHolder())
    }

    override fun update(event: AnActionEvent) {
        event.presentation.isEnabled = event.currentScratchFile() != null
    }

    override fun getActionUpdateThread() = EDT
}

class NewScratch : DumbAwareAction() {
    override fun actionPerformed(event: AnActionEvent) =
        mrScratchManager().userWantsToEnterNewScratchName(event.project.wrapAsDataHolder())

    override fun update(event: AnActionEvent) {
        event.presentation.isEnabled = event.project != null
    }

    override fun getActionUpdateThread() = EDT
}

class OpenDefaultScratch : DumbAwareAction() {
    override fun actionPerformed(event: AnActionEvent) =
        mrScratchManager().userWantsToOpenDefaultScratch(event.project.wrapAsDataHolder())

    override fun update(event: AnActionEvent) {
        event.presentation.isEnabled = event.project != null
    }

    override fun getActionUpdateThread() = EDT
}

class OpenScratchList : DumbAwareAction() {
    override fun actionPerformed(event: AnActionEvent) {
        mrScratchManager().userWantsToSeeScratchesList(event.project.wrapAsDataHolder())
    }

    override fun update(event: AnActionEvent) {
        event.presentation.isEnabled = event.project != null
    }

    override fun getActionUpdateThread() = EDT
}

class RenameScratch : DumbAwareAction() {
    override fun actionPerformed(event: AnActionEvent) {
        val scratchFile = event.currentScratchFile() ?: return
        mrScratchManager().userWantsToEditScratchName(scratchFile.name)
    }

    override fun update(event: AnActionEvent) {
        event.presentation.isEnabled = event.currentScratchFile() != null
    }

    override fun getActionUpdateThread() = EDT
}

class ListenToClipboard : ToggleAction(), DumbAware {
    override fun setSelected(event: AnActionEvent, selected: Boolean) {
        mrScratchManager().userWantsToListenToClipboard(selected)
        event.presentation.icon = isOnIcon
        event.presentation.isEnabled = isSelected(event)
    }

    override fun update(event: AnActionEvent) {
        super.update(event)
        event.presentation.icon = isOnIcon
        event.presentation.isEnabled = isSelected(event)
    }

    override fun isSelected(event: AnActionEvent) =
        mrScratchManager().shouldListenToClipboard()

    override fun getActionUpdateThread() = EDT

    companion object {
        private val isOnIcon = AllIcons.Actions.MenuPaste
    }
}

class MakeDefaultScratchBeTopmost : ToggleAction(), DumbAware {
    override fun setSelected(event: AnActionEvent, selected: Boolean) {
        val meaning = if (selected) TOPMOST else LAST_OPENED
        mrScratchManager().userWantsToChangeMeaningOfDefaultScratch(meaning)
    }

    override fun isSelected(event: AnActionEvent) =
        mrScratchManager().defaultScratchMeaning() == TOPMOST

    override fun getActionUpdateThread() = EDT
}

class MakeDefaultScratchBeLastOpened : ToggleAction(), DumbAware {
    override fun setSelected(event: AnActionEvent, selected: Boolean) {
        val meaning = if (selected) LAST_OPENED else TOPMOST
        mrScratchManager().userWantsToChangeMeaningOfDefaultScratch(meaning)
    }

    override fun isSelected(event: AnActionEvent) =
        mrScratchManager().defaultScratchMeaning() == LAST_OPENED

    override fun getActionUpdateThread() = EDT
}

private fun AnActionEvent.currentScratchFile(): VirtualFile? {
    val currentFile = project.currentFile() ?: return null
    return if (fileSystem().isScratch(currentFile)) currentFile else null
}

private fun Project?.currentFile() =
    if (this == null) null
    else FileEditorManagerEx.getInstanceEx(this).currentFile

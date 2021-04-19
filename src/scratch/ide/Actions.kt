package scratch.ide

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ToggleAction
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.IconLoader
import com.intellij.openapi.vfs.VirtualFile
import scratch.ScratchConfig.DefaultScratchMeaning.LAST_OPENED
import scratch.ScratchConfig.DefaultScratchMeaning.TOPMOST
import scratch.ide.ScratchComponent.Companion.fileSystem
import scratch.ide.ScratchComponent.Companion.mrScratchManager

class DeleteScratch: DumbAwareAction() {
    override fun actionPerformed(event: AnActionEvent) {
        val scratchFile = event.currentScratchFile() ?: return
        mrScratchManager().userAttemptedToDeleteScratch(scratchFile.name, event.project.wrapAsDataHolder())
    }

    override fun update(event: AnActionEvent) {
        event.presentation.isEnabled = event.currentScratchFile() != null
    }
}


class NewScratch: DumbAwareAction() {
    override fun actionPerformed(event: AnActionEvent) {
        mrScratchManager().userWantsToEnterNewScratchName(event.project.wrapAsDataHolder())
    }

    override fun update(event: AnActionEvent) {
        event.presentation.isEnabled = event.project != null
    }
}


class OpenDefaultScratch: DumbAwareAction() {
    override fun actionPerformed(event: AnActionEvent) {
        mrScratchManager().userWantsToOpenDefaultScratch(event.project.wrapAsDataHolder())
    }

    override fun update(event: AnActionEvent) {
        event.presentation.isEnabled = event.project != null
    }
}


class OpenScratchList: DumbAwareAction() {
    override fun actionPerformed(event: AnActionEvent) {
        mrScratchManager().userWantsToSeeScratchesList(event.project.wrapAsDataHolder())
    }

    override fun update(event: AnActionEvent) {
        event.presentation.isEnabled = event.project != null
    }
}


class RenameScratch: DumbAwareAction() {
    override fun actionPerformed(event: AnActionEvent) {
        val scratchFile = event.currentScratchFile() ?: return
        mrScratchManager().userWantsToEditScratchName(scratchFile.name)
    }

    override fun update(event: AnActionEvent) {
        event.presentation.isEnabled = event.currentScratchFile() != null
    }
}


class ListenToClipboard: ToggleAction(), DumbAware {
    override fun setSelected(event: AnActionEvent, selected: Boolean) {
        mrScratchManager().userWantsToListenToClipboard(selected)
        event.presentation.icon = if (selected) isOnIcon else isOffIcon
    }

    override fun update(event: AnActionEvent) {
        super.update(event)
        event.presentation.icon = if (isSelected(event)) isOnIcon else isOffIcon
    }

    override fun isSelected(event: AnActionEvent) = mrScratchManager().shouldListenToClipboard()

    companion object {
        private val isOnIcon = IconLoader.getIcon("/actions/menu-paste.png", IconLoader::class.java)
        private val isOffIcon = IconLoader.getDisabledIcon(isOnIcon)
    }
}

class MakeDefaultScratchBeTopmost: ToggleAction(), DumbAware {
    override fun setSelected(event: AnActionEvent, selected: Boolean) {
        val meaning = if (selected) TOPMOST else LAST_OPENED
        mrScratchManager().userWantsToChangeMeaningOfDefaultScratch(meaning)
    }

    override fun isSelected(event: AnActionEvent) = mrScratchManager().defaultScratchMeaning() == TOPMOST
}

class MakeDefaultScratchBeLastOpened: ToggleAction(), DumbAware {
    override fun setSelected(event: AnActionEvent, selected: Boolean) {
        val meaning = if (selected) LAST_OPENED else TOPMOST
        mrScratchManager().userWantsToChangeMeaningOfDefaultScratch(meaning)
    }

    override fun isSelected(event: AnActionEvent) = mrScratchManager().defaultScratchMeaning() == LAST_OPENED
}


private fun AnActionEvent.currentScratchFile(): VirtualFile? {
    val currentFile = project.currentFile() ?: return null
    return if (fileSystem().isScratch(currentFile)) currentFile else null
}

private fun Project?.currentFile(): VirtualFile? {
    return if (this == null) null
    else (FileEditorManagerEx.getInstance(this) as FileEditorManagerEx).currentFile
}

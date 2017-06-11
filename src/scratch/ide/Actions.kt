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
import scratch.ide.Util.holdingOnTo

object Actions {

    class DeleteScratch: DumbAwareAction() {
        override fun actionPerformed(event: AnActionEvent) {
            val scratchFile = event.currentScratchFile() ?: return
            mrScratchManager().userAttemptedToDeleteScratch(scratchFile.name, holdingOnTo(event.project))
        }

        override fun update(event: AnActionEvent?) {
            event!!.presentation.isEnabled = event.currentScratchFile() != null
        }
    }


    class NewScratch: DumbAwareAction() {
        override fun actionPerformed(event: AnActionEvent) {
            mrScratchManager().userWantsToEnterNewScratchName(holdingOnTo(event.project))
        }

        override fun update(event: AnActionEvent?) {
            event!!.presentation.isEnabled = event.project != null
        }
    }


    class OpenDefaultScratch: DumbAwareAction() {
        override fun actionPerformed(event: AnActionEvent) {
            mrScratchManager().userWantsToOpenDefaultScratch(holdingOnTo(event.project))
        }

        override fun update(event: AnActionEvent?) {
            event!!.presentation.isEnabled = event.project != null
        }
    }


    class OpenScratchList: DumbAwareAction() {
        override fun actionPerformed(event: AnActionEvent) {
            mrScratchManager().userWantsToSeeScratchesList(holdingOnTo(event.project))
        }

        override fun update(event: AnActionEvent?) {
            event!!.presentation.isEnabled = event.project != null
        }
    }


    class RenameScratch: DumbAwareAction() {
        override fun actionPerformed(event: AnActionEvent) {
            val scratchFile = event.currentScratchFile() ?: return
            mrScratchManager().userWantsToEditScratchName(scratchFile.name)
        }

        override fun update(event: AnActionEvent?) {
            event!!.presentation.isEnabled = event.currentScratchFile() != null
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
            private val isOnIcon = IconLoader.getIcon("/actions/menu-paste.png")
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
}

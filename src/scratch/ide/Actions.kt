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
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.util.IconLoader
import com.intellij.openapi.vfs.VirtualFile
import scratch.ScratchConfig.DefaultScratchMeaning.LAST_OPENED
import scratch.ScratchConfig.DefaultScratchMeaning.TOPMOST
import scratch.ide.ScratchComponent.Companion.fileSystem
import scratch.ide.ScratchComponent.Companion.mrScratchManager
import scratch.ide.Util.currentFileIn
import scratch.ide.Util.holdingOnTo

object Actions {

    class DeleteScratch: DumbAwareAction() {
        override fun actionPerformed(event: AnActionEvent) {
            val scratchFile = getCurrentScratchFile(event) ?: return
            mrScratchManager().userAttemptedToDeleteScratch(scratchFile.name, holdingOnTo(event.project))
        }

        override fun update(event: AnActionEvent?) {
            event!!.presentation.isEnabled = getCurrentScratchFile(event) != null
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
            val scratchFile = getCurrentScratchFile(event) ?: return
            mrScratchManager().userWantsToEditScratchName(scratchFile.name)
        }

        override fun update(event: AnActionEvent?) {
            event!!.presentation.isEnabled = getCurrentScratchFile(event) != null
        }
    }


    class ListenToClipboard: ToggleAction(), DumbAware {

        override fun setSelected(event: AnActionEvent, enabled: Boolean) {
            mrScratchManager().userWantsToListenToClipboard(enabled)
            event.presentation.icon = if (enabled) IS_ON_ICON else IS_OFF_ICON
        }

        override fun update(event: AnActionEvent) {
            super.update(event)
            event.presentation.icon = if (isSelected(event)) IS_ON_ICON else IS_OFF_ICON
        }

        override fun isSelected(event: AnActionEvent) = mrScratchManager().shouldListenToClipboard()

        companion object {
            private val IS_ON_ICON = IconLoader.getIcon("/actions/menu-paste.png")
            private val IS_OFF_ICON = IconLoader.getDisabledIcon(IS_ON_ICON)
        }
    }

    class MakeDefaultScratchBeTopmost: ToggleAction(), DumbAware {

        override fun setSelected(event: AnActionEvent, enabled: Boolean) {
            val meaning = if (enabled) TOPMOST else LAST_OPENED
            mrScratchManager().userWantsToChangeMeaningOfDefaultScratch(meaning)
        }

        override fun isSelected(event: AnActionEvent) = mrScratchManager().defaultScratchMeaning() == TOPMOST
    }

    class MakeDefaultScratchBeLastOpened: ToggleAction(), DumbAware {

        override fun setSelected(event: AnActionEvent, enabled: Boolean) {
            val meaning = if (enabled) LAST_OPENED else TOPMOST
            mrScratchManager().userWantsToChangeMeaningOfDefaultScratch(meaning)
        }

        override fun isSelected(event: AnActionEvent) = mrScratchManager().defaultScratchMeaning() == LAST_OPENED
    }


    private fun getCurrentScratchFile(event: AnActionEvent): VirtualFile? {
        val currentFile = currentFileIn(event.project)
        if (currentFile == null || !fileSystem().isScratch(currentFile)) return null
        return currentFile
    }
}

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

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.NotificationType.INFORMATION
import com.intellij.notification.NotificationType.WARNING
import com.intellij.notification.Notifications
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import scratch.Scratch


class ScratchLog {

    fun failedToRename(scratch: Scratch) {
        notifyUser("Failed to rename scratch: ${scratch.fileName}", WARNING)
    }

    fun migratedToIdeScratches() {
        log.info("Migrated plugin scratches to IDE")
        notifyUser("Migrated scratches to IDE, now you can find them in Project View -> Scratches tab.", INFORMATION)
    }

    fun failedToMigrateScratchesToIdeLocation(reason: String) =
        notifyUser("Failed to migrated plugin scratches to IDE: $reason", WARNING)

    fun listeningToClipboard(isListening: Boolean) =
        if (isListening) notifyUser("Started listening to clipboard", INFORMATION)
        else notifyUser("Stopped listening to clipboard", INFORMATION)

    fun failedToMigrateScratchesToFiles(scratchIndexes: List<Int>) =
        notifyUser("Failed to migrated scratches to physical files: ${scratchIndexes.joinToString(", ")}", WARNING)

    fun willNotMigrateBecauseTargetFolderIsNotEmpty() =
        notifyUser("Old scratches data won't be save because scratches folder is not empty", WARNING)

    fun failedToOpenDefaultScratch() = notifyUser("Failed to open default scratch", WARNING)

    fun failedToOpen(scratch: Scratch) = notifyUser("Failed to open scratch: '${scratch.fileName}'", WARNING)

    fun failedToCreate(scratch: Scratch) = notifyUser("Failed to create scratch: '${scratch.fileName}'", WARNING)

    fun failedToDelete(scratch: Scratch) = notifyUser("Failed to delete scratch: '${scratch.fileName}'", WARNING)

    fun failedToFindVirtualFileFor(scratch: Scratch) = log.warn("Failed to find virtual file for '${scratch.fileName}'")

    companion object {
        private val log = Logger.getInstance(ScratchLog::class.java)
        private val title = "Scratch Plugin"

        private fun notifyUser(message: String, notificationType: NotificationType) {
            val groupDisplayId = ScratchLog.title
            ApplicationManager.getApplication()
                .messageBus.syncPublisher(Notifications.TOPIC)
                .notify(Notification(groupDisplayId, title, message, notificationType))
        }
    }
}

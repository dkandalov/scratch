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
import com.intellij.openapi.util.text.StringUtil
import scratch.Scratch


class ScratchLog {

    fun failedToRename(scratch: Scratch) {
        notifyUser("", "Failed to rename scratch: " + scratch.fileName, WARNING)
    }

    fun migratedScratchesToFiles() {
        log.info("Migrated scratches to physical files")
    }

    fun listeningToClipboard(isListening: Boolean) {
        if (isListening)
            notifyUser(title, "Started listening to clipboard", INFORMATION)
        else
            notifyUser(title, "Stopped listening to clipboard", INFORMATION)
    }

    fun failedToMigrateScratchesToFiles(scratchIndexes: List<Int>) {
        val title = "Failed to migrated scratches to physical files. "
        val message = "Failed scratches: " + join(scratchIndexes)
        notifyUser(title, message, WARNING)
    }

    fun willNotMigrateBecauseTargetFolderIsNotEmpty() {
        notifyUser(title, "Old scratches data won't be save because scratches folder is not empty", WARNING)
    }

    fun failedToOpenDefaultScratch() {
        notifyUser("", "Failed to open default scratch", WARNING)
    }

    fun failedToOpen(scratch: Scratch) {
        notifyUser("", "Failed to open scratch: '" + scratch.fileName + "'", WARNING)
    }

    fun failedToCreate(scratch: Scratch) {
        notifyUser("", "Failed to create scratch: '" + scratch.fileName + "'", WARNING)
    }

    fun failedToDelete(scratch: Scratch) {
        notifyUser("", "Failed to delete scratch: '" + scratch.fileName + "'", WARNING)
    }

    fun failedToFindVirtualFileFor(scratch: Scratch) {
        log.warn("Failed to find virtual file for '" + scratch.fileName + "'")
    }

    companion object {
        private val log = Logger.getInstance(ScratchLog::class.java)
        private val title = "Scratch Plugin"

        private fun notifyUser(title: String, message: String, notificationType: NotificationType) {
            val groupDisplayId = ScratchLog.title
            val notification = Notification(groupDisplayId, title, message, notificationType)
            ApplicationManager.getApplication().messageBus.syncPublisher(Notifications.TOPIC).notify(notification)
        }

        private fun <T> join(values: List<T>): String {
            return StringUtil.join(values, { it.toString() }, ", ")
        }
    }
}

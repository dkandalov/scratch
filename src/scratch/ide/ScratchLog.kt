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

import com.intellij.notification.NotificationType.INFORMATION
import com.intellij.notification.NotificationType.WARNING
import com.intellij.openapi.diagnostic.Logger
import scratch.Scratch


class ScratchLog {

    fun failedToRename(scratch: Scratch) {
        showNotification("Failed to rename scratch: ${scratch.fileName}", WARNING)
    }

    fun migratedToIdeScratches() {
        log.info("Migrated plugin scratches to IDE")
        showNotification(
            "Migrated scratches to IDE. Now you can execute scratches and see scratches list in " +
            "\"Project View -> Scratches tab\".", INFORMATION)
    }

    fun failedToMigrateScratchesToIdeLocation(reason: String) =
        showNotification("Failed to migrated plugin scratches to IDE: $reason", WARNING)

    fun listeningToClipboard(isListening: Boolean) =
        if (isListening) showNotification("Started listening to clipboard", INFORMATION)
        else showNotification("Stopped listening to clipboard", INFORMATION)

    fun failedToOpenDefaultScratch() = showNotification("Failed to open default scratch", WARNING)

    fun failedToOpen(scratch: Scratch) = showNotification("Failed to open scratch: '${scratch.fileName}'", WARNING)

    fun failedToCreate(scratch: Scratch) = showNotification("Failed to create scratch: '${scratch.fileName}'", WARNING)

    fun failedToDelete(scratch: Scratch) = showNotification("Failed to delete scratch: '${scratch.fileName}'", WARNING)

    fun failedToFindVirtualFileFor(scratch: Scratch) = log.warn("Failed to find virtual file for '${scratch.fileName}'")

    companion object {
        private val log = Logger.getInstance(ScratchLog::class.java)
    }
}

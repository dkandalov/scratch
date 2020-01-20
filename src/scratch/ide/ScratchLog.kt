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

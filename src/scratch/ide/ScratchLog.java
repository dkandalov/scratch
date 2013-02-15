package scratch.ide;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.text.StringUtil;
import scratch.Scratch;

import java.util.List;

import static com.intellij.notification.NotificationType.WARNING;

/**
 * User: dima
 * Date: 14/02/2013
 */
public class ScratchLog {
	private static final Logger LOG = Logger.getInstance(ScratchLog.class);

	public void failedToRename(Scratch scratch) {
		notifyUser("", "Failed to rename scratch: " + scratch.name, WARNING);
	}

	public void migratedScratchesToFiles() {
		LOG.info("Migrated scratches to physical files");
	}

	public void failedToMigrateScratchesToFiles(List<Integer> scratchIndexes) {
		String title = "Failed to migrated scratches to physical files. ";
		String message = "Failed scratches: " + StringUtil.join(scratchIndexes, ", ");
		notifyUser(title, message, WARNING);
	}

	public void failedToOpenDefaultScratch() {
		notifyUser("", "Failed to open default scratch", WARNING);
	}

	public void failedToOpen(Scratch scratch) {
		notifyUser("", "Failed to open scratch: '" + scratch.name + "'", WARNING);
	}

	public void failedToOpenByName(String scratchName) {
		notifyUser("", "Failed to open scratch by name: '" + scratchName + "'", WARNING);
	}

	public void failedToCreate(Scratch scratch) {
		notifyUser("", "Failed to create scratch: '" + scratch.name + "'", WARNING);
	}

	public void failedToDelete(Scratch scratch) {
		notifyUser("", "Failed to delete scratch: '" + scratch.name + "'", WARNING);
	}

	public void failedToFindVirtualFileFor(Scratch scratch) {
		LOG.warn("Failed to find virtual file for '" + scratch.asFileName() + "'");
	}

	private static void notifyUser(String title, String message, NotificationType notificationType) {
		String groupDisplayId = "Scratch";
		Notification notification = new Notification(groupDisplayId, title, message, notificationType);
		ApplicationManager.getApplication().getMessageBus().syncPublisher(Notifications.TOPIC).notify(notification);
	}
}

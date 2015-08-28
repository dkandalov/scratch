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

package scratch.ide;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.Function;
import scratch.Scratch;

import java.util.List;

import static com.intellij.notification.NotificationType.INFORMATION;
import static com.intellij.notification.NotificationType.WARNING;


public class ScratchLog {
	private static final Logger LOG = Logger.getInstance(ScratchLog.class);
	private static final String TITLE = "Scratch Plugin";

	public void failedToRename(Scratch scratch) {
		notifyUser("", "Failed to rename scratch: " + scratch.asFileName(), WARNING);
	}

	public void migratedScratchesToFiles() {
		LOG.info("Migrated scratches to physical files");
	}

	public void listeningToClipboard(boolean isListening) {
		if (isListening)
			notifyUser(TITLE, "Started listening to clipboard", INFORMATION);
		else
			notifyUser(TITLE, "Stopped listening to clipboard", INFORMATION);
	}

	public void failedToMigrateScratchesToFiles(List<Integer> scratchIndexes) {
		String title = "Failed to migrated scratches to physical files. ";
		String message = "Failed scratches: " + join(scratchIndexes, ", ");
		notifyUser(title, message, WARNING);
	}

	public void willNotMigrateBecauseTargetFolderIsNotEmpty() {
		notifyUser(TITLE, "Old scratches data won't be save because scratches folder is not empty", WARNING);
	}

	public void failedToOpenDefaultScratch() {
		notifyUser("", "Failed to open default scratch", WARNING);
	}

	public void failedToOpen(Scratch scratch) {
		notifyUser("", "Failed to open scratch: '" + scratch.asFileName() + "'", WARNING);
	}

	public void failedToCreate(Scratch scratch) {
		notifyUser("", "Failed to create scratch: '" + scratch.asFileName() + "'", WARNING);
	}

	public void failedToDelete(Scratch scratch) {
		notifyUser("", "Failed to delete scratch: '" + scratch.asFileName() + "'", WARNING);
	}

	public void failedToFindVirtualFileFor(Scratch scratch) {
		LOG.warn("Failed to find virtual file for '" + scratch.asFileName() + "'");
	}

	private static void notifyUser(String title, String message, NotificationType notificationType) {
		String groupDisplayId = TITLE;
		Notification notification = new Notification(groupDisplayId, title, message, notificationType);
		ApplicationManager.getApplication().getMessageBus().syncPublisher(Notifications.TOPIC).notify(notification);
	}

	private static <T> String join(List<T> values, String separator) {
		return StringUtil.join(values, new Function<T, String>() {
			@Override public String fun(T it) {
				return it.toString();
			}
		}, separator);
	}
}

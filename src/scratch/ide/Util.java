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
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.UserDataHolder;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public class Util {
	private static final Key<Project> PROJECT_KEY = Key.create("Project");

	@Nullable
	public static VirtualFile getVirtualFile(String absolutePath) {
		if (!absolutePath.startsWith("file://")) {
			absolutePath = "file://" + absolutePath;
		}
		return VirtualFileManager.getInstance().refreshAndFindFileByUrl(FileUtil.toSystemIndependentName(absolutePath));
	}

	@Nullable
	public static VirtualFile getVirtualFile(File file) {
		return getVirtualFile(file.getAbsolutePath());
	}

	public static void notifyUser(String title, String message, NotificationType notificationType) {
		String groupDisplayId = "Scratch";
		Notification notification = new Notification(groupDisplayId, title, message, notificationType);
		ApplicationManager.getApplication().getMessageBus().syncPublisher(Notifications.TOPIC).notify(notification);
	}

	public static UserDataHolder holdingOnTo(Project project) {
		UserDataHolder userDataHolder = new UserDataHolderBase();
		userDataHolder.putUserData(PROJECT_KEY, project);
		return userDataHolder;
	}

	@SuppressWarnings("ConstantConditions")
	@NotNull public static Project takeProjectFrom(UserDataHolder userDataHolder) {
		return userDataHolder.getUserData(PROJECT_KEY);
	}
}

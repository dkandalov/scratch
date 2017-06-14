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
import com.intellij.notification.NotificationListener
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.command.UndoConfirmationPolicy.DO_NOT_REQUEST_CONFIRMATION
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.UserDataHolder
import com.intellij.openapi.util.UserDataHolderBase

object Util {
    private val projectKey = Key.create<Project>("Project")

    fun Project?.wrapAsDataHolder(): UserDataHolder = UserDataHolderBase().apply {
        putUserData(projectKey, this@wrapAsDataHolder)
    }

    fun UserDataHolder.extractProject(): Project = getUserData(projectKey)!!

    fun CommandProcessor.execute(f: () -> Unit) {
        executeCommand(null, f, null, null, DO_NOT_REQUEST_CONFIRMATION)
    }

    fun showNotification(message: String, notificationType: NotificationType, listener: () -> Unit = {}) {
        val title = "Scratch Plugin"
        val groupDisplayId = title
        val notificationListener = NotificationListener { notification, _ ->
            listener.invoke()
            notification.expire()
        }
        val notification = Notification(groupDisplayId, title, message, notificationType, notificationListener)

        ApplicationManager.getApplication()
            .messageBus.syncPublisher(Notifications.TOPIC)
            .notify(notification)
    }
}

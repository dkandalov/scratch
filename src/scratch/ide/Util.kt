package scratch.ide

import com.intellij.notification.Notification
import com.intellij.notification.NotificationListener
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.command.UndoConfirmationPolicy.DO_NOT_REQUEST_CONFIRMATION
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.UserDataHolder
import com.intellij.openapi.util.UserDataHolderBase

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
    val notificationListener = NotificationListener { notification, _ ->
        listener.invoke()
        notification.expire()
    }
    val notification = Notification(title, title, message, notificationType, notificationListener)

    ApplicationManager.getApplication()
        .messageBus.syncPublisher(Notifications.TOPIC)
        .notify(notification)
}

fun Disposable.whenDisposed(f: () -> Unit) {
    // Always register new instance of Disposable because there is one-to-one relationship between parent and child disposables.
    Disposer.register(this, Disposable { f() })
}

fun Disposable.reallyDispose() {
    Disposer.dispose(this)
}

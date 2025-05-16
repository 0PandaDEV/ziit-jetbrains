package net.pandadev.ziitjetbrains.util

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import net.pandadev.ziitjetbrains.config.ZiitConfig
import java.text.SimpleDateFormat
import java.util.*

@Service
class LogService {
    companion object {
        private val LOG = Logger.getInstance("#net.pandadev.ziitjetbrains.util.LogService")
        private const val NOTIFICATION_GROUP_ID = "Ziit Notifications"

        fun getInstance(): LogService = service()
    }

    private val config = ZiitConfig.getInstance()
    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

    fun log(message: String) {
        val timestamp = dateFormatter.format(Date())
        val logMessage = "[$timestamp] $message"


        LOG.info(logMessage)


        if (config.isDebugEnabled()) {


        }
    }

    fun error(message: String) {
        val timestamp = dateFormatter.format(Date())
        val logMessage = "[$timestamp] ERROR: $message"


        LOG.error(logMessage)


        if (config.isDebugEnabled()) {


        }
    }

    fun notifyInfo(title: String, message: String) {
        val notification = Notification(
            NOTIFICATION_GROUP_ID, title, message, NotificationType.INFORMATION
        )
        Notifications.Bus.notify(notification)
    }

    fun notifyError(title: String, message: String) {
        val notification = Notification(
            NOTIFICATION_GROUP_ID, title, message, NotificationType.ERROR
        )
        Notifications.Bus.notify(notification)
    }

    fun notifyWarning(title: String, message: String) {
        val notification = Notification(
            NOTIFICATION_GROUP_ID, title, message, NotificationType.WARNING
        )
        Notifications.Bus.notify(notification)
    }
} 
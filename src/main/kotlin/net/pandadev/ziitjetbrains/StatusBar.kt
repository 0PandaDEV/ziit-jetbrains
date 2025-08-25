package net.pandadev.ziitjetbrains

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.StatusBarWidgetFactory
import com.intellij.openapi.wm.WindowManager
import com.intellij.openapi.wm.impl.status.EditorBasedWidget
import com.intellij.util.Consumer
import java.awt.event.MouseEvent

class StatusBar : StatusBarWidgetFactory {
    override fun getId(): String = "StatusBar"

    override fun getDisplayName(): String = "Ziit"

    override fun isAvailable(project: Project): Boolean = true

    override fun createWidget(project: Project): StatusBarWidget {
        val widget = ZiitStatusBarWidget(project)
        LogService.getInstance().log("Created Ziit status bar widget for project: ${project.name}")
        return widget
    }

    override fun disposeWidget(widget: StatusBarWidget) {
        widget.dispose()
    }

    override fun canBeEnabledOn(statusBar: StatusBar): Boolean = true

    override fun isEnabledByDefault(): Boolean = true

    class ZiitStatusBarWidget(project: Project) : EditorBasedWidget(project) {
        private val config = Config.getInstance()
        private val heartbeatService = HeartbeatService.getInstance()

        private var totalSeconds: Int = 0
        private var trackingStartTime: Long = 0
        private var isTracking: Boolean = false
        private var isOnline: Boolean = true
        private var hasValidApiKey: Boolean = true

        init {
            heartbeatService.setStatusBarWidget(this)

            val updateThread = Thread {
                while (!Thread.currentThread().isInterrupted) {
                    try {
                        Thread.sleep(60000)
                        updateWidget()
                    } catch (_: InterruptedException) {
                        Thread.currentThread().interrupt()
                        break
                    }
                }
            }
            updateThread.isDaemon = true
            updateThread.start()
        }

        fun startTracking() {
            if (!isTracking) {
                isTracking = true
                trackingStartTime = System.currentTimeMillis()
                updateWidget()
            }
        }

        fun stopTracking() {
            if (isTracking) {
                isTracking = false
                updateWidget()
            }
        }

        fun updateTime(hours: Int, minutes: Int) {
            totalSeconds = hours * 3600 + minutes * 60
            updateWidget()
        }

        fun setOnlineStatus(isOnline: Boolean) {
            this.isOnline = isOnline
            updateWidget()
        }

        fun setApiKeyStatus(isValid: Boolean) {
            this.hasValidApiKey = isValid
            updateWidget()
        }

        private fun updateWidget() {
            WindowManager.getInstance().getStatusBar(project)?.updateWidget(ID())
        }

        override fun ID(): String = "StatusBar"

        override fun getPresentation(): StatusBarWidget.WidgetPresentation {
            return object : StatusBarWidget.TextPresentation {
                override fun getText(): String {
                    if (!hasValidApiKey) {
                        return "⚠ Unconfigured"
                    }

                    var displaySeconds = totalSeconds

                    if (isTracking) {
                        val elapsedSeconds = (System.currentTimeMillis() - trackingStartTime) / 1000
                        displaySeconds += elapsedSeconds.toInt()
                    }

                    val hours = displaySeconds / 3600
                    val minutes = (displaySeconds % 3600) / 60

                    return when {
                        !isOnline -> "⏱ $hours hrs $minutes mins (offline)"
                        else -> "⏱ $hours hrs $minutes mins"
                    }
                }

                override fun getTooltipText(): String? {
                    return when {
                        !hasValidApiKey -> "Invalid or missing API key. Click to configure."
                        !isOnline -> "Working offline. Changes will be synced when online."
                        else -> "Ziit: Today's coding time. Click to open dashboard."
                    }
                }

                override fun getAlignment(): Float = 0.5f

                override fun getClickConsumer(): Consumer<MouseEvent> {
                    return Consumer<MouseEvent> { _ ->
                        if (!hasValidApiKey) {
                            config.promptForApiKey()
                        } else {
                            openDashboard()
                        }
                    }
                }
            }
        }

        private fun openDashboard() {
            val baseUrl = config.getBaseUrl()
            if (baseUrl.isNotEmpty()) {
                BrowserUtil.browse(baseUrl)
            } else {
                config.promptForBaseUrl()
            }
        }

        override fun dispose() {}
    }
}
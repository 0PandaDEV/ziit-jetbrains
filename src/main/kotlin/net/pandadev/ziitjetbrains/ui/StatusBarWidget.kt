package net.pandadev.ziitjetbrains.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.WindowManager
import com.intellij.util.Consumer
import java.awt.event.MouseEvent
import net.pandadev.ziitjetbrains.config.ZiitConfig
import net.pandadev.ziitjetbrains.util.LogService

class StatusBarWidget(private val project: Project) : StatusBarWidget {
    companion object {
        const val WIDGET_ID = "ZiitStatusBarWidget"
    }

    private val logger = LogService.getInstance()
    private val config = ZiitConfig.getInstance()
    private var statusBar: StatusBar? = null

    private var totalSeconds: Int = 0
    private var trackingStartTime: Long = 0
    private var isTracking: Boolean = false
    private var isOnline: Boolean = true
    private var hasValidApiKey: Boolean = true

    init {

        updateWidget()

        val updateThread = Thread {
            while (!Thread.currentThread().isInterrupted) {
                try {
                    Thread.sleep(60000)
                    updateWidget()
                } catch (e: InterruptedException) {
                    Thread.currentThread().interrupt()
                    break
                }
            }
        }
        updateThread.isDaemon = true
        updateThread.start()
    }

    fun install() {
        val statusBar = WindowManager.getInstance().getStatusBar(project) ?: return
        this.statusBar = statusBar

        Disposer.register(project) { statusBar.removeWidget(WIDGET_ID) }

        statusBar.addWidget(this, project)
        statusBar.updateWidget(WIDGET_ID)
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
        val statusBar = this.statusBar ?: return
        statusBar.updateWidget(WIDGET_ID)
    }

    override fun ID(): String = WIDGET_ID

    override fun getPresentation(): StatusBarWidget.WidgetPresentation {
        return object : StatusBarWidget.MultipleTextValuesPresentation {
            override fun getTooltipText(): String {
                return when {
                    !hasValidApiKey -> "Invalid or missing API key. Click to configure."
                    !isOnline -> "Working offline. Changes will be synced when online."
                    else -> "Ziit: Today's coding time. Click to open dashboard."
                }
            }

            override fun getSelectedValue(): String {
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
                    !isOnline -> "⟳ $hours hrs $minutes mins (offline)"
                    else -> "⏱ $hours hrs $minutes mins"
                }
            }

            override fun getClickConsumer(): Consumer<MouseEvent>? {
                return Consumer { _ ->
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
            com.intellij.ide.BrowserUtil.browse(baseUrl)
        } else {
            logger.notifyError("Ziit Error", "No base URL configured for Ziit")
        }
    }

    override fun dispose() {}
}

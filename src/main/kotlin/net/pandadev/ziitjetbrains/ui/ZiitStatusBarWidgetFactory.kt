package net.pandadev.ziitjetbrains.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.StatusBarWidgetFactory
import net.pandadev.ziitjetbrains.config.ZiitConfig
import net.pandadev.ziitjetbrains.services.HeartbeatService
import net.pandadev.ziitjetbrains.util.LogService

class ZiitStatusBarWidgetFactory : StatusBarWidgetFactory {
    override fun getId(): String = "ZiitStatusBarWidget"

    override fun getDisplayName(): String = "Ziit Time Tracking"

    override fun isAvailable(project: Project): Boolean = true

    override fun createWidget(project: Project): StatusBarWidget {
        val widget = StatusBarWidget(project)


        HeartbeatService.getInstance().setStatusBarWidget(widget)

        LogService.getInstance().log("Created Ziit status bar widget for project: ${project.name}")
        return widget
    }

    override fun disposeWidget(widget: StatusBarWidget) {
        widget.dispose()
    }

    override fun canBeEnabledOn(statusBar: StatusBar): Boolean = true

    override fun isEnabledByDefault(): Boolean = true
} 
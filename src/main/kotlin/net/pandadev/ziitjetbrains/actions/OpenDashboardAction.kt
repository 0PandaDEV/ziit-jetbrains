package net.pandadev.ziitjetbrains.actions

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import net.pandadev.ziitjetbrains.config.ZiitConfig
import net.pandadev.ziitjetbrains.util.LogService

class OpenDashboardAction : AnAction() {
    private val logger = LogService.getInstance()
    private val config = ZiitConfig.getInstance()

    override fun actionPerformed(e: AnActionEvent) {
        logger.log("Open Dashboard action triggered")
        val baseUrl = config.getBaseUrl()

        if (baseUrl.isNotEmpty()) {
            BrowserUtil.browse(baseUrl)
        } else {
            logger.notifyError("Ziit Error", "No base URL configured for Ziit")
        }
    }
} 
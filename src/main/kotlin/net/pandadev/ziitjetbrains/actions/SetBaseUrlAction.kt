package net.pandadev.ziitjetbrains.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import net.pandadev.ziitjetbrains.config.ZiitConfig
import net.pandadev.ziitjetbrains.util.LogService

class SetBaseUrlAction : AnAction() {
    private val logger = LogService.getInstance()
    private val config = ZiitConfig.getInstance()

    override fun actionPerformed(e: AnActionEvent) {
        logger.log("Set Base URL action triggered")
        val baseUrl = config.promptForBaseUrl()

        if (baseUrl != null && baseUrl.isNotEmpty()) {
            logger.notifyInfo("Ziit", "Instance URL has been updated")
        }
    }
} 
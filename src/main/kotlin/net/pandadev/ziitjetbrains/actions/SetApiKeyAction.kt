package net.pandadev.ziitjetbrains.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import net.pandadev.ziitjetbrains.config.ZiitConfig
import net.pandadev.ziitjetbrains.services.HeartbeatService
import net.pandadev.ziitjetbrains.util.LogService

class SetApiKeyAction : AnAction() {
    private val logger = LogService.getInstance()
    private val config = ZiitConfig.getInstance()
    private val heartbeatService = HeartbeatService.getInstance()

    override fun actionPerformed(e: AnActionEvent) {
        logger.log("Set API Key action triggered")
        val apiKey = config.promptForApiKey()

        if (apiKey != null && apiKey.isNotEmpty()) {
            logger.notifyInfo("Ziit", "API key has been updated")
            heartbeatService.fetchUserSettings()
        }
    }
} 
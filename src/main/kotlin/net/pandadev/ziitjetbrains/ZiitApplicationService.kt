package net.pandadev.ziitjetbrains

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.project.ProjectManagerListener
import net.pandadev.ziitjetbrains.config.ZiitConfig
import net.pandadev.ziitjetbrains.services.HeartbeatService
import net.pandadev.ziitjetbrains.util.LogService

@Service
class ZiitApplicationService {
    companion object {
        fun getInstance(): ZiitApplicationService = service()
    }

    private val logger = LogService.getInstance()
    private val config = ZiitConfig.getInstance()
    private val heartbeatService = HeartbeatService.getInstance()

    init {
        logger.log("Ziit plugin initialized")
    }
} 
package net.pandadev.ziitjetbrains

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger

@Service
class LogService {
    private val logger = Logger.getInstance("Ziit")

    companion object {
        fun getInstance(): LogService = service()
    }

    fun log(message: String) {
        logger.info(message)
    }

    fun error(message: String) {
        logger.error(message)
    }

    fun warn(message: String){
        logger.warn(message)
    }

    fun info(message: String){
        logger.info(message)
    }
}